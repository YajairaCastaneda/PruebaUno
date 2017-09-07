package cl.bci.aplicaciones.productos.servicios.multilinea.mb;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJBException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import wcorp.bprocess.creditosglobales.CreditosGlobales;
import wcorp.bprocess.creditosglobales.CreditosGlobalesHome;
import wcorp.bprocess.multilinea.Multilinea;
import wcorp.bprocess.multilinea.MultilineaException;
import wcorp.bprocess.multilinea.MultilineaHome;
import wcorp.bprocess.multilinea.ResultCartolaMultilinea;
import wcorp.bprocess.multilinea.to.DatosCalendarioPagoTO;
import wcorp.bprocess.precioscontextos.PreciosContextos;
import wcorp.bprocess.precioscontextos.PreciosContextosHome;
import wcorp.serv.creditosglobales.OperacionCreditoSuperAmp;
import wcorp.serv.creditosglobales.ResultCalendarioPagoAmpliado;
import wcorp.serv.creditosglobales.ResultConsultaOperacionCredito;
import wcorp.serv.creditosglobales.to.ResultConsultaCalendarioPagosCancelacionesTO;
import wcorp.serv.precios.IteracionConsultaCgr;
import wcorp.serv.precios.ResultConsultaCgr;
import wcorp.util.EnhancedServiceLocator;
import wcorp.util.ErroresUtil;
import wcorp.util.GeneralException;
import wcorp.util.StringUtil;
import wcorp.util.TablaValores;
import wcorp.util.bee.CrearMultiEnviroment;
import wcorp.util.bee.MultiEnvironment;

import cl.bci.aplicaciones.cliente.mb.ClienteMB;
import cl.bci.aplicaciones.productos.mb.ProductosMB;
import cl.bci.infraestructura.web.seguridad.mb.SesionMB;

/**
 * Backing ManagedBean <br>
 * <b>MisCreditosViewMB</b> <br>
 * Managed bean que provee datos a la vista de despliegue 
 * de los créditos asociados al cliente. <br>
 * Registro de versiones:
 * <ul>
 * <li>1.0 (20/01/2014 Eduardo Pérez (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) ): versión inicial.</li>
 * <li>1.1 (31/12/2015 Eduardo Pérez (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) ): formatea operacion.</li>
 * <li>1.2 (18/08/2016 Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI) ): Se modifica el método seleccionOperacionListener
 *                                                   y se agregan obtenerValoresCredito,obtenerSeguros,obtenerGastoNotarial,crearEJBmultilinea
 *                                                   ,crearEJBprecios.</li> 
 * </ul>
 * 
 * <p>
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 * </p>
 */
@ManagedBean
@ViewScoped
public class MisCreditosViewMB implements Serializable {
    
	/**
	 * Atributo Logger.
	 */
	private static transient Logger logger  = (Logger)Logger.getLogger(MisCreditosViewMB.class);
    
	  /**
     * Constante para definir la localidad.
     */
    private static final Locale LOCAL = new Locale("ES", "CL");
	
	/**
	 * serialVersionUID de la clase.
	 */ 
	private static final long serialVersionUID = 1L;
	
	/**
     * Nombre JNDI del EJB Multilinea.
     */
    private static final String JNDI_NAME_MULTILINEA = "wcorp.bprocess.multilinea.Multilinea";
    
	/**
     * Nombre JNDI del EJB Multilinea.
     */
    private static final String JNDI_NAME_CREDITOS_GLOBALES = "wcorp.bprocess.creditosglobales.CreditosGlobales";
    
    /**
     * Nombre JNDI del EJB PreciosContextos.
     */
    private static final String JNDI_NAME_PRECIOS = "wcorp.bprocess.precioscontextos.PreciosContextos";  
    
    /** 
     * Variable de estado.
     */
 	private static final int PASO1 = 1;
 	
 	/** 
     * Variable de estado.
     */
 	private static final int PASO2 = 2;
 	
 	/** 
     * Variable de estado.
     */
 	private static final int PASO3 = 3;
 	
    /** 
     * Largo numerico operacion.
     */
 	private static final int LARGO_OPERACION = 8; 	
 	
    /**
     * Constante para identificar el banco a utilizar en la construccin del objeto MultiEnvironment.
     */
    private static final String BANCO = "BCI";
    
    /**
     * Constante para identificar la marca a utilizar en la construccin del objeto MultiEnvironment.
     */
    private static final String MARCA = "BCI";
    
