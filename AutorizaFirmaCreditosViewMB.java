package cl.bci.aplicaciones.productos.servicios.multilinea.mb;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJBException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

import wcorp.bprocess.boletadegarantia.SvcBoletaDeGarantiaImpl;
import wcorp.bprocess.creditosgenerales.LocalizadorDeServicios;
import wcorp.bprocess.creditosglobales.SvcCreditosGlobales;
import wcorp.bprocess.creditosglobales.SvcCreditosGlobalesImpl;
import wcorp.bprocess.multilinea.Multilinea;
import wcorp.bprocess.multilinea.MultilineaException;
import wcorp.bprocess.multilinea.MultilineaHome;
import wcorp.bprocess.multilinea.to.DatosAvalesTO;
import wcorp.bprocess.multilinea.to.DatosDisclaimerTO;
import wcorp.bprocess.multilinea.to.DatosOperacionTO;
import wcorp.bprocess.multilinea.to.DatosParaCorreoTO;
import wcorp.bprocess.multilinea.to.DatosParaFirmaTO;
import wcorp.bprocess.multilinea.to.DatosParaOperacionesFirmaTO;
import wcorp.bprocess.multilinea.to.DatosResultadoOperacionesTO;
import wcorp.bprocess.precioscontextos.PreciosContextos;
import wcorp.bprocess.precioscontextos.PreciosContextosHome;
import wcorp.bprocess.tables.TableManagerService;
import wcorp.gestores.validador.utils.ValidaCuentasUtility;
import wcorp.model.actores.DireccionClienteBci;
import wcorp.serv.bciexpress.ConApoquehanFirmado;
import wcorp.serv.bciexpress.ResultConsultarApoderadosquehanFirmado;
import wcorp.serv.boletadegarantia.RowConsultaMasivaDeOficinas;
import wcorp.serv.clientes.RetornoTipCli;
import wcorp.serv.clientes.ServiciosCliente;
import wcorp.serv.clientes.ServiciosClienteHome;
import wcorp.serv.controlriesgocrediticio.Aval;
import wcorp.serv.controlriesgocrediticio.ResultConsultaAvales;
import wcorp.serv.creditosglobales.CalendarioPago;
import wcorp.serv.creditosglobales.ResultActivacionDeOpcAlDia;
import wcorp.serv.creditosglobales.ResultConsultaCalendarioPago;
import wcorp.serv.creditosglobales.ResultConsultaOperacionCredito;
import wcorp.serv.direcciones.DireccionesException;
import wcorp.serv.direcciones.ServiciosDirecciones;
import wcorp.serv.direcciones.ServiciosDireccionesHome;
import wcorp.serv.precios.IteracionConsultaCgr;
import wcorp.serv.precios.ResultConsultaCgr;
import wcorp.serv.seguridad.SeguridadException;
import wcorp.util.EnhancedServiceLocator;
import wcorp.util.EnvioDeCorreo;
import wcorp.util.ErroresUtil;
import wcorp.util.FechasUtil;
import wcorp.util.GeneralException;
import wcorp.util.StringUtil;
import wcorp.util.TablaValores;
import wcorp.util.TextosUtil;
import wcorp.util.bee.CrearMultiEnviroment;
import wcorp.util.bee.MultiEnvironment;
import wcorp.util.tables.core.Row;
import wcorp.util.tables.core.TableAccessException;
import wcorp.util.tables.core.TableSpec;
import wcorp.util.tables.core.impl.TableSpecImpl;

import cl.bci.aplicaciones.cliente.mb.ClienteMB;
import cl.bci.aplicaciones.cliente.mb.UsuarioModelMB;
import cl.bci.aplicaciones.colaborador.mb.ColaboradorModelMB;
import cl.bci.aplicaciones.firma.mb.ServicioAutorizacionyFirmaModelMB;
import cl.bci.aplicaciones.firma.to.ResultadoFirmaTO;
import cl.bci.aplicaciones.iconos.util.mb.GeneraComprobanteUtilityMB;
import cl.bci.aplicaciones.productos.mb.ProductosMB;
import cl.bci.aplicaciones.servicios.multilinea.pyme.to.DatosComprobantePdfTO;
import cl.bci.infraestructura.web.journal.Journalist;
import cl.bci.infraestructura.web.seguridad.mb.SesionMB;
import cl.bci.infraestructura.web.seguridad.segundaclave.SegundaClaveUIInput;

/** 
 * ManagedBean
 * 
 * <pre><b>AutorizaFirmaCreditosViewMB</b>
 *
 * Componente encargado de interactuar con la vista para realizar la
 * firma de créditos, a través de diferentes pasos.
 * </pre>
 *
 * Registro de versiones:<ul>
 *
 * <li>1.0  12/03/2015, Braulio Rivas S. (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión inicial. </li>
 * <li>1.1  12/11/2015, Eduardo Perez G. (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI) : Se Incopora validación de 
 *                                                                     cuenta cliente BCI.
 * </li>
 * <li>1.2  18/05/2016, Manuel Escárate R. (BEE S.A.) - Pablo Paredes (ing.Soft.BCI) : Se modifica el método
 *                                                       firmarOperacion y confirmacionAutorizar además se agregan los métodos 
 *                                                       enviarCorreoEjecutivo,cumpleHorarios,enviaEmailEjecutivo,
 *                                                       buscarMailJefeOficina,obtieneCorreoEjecutivo,enviarCorreoAvales,
 *                                                       enviaEmailAval,enviaMensajeCorreo,armaEmailParaDestinatarioGenerico,
 *                                                       armaEmailParaDestinatarioAval,plantillaCorreo,instanciaEJBServicioDirecciones,
 *                                                       crearEJBServiciosCliente.</li>
 * <li>1.3 12/08/2016, Manuel Escárate R. (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se modifican los métodos confirmacionAutorizar,firmarOperacion,
 *                                                       enviarCorreoAvales,enviaEmailAval,armaEmailParaDestinatarioAval y se agregan métodos obtenerTablaDescripciones,
 *                                                       getValor,enviarCorreoContactoEmpresa,enviarEmailContactoEmpresa,armaEmailParaDestinatarioContacto,obtenerSeguros,
 *                                                       obtenerGastoNotarial,crearEJBprecios. </li>
 *                                                                                                     
 * </ul> 
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 */
@ManagedBean
@ViewScoped
public class AutorizaFirmaCreditosViewMB implements Serializable {

    /**
     * serialVersionUID de la clase.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Constante para identificar el usuario a utilizar en la construccin del objeto MultiEnvironment.
     */
    private static final String USUARIO = TablaValores.getValor("multilinea.parametros"
            , "tipoUsuarioParaMulti", "usuario");
    
    /**
     * Constante para identificar el perfil de usuario autorizado para cursar.
     */
    private static final String PERFIL_USUARIO = TablaValores.getValor("multilinea.parametros",
            "usuarioAutorizado", "perfilUsuario");
    
    /**
     * Constante para identificar el tipo de usuario autorizado para cursar.
     */
    private static final String TIPO_USUARIO = TablaValores.getValor("multilinea.parametros",
            "usuarioAutorizado", "tipoUsuario");
    
    /**
     * Tabla multilinea.
     */
    private static final String TABLA_MULTILINEA = "multilinea.parametros";
    
    /**
     * sistema tablas generales.
     */
    private static final String TABLE_MANANGER_SISTEMA_TAB = "TAB";
    
    /**
     * sistema tablas generales de colocaciones.
     */
    private static final String TABLE_MANANGER_TABLA_COM = "COM";

    /**
     * Nombre JNDI del EJB Multilinea.
     */
    private static final String JNDI_NAME_MULTILINEA = "wcorp.bprocess.multilinea.Multilinea";
    
    /**
     * Nombre JNDI del EJB PreciosContextos.
     */
    private static final String JNDI_NAME_PRECIOS = "wcorp.bprocess.precioscontextos.PreciosContextos";    
    
      /**
     * Locale para Chile.
     */
    private static final Locale CL = new Locale("es", "CL");
    
    /**
     * Esquema para url.
     */
    private static final String URL_SCHEME = "file:///";
    
    /**
     * Garantia Avales.
     */
    private static final String GARANTIA_AVALES = "2";
    
    /** 
     * Codigo tipo email.
     */
 	private static final char TIPOMAIL = '7';
    
    /**
     * Atributo numérica.
     */
    private static final int VALOR_DOS= 2;
    
    /**
     * Atributo numérica.
     */
    private static final int VALOR_TRES = 3;
    
    /**
     * Atributo numérica.
     */
    private static final int VALOR_CUATRO = 4;
    
    /**
     * Atributo numérica.
     */
    private static final int VALOR_CINCO = 5;
    
    /**
     * Atributo numérica.
     */
    private static final int VALOR_SEIS = 6;
    
    /**
     * Error generico.
     */
    private static final int ERROR_GENERICO = -2;
    
    /**
     * Usuario no posee apo_idn.
     */
    private static final int NO_POSEE_APO_IDN = 100;
    
    /**
     *  Firmo alguien con apo_idn.
     */
    private static final int FIRMO_APO_IDN = 101;
    
    /**
     *  Problemas con conexión banele.
     */
    private static final int PROBLEMAS_CONEXION = 102;
    
    /**
	 * Valor númerico.
	 */
	private static final int VALOR_12 =  12;

    /**
     * Atributo Logger.
     */ 
    private static transient Logger logger  = (Logger)Logger.getLogger(AutorizaFirmaCreditosViewMB.class);
    
    /**
     * Identificador del paso de firma.
     */
    private int paso = 0;
    /**
     * Indicador respuesta de firma.
     */
    private int respuestaFirma;
    /**
     * Mensajes de error.
     */
    private String error;
    
    /**
     * Mensajes de error en segunda clave.
     */
    private String errorSegundaClave;
    
    /**
     * Código de oficina.
     */
    private String codOficina;
    
    /**
     * Código de ejecutivo.
     */
    private String codEjecutivo;
    
   /**
    * Código de banca.
    */
    private String codBanca;
    
    /**
     * Condición de garantía.
     */
    private String condicionGarantia;
    
    /**
     * Tasa de interés obtenida.
     */
    private double tasaInteres;
    
    /**
     * Atributo para mostrar check de acepta condiciones para mandato.
     */

    private boolean mostrarCondicionesMandato;
    
    /**
     * Atributo que guardar el check de acepta condiciones.
     */
    private boolean aceptaCondicionesMandato;
    
    /**
     * Disclaimer para mandato.
     */
    private String disclaimerMandato;

    /**
     * Variable multiambiente.
     */
    private MultiEnvironment multiEnvironment;

    /**
     * Manejo Segunda Clave.
     */    
    private SegundaClaveUIInput segundaClaveAplicativo;

    /**
     * Objeto contiene datos para la firma.
     */
    private DatosParaFirmaTO datosParaFirmaTO;
    
    /**
     * Operacion Seleccionada para firmar.
     */
    private DatosOperacionTO operacionSeleccionada;
    
    /**
     * Calendario de pago para comprobante.
     */
    private CalendarioPago[] calendarioPago;
    
    /**
     * Largo comuna.
     */
    private int largoComuna;
    
    /**
     * Monto Correo.
     */
    private double montoCorreo;
    
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
     * Atributo representa Managed Bean inyectado ColaboradorModelMB.
     */
    @ManagedProperty(value = "#{colaboradorModelMB}")
    private ColaboradorModelMB colaborador;

    /**
     * Atributo representa Managed Bean inyectado ProductosMB.
     */
    @ManagedProperty(value = "#{productosMB}")
    private ProductosMB productosMB;

    /**
     * Atributo representa Managed Bean inyectado ServicioAutorizacionyFirmaModelMB.
     */
    @ManagedProperty(value = "#{servicioAutorizacionyFirmaModelMB}")
    private ServicioAutorizacionyFirmaModelMB servicioAutorizacionyFirmaModelMB;

    /**
     * Atributo representa Managed Bean inyectado UsuarioModelMB.
     */
    @ManagedProperty(value = "#{usuarioModelMB}")
    private UsuarioModelMB usuarioModelMB;

    /**
     * Glosas de los distintos tipos de crédito disponibles.
     */
    private HashMap<String,String> glosasTipoCredito = new HashMap<String,String>();
    
    /**
     * Atributo que representa el EJB de ServiciosCliente.
     */
    private ServiciosCliente serviciosCliente = null;
    
    /**
     * Método encargado de iniciar proceso de firma.
     *
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0  01/04/2015 Braulio Rivas S. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión inicial. </li>
     * </ul>
     * </p>
     * @since 1.0
     */
    public  void iniciarFirma(){
        if(paso != 0) return;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[iniciarFirma] Inicio metodo");
        }
        clienteMB.setDatosBasicos();