    /**
     * Constante para identificar la cartera a utilizar en la construccin del objeto MultiEnvironment.
     */
    private static final String CARTERA = "BEE";
    
    /**
     * Constante para identificar el usuario a utilizar en la construccin del objeto MultiEnvironment.
     */
    private static final String USUARIO = "WEBMLT";
    
    /**
     * Tabla paramétrica con las configuraciones necesarias para realizar los llamados al servicio de precios.
     *      */
    private static final String TABLA_MULTILINEA ="multilinea.parametros";
    
	/**
	 * Formato UF.
	 */
	private static final String FORMATO_UF = TablaValores.getValor(
			TABLA_MULTILINEA, "formatoUF", "valor");

	/**
	 * Formato Pesos.
	 */
	private static final String FORMATO_PESOS = TablaValores.getValor(
			TABLA_MULTILINEA, "formatoPeso", "valor");
    
    /**
     * Mapa con las operaciones cargadas. Necesario para el combobox de operaciones.
     */
    private static Map<String, OperacionCreditoSuperAmp> operacionesMap
    = new HashMap<String,OperacionCreditoSuperAmp>();
    
    /**
	 * Atributo representa Managed Bean inyectado ClienteMB.
	 */
	@ManagedProperty(value = "#{clienteMB}")
	private ClienteMB clienteMB;

	/**
	 * Atributo para inyectar MB SesionMB.
	 */
	@ManagedProperty(value = "#{sesionMB}")
	private SesionMB sesion;
	
	/**
	 * Atributo para inyectar MB ProductosMB.
	 */
    @ManagedProperty(value = "#{productosMB}")
	private ProductosMB productosMB;
    
    /**
     * Lista de operaciones multilinea asociadas.
     */
    private ArrayList<OperacionCreditoSuperAmp> operaciones;
    
    /**
     * Listado de operaciones seleccionables en el combobox. Necesario para el combobox de operaciones.
     */
    private ArrayList<SelectItem> selectItems;
    
    /**
     * Operación seleccionada por el combobox de operaciones.
     */
    private String selectedItem;
    
    /**
     * Calendarios de pago asociados a las operaciones.
     */
    private HashMap<String,ArrayList<DatosCalendarioPagoTO>> calendariosPago 
         = new HashMap<String,ArrayList<DatosCalendarioPagoTO>>();
    
    /**
     * Calendarios de pago asociados a las operaciones.
     */
    private HashMap<String,String> clasificacionCanales = new HashMap<String,String>();
    
    /**
     * Operación seleccionada.
     */
    private OperacionCreditoSuperAmp operacionSeleccionada;
    
    /**
     * Objeto con resultados específicos de la operación del crédito.
     */
    private ResultConsultaOperacionCredito operacionCredito;
    
    /**
     * Condicion de garantía.
     */
    private String condicionGarantia = "";
    
    /**
     * Condicion de garantía.
     */
    private int paso = 0;
    
    /**
     * Atributo que identifica error generico.
     */
    private boolean errorGenerico;

	/**
     * <p>Método que obtiene la información crediticia del cliente para ser
     * desplegada en el dashboard.
     * 
     * </p>  Registro de Versiones:<ul>
     * <li> 1.0 26/12/2014 Eduardo Pérez (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : versión inicial.</li>
     * </ul>
     *
     * @throws EJBException Exception de EJB.
     * @throws ParseException Exception de parseo de variables.
     * @throws MultilineaException Exception multilinea.
     * @throws GeneralException para error general.
     */
    private void obtenerOperaciones() throws EJBException, ParseException, MultilineaException, GeneralException{
    
    	if(paso!=1) return;
    	if(operaciones!=null) return;
    	if (getLogger().isInfoEnabled()) {
            getLogger().info("[obtenerOperaciones]["+clienteMB.getRut()+"][BCI_INI]");
        }
        int numCliente = (int) clienteMB.getRut();
        char vrfCliente = clienteMB.getDigitoVerif();
        
        MultiEnvironment multiEnvironment = null;
		multiEnvironment = obtenerMultiEnvironment();
		
		operaciones = new ArrayList<OperacionCreditoSuperAmp>();
		
        try {
        	
        	if (getLogger().isInfoEnabled()) {
                getLogger().info("[obtenerOperaciones][Creando Multilinea EJB]");
            }
        	
        	Multilinea multilineaBean = ((MultilineaHome) EnhancedServiceLocator
                     .getInstance().getHome(JNDI_NAME_MULTILINEA,
                     		MultilineaHome.class)).create();
        	 
        	if (getLogger().isDebugEnabled()) {
                getLogger().debug("[obtenerOperaciones][Multilinea EJB Creado]");
            }
        	
        	if (getLogger().isInfoEnabled()) {
                getLogger().info("[obtenerOperaciones][invocando Multilinea.obtenerOperaciones AVC]");
            }
        	
        	ResultCartolaMultilinea creditos = multilineaBean.consultaCartolaMultilinea(
        			multiEnvironment, numCliente, vrfCliente, null, false);
        	if (getLogger().isInfoEnabled()) {
                getLogger().info("[obtenerOperaciones][Multilinea.obtenerOperaciones OK!!]");
            }
        	if (getLogger().isInfoEnabled()) {
                getLogger().info("[creditos]->" + creditos);
            }
        	if (getLogger().isInfoEnabled()) {
        		getLogger().info("[creditos.getResultConsultaOperClienteSuperAmp()]->"
        				+ creditos.getResultConsultaOperClienteSuperAmp());
        	}
        
        	if(creditos!=null && creditos.getResultConsultaOperClienteSuperAmp()!=null){
        		for(int x=0;x<creditos.getResultConsultaOperClienteSuperAmp().getOperacionCreditoSuperAmp().length;x++){
            		OperacionCreditoSuperAmp operacion = creditos.getResultConsultaOperClienteSuperAmp().getOperacionCreditoSuperAmp()[x];
            		if(operacion!=null){
            			if (getLogger().isInfoEnabled()) {
                            getLogger().info("[obtenerOperaciones][Después de traer las operacion]");
                            getLogger().info("[obtenerOperaciones][Operacion rescatada para el canal]" + operacion.getCanalVenta());
                        }
            			operaciones.add(operacion);
            		}
            	}
        	}
        	
        	if (getLogger().isInfoEnabled()) {
                getLogger().info("[obtenerOperaciones][invocando Multilinea.obtenerOperaciones COM]");
            }
        	creditos = multilineaBean.consultaCartolaMultilinea(multiEnvironment,
        			numCliente, vrfCliente, null, false, "COM");
        	if (getLogger().isInfoEnabled()) {
                getLogger().info("[obtenerOperaciones][Multilinea.obtenerOperaciones OK!!]");
            }
        	if (getLogger().isInfoEnabled()) {
                getLogger().info("[creditos]->" + creditos);
            }
        	if (getLogger().isInfoEnabled()) {
        		getLogger().info("[creditos.getResultConsultaOperClienteSuperAmp()]->"
        				+ creditos.getResultConsultaOperClienteSuperAmp());
        	}
        
        	if(creditos!=null && creditos.getResultConsultaOperClienteSuperAmp()!=null){
        		for(int x=0;x<creditos.getResultConsultaOperClienteSuperAmp().getOperacionCreditoSuperAmp().length;x++){
            		OperacionCreditoSuperAmp operacion = creditos.getResultConsultaOperClienteSuperAmp().getOperacionCreditoSuperAmp()[x];
            		if(operacion!=null){
            			if (getLogger().isInfoEnabled()) {
                            getLogger().info("[obtenerOperaciones][Después de traer las operacion]");
                            getLogger().info("[obtenerOperaciones][Operacion rescatada para el canal]" + operacion.getCanalVenta());
                        }
            			operacion.setCodMonedaCred(operacion.getCodMonedaCred().trim());
            			operaciones.add(operacion);
            		}
            	}
        	}
        	
        	//Se carga el mapa String-Operacion para compatibilidad con combo JSF
        	operacionesMap = new HashMap<String, OperacionCreditoSuperAmp>();
        	for(int x=0; x<operaciones.size();x++){
        		if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[obtenerOperaciones][operaciones para seleccion into operacionesMap]" + operaciones.get(x).getIdOperacion()+operaciones.get(x).getNumOperacion());
                }
        		operacionesMap.put(operaciones.get(x).getIdOperacion()+operaciones.get(x).getNumOperacion(), operaciones.get(x));
        	}
        	selectItems = new ArrayList<SelectItem>();
        	for (OperacionCreditoSuperAmp op : operacionesMap.values()) {
        		if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[obtenerOperaciones][cargando selectItems]" + op.getIdOperacion()+op.getNumOperacion());
                }
        		selectItems.add(new SelectItem(op.getIdOperacion() + op.getNumOperacion() , op.getIdOperacion() + " " + StringUtil.rellenaPorLaIzquierda(String.valueOf(op.getNumOperacion()), LARGO_OPERACION, '0')));
            }
        } 
        catch (Exception e) {
        	if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error("[obtenerOperaciones]["+clienteMB.getRut()+"][BCI_FINEX][Exception]"
                +" error con mensaje: " + e.getMessage(), e);
            }
        	errorGenerico = true;
            throw new MultilineaException("UNKNOW", e.getMessage());
        } 
        
        if (getLogger().isInfoEnabled()) {
        	getLogger().info("[obtenerOperaciones]["+clienteMB.getRut()+"][BCI_FINOK]");
        }
      
    }
    
    /**
     * <p>Método que obtiene el detalle de pago de una operación multilinea.
     * 
     * </p>  Registro de Versiones:<ul>
     * <li> 1.0 26/01/2015 Eduardo Pérez (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : versión inicial.</li>
     * </ul>
     *
     * @throws EJBException Exception de EJB
     * @throws ParseException Exception de parseo de variables
     * @throws MultilineaException 
     * @throws GeneralException 
     */
    public void generaCalendarioOperacion() throws EJBException, ParseException
    		, MultilineaException, GeneralException{
    	if(paso!=PASO2) return;
    	Map parametros = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    	String caiOperacion = (String) parametros.get("caiOperacion");
    	int iicOperacion = Integer.valueOf((String) parametros.get("iicOperacion"));
    	int vencimiento = Integer.valueOf((String) parametros.get("vencimiento"));
    	
    	if (getLogger().isInfoEnabled()) {
            getLogger().info("[generaCalendarioOperacion]["+clienteMB.getRut()+"][BCI_INI]");
        }
        
    	MultiEnvironment multiEnvironment = null;
		multiEnvironment = obtenerMultiEnvironment();
		
        try {

        	if (getLogger().isInfoEnabled()) {
                getLogger().info("[generaCalendarioOperacion][Creando EJB de CreditosGlobales]");
            }
        	
        	CreditosGlobales creditosGlobalesBean = ((CreditosGlobalesHome) EnhancedServiceLocator
                     .getInstance().getHome(JNDI_NAME_CREDITOS_GLOBALES,
                    		 CreditosGlobalesHome.class)).create();
        	 
        	if (getLogger().isDebugEnabled()) {
                getLogger().debug("[generaCalendarioOperacion][CreditosGlobales EJB Creado]");
            }
        	
        	if (getLogger().isInfoEnabled()) {
                getLogger().info("[generaCalendarioOperacion][invocando "
                		+ "CreditosGlobales.calendarioPagoAmpliadoTodo]");
            }
        	
        	String key = caiOperacion + iicOperacion;
      		if(calendariosPago.get(key) == null){
      			ResultCalendarioPagoAmpliado resultadoCalendarioPagoAmpliadoTodo = null;
      			ResultConsultaCalendarioPagosCancelacionesTO  consultaCalendarioPagosCancelaciones= null;
      			
      			resultadoCalendarioPagoAmpliadoTodo = creditosGlobalesBean.calendarioPagoAmpliadoTodo(
      					multiEnvironment, caiOperacion, 
              			iicOperacion, vencimiento);
      			
      			ArrayList<DatosCalendarioPagoTO> resultado = new ArrayList<DatosCalendarioPagoTO>();
      			for(int i=0; i < resultadoCalendarioPagoAmpliadoTodo.getTotOcurrencias();i++){
      				DatosCalendarioPagoTO calendario = new DatosCalendarioPagoTO();
      				calendario.setDetalleCalendario(resultadoCalendarioPagoAmpliadoTodo.
      						getDetalleCalendario(i));
      				if(resultadoCalendarioPagoAmpliadoTodo.getDetalleCalendario(i).getIndPago()=='S'){
      					consultaCalendarioPagosCancelaciones = 
      							creditosGlobalesBean.consultaCalendarioPagosCancelaciones(
      									multiEnvironment, caiOperacion,iicOperacion,'A',i,' ');
      					if(consultaCalendarioPagosCancelaciones.getTotOcurrencias() > 0){
      						calendario.setValorCancelado(
      								consultaCalendarioPagosCancelaciones.getListaCalendario(0)
      								.getValorPagado());
      					}
      				}
      				resultado.add(calendario);
      			}
      			
      			if (getLogger().isInfoEnabled()) {
                    getLogger().info("[consultaCalendarioPagosCancelaciones][]" + resultado.toString());
                }
      			
          		calendariosPago.put(key, resultado);
      		}
      		
      		this.setPaso(PASO3);
        	
          	if (getLogger().isInfoEnabled()) {
                getLogger().info("[generaCalendarioOperacion][CreditosGlobales.calendarioPagoAmpliadoTodo OK!!]");
            }
        	
        } 
        catch (Exception e) {
        	if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error("[generaCalendarioOperacion]["+clienteMB.getRut()+"][BCI_FINEX][Exception]"
                +" error con mensaje: " + e.getMessage(), e);
            }
            throw new MultilineaException("UNKNOW", e.getMessage());
        } 
        
        if (getLogger().isInfoEnabled()) {
        	getLogger().info("[generaCalendarioOperacion]["+clienteMB.getRut()+"][BCI_FINOK]");
        }

    }
    
    
    /**
     * <p>Método de tipo prerender que carga los datos del dashboard.
     * 
     * </p>  Registro de Versiones:<ul>
     * <li> 1.0 25/12/2014 Eduardo Pérez (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : versión inicial.</li>
     * </ul>
     *
     * @throws MultilineaException Exception de Precios
     * @throws EJBException Exception EJB
     * @throws ParseException Exception de parseo de variables
     */
    public void cargarDatosBasicos() throws MultilineaException, EJBException, ParseException {
        if(paso!=0) return;
        paso=1;
        if (getLogger().isInfoEnabled()) {
            getLogger().info("[cargarDatosBasicos]["+clienteMB.getRut()+"][BCI_INI]");
        }
        
        try {
        	
        	clienteMB.setApellidoPaterno(null);
            clienteMB.setDatosBasicos();
            cargaGruposCanales();
            obtenerOperaciones();
        }
        catch (Exception e) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error("[cargarDatosBasicos]["+clienteMB.getRut()+"][BCI_FINEX][Exception]"
                +" error con mensaje: " + e.getMessage(), e);
            }
        }
        
        if (getLogger().isInfoEnabled()) {
            getLogger().info("[cargarDatosBasicos]["+clienteMB.getRut()+"][BCI_FINOK]");
        }
    }
    
    /**
     * Carga en un Mapa los códigos de canal agrupados por tipo de canal (web,movil,sucursal).
     */
    private void cargaGruposCanales(){
    	 int total = Integer.valueOf(
         		TablaValores.getValor(TABLA_MULTILINEA, "agrupacionCanales","total")).intValue();
         for(int i=1; i<=total; i++){
             String key = TablaValores.getValor(TABLA_MULTILINEA, "agrupacionCanales","key"+i);
             String[] codCanal = StringUtil.divide(TablaValores.getValor(TABLA_MULTILINEA, key,"canales"),"|");
             String codGrupo = TablaValores.getValor(TABLA_MULTILINEA, key,"codGrupo");
             for (int j = 0; j < codCanal.length; j++) {
            	 clasificacionCanales.put(codCanal[j], codGrupo);
             }
           
         }
    }
    
    /**
     * Permite obtener una instancia para loguear.
     * @return instancia para log
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass());
        }
        return logger;
    }
    
    /**
     * Obtiene el multienvironment.
	 * @return MultiEnvironment multiEnvironment seteado.
	 * @since 1.0
	 */  
	public MultiEnvironment obtenerMultiEnvironment(){
		if (getLogger().isInfoEnabled()) {
			getLogger().info("[obtenerMultiEnvironment][BCI_INI]");
		}
		MultiEnvironment multiEnvironment = new MultiEnvironment();
		multiEnvironment.setBanco(BANCO);
		multiEnvironment.setMarca(MARCA);
		multiEnvironment.setLocale(LOCAL);
		multiEnvironment.setUsuario(USUARIO);
		multiEnvironment.setCanal(sesion.getCanalId());
		multiEnvironment.setCartera(CARTERA);
		
		if (getLogger().isInfoEnabled()) {
			getLogger().info("[obtenerMultiEnvironment][BCI_FIN]");
		}
		return multiEnvironment;
	}
    
	/**
	 * <p>Actualiza la operación seleccionada.
     * 
     * </p>  Registro de Versiones:<ul>
     * <li> 1.0 25/12/2014 Eduardo Pérez (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : versión inicial.</li>
     * <li> 1.1 18/08/2016 Manuel Escárate (BEE) - Felipe Ojeda. (ing.Soft.BCI) : Se realiza llamada a obtenerValoresCredito
     *                                                            al momento de selecciona del combobox.</li>
     * </ul>
     *
	 */
	public void seleccionOperacionListener() {
		if (getLogger().isInfoEnabled()) {
	    	getLogger().info("[seleccionOperacionListener] [BCI_INI]");
		}
		
		this.setOperacionSeleccionada((OperacionCreditoSuperAmp) operacionesMap.get(selectedItem));
		obtenerValoresCredito();
		
		paso = PASO2;
		if (getLogger().isInfoEnabled()) {
	    	getLogger().info("[seleccionOperacionListener] [BCI_FINOK]");
		}
	}
	
	/**
	 * <p>Permite regresar a la primera pantalla.
     * 
     * </p>  Registro de Versiones:<ul>
     * <li> 1.0 25/12/2014 Eduardo Pérez (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : versión inicial.</li>
     * </ul>
	 */
	public void volverAListado(){
		paso = 1;
	}
	
	/**
	 * Método encargado de obtener valores asociados al crédito.
	 * <br>
	 * Registro de versiones:<ul>
	 * <li>1.0 11/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): versión inicial.</li> 
	 * </ul>
	 * @since 1.2
	 */    
	public void obtenerValoresCredito(){
		MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(sesion.getCanalId(), USUARIO);
		try {
			operacionCredito = crearEJBmultilinea().consultaOperacionCredito(multiEnvironment,
					operacionSeleccionada.getIdOperacion(), operacionSeleccionada.getNumOperacion());

			ResultConsultaCgr seguros = obtenerSeguros(multiEnvironment, operacionSeleccionada.getIdOperacion(), String.valueOf(operacionSeleccionada.getNumOperacion()));
			double sumaSeguros = 0;
			for(int j = 0; j < seguros.getInstanciaDeConsultaCgr().length; j++){
				IteracionConsultaCgr cgr = seguros.getInstanciaDeConsultaCgr()[j];
				if (cgr.getIndVigenciaInst() == 'S'){
					sumaSeguros = sumaSeguros + cgr.getTasaMontoFinal();
				}
			}
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[obtenerValoresCredito] Total Seguros: " + sumaSeguros);
			}
			operacionCredito.setValorOtroSeguro(sumaSeguros);
			if (getLogger().isDebugEnabled()){
				getLogger().debug("[obtenerValoresCredito] tasa de interes especial" + operacionCredito.getInteresEspecial());
				getLogger().debug("[obtenerValoresCredito] valor seguro" + operacionCredito.getValorOtroSeguro());
			}
			ResultConsultaCgr gastos = obtenerGastoNotarial(multiEnvironment, operacionSeleccionada.getIdOperacion(), String.valueOf(operacionSeleccionada.getNumOperacion()));
			double gastoNotario = 0;
			if (gastos != null && gastos.getTotOcurrencias() > 0){
				gastoNotario = gastos.getInstanciaDeConsultaCgr(0).getTasaMontoFinal();
				if (getLogger().isDebugEnabled()){ 
					getLogger().debug("[obtenerValoresCredito] valorNotario: " +  gastoNotario);
				}
			}
			else {
				if (getLogger().isDebugEnabled()){
					getLogger().debug("[obtenerValoresCredito] No existen ocurrencias en la consulta CGR para Gastos Notariales");
				}
			}
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[obtenerValoresCredito] Gastos Notariales: " + gastoNotario);
			}
			operacionCredito.setValorGasto(gastoNotario);
		} 
		catch (Exception ex) {
			if (getLogger().isEnabledFor(Level.ERROR)) {
				getLogger().debug("[obtenerValoresCredito] [BCI_FINEX] Exception:" + ErroresUtil.extraeStackTrace(ex));
			}
		}
	}
	
	/**
	 * Obtiene los seguros del credito.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 18/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): versión inicial.</li> 
     * </ul>
     * @param multiEnvironment multiambiente.
     * @param cai cai de la operación.
     * @param iic iic de la operación.
     * @return seguros del credito.
     * @since 1.2
     */    
    public ResultConsultaCgr obtenerSeguros(MultiEnvironment multiEnvironment, String cai, String iic){
    	if (getLogger().isDebugEnabled()) {
			getLogger().debug("[obtenerSeguros] [BCI_INI]");
		}
    	 ResultConsultaCgr obeanCGRSGS = new ResultConsultaCgr();
         try{
             
        	 obeanCGRSGS =  crearEJBprecios().consultaCgr(multiEnvironment,
            		  cai,
                      iic,
                      "",
                      "CRE",
                      "CUR",
                      0,
                      ' ',
                      ' ',
                      "",
                      "",
                      "SGS",
                      'E',
                      ' ',
                      "",
                      "",
                      "",
                      "",
                      "",
                      0);
         } 
         catch(Exception e) {
             if (getLogger().isDebugEnabled()) getLogger().debug("[obtenerSeguros] [BCI_FINEX] ERROR***Exception...Exception [" + e.getMessage() + "]");
         }
         if (getLogger().isDebugEnabled()) {
        	 getLogger().debug("[obtenerSeguros] [BCI_FINOK]");
         }
         return obeanCGRSGS;
    }
    
    /**
     * Obtiene los gastos notariales.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 18/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): versión inicial.</li> 
     * </ul>
     * @param multiEnvironment multiambiente.
     * @param cai cai de la operación.
     * @param iic iic de la operación.
     * @return seguros del credito.
     * @since 1.2
     */    
    public ResultConsultaCgr obtenerGastoNotarial(MultiEnvironment multiEnvironment, String cai, String iic){
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[obtenerGastoNotarial][" + clienteMB.getRut() + "][BCI_INI]");
    	}
    	 ResultConsultaCgr obeanCGRCGN = new ResultConsultaCgr();
         try{
        	 obeanCGRCGN =  crearEJBprecios().consultaCgr(multiEnvironment,
            		  cai,
                      iic,
                      "",
                      "CRE",
                      "CUR",
                      0,
                      ' ',
                      ' ',
                      "",
                      "",
                      "CGN",
                      'E',
                      ' ',
                      "",
                      "",
                      "",
                      "",
                      "",
                      0);
         } 
         catch(Exception e) {
             if (getLogger().isDebugEnabled()) getLogger().debug("[BCI_FINEX] ERROR***Exception...Exception [" + e.getMessage() + "]");
         }
         if (getLogger().isDebugEnabled()) {
             getLogger().debug("[obtenerGastoNotarial][" + clienteMB.getRut() + "][BCI_FINOK]");
        }
         return obeanCGRCGN;
    }
	
	/**
     * retorna una instancia del EJB Multilinea.
     * <P>
     * Registro de versiones:
     * <ul>
     * <li> 1.0  29/07/2016 Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI): Versión inicial.</li>
     * </ul>
     * <p>
     * @return Multilinea instancia del ejb Multilinea.
     * @throws GeneralException en caso de error.
     * @since 1.2
     */
    private Multilinea crearEJBmultilinea() throws GeneralException {
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[crearEJBmultilinea][" + clienteMB.getRut() + "][BCI_INI]");
    	}
        try {
            Multilinea multilineaBean = null;
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Obteniendo referencia del EJB " + JNDI_NAME_MULTILINEA);
            }
            multilineaBean = ((MultilineaHome) EnhancedServiceLocator
                    .getInstance().getHome(JNDI_NAME_MULTILINEA,
                            MultilineaHome.class)).create();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Referencia obtenida a " + JNDI_NAME_MULTILINEA);
            }
        	if (getLogger().isDebugEnabled()) {
        		getLogger().debug("[crearEJBmultilinea][" + clienteMB.getRut() + "][BCI_FINOK]");
        	}
            return multilineaBean;
        }
        catch (Exception e) {
            if (getLogger().isEnabledFor(Level.ERROR)){
                getLogger().error("[crearEJBmultilinea] [BCI_FINEX]RemoteException: Error al crear instancia EJB", e);
            }
            throw new GeneralException("ESPECIAL", "Error al crear instancia EJB");
        }
    }
    
    /**
     * retorna una instancia del EJB Precios.
     * <P>
     * Registro de versiones:
     * <ul>
     * <li> 1.0  18/08/2016 Manuel Escárate. (BEE) - Felipe Ojeda (ing.Soft.BCI): Versión inicial.</li>
     * </ul>
     * <p>
     * @return PreciosContextos instancia del ejb Multilinea.
     * @throws GeneralException en caso de error.
     * @since 1.2
     */
    private PreciosContextos crearEJBprecios() throws GeneralException {
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[crearEJBprecios][" + clienteMB.getRut() + "][BCI_INI]");
    	}
        try {
        	PreciosContextos preciosContextos = null;
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Obteniendo referencia del EJB " + JNDI_NAME_PRECIOS);
            }
            preciosContextos = ((PreciosContextosHome) EnhancedServiceLocator
                    .getInstance().getHome(JNDI_NAME_PRECIOS,
                    		PreciosContextosHome.class)).create();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Referencia obtenida a " + JNDI_NAME_PRECIOS);
            }
            if (getLogger().isDebugEnabled()) {
        		getLogger().debug("[crearEJBprecios][" + clienteMB.getRut() + "][BCI_FINOK]");
        	}
            return preciosContextos;
        }
        catch (Exception e) {
            if (getLogger().isEnabledFor(Level.ERROR)){
                getLogger().error("[crearEJBprecios] [BCI_FINEX]RemoteException: Error al crear instancia EJB", e);
            }
            throw new GeneralException("ESPECIAL", "Error al crear instancia EJB");
        }
    }    
	
    public ClienteMB getClienteMB() {
        return clienteMB;
    }
    
    public void setClienteMB(ClienteMB clienteMB) {
        this.clienteMB = clienteMB;
    }
    
    public SesionMB getSesion() {
		return sesion;
	}
    
    public void setSesion(SesionMB sesion) {
		this.sesion = sesion;
	}

	public ProductosMB getProductosMB() {
		return productosMB;
	}
	
	public void setProductosMB(ProductosMB productosMB) {
		this.productosMB = productosMB;
	}

	public String getCondicionGarantia() {
		return condicionGarantia;
	}

	public void setCondicionGarantia(String condicionGarantia) {
		this.condicionGarantia = condicionGarantia;
	}

	public ArrayList<OperacionCreditoSuperAmp> getOperaciones() {
		return operaciones;
	}

	public void setOperaciones(ArrayList<OperacionCreditoSuperAmp> operaciones) {
		this.operaciones = operaciones;
	}

	public int getPaso() {
		return paso;
	}

	public void setPaso(int paso) {
		this.paso = paso;
	}

	public OperacionCreditoSuperAmp getOperacionSeleccionada() {
		return operacionSeleccionada;
	}

	public HashMap<String, ArrayList<DatosCalendarioPagoTO>> getCalendariosPago() {
		return calendariosPago;
	}

	public void setCalendariosPago(
			HashMap<String, ArrayList<DatosCalendarioPagoTO>> calendariosPago) {
		this.calendariosPago = calendariosPago;
	}

	/**
	 *<p>Setea la operacion seleccionada.
     * 
     * </p>  Registro de Versiones:<ul>
     * <li> 1.0 25/12/2014 Eduardo Pérez (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : versión inicial.</li>
     * </ul>
	 * @param operacionSeleccionada operacion seleccionada.
	 */
	public void setOperacionSeleccionada(OperacionCreditoSuperAmp operacionSeleccionada) {
		this.operacionSeleccionada = operacionSeleccionada;
		this.selectedItem = operacionSeleccionada.getIdOperacion()+operacionSeleccionada.getNumOperacion();
	}

	public HashMap<String, String> getClasificacionCanales() {
		return clasificacionCanales;
	}

	public void setClasificacionCanales(HashMap<String, String> clasificacionCanales) {
		this.clasificacionCanales = clasificacionCanales;
	}

	public ArrayList<SelectItem> getSelectItems() {
        return selectItems;
    }
	
	public String getSelectedItem() {
        return selectedItem;
    }
	
	public void setSelectedItem(String selectedItem) {
        this.selectedItem = selectedItem;
    }
    
	public String getFormatoUf() {
		return FORMATO_UF;
	}

	public String getFormatoPesos() {
		return FORMATO_PESOS;
	}	
	
	public boolean isErrorGenerico() {
		return errorGenerico;
	}

	public void setErrorGenerico(boolean errorGenerico) {
		this.errorGenerico = errorGenerico;
	}

	public ResultConsultaOperacionCredito getOperacionCredito() {
		return operacionCredito;
	}

	public void setOperacionCredito(ResultConsultaOperacionCredito operacionCredito) {
		this.operacionCredito = operacionCredito;
	}
	
}