        try {
            multiEnvironment = obtenerMultiEnvironment();
            
            //Se carga la información de glosas para los distintos tipos de créditos seleccionados
            String[] tiposDeCreditos = StringUtil.divide(TablaValores.getValor("multilinea.parametros",
            		"tiposDeCreditos","valores").trim(), "|");
            for(int i=0; i< tiposDeCreditos.length; i++){
            	String glosaTipoCredito = TablaValores.getValor("multilinea.parametros",
            			tiposDeCreditos[i],"glosa");
            	glosasTipoCredito.put(tiposDeCreditos[i], glosaTipoCredito);
            }
            
            if (datosParaFirmaTO == null){
                datosParaFirmaTO = new DatosParaFirmaTO();
                List operaciones = (ArrayList) obtenerCreditosPorFirmar(
                        "", clienteMB.getNumeroConvenio(), clienteMB.getRut(), clienteMB.getDigitoVerif());
                if (operaciones != null && operaciones.size() > 0){
                	for (Iterator iterator = operaciones.iterator(); iterator
							.hasNext();) {
                		DatosOperacionTO datosOperacionTO = (DatosOperacionTO) iterator.next();
                        ResultConsultarApoderadosquehanFirmado apoderados = 
                                listaFirmantes(datosOperacionTO.getCaiOperacion() 
                                		+ datosOperacionTO.getIicOperacion(), 
                                clienteMB.getNumeroConvenio(),clienteMB.getRut());
                        datosOperacionTO.setTipoOperacion(datosOperacionTO.getTipoOperacion().trim());
                        datosOperacionTO.setAuxiliarOpe(datosOperacionTO.getAuxiliarOpe().trim());
                        datosOperacionTO.setApoderadosFirmados(apoderados);
                    }
                }
                datosParaFirmaTO.setOperaciones(operaciones);
            }

        } 
        catch (Exception e) {
        	  if (getLogger().isEnabledFor(Level.ERROR)) {
                  getLogger().error("[iniciarFirma] "
                          +  ErroresUtil.extraeStackTrace(e));
              }
        }
        if (getLogger().isDebugEnabled()){
	        getLogger().debug("[iniciarFirma] Operaciones: " + datosParaFirmaTO.getOperaciones());
	        getLogger().debug("[iniciarFirma] Fin metodo");
        }

    }
    
    /**
     * Método encargado de obtener las operaciones de credito por firmar.
     *
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0  01/04/2015 Braulio Rivas S. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión inicial. </li>
     * </ul>
     * </p>
     * @param idOperacion numero de operacion.
     * @param idConvenio  numero de convenio.
     * @param rutEmpresa  rut de la empresa.
     * @param dvEmpresa   digito verificador de empresa.
     * @return  List operaciones por firmar.
     * @throws MultilineaException control de multilinea.
     * @throws EJBException control de ejb .
     * @throws RemoteException control de remote exception.
     * @throws GeneralException  control general exce´tion.
     * @since 1.0
     */    
    private List obtenerCreditosPorFirmar(String idOperacion, String idConvenio,
    		long rutEmpresa, char dvEmpresa) throws 
    		MultilineaException, EJBException, RemoteException, GeneralException{
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[obtenerCreditosPorFirmar] Inicio metodo");
        }
        List operaciones = crearEJBmultilinea().obtenerCreditosPorFirmar(
        		multiEnvironment, idOperacion,idConvenio,String.valueOf(rutEmpresa),dvEmpresa);
        return operaciones;
    }

    /**
     * Método encargado de obtener la lista de apoderados que han firmado.
     *
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0  01/04/2015 Braulio Rivas S. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión inicial. </li>
     * </ul>
     * </p>
     * @param idOperacion numero de operacion.
     * @param idConvenio  numero de convenio.
     * @param rutEmpresa  rut de la empresa.
     * @return ResultConsultarApoderadosquehanFirmado apoderados que han firmado.
     * @throws MultilineaException .
     * @throws EJBException .
     * @throws RemoteException .
     * @throws GeneralException .
     * @since 1.0
     */    
    private ResultConsultarApoderadosquehanFirmado listaFirmantes(String idOperacion,
    		String idConvenio,long rutEmpresa)
    				throws MultilineaException, EJBException, RemoteException, GeneralException{
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[listaFirmantes] Inicio metodo");
        }    
        ResultConsultarApoderadosquehanFirmado apoderadorFirmados = 
                crearEJBmultilinea().listaTodosFirmantes(
                        String.valueOf(rutEmpresa), idConvenio, idOperacion, new Hashtable<String, String>());
        if (getLogger().isDebugEnabled()){
        	getLogger().debug("[listaFirmantes] Fin metodo: " + apoderadorFirmados);
        }
        return apoderadorFirmados;
    }

    /**
     * Método encargado de mostrar operacion que se firmara.
     *
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0  01/04/2015 Braulio Rivas S. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión inicial. </li>
     * <li>1.1  22/06/2016 Manuel Escárate R. (BEE) - Pablo Paredes (ing.Soft.BCI) : Se valida perfil de usuario. </li>
     * <li>1.2  12/08/2016 Manuel Escárate R. (BEE) - Felipe Ojeda (ing.Soft.BCI) : Se agrega lógica para disclaimer de aval. </li>
     * </ul>
     * </p>
     * @since 1.0
     */    
    public void confirmacionAutorizar(){ 
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[confirmacionAutorizar][" + clienteMB.getRut() + "][BCI_INI]");
    	} 
    	try {
    		long rutCliente = clienteMB.getRut();
    		error = null;
    		errorSegundaClave = null;
    		if (getLogger().isDebugEnabled()){
    			getLogger().debug("[confirmacionAutorizar] operacionSeleccionada: " + operacionSeleccionada);
    		}    
    		if (usuarioModelMB.getTipoUsuario().trim().equalsIgnoreCase(TIPO_USUARIO) && usuarioModelMB.getPerfil().trim().equalsIgnoreCase(PERFIL_USUARIO)){
    			boolean yafirmo = false;
    			if (operacionSeleccionada != null && operacionSeleccionada.getApoderadosFirmados() != null 
    					&&  operacionSeleccionada.getApoderadosFirmados().getConApoquehanFirmado() != null){
    				for (int i = 0; i 
    						< operacionSeleccionada.getApoderadosFirmados().getConApoquehanFirmado().length; i++) {
    					ConApoquehanFirmado apoderado = 
    							operacionSeleccionada.getApoderadosFirmados().getConApoquehanFirmado()[i];
    					String rutApo = apoderado.getRutUsuario().charAt(0) 
    							!= '0' ? apoderado.getRutUsuario().trim() : apoderado.getRutUsuario().substring(
    									1, apoderado.getRutUsuario().length()).trim();
    							if (getLogger().isDebugEnabled()){                    		
    								getLogger().debug("[confirmacionAutorizar] rutApo :" + rutApo + "[confirmacionAutorizar] rutUsuario :" + usuarioModelMB.getRut()+"-" );
    							}
    							if (rutApo.equals(String.valueOf(usuarioModelMB.getRut()))){
    								yafirmo = true;
    								break;
    							}
    				}
    			}
    			if (getLogger().isDebugEnabled()){
    				getLogger().debug("[confirmacionAutorizar] [Ya firmo] [?] " + yafirmo);
    			}
    			if (yafirmo){
    				paso = 0;
    				error = TablaValores.getValor("multilinea.parametros","mensajeYaFirmo","desc"); 
    			}
    			else{

    				boolean revisionMandato = Boolean.parseBoolean(TablaValores.getValor(TABLA_MULTILINEA
    						, "revisionMandato", "valor"));
    				mostrarCondicionesMandato = Boolean.parseBoolean(TablaValores.getValor(TABLA_MULTILINEA
    						, "activaCondicionesMandato", "valor"));
    				if (getLogger().isInfoEnabled()) {
    					getLogger().info("[confirmacionAutorizar][" + rutCliente + "] habilitada revisionMandato: "
    							+ revisionMandato);
    				}
    				if (getLogger().isInfoEnabled()) {
    					getLogger().info("[confirmacionAutorizar][" + rutCliente + "] habilitada activaCondicionesMandato: "
    							+ mostrarCondicionesMandato);
    				}
    				ResultConsultaOperacionCredito operacionCredito = new ResultConsultaOperacionCredito();
    				try {
    					operacionCredito = crearEJBmultilinea().consultaOperacionCredito(multiEnvironment, operacionSeleccionada.getCaiOperacion(), operacionSeleccionada.getIicOperacion());
    				    montoCorreo = operacionCredito.getMontoCredito();
    				
    				}
    				catch (Exception e) {
    					if (getLogger().isEnabledFor(Level.ERROR)) {
    						getLogger().error("[confirmacionAutorizar][" + rutCliente + "][BCI_FINEX] Error al consultar operacion de credito: " + e);
    					}
    					error = TablaValores.getValor("multilinea.parametros", "errorFlujoFirma","errorFirma"); 
    				}

    				if (operacionSeleccionada != null && error != null){
    					paso = 0;
    				}
    				else{
    					if (operacionCredito != null && operacionCredito.getCondicionGar() != null
    							&& !operacionCredito.getCondicionGar().trim().equals(GARANTIA_AVALES)){ 
    						mostrarCondicionesMandato = false;
    					}
    					else{
    						DatosDisclaimerTO datos = new DatosDisclaimerTO();
    						datos.setRutCliente(String.valueOf(usuarioModelMB.getRut() +"-"+ usuarioModelMB.getDigitoVerif()));
    						datos.setRutEmpresa(String.valueOf(clienteMB.getRut() +"-"+  clienteMB.getDigitoVerif()));
    						datos.setNombreCliente(usuarioModelMB.getNombres() + " " + usuarioModelMB.getApPaterno() + " " + usuarioModelMB.getApMaterno());
    						datos.setNombreempresa(clienteMB.getRazonSocial());
    						datos.setMonto(formatearMonto(operacionCredito.getMontoCredito(), 0, "#,###"));
    						disclaimerMandato = crearEJBmultilinea().obtieneDisclaimerMandato(datos);
    					}
    					paso = 1;
    				}
    			} 
    		}
    		else {
    			paso = 0;
    			error =  TablaValores.getValor("multilinea.parametros","usuarioAutorizado","mensaje"); 
    		}
    	} 
    	catch (Exception e) {
    		if (getLogger().isEnabledFor(Level.ERROR)){
    			getLogger().error("[confirmacionAutorizar] [BCI_FINEX] ERROR: " + e);
    		}
    		paso = VALOR_TRES;
    	}
    }

    /**
     * Método encargado de firmar operacion.
     *
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0  01/04/2015 Braulio Rivas S. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión inicial. </li>
     * <li>1.1  12/11/2015 Eduardo Perez G. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : Se Incopora validación de 
     *                                            cuenta cliente BCI y se modifica mensaje de error al firma.
     * </li>
     * <li>1.2  18/05/2016 Manuel Escárate R. (BEE) - Pablo Paredes López (ing.Soft.BCI) : Se incopora lógica de envio de correo
     *                                            y además se agregan consultas para obtener el número nuevo de una operación al renovar.
     * </li>
     * <li>1.3 10/08/2016 Manuel Escárate R. (BEE) -  Felipe Ojeda (ing.Soft.BCI) : Se agrega journalización para mandato, además se realiza llamada a método que envía
     *                                            correo a empresa.
     * </li>                                            
     * </ul>
     * </p>
     * @since 1.0
     */    
    public void firmarOperacion() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[firmarOperacion] [BCI_INI]");
        }    
        error = null;
        errorSegundaClave = null;
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[firmarOperacion][" + clienteMB.getRut()
                        + "]autenticacionSegundaClave");
            }
            
            if(aceptaCondicionesMandato){ 
                String codEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalFirma", "codEventoAceptaCondicionesMandato");
                String codSubEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalFirma", "codSubEventoAceptaCondicionesMandato");
                String producto = TablaValores.getValor(TABLA_MULTILINEA, "journalFirma", "productoAceptaCondicionesMandato");
                journalizar(codEvento,codSubEvento,producto);
            }
            
            if (!ValidaCuentasUtility.validarCuentaBCI(operacionSeleccionada.getCuentaAbono().trim(), "CCT", (int) clienteMB.getRut(), clienteMB.getDigitoVerif())) {
        		if (getLogger().isEnabledFor(Level.ERROR)) {
        			getLogger().error("[firmarOperacion][" + clienteMB.getRut() + "] ha fallado la validacion de la cuenta de abono");
        		}
        		throw new Exception("Problemas al validar la cuenta de abono del cliente");    
        	}
            
            boolean autenticacionSegundaClave = false;
            
            try {
                autenticacionSegundaClave = segundaClaveAplicativo.verificarAutenticacion();
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                            "[firmarOperacion][" + clienteMB.getRut()
                            + "]autenticacionSegundaClave[" + autenticacionSegundaClave + "]");
                }
            }
            catch (SeguridadException ex) {
                getLogger().error("Validacion erronea: " + ex.getMessage(), ex);
                paso = 1;
                error = ex.getMessage() != null ? ex.getMessage() : "Error en autenticacion";
            } 
            
            
            if (autenticacionSegundaClave){
            	if (getLogger().isDebugEnabled()){
            		getLogger().debug("[firmarOperacion] operacionSeleccionada: " + operacionSeleccionada);
            	}
            	SvcCreditosGlobales svcCreditos = new SvcCreditosGlobalesImpl();
            	ResultConsultaOperacionCredito operacion = null;
            	try {
            		operacion = svcCreditos.consultaOperacionCredito(multiEnvironment, 
            				operacionSeleccionada.getCaiOperacion(), operacionSeleccionada.getIicOperacion());
            	}
            	catch (Exception e) {
            		if (getLogger().isEnabledFor(Level.ERROR)) {
            			getLogger().error("[firmarOperacion] consultaOperacionCredito Exception:" 
            					+ ErroresUtil.extraeStackTrace(e));
            		}
            		try{
            			enviarCorreoEjecutivo(multiEnvironment,e.getMessage(),"asuntoEmailProblemaAvance",true,true);
            		}
            		catch (Exception ex){
            			  if (getLogger().isEnabledFor(Level.ERROR)){
                    		  getLogger().error("[firmarOperacion] ERROR al enviar correos ejecutivo: " + ex);
                    	  }
            		}
            	}
            	if (getLogger().isDebugEnabled()){
            		getLogger().debug("[firmarOperacion] condición de garantía ["+ operacion.getCondicionGar()  +"]");
            		getLogger().debug("[firmarOperacion] tasa de interés ["+ operacion.getTasaSprea()  +"]");
                }
            	condicionGarantia = operacion.getCondicionGar() != null ? operacion.getCondicionGar() : null;
            	tasaInteres = operacion.getTasaSprea() != 0 ? operacion.getTasaSprea() : 0;
            	
                ResultadoFirmaTO resultadoFirmaTO = servicioAutorizacionyFirmaModelMB.firmar(operacionSeleccionada.getCaiOperacion() + operacionSeleccionada.getIicOperacion() , operacionSeleccionada.getMontoOperacion(), false);
                respuestaFirma = resultadoFirmaTO.getCodigoRespuesta();
                
                switch (respuestaFirma) {
                case 1:
                 try{
                	if (getLogger().isDebugEnabled()){
                		getLogger().debug("[firmarOperacion]FIRMA OK");
                    }
                    
                    if (getLogger().isInfoEnabled()) {
                        getLogger().info("[ingresoOperacionFirma]["+clienteMB.getRut()+"]");
                    }
                    DatosParaOperacionesFirmaTO datosOperacionesFirma = new DatosParaOperacionesFirmaTO();
                    datosOperacionesFirma.setDigitoVerifEmp(clienteMB.getDigitoVerif());
                    datosOperacionesFirma.setIdConvenio(clienteMB.getNumeroConvenio());
                    datosOperacionesFirma.setIdentificadorFirma(operacionSeleccionada.getIdentificador());
                    datosOperacionesFirma.setRutEmpresa(String.valueOf(clienteMB.getRut()));
                    datosOperacionesFirma.setNumOperacion(operacionSeleccionada.getCaiOperacion());
                    datosOperacionesFirma.setAuxiliarCredito(String.valueOf(
                    		operacionSeleccionada.getIicOperacion()));
                    datosOperacionesFirma.setQuienVisa(VALOR_DOS);
                    datosOperacionesFirma.setCondicionGarantia("");
                    
                    datosOperacionesFirma.setMontoCredito(operacionSeleccionada.getMontoCredito());
                    datosOperacionesFirma.setProcesoNegocio(operacionSeleccionada.getProcesoNegocio());
                    datosOperacionesFirma.setCuentaAbono(operacionSeleccionada.getCuentaAbono());
                    datosOperacionesFirma.setCuentaCargo(operacionSeleccionada.getCuentaCargo());
                    datosOperacionesFirma.setTipoOperacion(operacionSeleccionada.getTipoOperacion());
                    datosOperacionesFirma.setNumOperacionCan(operacionSeleccionada.getNumOperacionCan());
                    datosOperacionesFirma.setCodAuxiliarCredito(operacionSeleccionada.getCodAuxiliarCredito());
                    datosOperacionesFirma.setOficinaIngreso(operacionSeleccionada.getOficinaIngreso());
                    datosOperacionesFirma.setFechaPrimerVencimiento(operacionSeleccionada.getFechaPrimerVcto());
                    datosOperacionesFirma.setCodigoMoneda(operacionSeleccionada.getMoneda());
                    datosOperacionesFirma.setCodigoMoneda2(operacionSeleccionada.getCodMonedaOrigen());
                    datosOperacionesFirma.setFechaExpiracion2(operacionSeleccionada.getFechaExpiracion2());
                    datosOperacionesFirma.setMonedaLinea(operacionSeleccionada.getCodMonedaOrigen());
                    datosOperacionesFirma.setTotalVencimientos(operacionSeleccionada.getTotalVencimientos());
                    datosOperacionesFirma.setGlosaTipoCredito(operacionSeleccionada.getTipoCredito());
                    datosOperacionesFirma.setAuxiliarOpe(operacionSeleccionada.getAuxiliarOpe());
	                DatosResultadoOperacionesTO resultadoOpe = null;
	                try{
	                	resultadoOpe = crearEJBmultilinea().ingresarOperacionFirma(
	                			multiEnvironment, datosOperacionesFirma, 1);
	                }	
	                catch (Exception e){
	                	if (getLogger().isEnabledFor(Level.ERROR)) {
	                		getLogger().error("[activaAvanceMultilinea] Exception:" 
	                				+ ErroresUtil.extraeStackTrace(e));
	                	}
	                	try{
	                		enviarCorreoEjecutivo(multiEnvironment, e.getMessage(),"asuntoEmailProblemaAvance",true,true);
	                	}
	                	catch (Exception ex){
	            			  if (getLogger().isEnabledFor(Level.ERROR)){
	                    		  getLogger().error("[firmarOperacion] ERROR al enviar correos ejecutivo: " + ex);
	                    	  }
	            		}
	                }
                    if(datosOperacionesFirma.getProcesoNegocio().trim().equalsIgnoreCase("AVC") || datosOperacionesFirma.getProcesoNegocio().trim().equalsIgnoreCase("AVN")){
	                    if (resultadoOpe.isRespuesta()){
	                    	ResultActivacionDeOpcAlDia respuestaActivacion = null;
	                    	try{ 
	                    		respuestaActivacion = crearEJBmultilinea().activaAvanceMultilinea(multiEnvironment,
	                    				datosOperacionesFirma.getNumOperacion(),Integer.parseInt(
	                    						datosOperacionesFirma.getAuxiliarCredito()),
	                    						datosOperacionesFirma.getRutEmpresa(),
	                    						String.valueOf(datosOperacionesFirma.getDigitoVerifEmp()),
	                    						datosOperacionesFirma.getCondicionGarantia(),
	                    						datosOperacionesFirma.getQuienVisa(),
	                    						null);
	                    	}
	                    	catch (Exception e){
	                    		if (getLogger().isEnabledFor(Level.ERROR)) {
									getLogger().error("[activaAvanceMultilinea] Exception:" 
											+ ErroresUtil.extraeStackTrace(e));
	                    		}
	                    		try{
	                    			enviarCorreoEjecutivo(multiEnvironment,e.getMessage(),"asuntoEmailProblemaAvance",true,true);
	                    		}
	                    		catch (Exception ex){
	                    			if (getLogger().isEnabledFor(Level.ERROR)){
	                    				getLogger().error("[firmarOperacion] ERROR al enviar correos ejecutivo: " + ex);
	                    			}
	                    		}
	                    	}
	                          
	                          if (respuestaActivacion != null && respuestaActivacion.getCim_status() != null 
	                        		  && respuestaActivacion.getCim_status().equals("0")){
		                          String codEvento = TablaValores.getValor("multilinea.parametros", "journalFirma", "codEventoFirmaOK");
		                          String codSubEvento = TablaValores.getValor("multilinea.parametros", "journalFirma", "codSubEventoFirmaOK");
		                          String producto = TablaValores.getValor("multilinea.parametros", "journalFirma", "productoFirmaOK");
		                          journalizar(codEvento,codSubEvento,producto);
		                          datosParaFirmaTO  = null;
		                          paso = VALOR_DOS;
		                          if (getLogger().isDebugEnabled()) {
		                              getLogger().debug("[firmarOperacion] [activación ok]");
		                          }        
		                          if (getLogger().isDebugEnabled()){
		                        	  getLogger().debug("[ingresarOperacionFirma] activarAvanceMultilinea:" + resultadoOpe.isRespuesta());
		                          }
		                          try { 
		                        	  if (condicionGarantia.trim().equals("2")){
		                        		  enviarCorreoAvales(multiEnvironment,"opeExitoAvance","asuntoEmailExito");
		                        	  }
		                          }
		                          catch (Exception ex){
		                        	  if (getLogger().isEnabledFor(Level.ERROR)){
		                        		  getLogger().error("[firmarOperacion] ERROR al enviar correos aval: " + ex);
		                        	  }
		                          }  
		                          try {  
		                        	  enviarCorreoEjecutivo(multiEnvironment,"opeExitoAvance","asuntoEmailExito",false,false);
		                          }
		                          catch (Exception ex){
		                        	  if (getLogger().isEnabledFor(Level.ERROR)){
		                            		getLogger().error("[firmarOperacion] ERROR al enviar correo ejecutivo: " + ex);
		                            	}
		                          }
		                          try { 
		                        	  enviarCorreoContactoEmpresa(multiEnvironment, "opeExitoAvance", "asuntoEmailExito");
		                          }
		                          catch (Exception ex){
		                        	  if (getLogger().isEnabledFor(Level.ERROR)){
		                        		  getLogger().error("[firmarOperacion] ERROR al enviar correo a la empresa: " + ex);
		                        	  }
		                          }  
	                          } 
	                          else {
	                              paso = 1;
	                              errorSegundaClave = TablaValores.getValor("multilinea.parametros", "erroActivacionFirma", "desc");
	                          }
	                    }
                    }
                    
                    if(datosOperacionesFirma.getProcesoNegocio().trim().equalsIgnoreCase("REN") || datosOperacionesFirma.getProcesoNegocio().trim().equalsIgnoreCase("RNN")){
                    	 if (resultadoOpe.isRespuesta()){
                    	    paso = VALOR_DOS;
                    	    datosParaFirmaTO  = null;
                         	ResultConsultaOperacionCredito operacionNueva = null;
                         	try {
                         		operacionNueva = svcCreditos.consultaOperacionCredito(multiEnvironment, 
                         				 resultadoOpe.getCaiOperacion(), resultadoOpe.getIicOperacion());
                         	}
                         	catch (Exception e) {
                         		if (getLogger().isEnabledFor(Level.ERROR)) {
                         			getLogger().error("[firmarOperacion] consultaOperacionCredito Exception:" 
                         					+ ErroresUtil.extraeStackTrace(e));
                         		}
                         		try{
                         			enviarCorreoEjecutivo(multiEnvironment, e.getMessage(),"asuntoEmailProblemaAvance",true,true);
                         		}
                         		catch (Exception ex){
                         			if (getLogger().isEnabledFor(Level.ERROR)){
                         				getLogger().error("[firmarOperacion] ERROR al enviar correos ejecutivo: " + ex);
                         			}
                         		}
                         	}
                         	
                            operacionSeleccionada = new DatosOperacionTO();
                     		operacionSeleccionada.setCuentaAbono(String.valueOf(operacionNueva.getCtaAbonoTer()));
                     		operacionSeleccionada.setCuentaCargo(String.valueOf(operacionNueva.getPinCtaCargo()));
                     		operacionSeleccionada.setMontoOperacion(operacionNueva.getValorRenovado()); 
                     		operacionSeleccionada.setCaiOperacion(resultadoOpe.getCaiOperacion());
                     		operacionSeleccionada.setIicOperacion(resultadoOpe.getIicOperacion());
                     		operacionSeleccionada.setMontoCredito(String.valueOf(operacionNueva.getMontoCredito()));
                            String formatoFecha = TablaValores.getValor(TABLA_MULTILINEA, "formatosFechas", "formatoFechaSimple");
                    		SimpleDateFormat ddMMyyyyForm = new SimpleDateFormat(formatoFecha);
                    		String fechaPrimerVencimiento = ddMMyyyyForm.format(operacionNueva.getEstructura_vencimientos()[0].getFechaPrimerVcto());
                            operacionSeleccionada.setFechaPrimerVcto(fechaPrimerVencimiento);
                            operacionSeleccionada.setTotalVencimientos(String.valueOf(operacionNueva.getTotalCuotasOrig()));
                            condicionGarantia = operacionNueva.getCondicionGar() != null ? operacionNueva.getCondicionGar() : null;
                        	tasaInteres = operacionNueva.getTasaSprea() != 0 ? operacionNueva.getTasaSprea() : 0;
                         	
                         	
                         	try {
                         		if (condicionGarantia.trim().equals("2")){
                         			enviarCorreoAvales(multiEnvironment,"estadoAvanceEfectuadoRen","asuntoEmailExitoRen");
                         		}
                         	}
                    		 catch (Exception ex){
                    			 if (getLogger().isEnabledFor(Level.ERROR)){
                    				 getLogger().error("[firmarOperacion] ERROR al enviar correo a aval: " + ex);
                    			 }
                    		 }	 
                    			 
                    		 try {	 
                    			 enviarCorreoEjecutivo(multiEnvironment,"estadoAvanceEfectuadoRen","asuntoEmailExitoRen",false,false);
	                          }
	                          catch (Exception ex){
	                        		if (getLogger().isEnabledFor(Level.ERROR)){
	                            		getLogger().error("[firmarOperacion] ERROR al enviar correo a ejecutivo: " + ex);
	                            	}
	                          }
                    		 try { 
	                        	  enviarCorreoContactoEmpresa(multiEnvironment, "estadoAvanceEfectuadoRen", "asuntoEmailExitoRen");
	                          }
	                          catch (Exception ex){
	                        	  if (getLogger().isEnabledFor(Level.ERROR)){
	                        		  getLogger().error("[firmarOperacion] ERROR al enviar correo a la empresa: " + ex);
	                        	  }
	                          }  
                    	 } 
                    }
                    
                 }
                 catch (Exception ex) {
                	 if (getLogger().isEnabledFor(Level.ERROR)) { 
                		 getLogger().error("[firmarOperacion][" + clienteMB.getRut() 
                				 +"][BCI_FINEX][firmarOperacion]" +" error con mensaje: " + ex.getMessage(), ex);
                	 }  
                	 paso = 1;
                	 String errorGeneralFirma = TablaValores.getValor("multilinea.parametros", "errorFlujoFirma","errorFirma");
                	 errorSegundaClave = errorGeneralFirma;
                 }
                 break;
                case 0:
                	paso = VALOR_DOS;
                	String codEvento = TablaValores.getValor("multilinea.parametros", "journalFirma", "codEventoPendienteFirma");
                    String codSubEvento = TablaValores.getValor("multilinea.parametros", "journalFirma", "codSubEventoPendienteFirma");
                    String producto = TablaValores.getValor("multilinea.parametros", "journalFirma", "productoPendienteFirma");
                    journalizar(codEvento,codSubEvento,producto);
                    try{
                    	enviarCorreoEjecutivo(multiEnvironment,"opePendienteFirmas","asuntoPendienteFirma",false,false);
                    }
                    catch (Exception ex){
                  	  if (getLogger().isEnabledFor(Level.ERROR)){
                  		  getLogger().error("[firmarOperacion] ERROR al enviar correos ejecutivo: " + ex);
                  	  }
                    }   
                    break;
                case NO_POSEE_APO_IDN:
                    paso = 1;    
                    errorSegundaClave = resultadoFirmaTO.getGlosa();
                    break;        
                case FIRMO_APO_IDN:
                    paso = 1;
                    errorSegundaClave = resultadoFirmaTO.getGlosa();
                    break;    
                case PROBLEMAS_CONEXION:
                	errorSegundaClave = resultadoFirmaTO.getGlosa();
                    paso = 1;                    
                    break;    
                case ERROR_GENERICO:
                	errorSegundaClave = resultadoFirmaTO.getGlosa();
                    paso = 1;                    
                    break;                    
                }
            }
        } 
        catch (Exception e) {
        	if (getLogger().isEnabledFor(Level.ERROR)){
        		getLogger().error("[firmarOperacion] ERROR: " + e);
        	}
        	paso = 1;
        	String errorGeneralFirma = TablaValores.getValor("multilinea.parametros", "errorFlujoFirma","errorFirma");
        	errorSegundaClave = errorGeneralFirma;
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[firmarOperacion] [BCI_FINOK]");
        }        
    }    


    /**
     * Método encargado de generar MultiEnvironment.
     *
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0  01/04/2015 Braulio Rivas S. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión inicial. </li>
     * </ul>
     * </p>
     * 
     * @return MultiEnvironment multiEnvironment.
     * @since 1.0
     */    
    private MultiEnvironment obtenerMultiEnvironment() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[obtenerMultiEnvironment] Inicio metodo");
        }    
        return  CrearMultiEnviroment.seteaMultiEnvironment(sesion.getCanalId(), USUARIO);

    }
     
    /**
     * <p> Método que genera el comprobante de la firma del crédito. </p>
     * 
     * Registro de versiones:
     * <ul>
     *     <li>1.0 25/05/2015 Manuel Escárate (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : version inicial.</li>
     * </ul>
     * 
     * @since 1.0
     */
    public void generarComprobanteCargaPDF(){
        if(getLogger().isEnabledFor(Level.INFO)){
            getLogger().info("[generarComprobanteCargaPDF]["+usuarioModelMB.getRut()+"][BCI_INI]");
        }
        DatosComprobantePdfTO comprobanteTO = new DatosComprobantePdfTO();
        comprobanteTO.setNombreCompleto((this.usuarioModelMB.getNombres() + " " 
        		+ this.usuarioModelMB.getApPaterno()));
        comprobanteTO.setCuentaCargo(operacionSeleccionada.getCuentaAbono());
        String montoCred = formatearMonto(operacionSeleccionada.getMontoOperacion(),0,"#,###");
        comprobanteTO.setMontoCargo(String.valueOf(montoCred));
        comprobanteTO.setNumeroOperacion(operacionSeleccionada.getCaiOperacion()
        		+operacionSeleccionada.getIicOperacion());
        
        if (getLogger().isEnabledFor(Level.DEBUG)){
            getLogger().debug("[generarComprobanteCargaPDF][" + usuarioModelMB.getRut()
                    + "] cargaNominaTO: [" + comprobanteTO.toString() + "]");
        }

        FacesContext ctx = FacesContext.getCurrentInstance();
        ServletContext servletContext = (ServletContext) ctx.getExternalContext().getContext();
        String rutaRecursosSello = servletContext.getRealPath("/").replace("\\", "/").concat("/");
        String rutaRecursosPDF = TablaValores.getValor("multilinea.parametros", "crearPDF","rutaRecursosPDF");
        if (getLogger().isEnabledFor(Level.DEBUG)){
            getLogger().debug("[generarComprobanteCargaPDF][" + usuarioModelMB.getRut()
                    + "] rutaRecursosPDF: [" + rutaRecursosPDF + "]");
        }
        String comprobateIngresar = TablaValores.getValor("multilinea.parametros",
        		"crearPDF","comprobanteAutorizaFirma");

        GeneraComprobanteUtilityMB generaComprobanteUtilityMB = new GeneraComprobanteUtilityMB();
        
        String urlComprobantePdf = rutaRecursosPDF
                .concat(comprobateIngresar);
        if (getLogger().isEnabledFor(Level.DEBUG)){
            getLogger().debug("[generarComprobanteCargaPDF][" + usuarioModelMB.getRut()
                    + "] urlComprobantePdf: [" + urlComprobantePdf + "]");
        }
        String html = generaComprobanteUtilityMB.generaComprobante(urlComprobantePdf, comprobanteTO);
        if (getLogger().isEnabledFor(Level.DEBUG)){
            getLogger().debug("[generarComprobanteCargaPDF]["+usuarioModelMB.getRut()+"] html: [" + html + "]");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy '/' HH:mm 'hrs'", CL);
        html = StringUtil.reemplazaTodo(html, "<!--fechaIngreso-->", sdf.format(new Date()));
        html = StringUtil.reemplazaTodo(html, "<!--sello_comprobante-->", URL_SCHEME.concat(rutaRecursosSello));
        
        if (getLogger().isEnabledFor(Level.DEBUG)){
        	getLogger().debug("[generarComprobanteCargaPDF]["+usuarioModelMB.getRut()+"] comprobanteHtml: [" 
        			+ html + "]");
        }
        html = reemplazaCaractereEspeciales(html);
        if (getLogger().isEnabledFor(Level.DEBUG)){
            getLogger().debug("[generarComprobanteCargaPDF]["+usuarioModelMB.getRut()
            		+"] cambiado!!: [" + html + "]");
        }
        try{
            if (!ctx.getResponseComplete()) {
                String contentType = "application/pdf";
                HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();
                response.setContentType(contentType);
                response.setHeader("Content-disposition","attachment;filename=comprobanteCreditosPorFirmar.pdf");
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputStream inputStream = new ByteArrayInputStream(html.getBytes());
                Document doc = builder.parse(inputStream);
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocument(doc, null);
                renderer.layout();
                renderer.createPDF(response.getOutputStream());
                response.getOutputStream().flush();
                response.getOutputStream().close();
                ctx.responseComplete();
                if (getLogger().isEnabledFor(Level.DEBUG)){
                	getLogger().debug("[generarComprobanteCargaPDF]["
                			+usuarioModelMB.getRut()+"] comprobante PDF Creado");
                }
            }
        }
        catch (Exception ex){
            if(getLogger().isEnabledFor(Level.ERROR)){
                getLogger().error("[generarComprobanteCargaPDF]["+usuarioModelMB.getRut()+"]"
                    + "[Exception]::" + ex.getMessage(), ex);
            }
        }
        if(getLogger().isEnabledFor(Level.INFO)){
            getLogger().info("[generarComprobanteCargaPDF]["+usuarioModelMB.getRut()+"][BCI_FINOK]");
        }
    }
    
    /**
     * Método que formatea un monto.
     * 
     * Registro de versiones:
     * <ul>
     * <li>1.0 19/03/2015, Manuel Escárate R. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.
     * </ul>
     * 
     * @param formatearMonto Monto monto a formatear.
     * @param decimales cantidad de decimales.
     * @param formato formato.
     * @return numero formateado.
     * @since 1.0
     */
    public String formatearMonto(double formatearMonto, int decimales, String formato){
    	  DecimalFormatSymbols decimal = new DecimalFormatSymbols(new Locale("es", "cl"));
          DecimalFormat localDecimalFormat = new DecimalFormat(formato, decimal);
          if (decimales > 0){
        	  localDecimalFormat.setMinimumFractionDigits(decimales);
          }
          else {
        	  decimal.setDecimalSeparator(',');
        	  decimal.setGroupingSeparator(('.'));
          }
          return localDecimalFormat.format(formatearMonto);
    }
    
    /**
     * <p> Metodo para reemplazar los caracteres especiales en la creacion del pdf</p>
     *
     * Registro de versiones:
     * <ul>
     *     <li>1.0 25/05/2015 Manuel Escárate R. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : version inicial.</li>
     * </ul>
     * @param texto a reemplazar caracteres.
     * @return String texto con los caracteres cambiados.
     * @since 1.0
     */
    public String reemplazaCaractereEspeciales(String texto){
        if(getLogger().isEnabledFor(Level.INFO)){
            getLogger().info("[reemplazaCaractereEspeciales][" + usuarioModelMB.getRut() + "][BCI_INI]");
        }
        texto = StringUtil.reemplazaTodo(texto, 'á', "&#225;");
        texto = StringUtil.reemplazaTodo(texto, 'Á', "&#193;");
        texto = StringUtil.reemplazaTodo(texto, 'é', "&#233;");
        texto = StringUtil.reemplazaTodo(texto, 'É', "&#201;");
        texto = StringUtil.reemplazaTodo(texto, 'í', "&#237;");
        texto = StringUtil.reemplazaTodo(texto, 'Í', "&#205;");
        texto = StringUtil.reemplazaTodo(texto, 'ó', "&#243;");
        texto = StringUtil.reemplazaTodo(texto, 'Ó', "&#211;");
        texto = StringUtil.reemplazaTodo(texto, 'ú', "&#250;");
        texto = StringUtil.reemplazaTodo(texto, 'Ú', "&#218;");
        texto = StringUtil.reemplazaTodo(texto, 'ñ', "&#241;");
        texto = StringUtil.reemplazaTodo(texto, 'Ñ', "&#209;");
        
        if(getLogger().isEnabledFor(Level.INFO)){
            getLogger().info("[reemplazaCaractereEspeciales][" + usuarioModelMB.getRut() + "][BCI_FINOK]");
        }
        return texto;
    }

    /**
     * Método que envía correo a ejecutivo.
     * 
     * Registro de versiones:
     * <ul>
     * <li>1.0 10/05/2016, Manuel Escárate R. (BEE) - Pablo Paredes. (ing.Soft.BCI): versión inicial.</li>
     * <li>1.0 18/08/2016, Manuel Escárate R. (BEE) - Felipe Ojeda (ing.Soft.BCI): Se modifica monto para correo.</li>
     * </ul>
     * 
     * @param multiEnvironmentCorreo multiambiente.
     * @param estado estado.
     * @param asunto asunto.
     * @param enviaBackOffice flag envio a backoffice.
     * @param esError flag para identificar si correo es por error.
     * @throws GeneralException excepcion general.
     * @since 1.0
     */    
    public void enviarCorreoEjecutivo(MultiEnvironment multiEnvironmentCorreo,String estado, String asunto,boolean enviaBackOffice,boolean esError) throws GeneralException{
    	if (getLogger().isDebugEnabled()){
    		getLogger().debug("[enviarCorreoEjecutivo][BCI_INI]");    
    	}
    	String destinoDelCredito = "";
    	try {
    		if (getLogger().isDebugEnabled()) getLogger().debug("consultando destinoDelCredito      [" + operacionSeleccionada.getCaiOperacion() + operacionSeleccionada.getIicOperacion() + "]" );
    		ResultConsultaOperacionCredito resConOpe = crearEJBmultilinea().consultaOperacionCredito(multiEnvironmentCorreo, operacionSeleccionada.getCaiOperacion(), operacionSeleccionada.getIicOperacion());
    		destinoDelCredito  = resConOpe.getGlosaDestinoEspecifico();
    		if (getLogger().isDebugEnabled()) getLogger().debug("despues destinoDelCredito          [" + operacionSeleccionada.getCaiOperacion() + operacionSeleccionada.getIicOperacion() + "]" );
    	}
    	catch (Exception ex) {
    		 if (getLogger().isEnabledFor(Level.ERROR)) { 
        		 getLogger().error("[enviarCorreoEjecutivo][consultaOperacionCredito][" + clienteMB.getRut() 
        				 +"][BCI_FINEX][enviarCorreoEjecutivo]" +" error con mensaje: " + ex.getMessage(), ex);
        	 }  
    		destinoDelCredito = " ";
    	}
    	boolean cumple = cumpleHorarios();
    	DatosParaCorreoTO datosCorreo = new DatosParaCorreoTO();
    	datosCorreo.setCaiOperacion(operacionSeleccionada.getCaiOperacion());
    	datosCorreo.setIicOperacion(operacionSeleccionada.getIicOperacion());
    	datosCorreo.setFechaVencimientoSeleccionada(operacionSeleccionada.getFechaPrimerVcto());
    	datosCorreo.setRut(clienteMB.getRut());
    	datosCorreo.setDv(clienteMB.getDigitoVerif());
    	datosCorreo.setRazonSocial(clienteMB.getRazonSocial());
    	datosCorreo.setMontoFinalCredito(montoCorreo);
    	datosCorreo.setCuotaSeleccionada(Integer.parseInt(operacionSeleccionada.getTotalVencimientos().trim()));
    	datosCorreo.setDestinoDelCredito(destinoDelCredito);
    	datosCorreo.setTasaInteres(formatearMonto((tasaInteres),VALOR_DOS,"#,##0"));
    	datosCorreo.setEstado(estado);
    	datosCorreo.setAsunto(asunto);
    	datosCorreo.setEnviaBackOffice(enviaBackOffice);
    	datosCorreo.setRutOperador(usuarioModelMB.getRut());
    	datosCorreo.setDvOperador(usuarioModelMB.getDigitoVerif());
    	datosCorreo.setCodTelefonoCliente("");
    	datosCorreo.setTelefonoCliente("");
    	datosCorreo.setCodCelularCliente("");
    	datosCorreo.setCelularCliente("");
    	datosCorreo.setEmailCliente("");
    	datosCorreo.setRegionCliente("");
    	datosCorreo.setComunaCliente("");

    	if (cumple) enviaEmailEjecutivo(multiEnvironmentCorreo,datosCorreo,esError);
    	if (getLogger().isDebugEnabled()){
    		getLogger().debug("[enviarCorreoEjecutivo][BCI_FINOK]");    
    	}
    }
    
    /**
     * cumpleHorarios verifica si la operacion de credito esta en los horarios establecidos.
     *
     * Registro de versiones:<ul>
     * <li>1.1 10/05/2016   Manuel Escárate   (BEE) - Pablo Paredes (ing.Soft.BCI): version inicial.
     * </ul>
     * <p>
     *
     * @return true o false.
     * @since 1.0
     */
     public boolean cumpleHorarios(){
    	 if (getLogger().isDebugEnabled()) {
    		  getLogger().debug("[cumpleHorarios][" + clienteMB.getRut() + "][BCI_INI]");
    	 }
         boolean retorno          = false;
         String horaTardeInicial  = TablaValores.getValor("multilinea.parametros", "HorarioTardeini", "desc");
         String horaTardeFinal    = TablaValores.getValor("multilinea.parametros", "HorarioTardefin", "desc");
         int horaIniTarde         = Integer.parseInt(horaTardeInicial);
         int horaTerTarde         = Integer.parseInt(horaTardeFinal);
         String hora              = FechasUtil.fechaActualFormateada("HHmm");
         int    horanow           = Integer.parseInt(hora);
         if(getLogger().isDebugEnabled()){
        	  getLogger().debug("cumpleHorarios  - El Horario es ["+ horanow      + "]");
        	  getLogger().debug("cumpleHorarios  - horaIniTarde  ["+ horaIniTarde + "]");
        	  getLogger().debug("cumpleHorarios  - horaTerTarde  ["+ horaTerTarde + "]");
         }
         if ((horanow > horaTerTarde) && (horanow < horaIniTarde)){
        	  getLogger().debug("cumpleHorarios  - [true]");
             retorno = true;
         }
         else{
             if(getLogger().isDebugEnabled())getLogger().debug("cumpleHorarios  - [false]");
             retorno = false;
         }
         if (getLogger().isDebugEnabled()) {
              getLogger().debug("[cumpleHorarios][" + clienteMB.getRut() + "][BCI_FINOK]");
         }
         return retorno;
     }
      
     /**
      * Método que envía email a ejecutivo.
      * 
      * Registro de versiones:
      * <ul>
      * <li>1.0 10/05/2016, Manuel Escárate R. (BEE) - Pablo Paredes (ing.Soft.BCI): versión inicial.</li>
      * </ul>
      * 
      * @param multiEnvironmentEmailEjecutivo multiambiente.
      * @param datosParaCorreo datos necesarios para el envío de correo.
      * @param esError flaga para identificar si es correo con errores.
      * @throws GeneralException excepcion general.
      * @since 1.0
      */
     public void enviaEmailEjecutivo(MultiEnvironment multiEnvironmentEmailEjecutivo, 
    		 DatosParaCorreoTO datosParaCorreo,boolean esError) throws GeneralException {
    	 if (getLogger().isDebugEnabled()) {
    		 getLogger().debug("[enviaEmailEjecutivo] [BCI_INI]");
    	 }
    	 RetornoTipCli datosCliente = null;
    	 long rut = clienteMB.getRut();
    	 char dv = clienteMB.getDigitoVerif();
    		try {
    			if (getLogger().isDebugEnabled()) {
        			getLogger().debug("[enviaEmailEjecutivo] [consultaAntecedentesGenerales] [rut] [" + rut + "] [dv] [" + dv + "]");
        		}
    			this.crearEJBServiciosCliente();
    			
    			datosCliente = serviciosCliente.consultaAntecedentesGenerales(rut,dv);
    			if (getLogger().isDebugEnabled()) {
        			getLogger().debug("[enviaEmailEjecutivo] [termine] [consultaAntecedentesGenerales]");
        		}
    		}
    		catch (Exception e){
    			if(getLogger().isEnabledFor(Level.ERROR)){
        			getLogger().error("[enviaEmailEjecutivo] [Exception] [consultaAntecedentesGenerales] "
        					+ "mensaje=< " + e.getMessage() + ">", e);
        		}
    			throw new MultilineaException("ESPECIAL", e.getMessage());
    		}
    		if (datosCliente != null){
    			if (getLogger().isDebugEnabled()) {
    				getLogger().debug("[enviaEmailEjecutivo] [consultaAntecedentesGenerales] datosCliente != null");
        		}
    			codBanca = ((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.TipoBca:datosCliente.DatEmpresa.TipoBca);
    			codOficina = ((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.CodOficina:datosCliente.DatEmpresa.CodOficina);
    		    codEjecutivo =((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.CodigoEjecutivo:datosCliente.DatEmpresa.CodigoEjecutivo);
    		} 
     	
         String fechaPrimerVencimiento   = "";
         String de                       = "";
         String direccionOrigen          = "";
         String direccionDestino         = "";
         String asunto                   = "";
         String cuerpoDelEmail           = "";
         String firma                    = "";
         String emailEjecutivo           = "";
         String glosaOficinaIngreso      = "";
         String emailJefeOficina         = "";
         String emailsbackoffice         = "";
         String estado                   = "";
         if (getLogger().isDebugEnabled()) {
         	 getLogger().debug("[enviaEmailEjecutivo] - caiOperacionNro   [" + datosParaCorreo.getCaiOperacion() +"]");
         }
         
         String montoCreditoCorreo = formatearMonto(datosParaCorreo.getMontoFinalCredito(),0,"#,###");
         if (getLogger().isDebugEnabled()) getLogger().debug("[enviaEmailEjecutivo] - iicOperacionNro              [" + datosParaCorreo.getIicOperacion() +"]");
         String fechaPrimerVcto = datosParaCorreo.getFechaVencimientoSeleccionada();
       
         fechaPrimerVencimiento = fechaPrimerVcto.substring(VALOR_SEIS) 
          		+ "/" +fechaPrimerVcto.substring(VALOR_CUATRO,VALOR_SEIS) + "/" 
                  +fechaPrimerVcto.substring(0,VALOR_CUATRO);
         
         if (getLogger().isDebugEnabled()) getLogger().debug("[enviaEmailEjecutivo] - Antes de emailEjecutivo");
         emailEjecutivo = obtieneCorreoEjecutivo(codEjecutivo);
         if (getLogger().isDebugEnabled()) getLogger().debug("[enviaEmailEjecutivo] - emailEjecutivo               [" + emailEjecutivo +"]");
        
         if (datosParaCorreo.isEnviaBackOffice()){
         	if (getLogger().isDebugEnabled()) getLogger().debug("[enviaEmailEjecutivo] - emailsbackoffice         [true]");
             emailsbackoffice = TablaValores.getValor(TABLA_MULTILINEA, "emailsbackoffice", "desc");
         }

         if (!codOficina.trim().equals("")) emailJefeOficina = buscarMailJefeOficina(
        		 multiEnvironmentEmailEjecutivo, codOficina);

         if (getLogger().isDebugEnabled()) getLogger().debug("[enviaEmailEjecutivo] - Antes de obtener glosa oficina");
         glosaOficinaIngreso = TablaValores.getValor("TabOfic.parametros", codOficina, "desc");
         if (getLogger().isDebugEnabled()) getLogger().debug("[enviaEmailEjecutivo] - oficinaIngreso               [" + codOficina        + "]");
         if (getLogger().isDebugEnabled()) getLogger().debug("[enviaEmailEjecutivo] - glosaOficinaIngreso          [" + glosaOficinaIngreso   + "]");

         de                  = TablaValores.getValor(TABLA_MULTILINEA, "emailsEjecutivoDe", "desc");
         direccionOrigen     = TablaValores.getValor(TABLA_MULTILINEA, "emailsEjecutivoFrom", "desc");
         direccionDestino    = emailEjecutivo + (emailJefeOficina.trim().equals("") ? "" : "," + emailJefeOficina);
         direccionDestino    += emailsbackoffice.trim().equals("") ? "" : "," + emailsbackoffice.trim();
         String modulo = TablaValores.getValor(TABLA_MULTILINEA, "modulo", "moduloFirma");
         asunto              = modulo +TablaValores.getValor(TABLA_MULTILINEA, datosParaCorreo.getAsunto(), "desc");
         asunto              = asunto + " - "+ datosParaCorreo.getRut() + "-" + datosParaCorreo.getDv() + " - " + datosParaCorreo.getRazonSocial();
         if (esError){
             String estadoNoCursada = TablaValores.getValor(TABLA_MULTILINEA, "opeNoCursada", "desc");
        	 estado = estadoNoCursada+ ";" + datosParaCorreo.getEstado();
         }
         else {
        	 estado = TablaValores.getValor(TABLA_MULTILINEA, datosParaCorreo.getEstado(), "desc");
         }
         
         cuerpoDelEmail      = armaEmailParaDestinatarioGenerico(
                  datosParaCorreo, codBanca, codEjecutivo, fechaPrimerVencimiento, 
                  codOficina, glosaOficinaIngreso, 
                  montoCreditoCorreo, estado);
         enviaMensajeCorreo(de, direccionOrigen, direccionDestino, asunto, cuerpoDelEmail, firma);
         if (getLogger().isDebugEnabled()) {
              getLogger().debug("enviaEmailEjecutivo [BCI_FINOK]");
         }
     }
     
    /**
 	 * busca email de jefe de oficina de ejecutivo.
 	 * <p>
 	 * Registro de Versiones
 	 * <ul>
 	 * <li>1.0 10/05/2016 Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): versión inicial.</li> 
 	 *
 	 * </ul>
 	 * </p>
 	 *
 	 * @param multiEnvironmentBuscaMail multiEnvironment.
 	 * @param oficinaIngresoCorreo oficiana de ingreso.
 	 * @return email de jefe oficina
 	 * @since 1.0
 	 *
 	 */
 	public String buscarMailJefeOficina(MultiEnvironment multiEnvironmentBuscaMail, String oficinaIngresoCorreo) {
 		if (getLogger().isDebugEnabled()){
 			getLogger().debug("buscaMailJefeOficina - [BCI_INI]");
 		}
 		
 		if (getLogger().isDebugEnabled()){
 			getLogger().debug("buscaMailJefeOficina - aqui imprimo oficinaIngreso ["
 				+ oficinaIngresoCorreo + "]");
 		}
 		RowConsultaMasivaDeOficinas[] result = null;

 		boolean errorConsulta = false;
 		try {
 			SvcBoletaDeGarantiaImpl svc = new SvcBoletaDeGarantiaImpl();
 			if (getLogger().isDebugEnabled()){
 				getLogger().debug("buscaMailJefeOficina - DESPUES DE INSTANCIAR   SvcBoletaDeGarantiaImpl");
 				getLogger().debug("buscaMailJefeOficina - ANTES DE LA CONSULTA DE LA CONSULTA MASIVA DE OFICINAS");
 			}
 			result = svc.consultaMasivaDeOficinas(multiEnvironmentBuscaMail,
 					oficinaIngresoCorreo, "").getRowConsultaMasivaDeOficinas();

 		}
 		catch (Exception e) {
 			if (getLogger().isDebugEnabled()){
 				getLogger().debug("buscaMailJefeOficina - Exception [" + e.toString() + "]");
 			}
 			errorConsulta = true;
 		}
 		if (getLogger().isDebugEnabled()){
 			getLogger().debug("buscaMailJefeOficina - aqui imprimo los datos de result de consulta Masiva De Oficinas ");
 		}
 		String emailJefeOficina = "";

 		if (!errorConsulta) {
 			int concurrencias = result.length;
 			if (getLogger().isDebugEnabled()){
 				getLogger().debug("buscaMailJefeOficina - cantidad de filas ["
 					+ concurrencias + "]");
 			}
 			if (concurrencias > 0) {
 				if (getLogger().isDebugEnabled()){
 					getLogger().debug("buscaMailJefeOficina -"
 							+ "] result[0].getCodigoOficina() ["
 							+ result[0].getCodigoOficina() + "] ["
 							+ result[0].getJefeOficina() + "]");
 				}
 				emailJefeOficina = obtieneCorreoEjecutivo(result[0].getJefeOficina());
 			}
 		} 
 		else {
 			if (getLogger().isDebugEnabled()){
 				getLogger().debug("buscaMailJefeOficina - Fallo en consulta emailJefeOficina []");
 			}
 		}
 		if (getLogger().isDebugEnabled()){
 			getLogger().debug("buscaMailJefeOficina - emailJefeOficina ["
 				+ emailJefeOficina.trim() + "]");
 		}
 		if (getLogger().isDebugEnabled()){
 			getLogger().debug("buscaMailJefeOficina - [BCI_FINOK]");
 		}

 		return emailJefeOficina.trim();
 	}
     
    /**
 	 * Metodo que obtiene email a partir de username de ejecutivo y/o jefe de
 	 * oficina.
 	 *
 	 *
 	 * Registro de versiones:
 	 * <ul>
 	 * <li>1.0 10/05/2016 Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): versión inicial.</li> 
 	 *
 	 * </ul>
 	 * <p>
 	 *
 	 * @param codEjecutivoCorreo
 	 *            codigo de ejecutivo/jefe de oficina.
 	 * @return correo del ejecutivo
 	 * @since 1.1
 	 */
 	public String obtieneCorreoEjecutivo(String codEjecutivoCorreo) {
 		if (getLogger().isDebugEnabled()){
 			getLogger().debug("[obtieneCorreoEjecutivo] - [BCI_INI]");
 		}
 		String mailEjecutivo = "";
 		try {
 			if (getLogger().isDebugEnabled()){
 				getLogger().debug("obtieneCorreoEjecutivo - Antes de mailEspecial");
 			}
 			String mailEspecial = TablaValores.getValor(
 					"multiworkflowintegrator.parametros", "USERNAME_"
 							+ codEjecutivoCorreo.trim(), "correo");
 			if (getLogger().isDebugEnabled()){
 				getLogger().debug("obtieneCorreoEjecutivo - mailEspecial [" + mailEspecial
 					+ "]");
 			}
 			if (mailEspecial != null) {
 				if (mailEspecial.length() > 0) {
 					mailEjecutivo = mailEspecial;
 				}
 			}
 			if (mailEjecutivo.trim().equals("")) {
 				if (!codEjecutivoCorreo.trim().equals("")) {
 					mailEjecutivo = codEjecutivoCorreo.toLowerCase().trim()
 							+ "@bci.cl";
 				}
 			}
 			if (getLogger().isDebugEnabled()){
 				getLogger().debug("obtieneCorreoEjecutivo - mailEjecutivo ["
 					+ mailEjecutivo + "]");
 			}
 		} 
 		catch (Exception e) {
 			if (getLogger().isEnabledFor(Level.ERROR)) { 
 				getLogger().error("[obtieneCorreoEjecutivo][" + clienteMB.getRut() 
 						+"][BCI_FINEX][obtieneCorreoEjecutivo]" +" error con mensaje: " + e.getMessage(), e);
 			}  
 			mailEjecutivo = "";
 		}
 		mailEjecutivo = mailEjecutivo.trim().equals("") ? (codEjecutivoCorreo
 				.trim().equals("") ? "" : codEjecutivoCorreo.toLowerCase().trim()
 				+ "@bci.cl") : mailEjecutivo;
 		if (getLogger().isDebugEnabled()){
 			getLogger().debug("[buscaMailJefeOficina] - [BCI_FINOK]");
 		}
 		return mailEjecutivo;
 	}
 	
     /**
      * Método encargado de enviar correo a avales.
      * <br>
      * Registro de versiones:<ul>
      * <li>1.0 10/05/2016 Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): versión inicial.</li> 
      * <li>1.1 12/08/2016 Manuel Escárate R. (BEE) -  Felipe Ojeda (ing.Soft.BCI) : Se agrega nueva lógica para obtener datos en el correo.</li>
      * </ul>
      * @param multiEnvironmentCorreoAvales multiambiente.
      * @param estadoAvance estado del avance.
      * @param asuntoAvance asunto del avance.
      *
      * @throws Exception excepcion en el metodo.
      */    
     public void enviarCorreoAvales(MultiEnvironment multiEnvironmentCorreoAvales, String estadoAvance,String asuntoAvance) throws Exception{
    	 if (getLogger().isDebugEnabled()){
    		 getLogger().debug("[enviarCorreoAvales][BCI_INI]");    
    	 }
    	String mailAval = " ";
     	String nombreAval = " ";
    	String comunaAval = " ";
    	String direccionAval = " ";
		String destinoDelCredito = ""; 
		String direccionEmpresa = "";
		String comunaEmpresa = "";
		String ciudadAval = " ";
		String ciudadEmpresa = " ";
		String salidaValorEmp = " ";
		String salidaValorAval = " ";
		String fechaUltimaCuota = " ";
		String tipoOperacion = " ";
		double tasaInteresCorreo = 0;
		String caiOpe = operacionSeleccionada.getCaiOperacion();
		int iicOpe = operacionSeleccionada.getIicOperacion();
		double segurosCorreo = 0;
		double valorNotarioCorreo = 0;
		double impuestosCorreo = 0;
   	    int diaDeVencimiento = 0;
		
     	DatosAvalesTO[] avalesMultilinea = null;
     	try{
     		ResultConsultaAvales obeanAval = new ResultConsultaAvales();
     		obeanAval = crearEJBmultilinea().consultaAvales(multiEnvironmentCorreoAvales,(int)clienteMB.getRut(),clienteMB.getDigitoVerif(), ' ', " "," ", " ", 0, 0, "AVL", "AVC");
     		if (getLogger().isDebugEnabled()) { 
     			getLogger().debug("[enviarCorreoAvales] despues consultaAvales");
     		}
     		Aval[] avales = null;
     		if (obeanAval != null){
     			avales    = obeanAval.getAvales();
     			if (getLogger().isDebugEnabled()) { 
     				getLogger().debug("avales.length ["+ avales.length +"]");
     			}
     			avalesMultilinea = new DatosAvalesTO[avales.length];
     			for (int i = 0; i < avales.length; i++) {
     				if (avales[i] != null){
     					if (avales[i].getVigente() == 'S'){ 
     						avalesMultilinea[i] = new DatosAvalesTO();
     						avalesMultilinea[i].setRutAval(String.valueOf(avales[i].getRutAval()));
     						avalesMultilinea[i].setDvAval(String.valueOf(avales[i].getDigitoVerificaAval()));
     					}
     				}
     			}
     		}
     		if (getLogger().isDebugEnabled()){
     			getLogger().debug("[enviarCorreoAvales] antes del seteo de avales"); 
     		}
     	}
		catch(Exception e) {
			if(getLogger().isEnabledFor(Level.ERROR)){
				getLogger().error("[enviarCorreoAvales]ERROR [consultaAvales] Cliente  Exception [ " + e.getMessage() + "]");
			}
		}
     	
     	try {
     		SvcCreditosGlobales svcCreditos = new SvcCreditosGlobalesImpl();
     		ResultConsultaCalendarioPago resultCalendario = svcCreditos.consultaCalendarioPago(
     				multiEnvironment, caiOpe, iicOpe);
     		if (resultCalendario != null){
     			calendarioPago = resultCalendario.getCalendario();
     			fechaUltimaCuota =   FechasUtil.convierteDateAString(calendarioPago[Integer.parseInt(operacionSeleccionada.getTotalVencimientos().trim())-1].getFecVencPago(),"dd/MM/yyyy"); 
     		}
     		
     	} 
     	catch (Exception ex) {
     		if (getLogger().isEnabledFor(Level.ERROR)) {
     			getLogger().debug("[enviarCorreoAvales] Exception:" + ErroresUtil.extraeStackTrace(ex));
     		}
     	}
     	
     	TableManagerService tableManagerService = (TableManagerService)LocalizadorDeServicios
				.obtenerInstanciaEJB(LocalizadorDeServicios.JNDI_MANEJADOR_DE_TABLAS);
     	List comunasParaCorreo = null;
     	try{
			comunasParaCorreo = obtenerTablaDescripciones(tableManagerService,multiEnvironment,TABLE_MANANGER_SISTEMA_TAB,TABLE_MANANGER_TABLA_COM,"");
			DireccionClienteBci[] direccionesEmpresa = instanciaEJBServicioDirecciones().getAddressBci(clienteMB.getRut());
			if (direccionesEmpresa != null) {
				for (int d = 0; d < direccionesEmpresa.length; d++) {
					DireccionClienteBci direccionEmp = (DireccionClienteBci) direccionesEmpresa[d];
					if (direccionEmp.tipoDireccion == 'D') {
						salidaValorEmp = getValor(comunasParaCorreo, direccionEmp.getComuna());
						comunaEmpresa = salidaValorEmp.substring(0, largoComuna);
						ciudadEmpresa = salidaValorEmp.substring(largoComuna).trim();
						direccionEmpresa = direccionEmp.getDireccion();
					}
				}
			}

		}
		catch (Exception ex){
			if (getLogger().isEnabledFor(Level.ERROR)) { 
				getLogger().error("[enviarCorreoAvales][" + clienteMB.getRut() +"][BCI_FINEX][enviarCorreoAvales] [obteniendo comunas]" +" error con mensaje: " + ex.getMessage(), ex);
			}  
		}
     	
     	try {
     		if (getLogger().isDebugEnabled()) {
     			getLogger().debug("[enviarCorreoAvales] consultando destinoDelCredito    " + "  [" + caiOpe + iicOpe + "]" );
     		}
     				
     		ResultConsultaOperacionCredito resConOpe = crearEJBmultilinea().consultaOperacionCredito(multiEnvironmentCorreoAvales,caiOpe, iicOpe);
     		destinoDelCredito  = resConOpe.getGlosaDestinoEspecifico();
     		tasaInteresCorreo = resConOpe.getTasaSprea()/VALOR_12;
     		tipoOperacion = resConOpe.getTipoOperacion().trim()+resConOpe.getCodigoAuxiliar();
     		ResultConsultaCgr seguros = obtenerSeguros(multiEnvironment, caiOpe, String.valueOf(iicOpe));
			double sumaSeguros = 0;
			if (seguros != null && seguros.getInstanciaDeConsultaCgr() != null){
				for(int j = 0; j < seguros.getInstanciaDeConsultaCgr().length; j++){
					IteracionConsultaCgr cgr = seguros.getInstanciaDeConsultaCgr()[j];
					if (cgr.getIndVigenciaInst() == 'S'){
						sumaSeguros = sumaSeguros + cgr.getTasaMontoFinal();
					}
				}
			}
			if (getLogger().isDebugEnabled()) getLogger().debug("[enviarCorreoAvales] Total Seguros: " + sumaSeguros);
			segurosCorreo = sumaSeguros;		
			ResultConsultaCgr gastos = obtenerGastoNotarial(multiEnvironment, caiOpe, String.valueOf(iicOpe));
			double gastoNotario = 0;
			
			if (gastos != null && gastos.getTotOcurrencias() > 0){
				gastoNotario = gastos.getInstanciaDeConsultaCgr(0).getTasaMontoFinal();
				if (getLogger().isDebugEnabled()){ 
					getLogger().debug("[enviarCorreoAvales] valorNotario: " +  gastoNotario);
				}
			} 
			else {
				if (getLogger().isDebugEnabled()){
					getLogger().debug("[enviarCorreoAvales] No existen ocurrencias en la consulta CGR para Gastos Notariales");
				}
	    	} 
			if (getLogger().isDebugEnabled()) getLogger().debug("[enviarCorreoAvales] Gastos Notariales: " + gastoNotario);
			valorNotarioCorreo = gastoNotario;		
			impuestosCorreo = resConOpe.getImpuestos();
			if (tipoOperacion.equals("AVC721")){
				if (resConOpe.getEstructura_vencimientos()[0] != null){
					if (resConOpe.getEstructura_vencimientos()[0].getDiaVencimiento() > 0){
						diaDeVencimiento = resConOpe.getEstructura_vencimientos()[0].getDiaVencimiento();
					}
					else {
						if (resConOpe.getEstructura_vencimientos()[0].getFechaPrimerVcto() != null){
							Date fechaProxVenc = resConOpe.getEstructura_vencimientos()[0].getFechaPrimerVcto();
							String fechaProxConvert = FechasUtil.convierteDateAString(fechaProxVenc,
									"ddMMyyyy"); 
							diaDeVencimiento = Integer.parseInt(fechaProxConvert.substring(0,VALOR_DOS));
						}
					}
				}
			}
     		if (getLogger().isDebugEnabled()) getLogger().debug("despues destinoDelCredito " + "[" + caiOpe + iicOpe + "]" );
     	}
     	catch (Exception ex) {
     		if (getLogger().isEnabledFor(Level.ERROR)) { 
     			getLogger().error("[enviarCorreoAvales][" + clienteMB.getRut() 
     					+"][BCI_FINEX][enviarCorreoAvales]" +" error con mensaje: " + ex.getMessage(),ex);
     		}  
     		destinoDelCredito = " ";
     	}
    	if (avalesMultilinea != null && avalesMultilinea.length > 0){
    		for (int k = 0; k < avalesMultilinea.length; k++) {
     			if (avalesMultilinea[k] != null){
     				DireccionClienteBci[] direcciones = instanciaEJBServicioDirecciones().getAddressBci(Long.parseLong(avalesMultilinea[k].getRutAval()));
     				if (direcciones != null) {
     					for (int j = 0; j < direcciones.length; j++) {
     						nombreAval = direcciones[0].getNombres();
     						DireccionClienteBci direccion = (DireccionClienteBci) direcciones[j];
     						if (direccion.tipoDireccion == TIPOMAIL){
     							mailAval = direccion.getDireccion();
     						}
     						if (direccion.tipoDireccion == 'D') {
     							salidaValorAval = getValor(comunasParaCorreo, direccion.getComuna());
     							comunaAval = salidaValorAval.substring(0, largoComuna);
     							ciudadAval = salidaValorAval.substring(largoComuna).trim();
     							direccionAval = direccion.getDireccion();
     						}
     					}
     				}
     				DatosAvalesTO datosAval = new DatosAvalesTO();
     				datosAval.setNombreAval(nombreAval);
     				datosAval.setMailAval(mailAval);
     				datosAval.setComunaAval(comunaAval);
     				datosAval.setCiudadAval(ciudadAval);
     				datosAval.setDireccionAval(direccionAval);
     				datosAval.setRutAval(avalesMultilinea[k].getRutAval());
     				datosAval.setDvAval(avalesMultilinea[k].getDvAval());

     				DatosParaCorreoTO datosCorreo = new DatosParaCorreoTO();
     				datosCorreo.setCaiOperacion(caiOpe);
     				datosCorreo.setIicOperacion(iicOpe);
     				datosCorreo.setFechaVencimientoSeleccionada(operacionSeleccionada.getFechaPrimerVcto());
     				datosCorreo.setRut(clienteMB.getRut());
     				datosCorreo.setDv(clienteMB.getDigitoVerif());
     				datosCorreo.setRazonSocial(clienteMB.getRazonSocial());
     				datosCorreo.setMontoFinalCredito(montoCorreo);
     				datosCorreo.setCuotaSeleccionada(Integer.parseInt(operacionSeleccionada.getTotalVencimientos().trim()));
     				datosCorreo.setDestinoDelCredito(destinoDelCredito);
     				datosCorreo.setEstado(estadoAvance);
     				datosCorreo.setAsunto(asuntoAvance);
     				datosCorreo.setTasaInteres(formatearMonto((tasaInteresCorreo),VALOR_DOS,"#,##0"));
     				String montoEnPalabras = TextosUtil.numeroEnPalabras(montoCorreo);
     				datosCorreo.setMontoExpresadoEnPalabras(montoEnPalabras);
     				datosCorreo.setValorCuota(calendarioPago[0].getCuota());
     				datosCorreo.setDatosAvales(datosAval);
     				datosCorreo.setComunaEmpresa(comunaEmpresa);
     				datosCorreo.setCiudadEmpresa(ciudadEmpresa);
     				datosCorreo.setDireccionEmpresa(direccionEmpresa);
     				datosCorreo.setFechaUltimaCuota(fechaUltimaCuota);
     				datosCorreo.setDiaVencimiento(diaDeVencimiento);
     				datosCorreo.setTipoOperacion(tipoOperacion);
     				datosCorreo.setSeguros(segurosCorreo);
     				datosCorreo.setImpuestos(impuestosCorreo);
     				datosCorreo.setValorNotario(valorNotarioCorreo);
     				enviaEmailAval(multiEnvironmentCorreoAvales,datosCorreo);
     			}	
    		}
    	}
     	if (getLogger().isDebugEnabled()){
 			getLogger().debug("[enviarCorreoAvales]  [BCI_FINOK]");
 		}
     }
     
     /**
      * Método que envía email al aval.
      * 
      * Registro de versiones:
      * <ul>
      * <li>1.0 10/05/2016, Manuel Escárate R. (BEE) - Pablo Paredes (ing.Soft.BCI): versión inicial.</li>
      * <li>1.1 12/08/2016, Manuel Escárate R. (BEE) -  Felipe Ojeda (ing.Soft.BCI) : Se modifica seteo de direccion de destino.</li>
      * </ul>
      * 
      * @param multiEnvironmentEnviaMail multiambiente.
      * @param datosCorreo datos necesarios para el envío de correo.
      * 
      * @throws GeneralException excepcion general.
      * @since 1.0
      */
     public  void enviaEmailAval(MultiEnvironment multiEnvironmentEnviaMail, 
             DatosParaCorreoTO datosCorreo) throws GeneralException {
         
    	 if (getLogger().isDebugEnabled()) {
    		 getLogger().debug("[enviaEmailAval] [BCI_INI]");
    	 }
         
         String fechaPrimerVencimiento   = "";
         String de                       = "";
         String direccionOrigen          = "";
         String direccionDestino         = "";
         String asunto                   = "";
         String cuerpoDelEmail           = "";
         String firma                    = "";
         String estado                   = "";
         if (getLogger().isDebugEnabled()) {
         	 getLogger().debug("enviaEmailAval - caiOperacionNro              [" + datosCorreo.getCaiOperacion() +"]");
         	 getLogger().debug("enviaEmailAval - iicOperacionNro              [" + datosCorreo.getIicOperacion() +"]");
         }
  
         String fechaPrimerVcto = datosCorreo.getFechaVencimientoSeleccionada();
         String montoCreditoCorreo = formatearMonto(datosCorreo.getMontoFinalCredito(),0,"#,###");
         fechaPrimerVencimiento = fechaPrimerVcto.substring(VALOR_SEIS) 
           		+ "/" +fechaPrimerVcto.substring(VALOR_CUATRO,VALOR_SEIS) + "/" 
                   +fechaPrimerVcto.substring(0,VALOR_CUATRO);
         
         
         de                  = TablaValores.getValor(TABLA_MULTILINEA, "emailMultilinea", "desc");
         direccionOrigen     = TablaValores.getValor(TABLA_MULTILINEA, "emailsEjecutivoFrom", "desc");
         direccionDestino    = datosCorreo.getDatosAvales().getMailAval();
         String modulo = TablaValores.getValor(TABLA_MULTILINEA, "modulo", "moduloFirma");
         asunto              = modulo + TablaValores.getValor(TABLA_MULTILINEA, datosCorreo.getAsunto(), "desc");
         asunto              = asunto + " - "+ datosCorreo.getRut() + "-" + datosCorreo.getDv() + " - " + datosCorreo.getRazonSocial();
         estado              = datosCorreo.getEstado().trim().equals("") ? " " : TablaValores.getValor(
                 TABLA_MULTILINEA, datosCorreo.getEstado(), "desc");
        
         cuerpoDelEmail      = armaEmailParaDestinatarioAval(
         		datosCorreo, codBanca,fechaPrimerVencimiento
                , montoCreditoCorreo,estado);

           enviaMensajeCorreo(de, direccionOrigen, direccionDestino, asunto, cuerpoDelEmail, firma);
           if (getLogger().isDebugEnabled()) {
         	  getLogger().debug("[enviaEmailAval] [BCI_FINOK]");
           }
     }
     
     /**
 	 * Metodo que envia email a destinatario.
 	 * Registro de versiones:
 	 * <ul>
 	 * <li>1.0 10/05/2016, Manuel Escárate. (BEE) - Pablo Paredes. (ing.Soft.BCI): version inicial</li>
 	 * </ul>
 	 * <p>
 	 *
 	 * @param de nombre del emisor del mensaje.
 	 * @param direccionOrigen dirección de correo electrónico del emisor.
 	 * @param direccionDestino dirección de correo electrónico del destinatario.
 	 * @param asunto asunto del mensaje.
 	 * @param cuerpo cuerpo del mensaje.
 	 * @param firma firma del mensaje.
 	 * @throws GeneralException posible.
 	 * @since 1.1
 	 */
 	public void enviaMensajeCorreo(String de, String direccionOrigen,
 			String direccionDestino, String asunto, String cuerpo, String firma)
 			throws GeneralException {

 		try {
 			   if (getLogger().isDebugEnabled()){
 				   getLogger().debug("INI enviaMensajeCorreo [BCI_INI]");
 				   getLogger().debug("de               [" + de + "]");
 				   getLogger().debug("direccionOrigen  [" + direccionOrigen + "]");
 				   getLogger().debug("direccionDestino [" + direccionDestino + "]");
 				   getLogger().debug("asunto           [" + asunto + "]");
 				   getLogger().debug("cuerpo           [" + cuerpo + "]");
 				   getLogger().debug("firma            [" + firma + "]");
 			   }

 			int resultEnvioCorreo = EnvioDeCorreo.simple(de, direccionOrigen,
 					direccionDestino, asunto, cuerpo, firma);

 			String resultadoFinalEnvio = "Fallo al enviar correo ";

 			if (resultEnvioCorreo == EnvioDeCorreo.EXITO) {
 				 if (getLogger().isDebugEnabled()){
 					 getLogger().debug("Mensaje de correo enviado !!!");
 				 }
 			}
 			else {
 				if (resultEnvioCorreo == EnvioDeCorreo.FALLOENELENVIO) {
 					resultadoFinalEnvio += "Exception. Retorno de código de error ("
 							+ resultEnvioCorreo + ")";
 					if (getLogger().isDebugEnabled()){
 						 getLogger().debug(resultadoFinalEnvio);
 					}
 				} 
 				else if (resultEnvioCorreo == EnvioDeCorreo.FROMVACIO) {
 					resultadoFinalEnvio += "From Vacio. Número de error ("
 							+ resultEnvioCorreo + ")";
 				    if (getLogger().isDebugEnabled()){
 				    	 getLogger().debug(resultadoFinalEnvio);
 				    }
 				} 
 				else if (resultEnvioCorreo == EnvioDeCorreo.FROMILEGAL) {
 					resultadoFinalEnvio += "From Ilegal. Número de error ("
 							+ resultEnvioCorreo + ")";
 					 if (getLogger().isDebugEnabled()){
 						 getLogger().debug(resultadoFinalEnvio);
 					 }
 				} 
 				else if (resultEnvioCorreo == EnvioDeCorreo.TOVACIO) {
 					resultadoFinalEnvio += "To Vacio. Número de error ("
 							+ resultEnvioCorreo + ")";
 					 if (getLogger().isDebugEnabled()){
 						 getLogger().debug(resultadoFinalEnvio);
 					 }
 				} 
 				else if (resultEnvioCorreo == EnvioDeCorreo.SUBJECTVACIO) {
 					resultadoFinalEnvio += "Subject Vacio. Número de error ("
 							+ resultEnvioCorreo + ")";
 					if (getLogger().isDebugEnabled()){
 						 getLogger().debug(resultadoFinalEnvio);
 					}
 				} 
 				else if (resultEnvioCorreo == EnvioDeCorreo.CUERPOVACIO) {
 					resultadoFinalEnvio += "Cuerpo Vacio. Número de error ("
 							+ resultEnvioCorreo + ")";
 					if (getLogger().isDebugEnabled()){
 						 getLogger().debug(resultadoFinalEnvio);
 					}
 				} 
 				else {
 					resultadoFinalEnvio += "Desconocido. Número de error ("
 							+ resultEnvioCorreo + ")";
 					if (getLogger().isDebugEnabled()){
 						 getLogger().debug(resultadoFinalEnvio);
 					}
 				}
 				throw new GeneralException(resultadoFinalEnvio);
 			}
 		    if (getLogger().isDebugEnabled()){
 		    	 getLogger().debug("[enviaMensajeCorreo] [BCI_FINOK]");
 		    }
 		} 
 		catch (Exception e) {
 			 if (getLogger().isEnabledFor(Level.ERROR)) { 
        		 getLogger().error("[enviaMensajeCorreo][" + clienteMB.getRut() 
        				 +"][BCI_FINEX][enviaMensajeCorreo]" +" error con mensaje: " + e.getMessage());
        	 }  
 		}
 	}
     
     /**
 	 * Metodo que arma mensaje de email generico a Destinatario.
 	 *
 	 *
 	 * Registro de versiones:
 	 * <ul>
 	 * <li>1.0 19/05/2016 Manuel Escárate(BEE - Pablo Paredes (ing.Soft.BCI) ): Version Inicial</li>
 	 * </ul>
 	 * <p>
 	 *
 	 * @param datosCorreo datos para correo.
 	 * @param codBancaCorreo codigo banca.
 	 * @param codEjecutivoCorreo codigo ejecutivo.
 	 * @param fechaPrimerVcto fecha primer vencimiento.
 	 * @param codOficinaIngreso codigo oficina del cliente.
 	 * @param glosaOficinaIngreso glosa oficina del cliente.
 	 * @param montoCredito monto del credito.
 	 * @param estado estado.
 	 * @throws GeneralException excepcion general.
 	 * @return cuerpo de correo.
 	 * @since 1.0
 	 */
 	public String armaEmailParaDestinatarioGenerico(
 			DatosParaCorreoTO datosCorreo, String codBancaCorreo,
 			String codEjecutivoCorreo, String fechaPrimerVcto,
 			String codOficinaIngreso, String glosaOficinaIngreso,
 			String montoCredito, String estado) throws GeneralException {

 		try {
 			if (getLogger().isDebugEnabled()) {
 				getLogger().debug("armaEmailParaDestinatario - [BCI_INI] ");
 			}
 			String noAplica = TablaValores.getValor("multilinea.parametros",
 					"datosNoAplica", "mensaje");
 			String fechaHora = FechasUtil.fechaActualFormateada("dd/MM/yyyy")
 					+ " - " + FechasUtil.fechaActualFormateada("HH:mm:ss");
 			StringBuffer cuerpoDelEmail;
 			String numeroOperacion = datosCorreo.getCaiOperacion()
 					+ datosCorreo.getIicOperacion();
 		        String idc = datosCorreo.getRut() + " - " + datosCorreo.getDv();
 			String telefonOperador = "";
 			String celularOperador = "";
 			String emailOperador = "";
 			String regionOperador = "";
 			String comunaOperador = "";
 			
 			if (!datosCorreo.getCodTelefonoCliente().equals("") && !datosCorreo.getTelefonoCliente().equals("")){
 				telefonOperador = datosCorreo.getCodTelefonoCliente() + " - " + datosCorreo.getTelefonoCliente();
 			}
 			else {
 				telefonOperador = noAplica;
 			}
 			
 			if (!datosCorreo.getCodCelularCliente().equals("") && !datosCorreo.getCelularCliente().equals("")){
 				celularOperador = datosCorreo.getCodCelularCliente() + " - " + datosCorreo.getCelularCliente();
 			}
 			else {
 				celularOperador = noAplica;
 			}
 			
 			if (!datosCorreo.getEmailCliente().equals("")){
 				emailOperador = datosCorreo.getEmailCliente();
 			}
 			else {
 				emailOperador = noAplica;
 			}
 			
 			if (!datosCorreo.getRegionCliente().equals("")){
 				regionOperador = datosCorreo.getRegionCliente();
 			}
 			else {
 				regionOperador = noAplica;
 			}
 			
 			if (!datosCorreo.getComunaCliente().equals("")){
 				comunaOperador = datosCorreo.getComunaCliente();
 			}
 			else {
 				comunaOperador = noAplica;
 			}
 			
 			String rutOperador = datosCorreo.getRutOperador() + " - " + datosCorreo.getDvOperador();
 			String glosaOficina = glosaOficinaIngreso.trim().equals("") ? codOficinaIngreso
 					: glosaOficinaIngreso.trim();
 			String archivoMail = TablaValores.getValor("multilinea.parametros",
 					"archivoMailGenerico", "desc");
 			if (getLogger().isDebugEnabled()) {
 				getLogger().debug("armaEmailParaDestinatario - ...");
 			}
 			Map paramsTemplate = new HashMap();
 			paramsTemplate.put("${fechaHora}", fechaHora);
 			paramsTemplate.put("${montoCredito}", montoCredito);
 			paramsTemplate.put("${totalVencimientos}",
 					String.valueOf(datosCorreo.getCuotaSeleccionada()));
 			paramsTemplate.put("${fechaPrimerVcto}", fechaPrimerVcto);
 			paramsTemplate.put("${numeroOperacion}", numeroOperacion);
 			paramsTemplate.put("${destinoCredito}",
 					datosCorreo.getDestinoDelCredito());
 			paramsTemplate.put("${tasaInteres}", datosCorreo.getTasaInteres());
 			paramsTemplate.put("${estado}", estado);
 			paramsTemplate.put("${nombreEmpresa}", datosCorreo.getRazonSocial());
 			paramsTemplate.put("${rutEmpresa}", idc);
 			paramsTemplate.put("${codBanca}", codBancaCorreo);
 			paramsTemplate.put("${codigoEjecutivo}", codEjecutivoCorreo);
 			paramsTemplate.put("${glosaOficina}", glosaOficina);
 			paramsTemplate.put("${rutOperador}", rutOperador);
 			paramsTemplate.put("${telefonoOperador}", telefonOperador);
 			paramsTemplate.put("${celularOperador}", celularOperador);
 			paramsTemplate.put("${emailOperador}", emailOperador);
 			paramsTemplate.put("${regionOperador}", regionOperador);
 			paramsTemplate.put("${comunaOperador}", comunaOperador);
 		
 			
 	    	cuerpoDelEmail = plantillaCorreo(paramsTemplate, archivoMail);

 			if (getLogger().isDebugEnabled()) {
 				getLogger().debug("armaEmailParaDestinatario - ANTECEDENTES DEL CREDITO");
 				getLogger().debug("armaEmailParaDestinatario - Fecha y Hora de la Solicitud   : "
 						+ fechaHora);
 				getLogger().debug("armaEmailParaDestinatario - Monto del Credito              : "
 						+ montoCredito);
 				getLogger().debug("armaEmailParaDestinatario - Cuotas                         : "
 						+ datosCorreo.getCuotaSeleccionada());
 				getLogger().debug("armaEmailParaDestinatario - Fecha 1° vencimiento           : "
 						+ fechaPrimerVcto);
 				getLogger().debug("armaEmailParaDestinatario - Número de operacion            : "
 						+ numeroOperacion);
 				getLogger().debug("armaEmailParaDestinatario - Destino del credito            : "
 						+ datosCorreo.getDestinoDelCredito());
 				getLogger().debug("armaEmailParaDestinatario - Tasa de Interes                : "
 						+ datosCorreo.getTasaInteres());
 				getLogger().debug("armaEmailParaDestinatario - Estado del Avance              : "
 						+ estado);
 				getLogger().debug("armaEmailParaDestinatario - ANTECEDENTES DEL CLIENTE");
 				getLogger().debug("armaEmailParaDestinatario - Nombre                         : "
 						+ datosCorreo.getRazonSocial());
 				getLogger().debug("armaEmailParaDestinatario - Rut                            : "
 						+ idc + datosCorreo.getDv());
 				getLogger().debug("armaEmailParaDestinatario - Banca                          : "
 						+ codBancaCorreo);
 				getLogger().debug("armaEmailParaDestinatario - Ejecutivo                      : "
 						+ codEjecutivoCorreo);
 				getLogger().debug("armaEmailParaDestinatario - Oficina                        : "
 						+ glosaOficina);
 				getLogger().debug("armaEmailParaDestinatario - rutOperador                     : "
 						+ rutOperador);
 				getLogger().debug("armaEmailParaDestinatario - telefonoOperador                     : "
 						+ datosCorreo.getTelefonoCliente());
 				getLogger().debug("armaEmailParaDestinatario - celularOperador                     : "
 						+ datosCorreo.getCelularCliente());
 				getLogger().debug("armaEmailParaDestinatario - emailOperador                     : "
 						+ datosCorreo.getEmailCliente());
 				getLogger().debug("armaEmailParaDestinatario - regionOperador                     : "
 						+ datosCorreo.getRegionCliente());
 				getLogger().debug("armaEmailParaDestinatario - comunaOperador                     : "
 						+ datosCorreo.getComunaCliente());
 				
 				getLogger().debug("[armaEmailParaDestinatario] - [BCI_FINOK]");
 			}
 			
 			String cuerpoMail = "";
 			if(cuerpoDelEmail != null) {
 				cuerpoMail = cuerpoDelEmail.toString();
 			}
 			return cuerpoMail;

 		} 
 		catch (Exception e) {
 			 if (getLogger().isEnabledFor(Level.ERROR)) { 
        		 getLogger().error("[armaEmailParaDestinatario][" + clienteMB.getRut() 
        				 +"][BCI_FINEX][armaEmailParaDestinatario]" +" error con mensaje: " + e.getMessage());
        	 } 
 			throw new GeneralException("UNKNOW", e.getMessage());
 		}

 	}

 	/**
 	 * Metodo que arma mensaje de email para Destinatario aval.
 	 *
 	 *
 	 * Registro de versiones:
 	 * <ul>
 	 * <li>1.0 10/05/2016 Manuel Escárate(BEE) - Pablo Paredes. (ing.Soft.BCI) ) : Version Inicial</li>
 	 * <li>1.1 12/08/2016, Manuel Escárate R. (BEE) -  Felipe Ojeda (ing.Soft.BCI) : Se agregan nuevos datos al correo.</li>
 	 * </ul>
 	 * <p>
 	 *
 	 * @param datosCorreo datos para correo.
 	 * @param codBancaCorreo codigo banca.
 	 * @param fechaPrimerVcto fecha primer vencimiento.
 	 * @param montoCredito Monto del credito.
 	 * @param estado estado.
 	 * @throws GeneralException excepcion general.
 	 * @return curepo de correo.
 	 * @since 2.0
 	 */
 	public String armaEmailParaDestinatarioAval(
 			DatosParaCorreoTO datosCorreo, String codBancaCorreo,
 			String fechaPrimerVcto, String montoCredito, String estado)
 			throws GeneralException {
 		if (getLogger().isDebugEnabled()) {
 			getLogger().debug("[armaEmailParaDestinatarioAval] [BCI_INI]");
 		}
 		try {
 			String fechaHora = FechasUtil.fechaActualFormateada("dd/MM/yyyy")
 					+ " - " + FechasUtil.fechaActualFormateada("HH:mm:ss");
 			String fechaActual =  FechasUtil.fechaActualFormateada("dd/MM/yyyy");
 			StringBuffer cuerpoDelEmail;
 			String numeroOperacion = datosCorreo.getCaiOperacion()
 					+ datosCorreo.getIicOperacion();
 			String idc = datosCorreo.getRut() + " - " + datosCorreo.getDv();
 			String idcAval =  datosCorreo.getDatosAvales().getRutAval() + " - " + datosCorreo.getDatosAvales().getDvAval();
 			if (getLogger().isDebugEnabled()) {
 				getLogger().debug("[armaEmailParaDestinatarioAval] antes del archivo mail avales");
 			}
 			String archivoMail = "";
 			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[armaEmailParaDestinatarioAval]  [tipoDeCreditoSeleccionado]" + datosCorreo.getTipoOperacion());
			}
 			
 			if (datosCorreo.getTipoOperacion().equals("AVC010")){
				archivoMail = TablaValores.getValor("multilinea.parametros",
						"archivoMailAvalesUnVencimiento", "desc");
			}
			else if (datosCorreo.getTipoOperacion().equals("AVC721")){
				archivoMail = TablaValores.getValor("multilinea.parametros",
						"archivoMailAvalesCuotaFija", "desc");
			}
 			if (getLogger().isDebugEnabled()) {
 				getLogger().debug("[armaEmailParaDestinatarioAval] arhicov mailaaa" + archivoMail);
 				getLogger().debug("[armaEmailParaDestinatarioAval]  - ...");
 			}
 			
 			String diaPrimerVenc = "";
			String mesPrimerVenc = "";
			String anioPrimerVenc = "";
			if (fechaPrimerVcto != null && !fechaPrimerVcto.equals("")){
				diaPrimerVenc = fechaPrimerVcto.substring(0,VALOR_DOS); 
				mesPrimerVenc = fechaPrimerVcto.substring(VALOR_TRES,VALOR_CINCO);
				anioPrimerVenc = fechaPrimerVcto.substring(VALOR_SEIS);      
			}
			
			String diaUltimoVenc = "";
			String mesUltimoVenc = "";
			String anioUltimoVenc = "";
			if (datosCorreo.getFechaUltimaCuota() != null && !datosCorreo.getFechaUltimaCuota().equals("")){
				diaUltimoVenc = datosCorreo.getFechaUltimaCuota().substring(0,VALOR_DOS); 
				mesUltimoVenc = datosCorreo.getFechaUltimaCuota().substring(VALOR_TRES,VALOR_CINCO);
				anioUltimoVenc = datosCorreo.getFechaUltimaCuota().substring(VALOR_SEIS);      
			}
			
 			Map paramsTemplate = new HashMap();
 			paramsTemplate.put("${nombreEmpresa}", datosCorreo.getRazonSocial());
 			paramsTemplate.put("${nombreAval}", datosCorreo.getDatosAvales().getNombreAval());
 			paramsTemplate.put("${fechaHora}", fechaHora);
 			paramsTemplate.put("${montoCredito}", montoCredito);
 			paramsTemplate.put("${totalVencimientos}",
 					String.valueOf(datosCorreo.getCuotaSeleccionada()));
 			paramsTemplate.put("${fechaPrimerVcto}", fechaPrimerVcto);
 			paramsTemplate.put("${numeroOperacion}", numeroOperacion);
 			paramsTemplate.put("${destinoCredito}",
 					datosCorreo.getDestinoDelCredito());
 			paramsTemplate.put("${estado}", estado);
 			paramsTemplate.put("${rutEmpresa}", idc);
 			paramsTemplate.put("${codBanca}", codBancaCorreo);
 			paramsTemplate.put("${montoEnPalabras}", datosCorreo.getMontoExpresadoEnPalabras());
			paramsTemplate.put("${valorCuota}", formatearMonto(datosCorreo.getValorCuota(),0,"#,###"));
			paramsTemplate.put("${diaPrimerVen}", diaPrimerVenc);
			paramsTemplate.put("${mesPrimerVen}", mesPrimerVenc);
			paramsTemplate.put("${anioPrimerVen}", anioPrimerVenc);
			paramsTemplate.put("${tasaInteres}", datosCorreo.getTasaInteres());
			paramsTemplate.put("${direccionAval}", datosCorreo.getDatosAvales().getDireccionAval());
			paramsTemplate.put("${comunaAval}", datosCorreo.getDatosAvales().getComunaAval());
			paramsTemplate.put("${ciudadAval}", datosCorreo.getDatosAvales().getCiudadAval());
			paramsTemplate.put("${rutAval}",idcAval);	
			paramsTemplate.put("${comunaEmpresa}",datosCorreo.getComunaEmpresa());	
			paramsTemplate.put("${ciudadEmpresa}",datosCorreo.getCiudadEmpresa());	
			paramsTemplate.put("${direccionEmpresa}",datosCorreo.getDireccionEmpresa());
			paramsTemplate.put("${diaUltimoVen}", diaUltimoVenc);
			paramsTemplate.put("${mesUltimoVen}", mesUltimoVenc);
			paramsTemplate.put("${anioUltimoVen}", anioUltimoVenc);
			paramsTemplate.put("${impuestos}", formatearMonto(datosCorreo.getImpuestos(),0,"#,###"));
 			paramsTemplate.put("${seguros}", formatearMonto(datosCorreo.getSeguros(),0,"#,###"));
 			paramsTemplate.put("${valorNotario}", formatearMonto(datosCorreo.getValorNotario(),0,"#,###"));
 			paramsTemplate.put("${fechaActual}", fechaActual);

 			cuerpoDelEmail = plantillaCorreo(paramsTemplate, archivoMail);

 			if (getLogger().isDebugEnabled()) {
 				getLogger().debug("[armaEmailParaDestinatarioAval]  - ANTECEDENTES DEL CREDITO");
 				getLogger().debug("[armaEmailParaDestinatarioAval]  - Fecha y Hora de la Solicitud   : "
 						+ fechaHora);
 				getLogger().debug("[armaEmailParaDestinatarioAval]  - Monto del Credito              : "
 						+ montoCredito);
 				getLogger().debug("[armaEmailParaDestinatarioAval]  - Cuotas                         : "
 						+ datosCorreo.getCuotaSeleccionada());
 				getLogger().debug("[armaEmailParaDestinatarioAval]  - Fecha 1° vencimiento           : "
 						+ fechaPrimerVcto);
 				getLogger().debug("[armaEmailParaDestinatarioAval]  - Número de operacion            : "
 						+ numeroOperacion);
 				getLogger().debug("[armaEmailParaDestinatarioAval]  - Destino del credito            : "
 						+ datosCorreo.getDestinoDelCredito());
 				getLogger().debug("[armaEmailParaDestinatarioAval]  - Estado del Avance              : "
 						+ datosCorreo.getEstado());
 				getLogger().debug("[armaEmailParaDestinatarioAval]  - FIN armaEmailParaDestinatario");
 			}
 			if (getLogger().isDebugEnabled()) {
 				getLogger().debug("[armaEmailParaDestinatarioAval]  [BCI_FINOK]");
 			}
 			String cuerpoMail = "";
 			if(cuerpoDelEmail != null) {
 				cuerpoMail = cuerpoDelEmail.toString();
 			}
 			return cuerpoMail;

 		} 
 		catch (Exception e) {
 			if (getLogger().isEnabledFor(Level.ERROR)) { 
 				getLogger().error("[armaEmailParaDestinatario][" + clienteMB.getRut() 
 						+"][BCI_FINEX][armaEmailParaDestinatario]" +" error con mensaje: " + e.getMessage());
 			} 
 			throw new GeneralException("UNKNOW", e.getMessage());
 		}

 	}
 	
 	/**
	 * Metodo que reemplaza en plantilla dada las campos dinamicos en correo de
	 * texto.
	 *
	 *
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 (10/05/2016 Manuel Escárate - BEE) - Pablo Paredes (ing.Soft.BCI): Version Inicial
	 *
	 * </ul>
	 * <p>
	 *
	 * @param aVariables a reemplazar en plantilla dada.
	 * @param archivo plantilla.
	 * @return plantilla con reemplazos.
	 * @since 1.0
	 */
	private  StringBuffer plantillaCorreo(Map aVariables, String archivo) {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("plantillaCorreo - [BCI_INI]");
		}
		StringBuffer buffer = null;
		if (archivo == null) {
			return null;
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(archivo));
			String linea = "";
			while (linea != null) {
				linea = reader.readLine();
				if (buffer == null) {
					buffer = new StringBuffer();
				}
				if (linea != null) {
					buffer.append(linea + "\r\n");
				}
			}
			if (aVariables != null) {
				Iterator claves = aVariables.keySet().iterator();
				while (claves.hasNext()) {
					String clave = (String) claves.next();
					String var = (String) aVariables.get(clave);

					String texto = buffer.toString();
					int pos = texto.indexOf(clave);

					while (pos > 0) {
						int largo = clave.length();
						buffer = buffer.replace(pos, pos + largo, var);
						texto = buffer.toString();
						pos = texto.indexOf(clave);
					}
				}
			}
		} 
		 catch (FileNotFoundException e) {
			 if (getLogger().isEnabledFor(Level.ERROR)) {
				 getLogger().error("plantillaCorreo - FileNotFoundException - Algo ocurrio ::"
					+ ErroresUtil.extraeStackTrace(e));
			 }
		} 
		catch (IOException e) {
			 if (getLogger().isEnabledFor(Level.ERROR)) { 
        		 getLogger().error("[plantillaCorreo][" + clienteMB.getRut() 
        				 +"][BCI_FINEX][plantillaCorreo]" +" error con mensaje: " + e.getMessage());
        	 } 
		}
		finally {
			try {
				reader.close();
			} 
			catch (IOException e) {
				if (getLogger().isEnabledFor(Level.ERROR)){
					getLogger().debug("[plantillaCorreo]" + archivo + "[algo ocurrio]" 
							+ ErroresUtil.extraeStackTrace( e ));
				}

			}
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("plantillaCorreo - [BCI_FINOK]");
		}
		return buffer;
	}
     
     /**
      * Método para obtener instancia del servicio de direcciones.
      * <br>
      * Registro de versiones:<ul>
      * <li>1.0 10/05/2016 Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): versión inicial.</li> 
      * </ul>
      * @return servicio de direcciones.
      * @throws DireccionesException excepcion de direccion.
      * @since 1.0
      */     
     private ServiciosDirecciones instanciaEJBServicioDirecciones()
     		throws DireccionesException{
     	if (getLogger().isDebugEnabled()) getLogger().debug("[instanciaEJBServicioDirecciones] [BCI_INI]");
     	ServiciosDireccionesHome localServiciosDireccionesHome = null;
     	try{
     		EnhancedServiceLocator localEnhancedServiceLocator1 = EnhancedServiceLocator.getInstance();
     		localServiciosDireccionesHome = (ServiciosDireccionesHome)localEnhancedServiceLocator1.getHome("wcorp.serv.direcciones.ServiciosDirecciones", ServiciosDireccionesHome.class);
     		if (getLogger().isDebugEnabled()) {
    			getLogger().debug("[instanciaEJBServicioDirecciones] [BCI_FINOK]");
    		}
     		return localServiciosDireccionesHome.create();
     	}
     	catch (Exception localException1){
     		if (getLogger().isEnabledFor(Level.ERROR)) {
     			getLogger().error("[instanciaEJBServicioDirecciones][BCI_FINEX] Fallo 1er intento.", localException1);
     		}
     		try{
     			EnhancedServiceLocator localEnhancedServiceLocator2 = EnhancedServiceLocator.getInstance();
     			localServiciosDireccionesHome = (ServiciosDireccionesHome)localEnhancedServiceLocator2.getHome("wcorp.serv.direcciones.ServiciosDirecciones", ServiciosDireccionesHome.class);
     			return localServiciosDireccionesHome.create();
     		}
     		catch (Exception localException2){
     			if (getLogger().isEnabledFor(Level.ERROR)) {
     				getLogger().error("[instanciaEJBServicioDirecciones] [BCI_FINEX] Fallo 2o intento.", localException1);
     			}
     			throw new DireccionesException("003", localException1.getMessage());
     		}
     	}
     }
     
     /**
      * obtiene lista de filas que son la tabla consultada.
      * <p>
      *
      * Registro de versiones:
      * <ul>
      *
      * <li>1.0 18/08/2016, Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI): : versión inicial.
      *
      * </ul></p>
      *
      * @param tableManagerService objeto usado para referenciar al manejador de tablas.
      * @param multienvironment contexto del sistema.
      * @param sistema código del sistema a consultar.
      * @param tabla código de la tabla a consultar.
      * @param codigo código llave a consultar.
      * @return lista de filas con descripciones.
      * @throws Exception Error ocuriido en la consulta.
      * @throws RemoteException Error ocurrido en EJB.
      * @throws TableAccessException Error Ocurrido en la tala del sistema.
      *
      * @since 1.3
      */
     public List obtenerTablaDescripciones(TableManagerService tableManagerService
                                           , MultiEnvironment multienvironment
                                           , String sistema
                                           , String tabla
                                           , String codigo) throws Exception, RemoteException,
                                           TableAccessException {
    	 if (getLogger().isDebugEnabled()) {
    		 getLogger().debug("[obtenerTablaDescripciones][BCI_INI]");
    	 }
    	 TableSpec tableSpec = new TableSpecImpl(sistema, tabla, codigo);
    	 if (getLogger().isDebugEnabled()) {
    		 getLogger().debug("[obtenerTablaDescripciones][BCI_FINOK]");
    	 }
    	 return tableManagerService.query(multienvironment, tableSpec, true);
     }
     
     /**
      * retorna texto con descripcion encontrada en la lista
      * <p>
      * Registro de versiones:
      * <ul>
      *
      * <li>1.0 18/08/2016, Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI): versión inicial.
      *
      * </ul>
      * </p>
      *
      * @param data Lista a recorrer para obtener descripcion.
      * @param campo campo de la descripción a obtener.
      * @return string con la descripcion larga del sistema.
      *
      * @since 1.3
      */
     public String getValor(List data, String campo) {
     	if (getLogger().isDebugEnabled()){
             getLogger().debug("[getValor][BCI_INI]");    
         }
     	largoComuna = 0;
     	if (data != null) {
             for (Iterator iter = data.iterator() ; iter.hasNext() ;) {
                 Row row = (Row) iter.next();
                 if (row.getCode().equals(campo)) {
                 	largoComuna = row.getShortDescription().length();
                 	 if (getLogger().isDebugEnabled()) {
                		 getLogger().debug("[getValor][BCI_FINOK]");
                	 }
                     return row.getLongDescription();
                 }
             }
     	}
     	if (getLogger().isDebugEnabled()){
     		getLogger().debug("[getValor][data es null]");    
     	}
     	return "";
     }
    
    /**
 	 * Método encargado de enviar correo a contacto de empresa.
 	 * <br>
 	 * Registro de versiones:<ul>
 	 * <li>1.0 11/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): versión inicial.</li> 
 	 * </ul>
 	 * @param multiEnvironmentCorreoEmpresa multiambiente.
 	 * @param estadoAvance estado del avance.
 	 * @param asuntoAvance asunto del avance.
 	 * @throws Exception excepcion en el metodo.
 	 * @since 1.3
 	 */    
     public void enviarCorreoContactoEmpresa(MultiEnvironment multiEnvironmentCorreoEmpresa, String estadoAvance,String asuntoAvance) throws Exception{
    	 if (getLogger().isDebugEnabled()){
    		 getLogger().debug("[enviarCorreoContactoEmpresa][BCI_INI]");    
    	 }
    	 double valorNotarioCorreo = 0;
    	 double impuestosCorreo = 0;
    	 double segurosCorreo = 0;
    	 String mailContactoEmpresa = " ";
    	 String glosasAvales = "";
    	 String textosParaAvales = "";
    	 String nombreAval = " ";
    	 String comunaAval = " ";
    	 String direccionAval = " ";
    	 String destinoDelCredito = ""; 
    	 String direccionEmpresa = "";
    	 String comunaEmpresa = "";
    	 String ciudadAval = " ";
    	 String ciudadEmpresa = " ";
    	 String salidaValorEmp = " ";
    	 String salidaValorAval = " ";
    	 String fechaUltimaCuota = " ";
    	 String caiOpe = operacionSeleccionada.getCaiOperacion();
    	 int iicOpe = operacionSeleccionada.getIicOperacion();
    	 int diaDeVencimiento = 0;
    	 String tipoOperacion = "";
    	 DatosAvalesTO[] avalesMultilinea = null;
    	 
    	 try{
    		 ResultConsultaAvales obeanAval = new ResultConsultaAvales();
    		 obeanAval = crearEJBmultilinea().consultaAvales(multiEnvironmentCorreoEmpresa,(int)clienteMB.getRut(),clienteMB.getDigitoVerif(), ' ', " "," ", " ", 0, 0, "AVL", "AVC");
    		 if (getLogger().isDebugEnabled()) { 
    			 getLogger().debug("[enviarCorreoContactoEmpresa] despues consultaAvales");
    		 }
    		 Aval[] avales = null;
    		 if (obeanAval != null){
    			 avales    = obeanAval.getAvales();
    			 if (getLogger().isDebugEnabled()) { 
    				 getLogger().debug("avales.length ["+ avales.length +"]");
    			 }
    			 avalesMultilinea = new DatosAvalesTO[avales.length];
    			 int contAvl = 0;
    			 for (int i = 0; i < avales.length; i++) {
    				 if (avales[i] != null){
    					 if (avales[i].getVigente() == 'S'){
    						 avalesMultilinea[contAvl] = new DatosAvalesTO();
    						 avalesMultilinea[contAvl].setRutAval(String.valueOf(avales[i].getRutAval()));
    						 avalesMultilinea[contAvl].setDvAval(String.valueOf(avales[i].getDigitoVerificaAval()));
    						 contAvl++;
    					 }
    				 }
    			 }
    		 }

    		 if (getLogger().isDebugEnabled()){
    			 getLogger().debug("[enviarCorreoContactoEmpresa] antes del seteo de avales"); 
    		 }
    	 }
    	 catch(Exception e) {
    		 if(getLogger().isEnabledFor(Level.ERROR)){
    			 getLogger().error("[enviarCorreoContactoEmpresa]ERROR [consultaAvales] Cliente  Exception [ " + e.getMessage() + "]");
    		 }
    	 }

    	 try {
    		 SvcCreditosGlobales svcCreditos = new SvcCreditosGlobalesImpl();
    		 ResultConsultaCalendarioPago resultCalendario = svcCreditos.consultaCalendarioPago(
    				 multiEnvironmentCorreoEmpresa, caiOpe, iicOpe);
    		 if (resultCalendario != null){
    			 calendarioPago = resultCalendario.getCalendario();
    			 fechaUltimaCuota =   FechasUtil.convierteDateAString(calendarioPago[Integer.parseInt(operacionSeleccionada.getTotalVencimientos().trim())-1].getFecVencPago(),"dd/MM/yyyy"); 
    		 }
    	 } 
    	 catch (Exception ex) {
    		 if (getLogger().isEnabledFor(Level.ERROR)) {
    			 getLogger().debug("[enviarCorreoContactoEmpresa] [BCI_FINEX] Exception:" + ErroresUtil.extraeStackTrace(ex));
    		 }
    	 }
    	 
    	 TableManagerService tableManagerService = (TableManagerService)LocalizadorDeServicios
    			 .obtenerInstanciaEJB(LocalizadorDeServicios.JNDI_MANEJADOR_DE_TABLAS);
    	 List comunasParaCorreo = null;
    	 try{
    		 comunasParaCorreo = obtenerTablaDescripciones(tableManagerService,multiEnvironmentCorreoEmpresa,TABLE_MANANGER_SISTEMA_TAB,TABLE_MANANGER_TABLA_COM,"");
    		 DireccionClienteBci[] direccionesEmpresa = instanciaEJBServicioDirecciones().getAddressBci(clienteMB.getRut());
    		 if (direccionesEmpresa != null) {
    			 for (int d = 0; d < direccionesEmpresa.length; d++) {
    				 DireccionClienteBci direccionEmp = (DireccionClienteBci) direccionesEmpresa[d];
    				 if (direccionEmp.tipoDireccion == 'D') {
    					 salidaValorEmp = getValor(comunasParaCorreo, direccionEmp.getComuna());
    					 comunaEmpresa = salidaValorEmp.substring(0, largoComuna);
    					 ciudadEmpresa = salidaValorEmp.substring(largoComuna).trim();
    					 direccionEmpresa = direccionEmp.getDireccion();
    				 }
    				 if (direccionEmp.tipoDireccion == TIPOMAIL){
    					 mailContactoEmpresa = direccionEmp.getDireccion();
    				 }
    			 }
    		 }
    	 }
    	 catch (Exception ex){
    		 if (getLogger().isEnabledFor(Level.ERROR)) { 
    			 getLogger().error("[enviarCorreoContactoEmpresa][" + clienteMB.getRut() 
    					 +"][BCI_FINEX][enviarCorreoContactoEmpresa] [obteniendo comunas]" +" error con mensaje: " + ex.getMessage(), ex);
    		 }  
    	 }
    	 try {
    		 if (getLogger().isDebugEnabled()) getLogger().debug("[enviarCorreoContactoEmpresa] consultando destinoDelCredito "
    				 + "  [" + caiOpe + iicOpe + "]" );
    		 ResultConsultaOperacionCredito resConOpe = crearEJBmultilinea().consultaOperacionCredito(multiEnvironmentCorreoEmpresa, caiOpe, iicOpe);
    		 destinoDelCredito  = resConOpe.getGlosaDestinoEspecifico();
    		 ResultConsultaCgr seguros = obtenerSeguros(multiEnvironmentCorreoEmpresa, caiOpe, String.valueOf(iicOpe));
    		 double sumaSeguros = 0;
    		 if (seguros != null && seguros.getInstanciaDeConsultaCgr() != null){
    			 for(int j = 0; j < seguros.getInstanciaDeConsultaCgr().length; j++){
    				 IteracionConsultaCgr cgr = seguros.getInstanciaDeConsultaCgr()[j];
    				 if (cgr.getIndVigenciaInst() == 'S'){
    					 sumaSeguros = sumaSeguros + cgr.getTasaMontoFinal();
    				 }

    			 }
    		 }
    		 if (getLogger().isDebugEnabled()) getLogger().debug("[enviarCorreoContactoEmpresa] Total Seguros: " + sumaSeguros);
    		 segurosCorreo = sumaSeguros;	
    		 ResultConsultaCgr gastos = obtenerGastoNotarial(multiEnvironmentCorreoEmpresa, caiOpe, String.valueOf(iicOpe));
    		 double gastoNotario = 0;

    		 if (gastos != null && gastos.getTotOcurrencias() > 0){
    			 gastoNotario = gastos.getInstanciaDeConsultaCgr(0).getTasaMontoFinal();
    			 if (getLogger().isDebugEnabled()){ 
    				 getLogger().debug("[enviarCorreoContactoEmpresa] valorNotario: " +  gastoNotario);
    			 }
    		 } 
    		 else {
    			 if (getLogger().isDebugEnabled()){
    				 getLogger().debug("[enviarCorreoContactoEmpresa] No existen ocurrencias en la consulta CGR para Gastos Notariales");
    			 }
    		 } 
    		 if (getLogger().isDebugEnabled()) getLogger().debug("[enviarCorreoContactoEmpresa] Gastos Notariales: " + gastoNotario);
    		 valorNotarioCorreo = gastoNotario;
    		 impuestosCorreo = resConOpe.getImpuestos();
    		 tipoOperacion = resConOpe.getTipoOperacion().trim()+resConOpe.getCodigoAuxiliar();
    		 if (tipoOperacion.equals("AVC721")){
    			 if (resConOpe.getEstructura_vencimientos()[0] != null){
    				 if (resConOpe.getEstructura_vencimientos()[0].getDiaVencimiento() > 0){
    					 diaDeVencimiento = resConOpe.getEstructura_vencimientos()[0].getDiaVencimiento();
    				 }
    				 else {
    					 if (resConOpe.getEstructura_vencimientos()[0].getFechaPrimerVcto() != null){
    						 Date fechaProxVenc = resConOpe.getEstructura_vencimientos()[0].getFechaPrimerVcto();
    						 String fechaProxConvert = FechasUtil.convierteDateAString(fechaProxVenc,"ddMMyyyy"); 
    						 diaDeVencimiento = Integer.parseInt(fechaProxConvert.substring(0,VALOR_DOS));
    					 }
    				 }
    			 }
    		 }
    		 if (getLogger().isDebugEnabled()) getLogger().debug("[enviarCorreoContactoEmpresa] despues destinoDelCredito "
    				 + "[" + caiOpe + iicOpe + "]" );
    	 }
    	 catch (Exception ex) {
    		 if (getLogger().isEnabledFor(Level.ERROR)) { 
    			 getLogger().error("[enviarCorreoContactoEmpresa][" + clienteMB.getRut() 
    					 +"][BCI_FINEX][enviarCorreoContactoEmpresa]" +" error con mensaje: " + ex.getMessage(), ex);
    		 }  
    		 destinoDelCredito = " ";
    	 }
    	 try {
    		 if (avalesMultilinea != null && avalesMultilinea.length > 0){
    			 textosParaAvales = crearEJBmultilinea().obtenerTextosParaAvales();
    			 for (int i = 0; i < avalesMultilinea.length; i++) {
    				 if (avalesMultilinea[i] != null){
    				 DireccionClienteBci[] direcciones = instanciaEJBServicioDirecciones().getAddressBci(Long.parseLong(avalesMultilinea[i].getRutAval()));
    				 if (direcciones != null) {
    					 for (int j = 0; j < direcciones.length; j++) {
    						 nombreAval = direcciones[0].getNombres();
    						 DireccionClienteBci direccion = (DireccionClienteBci) direcciones[j];
    						 if (direccion.tipoDireccion == 'D') {
    							 salidaValorAval = getValor(comunasParaCorreo, direccion.getComuna());
    							 comunaAval = salidaValorAval.substring(0, largoComuna);
    							 ciudadAval = salidaValorAval.substring(largoComuna).trim();
    							 direccionAval = direccion.getDireccion();
    						 }
    					 }
    				 }
    				 DatosAvalesTO datosAval = new DatosAvalesTO();
    				 datosAval.setNombreAval(nombreAval);
    				 datosAval.setComunaAval(comunaAval);
    				 datosAval.setCiudadAval(ciudadAval);
    				 datosAval.setDireccionAval(direccionAval);
    				 datosAval.setRutAval(avalesMultilinea[i].getRutAval());
    				 datosAval.setDvAval(avalesMultilinea[i].getDvAval());
    				 glosasAvales = glosasAvales + crearEJBmultilinea().obtenerGlosasPorAval(datosAval);
    				 }
    			 }	
    		 }
    	 }
    	 catch (Exception ex){
    		 if (getLogger().isEnabledFor(Level.ERROR)) { 
    			 getLogger().error("[enviarCorreoContactoEmpresa][" + clienteMB.getRut() 
    					 +"][BCI_FINEX][enviarCorreoContactoEmpresa]" +" error con mensaje: " + ex.getMessage(), ex);
    		 }  
    	 }
    	 try{		
    		 DatosParaCorreoTO datosCorreo = new DatosParaCorreoTO();
    		 datosCorreo.setCaiOperacion(caiOpe);
    		 datosCorreo.setIicOperacion(iicOpe);
    		 datosCorreo.setFechaVencimientoSeleccionada(operacionSeleccionada.getFechaPrimerVcto());
    		 datosCorreo.setRut(clienteMB.getRut());
    		 datosCorreo.setDv(clienteMB.getDigitoVerif());
    		 datosCorreo.setRazonSocial(clienteMB.getRazonSocial());
    		 datosCorreo.setMontoFinalCredito(montoCorreo);
		     datosCorreo.setCuotaSeleccionada(Integer.parseInt(operacionSeleccionada.getTotalVencimientos().trim()));
    		 datosCorreo.setDestinoDelCredito(destinoDelCredito);
    		 datosCorreo.setEstado(estadoAvance);
    		 datosCorreo.setAsunto(asuntoAvance);
    	     datosCorreo.setTasaInteres(formatearMonto((tasaInteres),VALOR_DOS,"#,##0"));
		     String montoEnPalabras = TextosUtil.numeroEnPalabras(montoCorreo);
    		 datosCorreo.setMontoExpresadoEnPalabras(montoEnPalabras);
    		 datosCorreo.setValorCuota(calendarioPago[0].getCuota());
    		 datosCorreo.setComunaEmpresa(comunaEmpresa);
    		 datosCorreo.setCiudadEmpresa(ciudadEmpresa);
    		 datosCorreo.setDireccionEmpresa(direccionEmpresa);
    		 datosCorreo.setFechaUltimaCuota(fechaUltimaCuota);
    		 datosCorreo.setDiaVencimiento(diaDeVencimiento);
    		 datosCorreo.setGlosasAvales(glosasAvales);
    		 datosCorreo.setTextosParaAvales(textosParaAvales);
    		 datosCorreo.setMailEmpresa(mailContactoEmpresa);
    		 datosCorreo.setSeguros(segurosCorreo);
    		 datosCorreo.setImpuestos(impuestosCorreo);
    		 datosCorreo.setValorNotario(valorNotarioCorreo);
    		 datosCorreo.setTipoOperacion(tipoOperacion);
    		 enviarEmailContactoEmpresa(multiEnvironmentCorreoEmpresa,datosCorreo);
    		 if (getLogger().isDebugEnabled()){
        		 getLogger().debug("[enviarCorreoContactoEmpresa][BCI_FINOK]");    
        	 }
    	 }
    	 catch (Exception ex){
    		 if (getLogger().isEnabledFor(Level.ERROR)) { 
    			 getLogger().error("[enviarCorreoContactoEmpresa][" + clienteMB.getRut() 
    					 +"][BCI_FINEX][enviarCorreoContactoEmpresa]" +" error con mensaje: " + ex.getMessage(), ex);
    		 }  
    	 }
     }
     
     /**
      * Método que envía email al contacto de la empresa.
      * 
      * Registro de versiones:
      * <ul>
      * <li>1.0 12/08/2016, Manuel Escárate R. (BEE) - Felipe Ojeda. (ing.Soft.BCI): versión inicial.</li>
      *
      * </ul>
      * 
      * @param multiEnvironmentEmce multiambiente.
      * @param datosCorreo datos necesarios para el envío de correo.
      * 
      * @throws GeneralException excepcion general.
      * @since 1.3
      */
     public void enviarEmailContactoEmpresa(MultiEnvironment multiEnvironmentEmce,DatosParaCorreoTO datosCorreo) throws GeneralException {
         
     	if (getLogger().isDebugEnabled()) {
             getLogger().debug("[enviarEmailContactoEmpresa] [BCI_INI]");
         }
         
         String fechaPrimerVencimiento   = "";
         String de                       = "";
         String direccionOrigen          = "";
         String direccionDestino         = "";
         String asunto                   = "";
         String cuerpoDelEmail           = "";
         String firma                    = "";
         String estado                   = "";
         if (getLogger().isDebugEnabled()) {
         	getLogger().debug("[enviarEmailContactoEmpresa] - caiOperacionNro              [" + datosCorreo.getCaiOperacion() +"]");
         	getLogger().debug("[enviarEmailContactoEmpresa] - iicOperacionNro              [" + datosCorreo.getIicOperacion() +"]");
         }
  
         String fechaPrimerVcto = datosCorreo.getFechaVencimientoSeleccionada();
         String montoCreditoCorreo = formatearMonto(datosCorreo.getMontoFinalCredito(),0,"#,###");
         fechaPrimerVencimiento = fechaPrimerVcto.substring(VALOR_SEIS) 
         		+ "/" +fechaPrimerVcto.substring(VALOR_CUATRO,VALOR_SEIS) + "/" 
                 +fechaPrimerVcto.substring(0,VALOR_CUATRO);
         de                  = TablaValores.getValor(TABLA_MULTILINEA, "emailMultilinea", "desc");
         direccionOrigen     = TablaValores.getValor(TABLA_MULTILINEA, "emailsEjecutivoFrom", "desc");
         direccionDestino    = datosCorreo.getMailEmpresa();
         String modulo = TablaValores.getValor(TABLA_MULTILINEA, "modulo", "moduloAvance");
         asunto              = modulo + TablaValores.getValor(TABLA_MULTILINEA, datosCorreo.getAsunto(), "desc");
         asunto              = asunto + " - "+ datosCorreo.getRut() + "-" + datosCorreo.getDv() + " - " + datosCorreo.getRazonSocial();
         estado              = datosCorreo.getEstado().trim().equals("") ? " " : TablaValores.getValor(
         		TABLA_MULTILINEA, datosCorreo.getEstado(), "desc");

         cuerpoDelEmail      = armaEmailParaDestinatarioContacto(
         		datosCorreo, codBanca,fechaPrimerVencimiento
         		, montoCreditoCorreo,estado);

         enviaMensajeCorreo(de, direccionOrigen, direccionDestino, asunto, cuerpoDelEmail, firma);
         if (getLogger().isDebugEnabled()) {
         	getLogger().debug("[enviarEmailContactoEmpresa] [BCI_FINOK]");
         }
     }
     
     /**
 	 * Metodo que arma mensaje de email para Destinatario contacto.
 	 *
 	 *
 	 * Registro de versiones:
 	 * <ul>
 	 * <li>1.0 09/08/2016 Manuel Escárate(BEE) - Felipe Ojeda. (ing.Soft.BCI) ) : Version Inicial</li>
 	 * </ul>
 	 * <p>
 	 *
 	 * @param datosCorreo datos para correo.
 	 * @param codBancaCorreo codigo banca.
 	 * @param fechaPrimerVcto fecha primer vencimiento.
 	 * @param montoCredito Monto del credito.
 	 * @param estado estado.
 	 * @throws GeneralException excepcion general.
 	 * @return curepo de correo.
 	 * @since 1.3
 	 */
 	public String armaEmailParaDestinatarioContacto(DatosParaCorreoTO datosCorreo, String codBancaCorreo,
 			String fechaPrimerVcto, String montoCredito, String estado) throws GeneralException {
 		if (getLogger().isDebugEnabled()) {
 			getLogger().debug("[armaEmailParaDestinatarioContacto] [BCI_INI]");
 		}
 		try {
 			String fechaHora = FechasUtil.fechaActualFormateada("dd/MM/yyyy")
 					+ " - " + FechasUtil.fechaActualFormateada("HH:mm:ss");
 			String fechaActual =  FechasUtil.fechaActualFormateada("dd/MM/yyyy");
 			StringBuffer cuerpoDelEmail;
 			String numeroOperacion = datosCorreo.getCaiOperacion()
 					+ datosCorreo.getIicOperacion();
 			String idc = datosCorreo.getRut() + " - " + datosCorreo.getDv();
 			if (getLogger().isDebugEnabled()) {
 				getLogger().debug("[armaEmailParaDestinatarioContacto] antes del archivo mail avales");
 			}
 			String archivoMail = "";
 			if (getLogger().isDebugEnabled()) {
 				getLogger().debug("[armaEmailParaDestinatarioContacto] [tipoDeCreditoSeleccionado]" + datosCorreo.getTipoOperacion());
 			}
 			if (datosCorreo.getTipoOperacion().equals("AVC010")){
 				archivoMail = TablaValores.getValor("multilinea.parametros",
 						"archivoMailAvalesUnVencimientoContacto", "desc");
 			}
 			else if (datosCorreo.getTipoOperacion().equals("AVC721")){
 				archivoMail = TablaValores.getValor("multilinea.parametros",
 						"archivoMailAvalesCuotaFijaContacto", "desc");
 			}
 			
 			if (getLogger().isDebugEnabled()) {
 				getLogger().debug("archivo mail" + archivoMail);
 				getLogger().debug("armaEmailParaDestinatario - ...");
 			}
 			
 			String diaPrimerVenc = "";
 			String mesPrimerVenc = "";
 			String anioPrimerVenc = "";
 			if (fechaPrimerVcto != null && !fechaPrimerVcto.equals("")){
 				diaPrimerVenc = fechaPrimerVcto.substring(0,VALOR_DOS); 
 				mesPrimerVenc = fechaPrimerVcto.substring(VALOR_TRES,VALOR_CINCO);
 				anioPrimerVenc = fechaPrimerVcto.substring(VALOR_SEIS);      
 			}
 			
 			String diaUltimoVenc = "";
 			String mesUltimoVenc = "";
 			String anioUltimoVenc = "";
 			if (datosCorreo.getFechaUltimaCuota() != null && !datosCorreo.getFechaUltimaCuota().equals("")){
 				diaUltimoVenc = datosCorreo.getFechaUltimaCuota().substring(0,VALOR_DOS); 
 				mesUltimoVenc = datosCorreo.getFechaUltimaCuota().substring(VALOR_TRES,VALOR_CINCO);
 				anioUltimoVenc = datosCorreo.getFechaUltimaCuota().substring(VALOR_SEIS);      
 			}
 			
 			Map paramsTemplate = new HashMap();
 			paramsTemplate.put("${nombreEmpresa}", datosCorreo.getRazonSocial());
 			paramsTemplate.put("${fechaHora}", fechaHora);
 			paramsTemplate.put("${montoCredito}", montoCredito);
 			paramsTemplate.put("${totalVencimientos}",
 					String.valueOf(datosCorreo.getCuotaSeleccionada()));
 			paramsTemplate.put("${fechaPrimerVcto}", fechaPrimerVcto);
 			paramsTemplate.put("${numeroOperacion}", numeroOperacion);
 			paramsTemplate.put("${destinoCredito}",
 					datosCorreo.getDestinoDelCredito());
 			paramsTemplate.put("${estado}", estado);
 			paramsTemplate.put("${rutEmpresa}", idc);
 			paramsTemplate.put("${codBanca}", codBancaCorreo);
 			paramsTemplate.put("${montoEnPalabras}", datosCorreo.getMontoExpresadoEnPalabras());
 			paramsTemplate.put("${valorCuota}", formatearMonto(datosCorreo.getValorCuota(),0,"#,###"));
 			paramsTemplate.put("${diaPrimerVen}", diaPrimerVenc);
 			paramsTemplate.put("${mesPrimerVen}", mesPrimerVenc);
 			paramsTemplate.put("${anioPrimerVen}", anioPrimerVenc);
 			paramsTemplate.put("${tasaInteres}", datosCorreo.getTasaInteres());
 			paramsTemplate.put("${comunaEmpresa}",datosCorreo.getComunaEmpresa());	
 			paramsTemplate.put("${ciudadEmpresa}",datosCorreo.getCiudadEmpresa());	
 			paramsTemplate.put("${direccionEmpresa}",datosCorreo.getDireccionEmpresa());
 			paramsTemplate.put("${diaUltimoVen}", diaUltimoVenc);
 			paramsTemplate.put("${mesUltimoVen}", mesUltimoVenc);
 			paramsTemplate.put("${anioUltimoVen}", anioUltimoVenc);
 			paramsTemplate.put("${diaVencimiento}", String.valueOf(datosCorreo.getDiaVencimiento()));
 			paramsTemplate.put("${impuestos}", formatearMonto(datosCorreo.getImpuestos(),0,"#,###"));
 			paramsTemplate.put("${seguros}", formatearMonto(datosCorreo.getSeguros(),0,"#,###"));
 			paramsTemplate.put("${valorNotario}", formatearMonto(datosCorreo.getValorNotario(),0,"#,###"));
 			paramsTemplate.put("${glosasAvales}", datosCorreo.getGlosasAvales());
 			paramsTemplate.put("${textosParaAvales}", datosCorreo.getTextosParaAvales());
 			paramsTemplate.put("${fechaActual}", fechaActual);
 			cuerpoDelEmail = plantillaCorreo(paramsTemplate, archivoMail);

 			if (getLogger().isDebugEnabled()) {
 				getLogger().debug("[armaEmailParaDestinatarioContacto] - ANTECEDENTES DEL CREDITO");
 				getLogger().debug("[armaEmailParaDestinatarioContacto] - Fecha y Hora de la Solicitud   : "
 						+ fechaHora);
 				getLogger().debug("[armaEmailParaDestinatarioContacto] - Monto del Credito              : "
 						+ montoCredito);
 				getLogger().debug("[armaEmailParaDestinatarioContacto] - Cuotas                         : "
 						+ datosCorreo.getCuotaSeleccionada());
 				getLogger().debug("[armaEmailParaDestinatarioContacto] - Fecha 1° vencimiento           : "
 						+ fechaPrimerVcto);
 				getLogger().debug("[armaEmailParaDestinatarioContacto] - Número de operacion            : "
 						+ numeroOperacion);
 				getLogger().debug("[armaEmailParaDestinatarioContacto] - Destino del credito            : "
 						+ datosCorreo.getDestinoDelCredito());
 				getLogger().debug("[armaEmailParaDestinatarioContacto] - Estado del Avance              : "
 						+ datosCorreo.getEstado());
 				getLogger().debug("[armaEmailParaDestinatarioContacto] - FIN armaEmailParaDestinatario");
 			}
 			if (getLogger().isDebugEnabled()) {
 				getLogger().debug("[armaEmailParaDestinatarioContacto] [BCI_FINOK]");
 			}
 			String cuerpoMail = "";
 			if(cuerpoDelEmail != null) {
 				cuerpoMail = cuerpoDelEmail.toString();
 			}
 			if (getLogger().isDebugEnabled()) {
 				getLogger().debug("[armaEmailParaDestinatarioContacto] [BCI_FINOK]");
 			}
 			return cuerpoMail;

 		} 
 		catch (Exception e) {
 			if (getLogger().isEnabledFor(Level.ERROR)) {
 				getLogger().error("[armaEmailParaDestinatarioContacto] Exception [BCI_FINEX] " + e.getMessage());
 			}
 			throw new GeneralException("UNKNOW", e.getMessage());
 		}

 	}
     
     /** 
      * Método que obtiene los seguros del credito.
      * <br>
      * Registro de versiones:<ul>
      * <li>1.0 16/06/2015 Braulio Rivas (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
      * </ul>
      * @param multiEnvironmentSeg multiambiente.
      * @param cai cai de la operación.
      * @param iic iic de la operación.
      * @return seguros del credito.
      * @since 1.3
      */    
     public ResultConsultaCgr obtenerSeguros(MultiEnvironment multiEnvironmentSeg, String cai, String iic){
    	 if (getLogger().isDebugEnabled()) {
    		 getLogger().debug("[obtenerSeguros] [BCI_INI]");
    	 }
    	 ResultConsultaCgr obeanCGRSGS = new ResultConsultaCgr();
    	 try{
    		 obeanCGRSGS =  crearEJBprecios().consultaCgr(multiEnvironmentSeg,
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
    		 if (getLogger().isDebugEnabled()) getLogger().debug("ERROR*** Exception [BCI_FINEX] [" + e.getMessage() + "]");
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
      * <li>1.0 29/07/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): versión inicial.</li> 
      * </ul>
      * @param multiEnvironmentGasto multiambiente.
      * @param cai cai de la operación.
      * @param iic iic de la operación.
      * @return seguros del credito.
      * @since 1.3
      */    
     public ResultConsultaCgr obtenerGastoNotarial(MultiEnvironment multiEnvironmentGasto, String cai, String iic){
     	 if (getLogger().isDebugEnabled()) {
              getLogger().debug("[obtenerGastoNotarial][" + clienteMB.getRut() + "][BCI_INI]");
         }
     	 ResultConsultaCgr obeanCGRCGN = new ResultConsultaCgr();
          try{
         	 obeanCGRCGN =  crearEJBprecios().consultaCgr(multiEnvironmentGasto,
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
              if (getLogger().isDebugEnabled()) getLogger().debug("[obtenerGastoNotarial] [BCI_FINEX] ERROR***Exception...Exception [" + e.getMessage() + "]");
          }
          if (getLogger().isDebugEnabled()) {
        	  getLogger().debug("[obtenerGastoNotarial] [BCI_FINOK]");
          }
          return obeanCGRCGN;
     }
     
     /**
      * retorna una instancia del EJB Precios.
      * <P>
      * Registro de versiones:
      * <ul>
      * <li> 1.0  10/08/2016 Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI): Versión inicial.</li>
      * </ul>
      * <p>
      * @return PreciosContextos instancia del ejb Multilinea.
      * @throws GeneralException en caso de error.
      * @since 1.3
      */
     private PreciosContextos crearEJBprecios() throws GeneralException {
    	 if (getLogger().isDebugEnabled()) {
    		 getLogger().debug("[crearEJBprecios][" + clienteMB.getRut() + "][BCI_INI]");
    	 }
    	 try {
         	PreciosContextos preciosContextos = null;
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("[crearEJBprecios] Obteniendo referencia del EJB " + JNDI_NAME_PRECIOS);
             }
             preciosContextos = ((PreciosContextosHome) EnhancedServiceLocator
                     .getInstance().getHome(JNDI_NAME_PRECIOS,
                     		PreciosContextosHome.class)).create();
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("[crearEJBprecios] Referencia obtenida a " + JNDI_NAME_PRECIOS);
             }
             if (getLogger().isDebugEnabled()) {
           	  getLogger().debug("[crearEJBprecios] [BCI_FINOK]");
             }
             return preciosContextos;
         }
         catch (Exception e) {
             if (getLogger().isEnabledFor(Level.ERROR)){
                 getLogger().error("[crearEJBprecios ] [BCI_FINEX] :  Error al crear instancia EJB", e);
             }
             throw new GeneralException("ESPECIAL", "Error al crear instancia EJB");
         }
     }    

    /**
     * <p>Método encargado de la journalización de un evento.</p>
     * <p>
     * Registro de versiones:
     * <ul>
     * 
     * <li>1.0 15/06/2015, Manuel Escárate  (BEE): versión inicial.</li>
     * </ul>
     * @param codEvento Código del evento.
     * @param subCodEvento Sub código del evento.
     * @param producto Código del producto.
     * 
     * @since 1.0
     */
    public void journalizar(String codEvento, String subCodEvento, String producto) {
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[journalizar] ["+usuarioModelMB.getRut()
    				+"] codEvento [" + codEvento + "]   subCodEvento ["+subCodEvento+"]   producto ["+producto+"]");
    	}
    	try{
    		HashMap datos = new HashMap();
    		datos.put("codEventoNegocio", codEvento);
    		datos.put("subCodEventoNegocio", subCodEvento);
    		datos.put("idProducto", producto);

    		Journalist.getInstance().publicar(datos);

    		if(getLogger().isEnabledFor(Level.DEBUG)){
    			getLogger().debug("[journalizacion][" + usuarioModelMB.getRut() + "] Journalizacion OK");
    		}
    	}
    	catch(Exception e){
    		if(getLogger().isEnabledFor(Level.ERROR)){
    			getLogger().error("[journalizacion] [" + usuarioModelMB.getRut() + "] [Exception] " 
    					+"No Journalizo, mensaje=<" 
    					+ e.getMessage() + "> ",e);
    		}
    	}

    }
    

    /**
     * Método que permite obtener una instancia de Logger de la clase.
     *
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0  12/03/2015 Braulio Rivas S. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión inicial. </li>
     * </ul>
     * </p>
     * @return con la instancia del Logger.
     * @since 1.0
     */
    public Logger getLogger() {
        if (logger == null){
            logger = Logger.getLogger(this.getClass());
        }
        return logger;
    }
    
    /**
     * retorna una instancia del EJB Multilinea.
     * <P>
     * Registro de versiones:
     * <ul>
     * <li> 1.0  12/03/2015 Braulio Rivas S. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión inicial.</li>
     * </ul>
     * <p>
     * @return Multilinea instancia del ejb Multilinea.
     * @throws GeneralException en caso de error.
     * @since 1.0
     */
    private Multilinea crearEJBmultilinea() throws GeneralException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[crearEJBmultilinea] Inicio metodo");
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
            return multilineaBean;

        }
        catch (Exception e) {
            if (getLogger().isEnabledFor(Level.ERROR)){
                getLogger().error("RemoteException: Error al crear instancia EJB", e);
            }
            throw new GeneralException("ESPECIAL", "Error al crear instancia EJB");
        }
    }

    /**
     * Obtiene una instancia del EJB de ServiciosCliente
     *
     * <p>
     * Registro de Versiones : <ul>
     * <li>1.0 13/05/2016 Manuel Escárate R. (BEE) - Pablo Paredes (ing.Soft.BCI) : versión inicial.
     *
     * </ul>
     * 
     * @since 1.0
     */
    private void crearEJBServiciosCliente() {
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[crearEJBServiciosCliente] [BCI_INI]");
    	}    
    	ServiciosClienteHome clienteHome = null;
    	String jndiName = "wcorp.serv.clientes.ServiciosCliente";
        Class homeClass = ServiciosClienteHome.class;
        try {
        	if (getLogger().isDebugEnabled()) {
        		getLogger().debug("[createEJB]: recupera ServiciosCliente desde EnhancedServiceLocator1");
        	}
            clienteHome = (ServiciosClienteHome)
            EnhancedServiceLocator.getInstance().getHome(jndiName, homeClass);
            serviciosCliente = clienteHome.create();
            if (getLogger().isDebugEnabled()) {
            	getLogger().debug("[createEJB]: fin recuperacion ServiciosCliente");
            }
        }
        catch (Exception ex) {
            if(getLogger().isEnabledFor(Level.ERROR)){
            	getLogger().error("[createEJB]:[BCI_FINEX] No se pudo recuperar la instancia de :", ex);
            }
        }
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[crearEJBServiciosCliente] [BCI_FINOK]");
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

    public ServicioAutorizacionyFirmaModelMB getServicioAutorizacionyFirmaModelMB() {
        return servicioAutorizacionyFirmaModelMB;
    }

    public void setServicioAutorizacionyFirmaModelMB(
            ServicioAutorizacionyFirmaModelMB servicioAutorizacionyFirmaModelMB) {
        this.servicioAutorizacionyFirmaModelMB = servicioAutorizacionyFirmaModelMB;
    }

    public DatosParaFirmaTO getDatosParaFirmaTO() {

        return datosParaFirmaTO;
    }
    public void setDatosParaFirmaTO(DatosParaFirmaTO datosParaFirmaTO) {
        this.datosParaFirmaTO = datosParaFirmaTO;
    }
    public int getPaso() {
        return paso;
    }
    public void setPaso(int paso) {
        this.paso = paso;
    }
    public DatosOperacionTO getOperacionSeleccionada() {
        return operacionSeleccionada;
    }
    public void setOperacionSeleccionada(
            DatosOperacionTO operacionSeleccionada) {
        this.operacionSeleccionada = operacionSeleccionada;
    }
    public int getRespuestaFirma() {
        return respuestaFirma;
    }
    public void setRespuestaFirma(int respuestaFirma) {
        this.respuestaFirma = respuestaFirma;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    public MultiEnvironment getMultiEnvironment() {
        return multiEnvironment;
    }
    public void setMultiEnvironment(MultiEnvironment multiEnvironment) {
        this.multiEnvironment = multiEnvironment;
    }
    public ColaboradorModelMB getColaborador() {
        return colaborador;
    }
    public void setColaborador(ColaboradorModelMB colaborador) {
        this.colaborador = colaborador;
    }
    public SegundaClaveUIInput getSegundaClaveAplicativo() {
        return segundaClaveAplicativo;
    }
    public void setSegundaClaveAplicativo(SegundaClaveUIInput segundaClaveAplicativo) {
        this.segundaClaveAplicativo = segundaClaveAplicativo;
    }
    public UsuarioModelMB getUsuarioModelMB() {
        return usuarioModelMB;
    }
    public void setUsuarioModelMB(UsuarioModelMB usuarioModelMB) {
        this.usuarioModelMB = usuarioModelMB;
    }
    public HashMap<String, String> getGlosasTipoCredito() {
		return glosasTipoCredito;
	}

	public void setGlosasTipoCredito(HashMap<String, String> glosasTipoCredito) {
		this.glosasTipoCredito = glosasTipoCredito;
	}

	public String getErrorSegundaClave() {
		return errorSegundaClave;
	}

	public void setErrorSegundaClave(String errorSegundaClave) {
		this.errorSegundaClave = errorSegundaClave;
	}

	public String getCodOficina() {
		return codOficina;
	}

	public void setCodOficina(String codOficina) {
		this.codOficina = codOficina;
	}

	public String getCodEjecutivo() {
		return codEjecutivo;
	}

	public void setCodEjecutivo(String codEjecutivo) {
		this.codEjecutivo = codEjecutivo;
	}

	public String getCodBanca() {
		return codBanca;
	}

	public void setCodBanca(String codBanca) {
		this.codBanca = codBanca;
	}

	public String getCondicionGarantia() {
		return condicionGarantia;
	}

	public void setCondicionGarantia(String condicionGarantia) {
		this.condicionGarantia = condicionGarantia;
	}

	public double getTasaInteres() {
		return tasaInteres;
	}

	public void setTasaInteres(double tasaInteres) {
		this.tasaInteres = tasaInteres;
	}

	public CalendarioPago[] getCalendarioPago() {
		return calendarioPago;
	}

	public void setCalendarioPago(CalendarioPago[] calendarioPago) {
		this.calendarioPago = calendarioPago;
	}

	public int getLargoComuna() {
		return largoComuna;
	}

	public void setLargoComuna(int largoComuna) {
		this.largoComuna = largoComuna;
	}

	public boolean isMostrarCondicionesMandato() {
		return mostrarCondicionesMandato;
	}

	public void setMostrarCondicionesMandato(boolean mostrarCondicionesMandato) {
		this.mostrarCondicionesMandato = mostrarCondicionesMandato;
	}

	public String getDisclaimerMandato() {
		return disclaimerMandato;
	}

	public void setDisclaimerMandato(String disclaimerMandato) {
		this.disclaimerMandato = disclaimerMandato;
	}

	public boolean isAceptaCondicionesMandato() {
		return aceptaCondicionesMandato;
	}

	public void setAceptaCondicionesMandato(boolean aceptaCondicionesMandato) {
		this.aceptaCondicionesMandato = aceptaCondicionesMandato;
	}

	public double getMontoCorreo() {
		return montoCorreo;
	}

	public void setMontoCorreo(double montoCorreo) {
		this.montoCorreo = montoCorreo;
	}	
	
}