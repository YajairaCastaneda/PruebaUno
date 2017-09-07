package cl.bci.aplicaciones.productos.colocaciones.simulacion.mb;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import wcorp.model.seguridad.SessionBCI;
import wcorp.serv.direcciones.to.CiudadTO;
import wcorp.serv.direcciones.to.ComunaTO;
import wcorp.serv.direcciones.to.RegionTO;
import wcorp.util.ErroresUtil;
import wcorp.util.FechasUtil;
import wcorp.util.GeneralException;
import wcorp.util.NumerosUtil;
import wcorp.util.RUTUtil;
import wcorp.util.StringUtil;
import wcorp.util.TablaValores;

import cl.bci.aplicaciones.cliente.mb.ClienteMB;
import cl.bci.aplicaciones.colaborador.mb.ColaboradorModelMB;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.CoberturaSeguroTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.ConfiguracionSimuladorTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.GastosOperacionalesTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.InmobiliariaTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.ParametroSimulacionTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.ProductoHipotecarioTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.ProyectoTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.RangoCreditoFinanciamientoTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.ResultSimulaProcesoCHIPTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.ResultadoSimulacionTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.SeguroAdicionalTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.SimulacionPlazoTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.SimulacionTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.TablaTO;
import cl.bci.infraestructura.web.seguridad.autorizaciones.mb.AutorizadorUtilityMB;
import cl.bci.infraestructura.web.seguridad.util.ConectorStruts;
import cl.bci.infraestructura.web.util.mb.ResolutorResourceBundleMB;

/**
 * Backing ManagedBean que proporciona los atributos y acciones para la Vista
 * del Simulador Credito Hipotecario.
 * <p>
 * Registro de versiones:
 * <ul>
 * <li>1.0 (22/11/2013 Nicole Sanhueza.(Sermaluc)): versión inicial.</li>
 * <li>1.1 (05/01/2015 Nicole Sanhueza (Sermaluc)): se modificaron los metodos:
 * {@link #cargaInicial()}
 * {@link #cambiaRegion()}
 * {@link #cambiaCiudad()}
 * {@link #volverPasoUno()}
 * {@link #calculaSimulacion()}
 * {@link #recalculaSimulacion()}
 * {@link #cambiaProducto()} y se elimino el metodo 
 * obtieneProvinciaCiudad(int codCiudad), porque ya no es necesario y para 
 * reducir numero de lineas de la clase.</li>
 * <li>1.2 (21/04/2015 Nicole Sanhueza (Sermaluc)) - Harold Mora (Ing. BCI): se modificaron los metodos:
 * {@link #cambiaProducto()}
 * {@link #volverPasoUno()}</li>
 * <li>1.3 (04/06/2015 Nicole Sanhueza (Sermaluc Ltda.) - Harold Mora (Ing. BCI)): Se modifica los metodos:
 * {@link #calculaSimulacion()}
 * {@link #generarPDFSimulacion()} 
 * {@link #recalculaSimulacion()}
 * <li>1.3 07/04/2015 Jose Palma (TINet), Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): 
 * Para la integración del Simulador Hipotecario con el proceso CHIP BPMS se realizan los siguiente cambios.
 * <ul> Métodos modificados:
 * <li>{@link #calculaSimulacion()}</li>
 * <li>{@link #recalculaIndividuales()}</li>
 * <li>{@link #recalculaSimulacion()}</li>
 * </ul><ul> Métodos agregados:
 * <li>{@link #construyeTOResultSimulacionProcesoCHIP()}</li>
 * <li>{@link #encuentraBean(String)}</li>
 * <li>{@link #generarMensajeError(String, String)}</li>
 * <li>{@link #generarMensajeExito(String)}</li>
 * <li>{@link #iniciarGeneracionInstanciaProcesoCHIP()}</li>
 * <li>{@link #marcarSimulacionProcesoCHIP()}</li>
 * <li>{@link #obtieneDetalleCreditoTOCHIP(ResultSimulaProcesoCHIPTO)}</li>
 * <li>{@link #obtenerMensajeTablaErrores(String)}</li>
 * <li>{@link #guardarSimulacionProcesoCHIP()}</li>
 * </ul>
 * <li>1.3 (19/05/2015 Alfredo Parra. (Sermaluc Ltda.) - Alfonso Sanchez (Ing. BCI)): se agregan metodos:
 * {@link #cambiaProyecto()}
 * {@link #seleccionaProyecto()}, 
 * Se modifica los metodos:
 * {@link #cambiaAntiguedad()} 
 * {@link #calculaSimulacion()}
 * {@link #recalculaSimulacion()}
 * se agregan atributos, para definir la inmobiliaria y proyecto seleccionado, ademas de una lista de
 * proyectos</li>
 * </ul>
 * </p>
 * <p>
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 * </p>
 */

@ManagedBean
@ViewScoped
public class SimulacionCreditoHipotecarioBackingMB implements Serializable {

    /**
     * Serial de la clase.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Estado de la vivienda usada.
     */
    private static final String VIVIENDA_USADA = "Usada";
    
    /**
     * Estado de la vivienda nueva. 
     */
    private static final String VIVIENDA_NUEVA = "Nueva";
    
    /**
     * Origen Simulacion Everest.
     */
    private static final String ORIGEN_EVEREST = "EVST";

    /**
     * Origen Simulacion Telecanal.
     */
    private static final String ORIGEN_TELECANAL = "TLCL";
    
    /**
     * Tabla parametro de simulador chip.
     */
    private static final String SIM_CHIP = "SimCHIP.parametros";
    /**
     * Tabla parametro de hipotecarios.
     */
    private static final String HIPOTECARIOS = "hipotecarios.parametros";
    /**
     * Valor inicial plazo.
     */
    private static final int INICIAL_PLAZO = 5;

    /**
     * Valor inicial vencimiento.
     */
    private static final int INICIAL_VENCIMIENTO = 10;

    /**
     * Valor inicial gracia.
     */
    private static final int INICIAL_GRACIA = 2;

    /**
     * Valor porcentaje 100.
     */
    private static final int PORCENTAJE_CIEN = 100;

    /**
     * Valor inicial combo.
     */
    private static final int INICIAL_COMBO = 0;

    /**
     * Valor inicial vivienda.
     */
    private static final String INICIAL_VIVIENDA = "0001";

    /**
     * Edad mínima cliente.
     */
    private static final int EDAD_MINIMA = 18;

    /**
     * Edad máxima cliente.
     */
    private static final int EDAD_MAXIMA = 75;

    /**
     * Seguro Basico.
     */
    private static final int SEGURO_BASICO = 1;
    
    /**
     * Producto bciHome Universal.
     */
    private static final int PRODUCTO_UNIVERSAL = 29;
    
    /**
     * Atributo codigo producto Bci Paga la Mitad.
     */
    private static final int PAGA_LA_MITAD = 20;

    /**
     * Atributo codigo producto Bci Paga la Mitad Pesos.
     */
    private static final int PAGA_LA_MITAD_PESOS = 22;

    /**
     * Atributo codigo producto BciHome Extra (Tasa Fija).
     */
    private static final int PRODUCTO_TASA_FIJA = 23;

    /**
     * Atributo codigo producto.
     */
    private static final int PRODUCTO_TASA_FIJA_VARIABLE = 8;

    /**
     * Atributo con el inicio de meses de gracia.
     */
    private static final int INICIO_MESES_GRACIA = 2;
	
	/**
	 * Página vista error simulación.
	 */
	private static final String VISTA_ERROR = "vistaErrorSimulacion.jsf";

    /**
     * Atributo codigo Seguro Cesantia Involuntaria.
     */
    private static final int SEGURO_CESANTIA_INV = 7;

    /**
     * Atributo codigo Seguro Cesantia DS40.
     */
    private static final int SEGURO_CESANTIA_DS40 = 8;

    /**
     * Atributo codigo Seguro Cesantia Doble Proteccion.
     */
    private static final int SEGURO_CESANTIA_DOBLE_PROTECCION = 9;

    /**
     * Atributo codigo Seguro Cesantia DS01.
     */
    private static final int SEGURO_CESANTIA_DS01 = 10;

    /**
     * Ruta con los recursos de mensajes.
     */
    private static final String RECURSO_MENSAJE = "cl.bci.aplicaciones.productos."
            + "colocaciones.simulacion.hipotecario.simulacionCreditoHipotecario";

    /**
     * Tabla que contiene lo errores del banco.
     */
    private static final String TABLA_ERRORES = "errores.codigos";

    /**
     * Formato de las fechas.
     */
    private static final String FORMATO_FECHA = "dd/MM/yyyy";
    
	/**
	 * Log de la clase.
	 */
	private transient Logger logger = (Logger) Logger
			.getLogger(SimulacionCreditoHipotecarioBackingMB.class);

    /**
     * Atributo ManagedBean del Cliente.
     */
    @ManagedProperty(value = "#{clienteMB}")
    private ClienteMB clienteMB;

    /**
     * Atributo ManagedBean de SimulacionCreditoHipotecario.
     */
    @ManagedProperty(value = "#{simulacionCreditoHipotecarioModelMB}")
    private SimulacionCreditoHipotecarioModelMB simulacionCreditoHipotecarioModelMB;

    /**
     * Atributo ManagedBean de Informacion Geografica del Simulador Hipotecario.
     */
    @ManagedProperty(value = "#{informacionGeograficaHipotecarioSupportMB}")
    private InformacionGeograficaHipotecarioSupportMB informacionGeograficaHipotecarioSupportMB;

    /**
     * Atributo ManagedBean de los permisos del usuario.
     */
    @ManagedProperty(value = "#{autorizadorUtilityMB}")
    private AutorizadorUtilityMB autorizadorUtilityMB;
    
    
    /**
     * Atributo ManagedBean del Colaborador.
     */
    @ManagedProperty(value = "#{colaboradorModelMB}")
    private ColaboradorModelMB colaboradorModelMB;
    
    /**
     * Canal de la sesion.
     */
    private String canal;

    /**
     * Rut del Cliente.
     */
    private String rutCliente;

    /**
     * Digito verificador rut del cliente.
     */
    private char dvCliente;

    /**
     * Nombre del Cliente.
     */
    private String nombreCliente;

    /**
     * Fecha de nacimiento del Cliente.
     */
    private String fechaNacimientoCliente;

    /**
     * Edad maxima.
     */
    private short edadMaxima;

    /**
     * Edad minima.
     */
    private short edadMinima;

    /**
     * Porcentaje del Seguro de Desgravamen del Cliente.
     */
    private int porcentajeSegDesgravamenCliente;

    /**
     * Indicador de que existe coudeudor.
     */
    private boolean tieneCodeudor;

    /**
     * Fecha de nacimiento del Codeudor.
     */
    private String fechaNacimientoCodeudor;

    /**
     * Procentaje del Seguro de Desgravamen del Codeudor.
     */
    private int porcentajeSegDesgravamenCodeudor;

    /**
     * Inmobiliaria a la que pertenece la vivienda del Cliente.
     */
    private String inmobiliaria;

    /**
     * Proyecto al que pertenece la Vivienda del Cliente.
     */
    private String proyecto;

    /**
     * Dfl2.
     */
    private int dfl2;

    /**
     * Tipo de Vivienda del Cliente.
     */
    private String tipoVivienda;

    /**
     * Descripcion tipo de Vivienda del Cliente.
     */
    private String descripcionVivienda;

    /**
     * Seguro de la vivienda del cliente.
     */
    private int seguro;

    /**
     * Region seleccionada.
     */
    private String region;

    /**
     * Ciudad seleccionada.
     */
    private String ciudad;

    /**
     * Comuna seleccionada.
     */
    private String comuna;

    /**
     * Lista de TO de regiones de simulador de creditos.
     */
    private RegionTO[] regiones;

    /**
     * Lista de TO de ciudades de simulador de creditos.
     */
    private CiudadTO[] ciudadesRegion;

    /**
     * Lista de TO de comunas de simulador de creditos.
     */
    private ComunaTO[] comunasCiudad;

    /**
     * Material de la Vivienda.
     */
    private int materialAdobe;

    /**
     * Antiguedad de la Vivienda.
     */
    private String antiguedadVivienda;

    /**
     * Producto del credito.
     */
    private int producto;

    /**
     * Tasa del credito.
     */
    private double tasa;

    /**
     * Mes de gracia del credito.
     */
    private int mesGracia;

    /**
     * Destino del credito.
     */
    private int destino;

    /**
     * Precio de la vivienda en UF.
     */
    private double precioViviendaUF;

    /**
     * Subsidio en UF.
     */
    private double subsidioUF;

    /**
     * Cuota contado en UF.
     */
    private double cuotaContadoUF;

    /**
     * Credito en UF.
     */
    private double creditoUF;

    /**
     * Porcentaje de financiamiento.
     */
    private double porcentajeFinanciamiento;

    /**
     * Plazo del credito.
     */
    private int plazo;

    /**
     * Dia de vencimiento.
     */
    private int diaVencimiento;

    /**
     * Mes de exclusion.
     */
    private int mesExclusion;

    /**
     * Monto maximo en UF.
     */
    private double montoMaximoUF;

    /**
     * Monto minimo en UF.
     */
    private double montoMinimoUF;

    /**
     * Porcentaje maximo de financiamiento.
     */
    private double porcentajeFinancMax;

    /**
     * Porcentaje minimo de financiamiento.
     */
    private double porcentajeFinancMin;

    /**
     * Indicador de comparacion con credito de conveniencia.
     */
    private boolean deseaComparar;

    /**
     * Tasa de conveniencia del credito.
     */
    private double tasaConveniencia;

    /**
     * Producto de comparacion del credito.
     */
    private int productoComparacion;

    /**
     * Seguros Adicionales.
     */
    private int[] segurosAdicionales;

    /**
     * Borrador Escritura.
     */
    private double borradorEscritura;

    /**
     * Borrador Escritura Pesos.
     */
    private double borradorEscrituraPesos;

    /**
     * Conservador BRUF.
     */
    private double conservadorBRUF;

    /**
     * Conservador BRUF en pesos.
     */
    private double conservadorBRUFPesos;

    /**
     * Estudio de Titulos.
     */
    private double estudioTitulos;

    /**
     * Estudio de Titulos en pesos.
     */
    private double estudioTitulosPesos;

    /**
     * Gestoria.
     */
    private double gestoria;

    /**
     * Gestoria en pesos.
     */
    private double gestoriaPesos;

    /**
     * Impuesto en UF.
     */
    private double impuestoUF;

    /**
     * Impuesto en pesos.
     */
    private double impuestoPesos;

    /**
     * Monto del valor asegurable.
     */
    private double montoValorAsegurable;

    /**
     * Notaria.
     */
    private double notaria;

    /**
     * Notaria en pesos.
     */
    private double notariaPesos;

    /**
     * Variable que indica Paso de impresion.
     */
    private boolean pasoImpresion;

    /**
     * Variable que indica Paso Inicial.
     */
    private boolean pasoInicial;

    /**
     * Variable que indica Paso de Simulacion.
     */
    private boolean pasoSimulacion;

    /**
     * Producto de anios de tasa fija.
     */
    private int productoAnosTasaFija;

    /**
     * TO de configuracion de simulador de creditos.
     */
    private ConfiguracionSimuladorTO configuracionSimuladorTO;

    /**
     * Lista de TO de seguros adicionales de simulador de creditos.
     */
    private SeguroAdicionalTO[] segurosAdicionalesTO;

    /**
     * Lista de TO de seguros individuales simulador de creditos.
     */
    private SeguroAdicionalTO[] segurosIndividualesTO;

    /**
     * Lista de TO de meses de exclusion del credito.
     */
    private SelectItem[] mesesExclusion;

    /**
     * Producto seleccionado.
     */
    private ProductoHipotecarioTO productoSeleccionado;

    /**
     * Nombres de inmobiliarias.
     */
    private InmobiliariaTO[] inmobiliarias;

    /**
     * Lista de proyectos asociados a una inmobiliaria.
     */
    private ProyectoTO[] proyectos;

    /**
     * Inmobiliaria Seleccionada.
     */
    private InmobiliariaTO inmobiliariaSeleccionada;
      
    /**
     * Proyecto seleccionado.
     */
    private ProyectoTO proyectoSeleccionado;
    

    /**
     * Meses de gracia.
     */
    private SelectItem[] mesesGracia;

    /**
     * Productos hipotecarios de la simulacion.
     */
    private SelectItem[] productosHipotecarios;

    /**
     * Productos comparacion credito.
     */
    private ArrayList<ProductoHipotecarioTO> listaProductosComparacion;

    /**
     * Codigos de seguro de cesantia serviu.
     */
    private String codigosServiu;

    /**
     * Códigos de seguros de cesantia.
     */
    private String codigosCesantia;

    /**
     * Nombre mes.
     */
    private String nombreMes;

    /**
     * Tipo de financiamiento.
     */
    private int tipoFinanciamiento;

    /**
     * Indicador PAC.
     */
    private int suscribePac;

    /**
     * Moneda del producto de la simulacion.
     */
    private int productoMoneda;

    /**
     * Tasacion.
     */
    private double tasacion;

    /**
     * Tasacion en Pesos.
     */
    private double tasacionPesos;

    /**
     * Tasa de Comision.
     */
    private double tasaComision;

    /**
     * Tasa de Costo Fondo.
     */
    private double tasaCostoFondo;

    /**
     * Seguro individual de desgravamen.
     */
    private int seguroIndividualDesgravamen;

    /**
     * Seguro individual de incendio.
     */
    private int seguroIndividualIncendio;

    /**
     * Indicador Volver.
     */
    private int indVolver;

    /**
     * Textos de coberturas de seguro.
     */
    private CoberturaSeguroTO[] coberturasSeguros;
	
	/**
	 * Origen de la simulacion. Valores posibles: true --> everest, false -->
	 * telecanal.
	 */
	private boolean origenComercial;

 /**
     * Dividendo total uf para setear en pdf.
     */
    private double dividendoTotalUFPDF;
    
    /**
     * Dividendo total pesos para setear en pdf.
     */
    private double dividendoTotalPesosPDF;
    
    /**
     * Dividendo total paga la mitad para setear en pdf.
     */
    private double dividendoTotalPagaMitadPDF;
    
    /**
     * Dividendo total pesos paga la mitad para setear en pdf.
     */
    private double dividendoTotalPesosPagaMitadPDF;
	
    /**
     * Constructor de la clase.
     */
    public SimulacionCreditoHipotecarioBackingMB() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[SimulacionCreditoHipotecarioBackingMB]: Inicia constructor");
        }
    }

    /**
     * Clase get logger.
     * 
     * Registro de versiones:
     * <ul>
     * <li>1.0 02/02/2014 Nicole Sanhueza. (Sermaluc ltda.)): Version Inicial</li>
     * </ul>
     * 
     * @return logger logeo de la aplicacion.
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass());
        }
        return logger;
    }

    public ClienteMB getClienteMB() {
        return clienteMB;
    }

    public void setClienteMB(ClienteMB clienteMB) {
        this.clienteMB = clienteMB;
    }

    public SimulacionCreditoHipotecarioModelMB getSimulacionCreditoHipotecarioModelMB() {
        return simulacionCreditoHipotecarioModelMB;
    }

    public void setSimulacionCreditoHipotecarioModelMB(
        SimulacionCreditoHipotecarioModelMB simulacionCreditoHipotecarioModelMB) {
        this.simulacionCreditoHipotecarioModelMB = simulacionCreditoHipotecarioModelMB;
    }
    
    public InformacionGeograficaHipotecarioSupportMB getInformacionGeograficaHipotecarioSupportMB() {
        return informacionGeograficaHipotecarioSupportMB;
    }

    public void setInformacionGeograficaHipotecarioSupportMB(
        InformacionGeograficaHipotecarioSupportMB informacionGeograficaHipotecarioSupportMB) {
        this.informacionGeograficaHipotecarioSupportMB = informacionGeograficaHipotecarioSupportMB;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getRutCliente() {
        return rutCliente;
    }

    public void setRutCliente(String rutCliente) {
        this.rutCliente = rutCliente;
    }

    public char getDvCliente() {
        return dvCliente;
    }

    public void setDvCliente(char dvCliente) {
        this.dvCliente = dvCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getFechaNacimientoCliente() {
        return fechaNacimientoCliente;
    }

    public void setFechaNacimientoCliente(String fechaNacimientoCliente) {
        this.fechaNacimientoCliente = fechaNacimientoCliente;
    }

    public short getEdadMaxima() {
        return edadMaxima;
    }

    public void setEdadMaxima(short edadMaxima) {
        this.edadMaxima = edadMaxima;
    }

    public short getEdadMinima() {
        return edadMinima;
    }

    public void setEdadMinima(short edadMinima) {
        this.edadMinima = edadMinima;
    }

    public int getPorcentajeSegDesgravamenCliente() {
        return porcentajeSegDesgravamenCliente;
    }

    public void setPorcentajeSegDesgravamenCliente(int porcentajeSegDesgravamenCliente) {
        this.porcentajeSegDesgravamenCliente = porcentajeSegDesgravamenCliente;
    }

    public boolean isTieneCodeudor() {
        return tieneCodeudor;
    }

    public void setTieneCodeudor(boolean tieneCodeudor) {
        this.tieneCodeudor = tieneCodeudor;
    }

    public String getFechaNacimientoCodeudor() {
        return fechaNacimientoCodeudor;
    }

    public void setFechaNacimientoCodeudor(String fechaNacimientoCodeudor) {
        this.fechaNacimientoCodeudor = fechaNacimientoCodeudor;
    }

    public int getPorcentajeSegDesgravamenCodeudor() {
        return porcentajeSegDesgravamenCodeudor;
    }

    public void setPorcentajeSegDesgravamenCodeudor(int porcentajeSegDesgravamenCodeudor) {
        this.porcentajeSegDesgravamenCodeudor = porcentajeSegDesgravamenCodeudor;
    }

    public String getInmobiliaria() {
        return inmobiliaria;
    }

    public void setInmobiliaria(String inmobiliaria) {
        this.inmobiliaria = inmobiliaria;
    }

    public String getProyecto() {
        return proyecto;
    }

    public void setProyecto(String proyecto) {
        this.proyecto = proyecto;
    }

    public int getDfl2() {
        return dfl2;
    }

    public void setDfl2(int dfl2) {
        this.dfl2 = dfl2;
    }

    public String getTipoVivienda() {
        return tipoVivienda;
    }

    public void setTipoVivienda(String tipoVivienda) {
        this.tipoVivienda = tipoVivienda;
    }

    public String getDescripcionVivienda() {
        return descripcionVivienda;
    }

    public void setDescripcionVivienda(String descripcionVivienda) {
        this.descripcionVivienda = descripcionVivienda;
    }

    public int getSeguro() {
        return seguro;
    }

    public void setSeguro(int seguro) {
        this.seguro = seguro;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public RegionTO[] getRegiones() {
        return regiones;
    }

    public void setRegiones(RegionTO[] regiones) {
        this.regiones = regiones;
    }

    public CiudadTO[] getCiudadesRegion() {
        return ciudadesRegion;
    }

    public void setCiudadesRegion(CiudadTO[] ciudadesRegion) {
        this.ciudadesRegion = ciudadesRegion;
    }

    public ComunaTO[] getComunasCiudad() {
        return comunasCiudad;
    }

    public void setComunasCiudad(ComunaTO[] comunasCiudad) {
        this.comunasCiudad = comunasCiudad;
    }

    public int getMaterialAdobe() {
        return materialAdobe;
    }

    public void setMaterialAdobe(int materialAdobe) {
        this.materialAdobe = materialAdobe;
    }

    public String getAntiguedadVivienda() {
        return antiguedadVivienda;
    }

    public void setAntiguedadVivienda(String antiguedadVivienda) {
        this.antiguedadVivienda = antiguedadVivienda;
    }

    public int getProducto() {
        return producto;
    }

    public void setProducto(int producto) {
        this.producto = producto;
    }

    public double getTasa() {
        return tasa;
    }

    public void setTasa(double tasa) {
        this.tasa = tasa;
    }

    public int getMesGracia() {
        return mesGracia;
    }

    public void setMesGracia(int mesGracia) {
        this.mesGracia = mesGracia;
    }

    public int getDestino() {
        return destino;
    }

    public void setDestino(int destino) {
        this.destino = destino;
    }

    public double getPrecioViviendaUF() {
        return precioViviendaUF;
    }

    public void setPrecioViviendaUF(double precioViviendaUF) {
        this.precioViviendaUF = precioViviendaUF;
    }

    public double getSubsidioUF() {
        return subsidioUF;
    }

    public void setSubsidioUF(double subsidioUF) {
        this.subsidioUF = subsidioUF;
    }

    public double getCuotaContadoUF() {
        return cuotaContadoUF;
    }

    public void setCuotaContadoUF(double cuotaContadoUF) {
        this.cuotaContadoUF = cuotaContadoUF;
    }

    public double getCreditoUF() {
        return creditoUF;
    }

    public void setCreditoUF(double creditoUF) {
        this.creditoUF = creditoUF;
    }

    public double getPorcentajeFinanciamiento() {
        return porcentajeFinanciamiento;
    }

    public void setPorcentajeFinanciamiento(double porcentajeFinanciamiento) {
        this.porcentajeFinanciamiento = porcentajeFinanciamiento;
    }

    public int getPlazo() {
        return plazo;
    }

    public void setPlazo(int plazo) {
        this.plazo = plazo;
    }

    public int getDiaVencimiento() {
        return diaVencimiento;
    }

    public void setDiaVencimiento(int diaVencimiento) {
        this.diaVencimiento = diaVencimiento;
    }

    public int getMesExclusion() {
        return mesExclusion;
    }

    public void setMesExclusion(int mesExclusion) {
        this.mesExclusion = mesExclusion;
    }

    public double getMontoMaximoUF() {
        return montoMaximoUF;
    }

    public void setMontoMaximoUF(double montoMaximoUF) {
        this.montoMaximoUF = montoMaximoUF;
    }

    public double getMontoMinimoUF() {
        return montoMinimoUF;
    }

    public void setMontoMinimoUF(double montoMinimoUF) {
        this.montoMinimoUF = montoMinimoUF;
    }

    public double getPorcentajeFinancMax() {
        return porcentajeFinancMax;
    }

    public void setPorcentajeFinancMax(double porcentajeFinancMax) {
        this.porcentajeFinancMax = porcentajeFinancMax;
    }

    public double getPorcentajeFinancMin() {
        return porcentajeFinancMin;
    }

    public void setPorcentajeFinancMin(double porcentajeFinancMin) {
        this.porcentajeFinancMin = porcentajeFinancMin;
    }

    public boolean isDeseaComparar() {
        return deseaComparar;
    }

    public void setDeseaComparar(boolean deseaComparar) {
        this.deseaComparar = deseaComparar;
    }

    public double getTasaConveniencia() {
        return tasaConveniencia;
    }

    public void setTasaConveniencia(double tasaConveniencia) {
        this.tasaConveniencia = tasaConveniencia;
    }

    public int getProductoComparacion() {
        return productoComparacion;
    }

    public void setProductoComparacion(int productoComparacion) {
        this.productoComparacion = productoComparacion;
    }

    public int[] getSegurosAdicionales() {
        return segurosAdicionales;
    }

    public void setSegurosAdicionales(int[] segurosAdicionales) {
        this.segurosAdicionales = segurosAdicionales;
    }

    public double getBorradorEscritura() {
        return borradorEscritura;
    }

    public void setBorradorEscritura(double borradorEscritura) {
        this.borradorEscritura = borradorEscritura;
    }

    public double getConservadorBRUF() {
        return conservadorBRUF;
    }

    public void setConservadorBRUF(double conservadorBRUF) {
        this.conservadorBRUF = conservadorBRUF;
    }

    public double getEstudioTitulos() {
        return estudioTitulos;
    }

    public void setEstudioTitulos(double estudioTitulos) {
        this.estudioTitulos = estudioTitulos;
    }

    public double getGestoria() {
        return gestoria;
    }

    public void setGestoria(double gestoria) {
        this.gestoria = gestoria;
    }

    public double getImpuestoUF() {
        return impuestoUF;
    }

    public void setImpuestoUF(double impuestoUF) {
        this.impuestoUF = impuestoUF;
    }

    public double getMontoValorAsegurable() {
        return montoValorAsegurable;
    }

    public void setMontoValorAsegurable(double montoValorAsegurable) {
        this.montoValorAsegurable = montoValorAsegurable;
    }

    public double getNotaria() {
        return notaria;
    }

    public void setNotaria(double notaria) {
        this.notaria = notaria;
    }

    public boolean isPasoImpresion() {
        return pasoImpresion;
    }

    public void setPasoImpresion(boolean pasoImpresion) {
        this.pasoImpresion = pasoImpresion;
    }

    public boolean isPasoInicial() {
        return pasoInicial;
    }

    public void setPasoInicial(boolean pasoInicial) {
        this.pasoInicial = pasoInicial;
    }

    public boolean isPasoSimulacion() {
        return pasoSimulacion;
    }

    public void setPasoSimulacion(boolean pasoSimulacion) {
        this.pasoSimulacion = pasoSimulacion;
    }

    public int getProductoAnosTasaFija() {
        return productoAnosTasaFija;
    }

    public void setProductoAnosTasaFija(int productoAnosTasaFija) {
        this.productoAnosTasaFija = productoAnosTasaFija;
    }

    public ConfiguracionSimuladorTO getConfiguracionSimuladorTO() {
        return configuracionSimuladorTO;
    }

    public void setConfiguracionSimuladorTO(ConfiguracionSimuladorTO configuracionSimuladorTO) {
        this.configuracionSimuladorTO = configuracionSimuladorTO;
    }

    public SeguroAdicionalTO[] getSegurosAdicionalesTO() {
        return segurosAdicionalesTO;
    }

    public void setSegurosAdicionalesTO(SeguroAdicionalTO[] segurosAdicionalesTO) {
        this.segurosAdicionalesTO = segurosAdicionalesTO;
    }

    public SelectItem[] getMesesExclusion() {
        return mesesExclusion;
    }

    public void setMesesExclusion(SelectItem[] mesesExclusion) {
        this.mesesExclusion = mesesExclusion;
    }

    public ProductoHipotecarioTO getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProductoSeleccionado(ProductoHipotecarioTO productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
    }

    public InmobiliariaTO[] getInmobiliarias() {
        return inmobiliarias;
    }

    public void setInmobiliarias(InmobiliariaTO[] inmobiliarias) {
        this.inmobiliarias = inmobiliarias;
    }

    public ProyectoTO[] getProyectos() {
        return proyectos;
    }

    public void setProyectos(ProyectoTO[] proyectos) {
        this.proyectos = proyectos;
    }
    
    public InmobiliariaTO getInmobiliariaSeleccionada() {
        return inmobiliariaSeleccionada;
    }
    
    public void setInmobiliariaSeleccionada(InmobiliariaTO inmobiliariaSeleccionada) {
        this.inmobiliariaSeleccionada = inmobiliariaSeleccionada;
    }

    public ProyectoTO getProyectoSeleccionado() {
        return proyectoSeleccionado;
    }

    public void setProyectoSeleccionado(ProyectoTO proyectoSeleccionado) {
        this.proyectoSeleccionado = proyectoSeleccionado;
    }
    
    
    public SelectItem[] getMesesGracia() {
        return mesesGracia;
    }

    public void setMesesGracia(SelectItem[] mesesGracia) {
        this.mesesGracia = mesesGracia;
    }

    public SelectItem[] getProductosHipotecarios() {
        return productosHipotecarios;
    }

    public void setProductosHipotecarios(SelectItem[] productosHipotecarios) {
        this.productosHipotecarios = productosHipotecarios;
    }

    public ArrayList<ProductoHipotecarioTO> getListaProductosComparacion() {
        return listaProductosComparacion;
    }

    public void setListaProductosComparacion(
        ArrayList<ProductoHipotecarioTO> listaProductosComparacion) {
        this.listaProductosComparacion = listaProductosComparacion;
    }

    public String getCodigosServiu() {
        return codigosServiu;
    }

    public void setCodigosServiu(String codigosServiu) {
        this.codigosServiu = codigosServiu;
    }

    public String getCodigosCesantia() {
        return codigosCesantia;
    }

    public void setCodigosCesantia(String codigosCesantia) {
        this.codigosCesantia = codigosCesantia;
    }

    public String getNombreMes() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[getNombreMes]: inicio consulta.");
        }
        try {
            for (int i = 0; i < mesesExclusion.length; i++) {
                if (String.valueOf(mesExclusion).equals(mesesExclusion[i].getValue()))
                    nombreMes = mesesExclusion[i].getLabel();
            }
        }
        catch (Exception e) {
            getLogger().error("[getNombreMes]: excepción:" + ErroresUtil.extraeStackTrace(e));
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[getNombreMes]: fin consulta.");
        }
        return nombreMes;
    }

    public void setNombreMes(String nombreMes) {
        this.nombreMes = nombreMes;
    }

    public int getTipoFinanciamiento() {
        return tipoFinanciamiento;
    }

    public void setTipoFinanciamiento(int tipoFinanciamiento) {
        this.tipoFinanciamiento = tipoFinanciamiento;
    }

    public int getSuscribePac() {
        return suscribePac;
    }

    public void setSuscribePac(int suscribePac) {
        this.suscribePac = suscribePac;
    }

    public int getProductoMoneda() {
        return productoMoneda;
    }

    public void setProductoMoneda(int productoMoneda) {
        this.productoMoneda = productoMoneda;
    }

    public double getTasacion() {
        return tasacion;
    }

    public void setTasacion(double tasacion) {
        this.tasacion = tasacion;
    }

    public double getTasaComision() {
        return tasaComision;
    }

    public void setTasaComision(double tasaComision) {
        this.tasaComision = tasaComision;
    }

    public double getTasaCostoFondo() {
        return tasaCostoFondo;
    }

    public void setTasaCostoFondo(double tasaCostoFondo) {
        this.tasaCostoFondo = tasaCostoFondo;
    }

    public int getSeguroIndividualDesgravamen() {
        return seguroIndividualDesgravamen;
    }

    public void setSeguroIndividualDesgravamen(int seguroIndividualDesgravamen) {
        this.seguroIndividualDesgravamen = seguroIndividualDesgravamen;
    }

    public int getSeguroIndividualIncendio() {
        return seguroIndividualIncendio;
    }

    public void setSeguroIndividualIncendio(int seguroIndividualIncendio) {
        this.seguroIndividualIncendio = seguroIndividualIncendio;
    }

    public SeguroAdicionalTO[] getSegurosIndividualesTO() {
        return segurosIndividualesTO;
    }

    public void setSegurosIndividualesTO(SeguroAdicionalTO[] segurosIndividualesTO) {
        this.segurosIndividualesTO = segurosIndividualesTO;
    }
    
    public int getIndVolver() {
        return indVolver;
    }

    public void setIndVolver(int indVolver) {
        this.indVolver = indVolver;
    }

    public CoberturaSeguroTO[] getCoberturasSeguros() {
        return coberturasSeguros;
    }

    public void setCoberturasSeguros(CoberturaSeguroTO[] coberturasSeguros) {
        this.coberturasSeguros = coberturasSeguros;
    }

    public double getBorradorEscrituraPesos() {
        return borradorEscrituraPesos;
    }

    public void setBorradorEscrituraPesos(double borradorEscrituraPesos) {
        this.borradorEscrituraPesos = borradorEscrituraPesos;
    }

    public double getConservadorBRUFPesos() {
        return conservadorBRUFPesos;
    }

    public void setConservadorBRUFPesos(double conservadorBRUFPesos) {
        this.conservadorBRUFPesos = conservadorBRUFPesos;
    }

    public double getEstudioTitulosPesos() {
        return estudioTitulosPesos;
    }

    public void setEstudioTitulosPesos(double estudioTitulosPesos) {
        this.estudioTitulosPesos = estudioTitulosPesos;
    }

    public double getGestoriaPesos() {
        return gestoriaPesos;
    }

    public void setGestoriaPesos(double gestoriaPesos) {
        this.gestoriaPesos = gestoriaPesos;
    }

    public double getImpuestoPesos() {
        return impuestoPesos;
    }

    public void setImpuestoPesos(double impuestoPesos) {
        this.impuestoPesos = impuestoPesos;
    }

    public double getNotariaPesos() {
        return notariaPesos;
    }

    public void setNotariaPesos(double notariaPesos) {
        this.notariaPesos = notariaPesos;
    }

    public double getTasacionPesos() {
        return tasacionPesos;
    }

    public void setTasacionPesos(double tasacionPesos) {
        this.tasacionPesos = tasacionPesos;
    }    

    public boolean isOrigenComercial() {
        return origenComercial;
    }

    public void setOrigenComercial(boolean origenComercial) {
        this.origenComercial = origenComercial;
    }

    public AutorizadorUtilityMB getAutorizadorUtilityMB() {
        return autorizadorUtilityMB;
    }

    public void setAutorizadorUtilityMB(
            AutorizadorUtilityMB autorizadorUtilityMB) {
        this.autorizadorUtilityMB = autorizadorUtilityMB;
    }

    public double getDividendoTotalUFPDF() {
        return dividendoTotalUFPDF;
    }

    public void setDividendoTotalUFPDF(double dividendoTotalUFPDF) {
        this.dividendoTotalUFPDF = dividendoTotalUFPDF;
    }

    public double getDividendoTotalPesosPDF() {
        return dividendoTotalPesosPDF;
    }

    public void setDividendoTotalPesosPDF(double dividendoTotalPesosPDF) {
        this.dividendoTotalPesosPDF = dividendoTotalPesosPDF;
    }

    public double getDividendoTotalPagaMitadPDF() {
        return dividendoTotalPagaMitadPDF;
    }

    public void setDividendoTotalPagaMitadPDF(double dividendoTotalPagaMitadPDF) {
        this.dividendoTotalPagaMitadPDF = dividendoTotalPagaMitadPDF;
    }

    public double getDividendoTotalPesosPagaMitadPDF() {
        return dividendoTotalPesosPagaMitadPDF;
    }

    public void setDividendoTotalPesosPagaMitadPDF(double dividendoTotalPesosPagaMitadPDF) {
        this.dividendoTotalPesosPagaMitadPDF = dividendoTotalPesosPagaMitadPDF;
    }
 
    public ColaboradorModelMB getColaboradorModelMB() {
        return colaboradorModelMB;
    }

    public void setColaboradorModelMB(ColaboradorModelMB colaboradorModelMB) {
        this.colaboradorModelMB = colaboradorModelMB;
    }       
    /**
     * Método encargado de realizar la carga inicial de datos de la vista.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 21/11/2013 Nicole Sanhueza. (Sermaluc): version inicial.</li>
     * <li>1.1 14/01/2015 Nicole Sanhueza (Sermaluc): se modifica llamada
     * a servicio de regiones.</li>
     * <li>1.2 25/06/2015 Eduardo Mascayano (TInet) - Patricio Candia (Ing. Soft. BCI): Se agrega llamada
     * para inicializar datos del Colaborador de la sesión.</li>
     * </ul>
     * 
     * @since 1.0
     */
    public void cargaInicial() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cargaInicial] [" + canal + "] inicio consulta.");
        }
        try {
            if (canal == null) {
                SessionBCI sessionBci = ConectorStruts.getSessionBCI();
                canal = sessionBci.getCanal().getCanalID();
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "[cargaInicial] [" + canal + "] se setean datos basicos del cliente.");
                }
                clienteMB.setApellidoPaterno(null);
                clienteMB.setDatosBasicos();
                rutCliente = clienteMB.getFullRut();
                nombreCliente = clienteMB.getApellidoPaterno() + " "
                    + clienteMB.getApellidoMaterno() + " " + clienteMB.getNombres();
                fechaNacimientoCliente = null;
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "[cargaInicial]: obteniendo configuracion del canal.[" + rutCliente + "]");
                }
                configuracionSimuladorTO = simulacionCreditoHipotecarioModelMB
                    .obtieneConfiguracion(canal);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "[cargaInicial]: obteniendo seguros adicionales.[" + rutCliente + "]");
                }
                cargaAgrupacionProductos();
                segurosAdicionalesTO = simulacionCreditoHipotecarioModelMB
                    .obtieneSegurosAdicionales(canal);
                codigosServiu = TablaValores.getValor(SIM_CHIP, "SEGUROS_ADICIONALES_SERVIU",
                    "CODIGOS");
                codigosCesantia = TablaValores.getValor(SIM_CHIP, "SEGUROS_ADICIONALES_CESANTIA",
                    "CODIGOS");
                cargaMesesExclusion();
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "[cargaInicial]: obteniendo regiones y datos iniciales.[" + rutCliente
                            + "]");
                }
                porcentajeSegDesgravamenCliente = PORCENTAJE_CIEN;
                porcentajeSegDesgravamenCodeudor = PORCENTAJE_CIEN;
                dfl2 = INICIAL_COMBO;
                materialAdobe = INICIAL_COMBO;
                seguro = INICIAL_GRACIA;
                tipoVivienda = INICIAL_VIVIENDA;
                regiones = informacionGeograficaHipotecarioSupportMB.obtenerRegiones();
                
                setOrigenComercial(true);
                cambiaRegion();
                cambiaCiudad();
                cambiaProducto();
                setPasoInicial(true);
                setPasoSimulacion(false);
                
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "[cargaInicial]: obteniendo sesionBCI e inicializando ColaboradorModel.[" + rutCliente
                            + "]");
                }                
                colaboradorModelMB.setDatosColaborador(sessionBci.getColaborador().getUsuario());
            }
        }
        catch (Exception e) {
            getLogger().error("[cargaInicial]: excepcion:" + ErroresUtil.extraeStackTrace(e));
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cargaInicial] [" + canal + "] fin del metodo.");
        }
    }

    /**
     * Método encargado de filtrar los productos requeridos para el simulador.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 17/09/2014 Nicole Sanhueza. (Sermaluc): version inicial.</li>
     * </ul>
     * 
     * @since 1.0
     */
    private void cargaAgrupacionProductos() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cargaAgrupacionProductos] inicio del metodo.");
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[cargaAgrupacionProductos]: obteniendo lista de productos.");
        }
        ArrayList<ProductoHipotecarioTO> listaProductos = configuracionSimuladorTO
            .getListaProductos();
        int cantidadProductos = Integer.parseInt(TablaValores.getValor(SIM_CHIP, "productos",
            "CANTIDAD"));
        productosHipotecarios = new SelectItem[cantidadProductos];
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[cargaAgrupacionProductos]: filtrando productos desde tabla.");
        }
        for (int j = 0; j < productosHipotecarios.length; j++) {
            int codigoProducto = Integer.parseInt(TablaValores.getValor(SIM_CHIP, "producto" + j,
                "CODIGO"));
            for (int i = 0; i < listaProductos.size(); i++) {
                SelectItem item = null;
                if (listaProductos.get(i).getCodProducto() == codigoProducto) {
                    String descripProducto = TablaValores.getValor(SIM_CHIP, "producto" + j,
                        "GLOSA");
                    item = new SelectItem(listaProductos.get(i).getCodProducto(), descripProducto);
                    productosHipotecarios[j] = item;
                }
            }
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cargaAgrupacionProductos] fin del metodo.");
        }
    }

    /**
     * Método encargado de cargar meses de exclusion.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 17/09/2014 Nicole Sanhueza. (Sermaluc): versión inicial.</li>
     * </ul>
     * 
     * @since 1.0
     */
    private void cargaMesesExclusion() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cargaMesesExclusion] : inicio.");
        }
        String maxMesesExclusion = TablaValores.getValor(SIM_CHIP, "NUMERO_MESES_EXCLUSION", "MAX");
        mesesExclusion = new SelectItem[Integer.parseInt(maxMesesExclusion)];
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[cargaMesesExclusion]: obteniendo meses de exclusion desde tabla.");
        }
        for (int i = 0; i < Integer.parseInt(maxMesesExclusion); i++) {
            SelectItem item = new SelectItem(TablaValores.getValor(SIM_CHIP, "MES_EXCLUSION_" + i,
                "VALOR"), TablaValores.getValor(SIM_CHIP, "MES_EXCLUSION_" + i, "GLOSA"));
            mesesExclusion[i] = item;
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cargaMesesExclusion] : fin del metodo.");
        }
    }

    /**
     * <p>
     * Método que obtiene las ciudades dada la region seleccionada.
     * 
     * </p>
     * Registro de Versiones:
     * <ul>
     * <li>1.0 22/11/2013 Nicole Sanhueza (Sermaluc): versión inicial.</li>
     * <li>1.1 14/01/2015 Nicole Sanhueza (Sermaluc): se modifica llamada
     * a servicio de regiones y ciudades.</li>
     * </ul>
     * 
     * @since 1.0
     */
    public void cambiaRegion() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cambiaRegion] [" + region + "] inicio.");
        }
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[cambiaRegion]: obteniendo cuidades por region.");
            }
            this.ciudadesRegion = null;
            this.comunasCiudad = null;
            if (!region.equals("0")) {
                this.ciudadesRegion = informacionGeograficaHipotecarioSupportMB.obtenerCiudadesPorRegion(region);
            }
        }
        catch (Exception e) {
            getLogger().error(
                "[cambiaRegion] [" + region + "] excepcion: " + ErroresUtil.extraeStackTrace(e));
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cambiaRegion] [" + region + "] fin del metodo");
        }
    }
    
    /**
     * <p>
     * Método que obtiene las comunas dada la ciudad seleccionada.
     * 
     * </p>
     * Registro de Versiones:
     * <ul>
     * <li>1.0 22/11/2013 Nicole Sanhueza (Sermaluc): version inicial.</li>
     * <li>1.1 14/01/2015 Nicole Sanhueza (Sermaluc): se modifica llamada
     * a servicio de comunas y ciudades.</li>
     * </ul>
     * 
     * @since 1.0
     */
    public void cambiaCiudad() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cambiaCiudad] [" + ciudad + "] inicio.");
        }
        try {
            this.comunasCiudad = null;
            if (!ciudad.equals("0")) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "[cambiaCiudad] [" + ciudad + "] obteniendo comunas por ciudad.");
                }
                comunasCiudad = informacionGeograficaHipotecarioSupportMB.obtenerComunasPorCiudad(ciudad);
            }
        }
        catch (Exception e) {
            getLogger().error(
                "[cambiaCiudad] [" + ciudad + "] excepcion: " + ErroresUtil.extraeStackTrace(e));
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cambiaCiudad] [" + ciudad + "] fin del metodo.");
        }
    }

    /**
     * <p>
     * Método que obtiene las comunas dada la ciudad seleccionada.
     * 
     * </p>
     * Registro de Versiones:
     * <ul>
     * <li>1.0 22/11/2013 Nicole Sanhueza (Sermaluc): version inicial.</li>
     * <li>1.3 01/06/2015 Alfredo Parra (Sermaluc Ltda.) - Alfonso Sanchez (Ing. BCI): se modifica metodo, se restablecen en null
     * los valores de inmobiliaria y proyecto seleccionado</li>
     * </ul>
     * 
     * @since 1.0
     */
    public void cambiaAntiguedad() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cambiaAntiguedad]  inicio.");
        }
        try {
            inmobiliarias = null;
            if (antiguedadVivienda.equals("T1")) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[cambiaAntiguedad]  obteniendo inmobiliarias.");
                }
                if (inmobiliarias == null) {
                    inmobiliarias = simulacionCreditoHipotecarioModelMB.obtieneInmobiliarias(canal);
                }
            }
            else {
                inmobiliaria = "0";
                inmobiliariaSeleccionada = null;
                proyectoSeleccionado = null;
            }
        }
        catch (Exception e) {
            getLogger().error("[cambiaAntiguedad] excepcion: " + ErroresUtil.extraeStackTrace(e));
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cambiaAntiguedad]  fin del metodo.");
        }
    }

    /**
     * <p>
     * Método para obtener los proyectos asociados a una inmobiliaria.
     * 
     * </p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 22/05/2015 Alfredo Parra. (Sermaluc Ltda.) - Alfonso Sanchez (Ing. BCI): 
     * version inicial.</li>
     * </ul>
     *
     * @since 1.3
     */
    public void cambiaProyecto() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cambiaProyecto] [" + inmobiliaria + "] inicio.");
        }
        try {
            proyectos = null;
            proyecto = "0";
            int cantInmobiliarias;
            if (!inmobiliaria.equals("0")) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[cambiaProyecto] [" + inmobiliaria + "] obteniendo proyectos.");
                }
                if (inmobiliarias != null) {
                    cantInmobiliarias = inmobiliarias.length;
                    for (int i = 0; i < cantInmobiliarias; i ++){
                        if (inmobiliarias[i].getCodInmobiliaria().equals(inmobiliaria)){
                            inmobiliariaSeleccionada = new InmobiliariaTO();
                            proyectos = inmobiliarias[i].getProyectosInmobiliaria();
                            inmobiliariaSeleccionada.setCodInmobiliaria(inmobiliarias[i].getCodInmobiliaria());
                            inmobiliariaSeleccionada.setNombreInmobiliaria(
                                inmobiliarias[i].getNombreInmobiliaria());
                            inmobiliariaSeleccionada.setProyectosInmobiliaria(inmobiliarias[i].getProyectosInmobiliaria());
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            getLogger().error("[cambiaProyecto] [" + inmobiliaria + "] excepcion: " + ErroresUtil.extraeStackTrace(e));
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cambiaProyecto] [" + inmobiliaria + "]  fin del metodo.");
        }
    }
    
    /**
     * <p>
     * Método que determina el proyecto seleccionado .
     * 
     * </p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 29/05/2015 Alfredo Parra. (Sermaluc Ltda.) - Alfonso Sanchez (Ing. BCI): 
     * version inicial.</li>
     * </ul>
     *
     * @since 1.3
     */
    public void seleccionaProyecto(){
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[seleccionaProyecto] [" + proyecto + "] inicio.");
        }
        try {
            ProyectoTO[] listaProyectos = null;
            proyectoSeleccionado = null;
            if(inmobiliariaSeleccionada != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "[seleccionaProyecto] [" + proyecto + "] obteniendo proyecto.");
                }
                listaProyectos = inmobiliariaSeleccionada.getProyectosInmobiliaria();
                for (int i = 0; i < listaProyectos.length; i ++){
                    if (proyecto != "0"){
                        if(listaProyectos[i].getCodProyecto().equals(proyecto)){
                            proyectoSeleccionado = new ProyectoTO();
                            proyectoSeleccionado.setCodProyecto(listaProyectos[i].getCodProyecto());
                            proyectoSeleccionado.setDescripcionProyecto(
                                listaProyectos[i].getDescripcionProyecto());
                            proyectoSeleccionado.setConvenioProyecto(
                                listaProyectos[i].getConvenioProyecto());
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            getLogger().error("[seleccionaProyecto] [" + proyecto + "] excepcion: " + ErroresUtil.extraeStackTrace(e));
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[seleccionaProyecto] [" + proyecto + "] fin del metodo.");
        }
    }
    
    /**
     * <p>
     * Metodo que carga las caracteristicas del producto hipotecario
     * seleccionado.
     * 
     * </p>
     * Registro de Versiones:
     * <ul>
     * <li>1.0 26/11/2013 Nicole Sanhueza (Sermaluc): version inicial.</li>
     * <li>1.1 23/01/2015 Nicole Sanhueza (Sermaluc): Se agrega validacion
     * para meses de gracia de productos paga la mitad.</li>
     * <li>1.2 21/04/2015 Nicole Sanhueza (Sermaluc) - Harold Mora (Ing. BCI): Se modifica los meses
     * de gracia para que ya no contengan el valor 0 y se valida que se obtengan
     * solo cuando el indice de gracia venga en true.</li>
     * </ul>
     * 
     * @since 1.0
     */
    public void cambiaProducto() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cambiaProducto] [" + producto + "] inicio.");
        }
        try {
            if (producto != 0 && antiguedadVivienda != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "[cambiaProducto] [" + producto + "] obteniendo producto seleccionado.");
                }
                productoSeleccionado = obtieneCorrespondenciaProductoAgrupacion(producto,
                    productoMoneda, productoAnosTasaFija);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "[cambiaProducto] [" + producto
                            + "] obteniendo lista de productos de comparacion.");
                }
                listaProductosComparacion = simulacionCreditoHipotecarioModelMB
                    .obtieneProductosComparacion(configuracionSimuladorTO.getListaProductos(),
                        canal);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "[cambiaProducto] [" + producto
                            + "] seteando rangos de credito y financiamiento.");
                }
                ArrayList<RangoCreditoFinanciamientoTO> rangosCredito = productoSeleccionado
                    .getRangoCreditoFinanciamiento();
                if (rangosCredito.size() == 1) {
                    montoMinimoUF = rangosCredito.get(0).getMontoMinCredito();
                    montoMaximoUF = rangosCredito.get(0).getMontoMaxCredito();
                    porcentajeFinancMin = rangosCredito.get(0).getPorcentajeMinFinanciamiento();
                    porcentajeFinancMax = rangosCredito.get(0).getPorcentajeMaxFinanciamiento();
                }
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "[cambiaProducto] [" + producto + "] obteniendo meses de gracia.");
                }
                if(productoSeleccionado.isIndGracia()){
                    mesesGracia = new SelectItem[productoSeleccionado.getMaxMesesGracia()-1];
                    for (int i = INICIO_MESES_GRACIA, j = 0; j < productoSeleccionado.getMaxMesesGracia() - 1; i++) {
                        SelectItem item = new SelectItem(i, String.valueOf(i));
                        mesesGracia[j] = item;
                        j++;
                    }
                }
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "[cambiaProducto] [" + producto + "] seteando datos iniciales producto.");
                }
                plazo = INICIAL_PLAZO;
                diaVencimiento = INICIAL_VENCIMIENTO;
                if(producto != PAGA_LA_MITAD && producto != PAGA_LA_MITAD_PESOS){
                    mesGracia = INICIAL_GRACIA;
                }
                else{
                    mesGracia = INICIAL_COMBO;
                }
            }
        }
        catch (Exception e) {
            getLogger()
                .error(
                    "[cambiaProducto] [" + producto + "] excepcion: "
                        + ErroresUtil.extraeStackTrace(e));
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cambiaPoducto] [" + producto + "] fin del metodo");
        }
    }

    /**
     * <p>
     * Metodo que obtiene la correspondencia del codigo de producto.
     * 
     * </p>
     * Registro de Versiones:
     * <ul>
     * <li>1.0 23/09/2014 Nicole Sanhueza (Sermaluc): version inicial.</li>
     * </ul>
     * 
     * @param codProductoAgrupado codigo producto seleccionado.
     * @param tipoMoneda tipo de moneda seleccionada.
     * @param anosTasa anios de tasa fija del producto.
     * @return ProductoHipotecarioTO producto correspondiente.
     * @since 1.0
     */
    private ProductoHipotecarioTO obtieneCorrespondenciaProductoAgrupacion(int codProductoAgrupado,
        int tipoMoneda, int anosTasa) {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[obtieneCorrespondenciaProductoAgrupacion] [" + codProductoAgrupado
                    + "] inicio del metodo.");
        }
        int codProducto = Integer.parseInt(TablaValores.getValor(SIM_CHIP, codProductoAgrupado
            + "-" + tipoMoneda + "-" + anosTasa, "CODIGO"));
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(
                "[obtieneCorrespondenciaProductoAgrupacion] [" + codProductoAgrupado
                    + "] obteniendo caracteristicas producto.");
        }
        return simulacionCreditoHipotecarioModelMB.cargaCaracteristicasProducto(codProducto,
            antiguedadVivienda, destino, String.valueOf(tipoVivienda), canal);
    }

    /**
     * <p>
     * Metodo para calcular la simulacion del credito.
     * 
     * </p>
     * Registro de Versiones:
     * <ul>
     * <li>1.0 23/09/2014 Nicole Sanhueza (Sermaluc): version inicial.</li>
     * <li>1.1 19/01/2015 Nicole Sanhueza (Sermaluc): Se modifica seteo de
     * variables region,ciudad y comuna.</li>
	 * <li>1.2 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): Se agrega el almacenamiento de los
	 * datos para el proceso CHIP.
     * <li>1.3 29/05/2015 Alfredo Parra (Sermaluc Ltda.) - Alfonso Sanchez (Ing. BCI): se modifica el seteo de 
     * variables inmobiliaria y proyecto</li>	 
     * <li>1.2 (04/06/2015 Nicole Sanhueza (Sermaluc Ltda.) - Harold Mora (Ing. BCI)): Se agrega
     * seteo de parametros de dividendos totales para manejo en impresion de PDF</li>
     * </ul>
     * 
     * @throws Exception excepcion en caso de error.
     * 
     * @since 1.0
     */
    public void calculaSimulacion() throws Exception {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[calculaSimulacion] inicio del metodo.");
        }
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[calculaSimulacion] seteando parametros de simulacion.");
            }
            ParametroSimulacionTO parametrosSimulacion = new ParametroSimulacionTO();
            parametrosSimulacion.setAntiguedadVivienda(antiguedadVivienda);
            parametrosSimulacion.setCanal(canal);
            parametrosSimulacion.setCiudad(Integer.parseInt(ciudad));
            parametrosSimulacion.setComuna(Integer.parseInt(comuna));
            parametrosSimulacion.setCreditoUF(creditoUF);
            parametrosSimulacion.setCuotaContadoUF(cuotaContadoUF);
            parametrosSimulacion.setDescripcionVivienda(descripcionVivienda);
            parametrosSimulacion.setDeseaComparar(deseaComparar);
            parametrosSimulacion.setDestino(destino);
            parametrosSimulacion.setDfl2(dfl2);
            parametrosSimulacion.setDiaVencimiento(diaVencimiento);
            Date fechaNacimientoCli = FechasUtil.convierteStringADate(fechaNacimientoCliente,
                new SimpleDateFormat("dd/MM/yyyy"));
            int edadCliente = FechasUtil.calculaEdad(fechaNacimientoCli);
            parametrosSimulacion.setSimulaIndividuales(false);
            if (edadCliente >= EDAD_MINIMA || edadCliente <= EDAD_MAXIMA) {
                parametrosSimulacion.setSimulaIndividuales(true);
            }
            parametrosSimulacion.setFechaNacimientoCliente(fechaNacimientoCli);
            Date fechaNacimientoCode = FechasUtil.convierteStringADate(fechaNacimientoCodeudor,
                new SimpleDateFormat("dd/MM/yyyy"));
            parametrosSimulacion.setFechaNacimientoCodeudor(fechaNacimientoCode);
            if(inmobiliariaSeleccionada != null) {
                parametrosSimulacion.setInmobiliaria(inmobiliariaSeleccionada.getNombreInmobiliaria());
                parametrosSimulacion.setCodigoInmobiliaria(inmobiliariaSeleccionada.getCodInmobiliaria());
            }
            else{
                parametrosSimulacion.setInmobiliaria(inmobiliaria);
                parametrosSimulacion.setCodigoInmobiliaria(inmobiliaria);
            }
            parametrosSimulacion.setMaterialAdobe(materialAdobe);
            parametrosSimulacion.setMesesGracia(mesGracia);
            parametrosSimulacion.setMesExclusion(mesExclusion);
            parametrosSimulacion.setMontoValorAsegurable(montoValorAsegurable);
            parametrosSimulacion.setNegociacion(false);
            parametrosSimulacion.setNombreCliente(nombreCliente);
            parametrosSimulacion.setPlazo(plazo);
            parametrosSimulacion.setPorcentajeFinanciamiento(porcentajeFinanciamiento);
            parametrosSimulacion
                .setPorcentajeSegDesgravamenCliente(porcentajeSegDesgravamenCliente);
            parametrosSimulacion
                .setPorcentajeSegDesgravamenCodeudor(porcentajeSegDesgravamenCodeudor);
            parametrosSimulacion.setPrecioViviendaUF(precioViviendaUF);
            int codProducto = Integer.parseInt(TablaValores.getValor(SIM_CHIP, producto + "-"
                + productoMoneda + "-" + productoAnosTasaFija, "CODIGO"));
            parametrosSimulacion.setProducto(codProducto);
            parametrosSimulacion.setProductoComparacion(productoComparacion);
            parametrosSimulacion.setProductoMoneda(productoMoneda);
            if (proyectoSeleccionado != null) {
                parametrosSimulacion.setProyecto(proyectoSeleccionado.getDescripcionProyecto());
                parametrosSimulacion.setCodigoProyecto(proyectoSeleccionado.getCodProyecto());
                parametrosSimulacion.setConvenioProyecto(proyectoSeleccionado.getConvenioProyecto());
            }
            else {
                parametrosSimulacion.setProyecto(proyecto);
                parametrosSimulacion.setCodigoProyecto(proyecto);
                parametrosSimulacion.setConvenioProyecto(proyecto);
            }
            parametrosSimulacion.setRegion(Integer.parseInt(region));
            long rut = RUTUtil.extraeRUT(rutCliente);
            char dv = RUTUtil.extraeDigitoVerificador(rutCliente);
            parametrosSimulacion.setRutCliente(rut);
            parametrosSimulacion.setDvCliente(dv);
            parametrosSimulacion.setSeguro(seguro);
            parametrosSimulacion.setSeguroIndividualDesgravamen(SEGURO_BASICO);
            parametrosSimulacion.setSeguroIndividualIncendio(SEGURO_BASICO);
            parametrosSimulacion.setSubsidioUF(subsidioUF);
            parametrosSimulacion.setSuscribePAC(suscribePac);
            parametrosSimulacion.setTasaComision(tasaComision);
            parametrosSimulacion.setTasaCostoFondo(tasaCostoFondo);
            parametrosSimulacion.setTipoCodeudor(tieneCodeudor);
            parametrosSimulacion.setTipoFinanciamiento(tipoFinanciamiento);
            parametrosSimulacion.setTipoVivienda(tipoVivienda);
            parametrosSimulacion.setSegurosAdicionales(segurosAdicionales);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(
                    "[calculaSimulacion][" + parametrosSimulacion.toString()
                        + "] invocando metodo para calcular simulacion.");
            }
            simulacionCreditoHipotecarioModelMB.calculaSimulacion(parametrosSimulacion, canal);
            ResultadoSimulacionTO resultadoSimulacion = simulacionCreditoHipotecarioModelMB
                .getResultadoSimulacionTO();
            double valorUF = simulacionCreditoHipotecarioModelMB.getConfiguracionSimuladorTO()
                .getValorUF();
            SimulacionTO simulacion = resultadoSimulacion.getSimulacionTO();
            borradorEscritura = simulacion.getGastosOperacionales().getBorradorEscritura()
                / valorUF;
            borradorEscrituraPesos = simulacion.getGastosOperacionales().getBorradorEscritura();
            estudioTitulos = simulacion.getGastosOperacionales().getEstudioTitulos() / valorUF;
            estudioTitulosPesos = simulacion.getGastosOperacionales().getEstudioTitulos();
            gestoria = simulacion.getGastosOperacionales().getGestoria() / valorUF;
            gestoriaPesos = simulacion.getGastosOperacionales().getGestoria();
            impuestoUF = simulacion.getGastosOperacionales().getImptoAlMutuo() / valorUF;
            impuestoPesos = simulacion.getGastosOperacionales().getImptoAlMutuo();
            conservadorBRUF = simulacion.getGastosOperacionales().getInscripcionConservador() / valorUF;
            conservadorBRUFPesos = simulacion.getGastosOperacionales().getInscripcionConservador();
            notaria = simulacion.getGastosOperacionales().getNotariales() / valorUF;
            notariaPesos = simulacion.getGastosOperacionales().getNotariales();
            tasacion = simulacion.getGastosOperacionales().getTasacion() / valorUF;
            tasacionPesos = simulacion.getGastosOperacionales().getTasacion();
            segurosIndividualesTO = obtieneSegurosIndividuales();
            dividendoTotalPesosPDF = resultadoSimulacion.getSimulacionPlazosTO().getDividendoTotalPesosConIndividuales();
            dividendoTotalUFPDF = resultadoSimulacion.getSimulacionPlazosTO().getDividendoTotalConIndividuales();
            if(parametrosSimulacion.getProducto() == PAGA_LA_MITAD || parametrosSimulacion.getProducto() == PAGA_LA_MITAD_PESOS){
                dividendoTotalPesosPagaMitadPDF = resultadoSimulacion.getSimulacionPlazosPagaLaMitadIndividualesTO().getDividendoTotalPesosConIndividuales();
                dividendoTotalPagaMitadPDF = resultadoSimulacion.getSimulacionPlazosPagaLaMitadIndividualesTO().getDividendoTotalConIndividuales();
            }
            seteaCoberturas();
            setPasoInicial(false);
            setPasoSimulacion(true);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[calculaSimulacion][" 
            + resultadoSimulacion.toString() + parametrosSimulacion.toString()
            + "] invocando metodo para guardar la simulacion del proceso CHIP.");
            }
            this.guardarSimulacionProcesoCHIP();
            if (getLogger().isEnabledFor(Level.INFO)) {
                getLogger().info("[calculaSimulacion] fin del metodo.");
            }
        }
        catch (Exception e) {
            getLogger().error("[calculaSimulacion] : error al obtener la simulacion.", e);
            FacesContext.getCurrentInstance().getExternalContext().redirect(VISTA_ERROR);
        }
    }

    /**
     * Metodo para obtener la lista de seguros individuales.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 13/12/2013 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * </ul>
     * 
     * @return SeguroAdicionalTO[] lista de seguros individuales.
     * @throws Exception excepcion en caso de error.
     * @since 1.0
     */
    public SeguroAdicionalTO[] obtieneSegurosIndividuales() throws Exception {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtieneSegurosIndividuales] : inicio.");
        }
        SeguroAdicionalTO[] listaSegurosIndividuales = null;
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger()
                    .debug("[obtieneSegurosIndividuales] : Obteniendo seguros individuales.");
            }
            String maxSegurosIndividuales = TablaValores.getValor(SIM_CHIP,
                "NUMERO_SEGURO_INDIVIDUALES", "MAX");
            listaSegurosIndividuales = new SeguroAdicionalTO[Integer
                .parseInt(maxSegurosIndividuales)];
            for (int i = 0; i < Integer.parseInt(maxSegurosIndividuales); i++) {
                listaSegurosIndividuales[i] = new SeguroAdicionalTO();
                listaSegurosIndividuales[i].setCodigo(TablaValores.getValor(SIM_CHIP,
                    "SEGUROS_INDIVIDUALES_" + i, "CODIGO"));
                listaSegurosIndividuales[i].setNombre(TablaValores.getValor(SIM_CHIP,
                    "SEGUROS_INDIVIDUALES_" + i, "GLOSA"));
                listaSegurosIndividuales[i].setTipo(TablaValores.getValor(SIM_CHIP,
                    "SEGUROS_INDIVIDUALES_" + i, "TIPO"));
            }
        }
        catch (Exception e) {
            getLogger()
                .error(
                    "[obtieneSegurosIndividuales] : error al obtener la lista de seguros individuales.");
            throw new Exception("[obtieneSegurosIndividuales]  excepcion: "
                + ErroresUtil.extraeStackTrace(e));
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtieneSegurosIndividuales]: fin del metodo.");
        }
        return listaSegurosIndividuales;
    }

    /**
     * Metodo para recalcular la tabla de seguros individuales.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 04/04/2014 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * <li>1.1 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): Se agrega el almacenamiento de los
     * datos para el proceso CHIP.
     * </ul>
     * 
     * @since 1.0
     */
    public void recalculaIndividuales() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[recalculaIndividuales]: inicio del metodo.");
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[recalculaIndividuales] :recalculando seguros individuales.");
        }
        simulacionCreditoHipotecarioModelMB.recalculaCAEIndividuales(seguroIndividualIncendio,
            seguroIndividualDesgravamen, canal);
            if (getLogger().isDebugEnabled()) {
            getLogger().debug("[recalculaIndividuales][seguro individual incendio["
            + seguroIndividualIncendio  + "], seguro individual desgravamen["
            + seguroIndividualDesgravamen + "], canal[" + canal
            + "] invocando metodo para guardar la simulacion del proceso CHIP.");
        }
        this.guardarSimulacionProcesoCHIP();
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[recalculaIndividuales]: fin del metodo.");
        }
    }

    /**
     * Metodo para recalcular una simulacion.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 07/04/2014 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * <li>1.1 19/01/2015 Nicole Sanhueza (Sermaluc): Se modifica seteo de
     * variables region,ciudad y comuna.</li>
     * <li>1.2 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): Se agrega el almacenamiento de los
     * datos para el proceso CHIP.
     * <li>1.3 29/05/2015 Alfredo Parra (Sermaluc Ltda.) - Alfonso Sanchez (Ing. BCI): se modifica seteo de variables
     * inmobiliaria y proyecto</li>
     * <li>1.2 (04/06/2015 Nicole Sanhueza (Sermaluc Ltda.) - Harold Mora (Ing. BCI)): Se agrega
     * seteo de parametros de dividendos totales para manejo en impresion de PDF</li>	 
     * </ul>
     * 
     * @since 1.0
     */
    public void recalculaSimulacion() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[recalculaSimulacion]: inicio.");
        }
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[recalculaSimulacion] seteando parametros de simulacion.");
            }
            ParametroSimulacionTO parametrosSimulacion = new ParametroSimulacionTO();
            parametrosSimulacion.setAntiguedadVivienda(antiguedadVivienda);
            parametrosSimulacion.setCanal(canal);
            parametrosSimulacion.setCiudad(Integer.parseInt(ciudad));
            parametrosSimulacion.setComuna(Integer.parseInt(comuna));
            parametrosSimulacion.setCreditoUF(creditoUF);
            parametrosSimulacion.setCuotaContadoUF(cuotaContadoUF);
            parametrosSimulacion.setDescripcionVivienda(descripcionVivienda);
            parametrosSimulacion.setDeseaComparar(deseaComparar);
            parametrosSimulacion.setDestino(destino);
            parametrosSimulacion.setDfl2(dfl2);
            parametrosSimulacion.setDiaVencimiento(diaVencimiento);
            Date fechaNacimientoCli = FechasUtil.convierteStringADate(fechaNacimientoCliente,
                new SimpleDateFormat("dd/MM/yyyy"));
            int edadCliente = FechasUtil.calculaEdad(fechaNacimientoCli);
            parametrosSimulacion.setSimulaIndividuales(false);
            if (edadCliente >= EDAD_MINIMA || edadCliente <= EDAD_MAXIMA) {
                parametrosSimulacion.setSimulaIndividuales(true);
            }
            parametrosSimulacion.setFechaNacimientoCliente(fechaNacimientoCli);
            Date fechaNacimientoCode = FechasUtil.convierteStringADate(fechaNacimientoCodeudor,
                new SimpleDateFormat("dd/MM/yyyy"));
            parametrosSimulacion.setFechaNacimientoCodeudor(fechaNacimientoCode);
            if (inmobiliariaSeleccionada != null) {
                parametrosSimulacion.setInmobiliaria(inmobiliariaSeleccionada.getNombreInmobiliaria());
                parametrosSimulacion.setCodigoInmobiliaria(inmobiliariaSeleccionada.getCodInmobiliaria());
            }
            else {
                parametrosSimulacion.setInmobiliaria(inmobiliaria);
                parametrosSimulacion.setCodigoInmobiliaria(inmobiliaria);
            }
            parametrosSimulacion.setMaterialAdobe(materialAdobe);
            parametrosSimulacion.setMesesGracia(mesGracia);
            parametrosSimulacion.setMesExclusion(mesExclusion);
            parametrosSimulacion.setMontoValorAsegurable(montoValorAsegurable);
            parametrosSimulacion.setNegociacion(true);
            parametrosSimulacion.setNombreCliente(nombreCliente);
            parametrosSimulacion.setPlazo(plazo);
            parametrosSimulacion.setPorcentajeFinanciamiento(porcentajeFinanciamiento);
            parametrosSimulacion
                .setPorcentajeSegDesgravamenCliente(porcentajeSegDesgravamenCliente);
            parametrosSimulacion
                .setPorcentajeSegDesgravamenCodeudor(porcentajeSegDesgravamenCodeudor);
            parametrosSimulacion.setPrecioViviendaUF(precioViviendaUF);
            int codProducto = Integer.parseInt(TablaValores.getValor(SIM_CHIP, producto + "-"
                + productoMoneda + "-" + productoAnosTasaFija, "CODIGO"));
            parametrosSimulacion.setProducto(codProducto);
            parametrosSimulacion.setProductoComparacion(productoComparacion);
            parametrosSimulacion.setProductoMoneda(productoMoneda);
            if(proyectoSeleccionado != null) {
                parametrosSimulacion.setProyecto(proyectoSeleccionado.getDescripcionProyecto());
                parametrosSimulacion.setCodigoProyecto(proyectoSeleccionado.getCodProyecto());
                parametrosSimulacion.setConvenioProyecto(proyectoSeleccionado.getConvenioProyecto());
            }
            else{
                parametrosSimulacion.setProyecto(proyecto);
                parametrosSimulacion.setCodigoProyecto(proyecto);
                parametrosSimulacion.setConvenioProyecto(proyecto);
            }
            parametrosSimulacion.setRegion(Integer.parseInt(region));
            long rut = RUTUtil.extraeRUT(rutCliente);
            char dv = RUTUtil.extraeDigitoVerificador(rutCliente);
            parametrosSimulacion.setRutCliente(rut);
            parametrosSimulacion.setDvCliente(dv);
            parametrosSimulacion.setSeguro(seguro);
            parametrosSimulacion.setSeguroIndividualDesgravamen(SEGURO_BASICO);
            parametrosSimulacion.setSeguroIndividualIncendio(SEGURO_BASICO);
            parametrosSimulacion.setSubsidioUF(subsidioUF);
            parametrosSimulacion.setSuscribePAC(suscribePac);
            parametrosSimulacion.setTasaComision(tasaComision);
            parametrosSimulacion.setTasaCostoFondo(tasaCostoFondo);
            parametrosSimulacion.setTipoCodeudor(tieneCodeudor);
            parametrosSimulacion.setTipoFinanciamiento(tipoFinanciamiento);
            parametrosSimulacion.setTipoVivienda(tipoVivienda);
            parametrosSimulacion.setSegurosAdicionales(segurosAdicionales);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(
                    "[recalculaSimulacion][" + parametrosSimulacion.toString()
                        + "] invocando metodo para recalcular simulacion.");
            }
            simulacionCreditoHipotecarioModelMB.calculaSimulacion(parametrosSimulacion, canal);
dividendoTotalPesosPDF = simulacionCreditoHipotecarioModelMB.getResultadoSimulacionTO().getSimulacionPlazosTO().getDividendoTotalPesosConIndividuales();
            dividendoTotalUFPDF = simulacionCreditoHipotecarioModelMB.getResultadoSimulacionTO().getSimulacionPlazosTO().getDividendoTotalConIndividuales();
            if(parametrosSimulacion.getProducto() == PAGA_LA_MITAD || parametrosSimulacion.getProducto() == PAGA_LA_MITAD_PESOS){
                dividendoTotalPesosPagaMitadPDF = simulacionCreditoHipotecarioModelMB.getResultadoSimulacionTO().getSimulacionPlazosPagaLaMitadIndividualesTO().getDividendoTotalPesosConIndividuales();
                dividendoTotalPagaMitadPDF = simulacionCreditoHipotecarioModelMB.getResultadoSimulacionTO().getSimulacionPlazosPagaLaMitadIndividualesTO().getDividendoTotalConIndividuales();
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[recalculaSimulacion]["
                + simulacionCreditoHipotecarioModelMB.getResultadoSimulacionTO()
                .toString() + parametrosSimulacion.toString()
                + "] invocando metodo para guardar la simulacion del proceso CHIP.");
            }
            this.guardarSimulacionProcesoCHIP();
        }
        catch (Exception e) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error(
                    "[recalculaSimulacion] excepción: "
                            + ErroresUtil.extraeStackTrace(e));
            }
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[recalculaSimulacion]: fin del metodo.");
        }
    }

    /**
     * Metodo para generar la impresion del pdf de simulacion del credito
     * hipotecario.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 02/02/2013 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * <li>1.1 (04/06/2015 Nicole Sanhueza (Sermaluc Ltda.) - Harold Mora (Ing. BCI)): Se agrega
     * seteo de parametros de dividendos totales para manejo en impresion de PDF</li>
     * </ul>
     * 
     * @since 1.0
     */
    public void generarPDFSimulacion() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[generarPDFSimulacion]: inicio.");
        }
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[generarPDFSimulacion]: seteando datos simulacion.");
            }
            ResultadoSimulacionTO resultadoSimulacion = simulacionCreditoHipotecarioModelMB
                .getResultadoSimulacionTO();
            SimulacionTO simulacion = resultadoSimulacion.getSimulacionTO();
            GastosOperacionalesTO gastosOperacionales = simulacion.getGastosOperacionales();
            gastosOperacionales.setBorradorEscritura(borradorEscritura);
            gastosOperacionales.setEstudioTitulos(estudioTitulos);
            gastosOperacionales.setGestoria(gestoria);
            gastosOperacionales.setImptoAlMutuo(impuestoUF);
            gastosOperacionales.setInscripcionConservador(conservadorBRUF);
            gastosOperacionales.setNotariales(notaria);
            gastosOperacionales.setTasacion(tasacion);
            GastosOperacionalesTO gastosOperacionalesPesos = new GastosOperacionalesTO();
            gastosOperacionalesPesos.setBorradorEscritura(borradorEscrituraPesos);
            gastosOperacionalesPesos.setEstudioTitulos(estudioTitulosPesos);
            gastosOperacionalesPesos.setGestoria(gestoriaPesos);
            gastosOperacionalesPesos.setImptoAlMutuo(impuestoPesos);
            gastosOperacionalesPesos.setInscripcionConservador(conservadorBRUFPesos);
            gastosOperacionalesPesos.setNotariales(notariaPesos);
            gastosOperacionalesPesos.setTasacion(tasacionPesos);
            simulacion.setGastosOperacionales(gastosOperacionales);
            resultadoSimulacion.setSimulacionTO(simulacion);
            resultadoSimulacion.setGastosOperacionalesPesos(gastosOperacionalesPesos);
            resultadoSimulacion.setNombreMesExclusion(nombreMes);
            resultadoSimulacion.getSimulacionPlazosIndividualesTO().setDividendoTotalPesosConIndividuales(dividendoTotalPesosPDF);
            resultadoSimulacion.getSimulacionPlazosIndividualesTO().setDividendoTotalConIndividuales(dividendoTotalUFPDF);
            if(simulacionCreditoHipotecarioModelMB.getParametrosSimulacion().getProducto() == PAGA_LA_MITAD || simulacionCreditoHipotecarioModelMB.getParametrosSimulacion().getProducto() == PAGA_LA_MITAD_PESOS){
                resultadoSimulacion.getSimulacionPlazosPagaLaMitadIndividualesTO().setDividendoTotalPesosConIndividuales(dividendoTotalPesosPagaMitadPDF);
                resultadoSimulacion.getSimulacionPlazosPagaLaMitadIndividualesTO().setDividendoTotalConIndividuales(dividendoTotalPagaMitadPDF);
            }

            simulacionCreditoHipotecarioModelMB.setResultadoSimulacionTO(resultadoSimulacion);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[generarPDFSimulacion]: creando pdf.");
            }
            SessionBCI sessionBci = ConectorStruts.getSessionBCI();
            simulacionCreditoHipotecarioModelMB.creaPDF(canal, coberturasSeguros,sessionBci);
        }
        catch (Exception e) {
            getLogger().error(
                "[generarPDFSimulacion] excepcion: " + ErroresUtil.extraeStackTrace(e));
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[generarPDFSimulacion]: fin del metodo.");
        }
    }

    /**
     * Metodo para volver al paso uno de la simulacion.
     * 
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 26/09/2014 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * <li>1.1 19/01/2015 Nicole Sanhueza. (Sermaluc): se agrega seteo de 
     * parametros.</li>
     * <li>1.2 21/04/2015 Nicole Sanhueza. (Sermaluc) - Harold Mora (Ing. BCI): se modifica seteo de 
     * variable mes de gracia.</li>
     * </ul>
     * 
     * @since 1.0
     */
    public void volverPasoUno() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[volverPasoUno]: inicio del metodo.");
        }
        setPasoInicial(true);
        setPasoSimulacion(false);
        setMesGracia(INICIAL_GRACIA);
        setIndVolver(SEGURO_BASICO);
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[volverPasoUno]: fin del metodo.");
        }
    }

    /**
     * Metodo para setear coberturas de la vista resultado.
     * 
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 03/10/2014 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * </ul>
     * 
     * @since 1.0
     */
    public void seteaCoberturas() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[seteaCoberturas]: inicio del metodo.");
        }
        int cantidadCoberturas = Integer.parseInt(TablaValores.getValor(SIM_CHIP, "Coberturas",
            "CANTIDAD"));
        coberturasSeguros = new CoberturaSeguroTO[cantidadCoberturas];
        CoberturaSeguroTO cobertura = null;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[seteaCoberturas]: obteniendo coberturas.");
        }
        for (int i = 0; i < cantidadCoberturas; i++) {
            cobertura = new CoberturaSeguroTO();
            cobertura.setDescripcionCobertura(TablaValores.getValor(SIM_CHIP, "cobertura" + i,
                "GLOSA"));
            cobertura.setSubDescripcionCobertura(TablaValores.getValor(SIM_CHIP, "cobertura" + i,
                "GLOSA2"));
            if (i >= PRODUCTO_UNIVERSAL) {
                cobertura.setDesgravamenColectivoLicitado(Integer.parseInt(TablaValores.getValor(
                    SIM_CHIP, "cobertura" + i, "VALOR4")));
                cobertura.setDesgravamenIndividualBasico(Integer.parseInt(TablaValores.getValor(
                    SIM_CHIP, "cobertura" + i, "VALOR3")));
                cobertura.setDesgravamenIndividualFull(Integer.parseInt(TablaValores.getValor(
                    SIM_CHIP, "cobertura" + i, "VALOR1")));
                cobertura.setDesgravamenIndividualPlus(Integer.parseInt(TablaValores.getValor(
                    SIM_CHIP, "cobertura" + i, "VALOR2")));
            }
            else {
                cobertura.setIncendioColectivoLicitado(Integer.parseInt(TablaValores.getValor(
                    SIM_CHIP, "cobertura" + i, "VALOR1")));
                cobertura.setIncendioIndividualBasico(Integer.parseInt(TablaValores.getValor(
                    SIM_CHIP, "cobertura" + i, "VALOR2")));
                cobertura.setIncendioIndividualHogar(Integer.parseInt(TablaValores.getValor(
                    SIM_CHIP, "cobertura" + i, "VALOR3")));
                cobertura.setIncendioAdicional(Integer.parseInt(TablaValores.getValor(SIM_CHIP,
                    "cobertura" + i, "VALOR4")));
            }
            if (i == 0) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[seteaCoberturas]: obteniendo porcentajes coberturas.");
                }
                cobertura.setPorcentajeIncendioColectivoLicitado(TablaValores.getValor(SIM_CHIP,
                    "PorcentajeIncendioColectivo", "PORCENTAJE"));
                cobertura.setPorcentajeIncendioIndividualBasico(TablaValores.getValor(SIM_CHIP,
                    "PorcentajeIncendioBasico", "PORCENTAJE"));
                cobertura.setPorcentajeIncendioIndividualHogar(TablaValores.getValor(SIM_CHIP,
                    "PorcentajeIncendioHogarSeguro", "PORCENTAJE"));
                cobertura.setPorcentajeDesgravamenColectivoLicitado(TablaValores.getValor(SIM_CHIP,
                    "PorcentajeDesgravamenColectivo", "PORCENTAJE"));
                cobertura.setPorcentajeDesgravamenIndividualFull(TablaValores.getValor(SIM_CHIP,
                    "PorcentajeDesgravamenFull", "PORCENTAJE"));
                cobertura.setPorcentajeDesgravamenIndividualBasico(TablaValores.getValor(SIM_CHIP,
                    "PorcentajeDesgravamenBasico", "PORCENTAJE"));
                cobertura.setPorcentajeDesgravamenIndividualPlus(TablaValores.getValor(SIM_CHIP,
                    "PorcentajeDesgravamenPlus", "PORCENTAJE"));
            }
            cobertura.setInicioFinIncendio(Integer.parseInt(TablaValores.getValor(SIM_CHIP,
                "InicioFinIncendio", "VALOR")));
            cobertura.setInicioFinIncendioContenido(Integer.parseInt(TablaValores.getValor(
                SIM_CHIP, "InicioFinIncendioContenido", "VALOR")));
            cobertura.setInicioFinExclusionIncendio(Integer.parseInt(TablaValores.getValor(
                SIM_CHIP, "InicioFinExclusionIncendio", "VALOR")));
            cobertura.setInicioFinExclusionContenido(Integer.parseInt(TablaValores.getValor(
                SIM_CHIP, "InicioFinExclusionContenido", "VALOR")));
            cobertura.setInicioFinDesgravamen(Integer.parseInt(TablaValores.getValor(SIM_CHIP,
                "InicioFinDesgravamen", "VALOR")));
            cobertura.setInicioFinExclusionDesgravamen(Integer.parseInt(TablaValores.getValor(
                SIM_CHIP, "InicioFinExclusionDesgravamen", "VALOR")));
            coberturasSeguros[i] = cobertura;
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[seteaCoberturas] : fin del metodo.");
        }
 	}
    
    /**
     * Metodo para iniciar la generacion de una instancia de proceso CHIP.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @since 1.3
     */
    public void iniciarGeneracionInstanciaProcesoCHIP() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger()
                    .info("[iniciaGeneracionInstanciaProcesoCHIP] [BCI_INI]: Backing.");
        }
        boolean poseeInstanciaVigente;
        try {
            poseeInstanciaVigente = simulacionCreditoHipotecarioModelMB
                    .poseeProcesoCHIPVigente(RUTUtil.extraeRUT(rutCliente));
            if (!poseeInstanciaVigente) {
                simulacionCreditoHipotecarioModelMB
                        .generarInstanciaProcesoCHIP(this
                                .construyeTOResultSimulacionProcesoCHIP());
                try {
                    simulacionCreditoHipotecarioModelMB.registrarInicioProcesoCHIP(RUTUtil.extraeRUT(rutCliente));
                }
                catch (GeneralException e) {
                    if (logger.isEnabledFor(Level.WARN)) {
                        logger.warn("[iniciarGeneracionInstanciaProcesoCHIP]" + " [GeneralException] [rut: "
                            + rutCliente
                            + "] [BCI_FINEX] : Error al registrar la fecha de inicio del proceso CHIP generado. ",
                            e);
                    }
                    String msg = obtenerMensajeTablaErrores("SIMCHIP006");
                    generarMensajeError(msg, null);                    
                }
                
                String key = "mensajeOKAceptarOferta";
                generarMensajeExito(key);
            } 
            else {
                String msg = obtenerMensajeTablaErrores("SIMCHIP004");
                generarMensajeError(msg, null);
            }
            
            if (getLogger().isEnabledFor(Level.INFO)) {
                getLogger()
                        .info("[iniciaGeneracionInstanciaProcesoCHIP] [BCI_FINOK]: Backing.");
            }           
        } 
        catch (GeneralException e) {
            if (logger.isEnabledFor(Level.ERROR)) {
                logger.error("[iniciarGeneracionInstanciaProcesoCHIP]" 
                        + " [GeneralException] [rut: " + rutCliente
                        + "] [BCI_FINEX] : Error al generar instancia CHIP. ",
                        e);
            }
            String msg = obtenerMensajeTablaErrores("SIMCHIP001");
            generarMensajeError(msg, null);
        }
        
    }

    /**
     * Metodo para obtener un ResultSimulaProcesoCHIPTO con los datos de la
     * simulacion.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @return Simulacion del Proceso CHIP.
     * @since 1.3
     */
    private ResultSimulaProcesoCHIPTO construyeTOResultSimulacionProcesoCHIP() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[construyeTOResultSimulacionProcesoCHIP] [BCI_INI]: Backing.");
        }
        ResultSimulaProcesoCHIPTO resultadoCHIP = new ResultSimulaProcesoCHIPTO();
        SimpleDateFormat sdf = new SimpleDateFormat(FORMATO_FECHA);
        ResultadoSimulacionTO resultadoSimulacion = simulacionCreditoHipotecarioModelMB.getResultadoSimulacionTO();

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[construyeTOResultSimulacionProcesoCHIP] [resultadoSimulacion] 1:" + resultadoSimulacion);
            getLogger().info(
                "[construyeTOResultSimulacionProcesoCHIP] [resultadoCHIP] 1:" + resultadoCHIP);            
        }

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[construyeTOResultSimulacionProcesoCHIP] asignarDatosSolicitante...");
        }
        this.asignarDatosSolicitante(resultadoCHIP, sdf);
        resultadoCHIP.setPorcentajeSegDesgravamenCliente(porcentajeSegDesgravamenCliente);

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[construyeTOResultSimulacionProcesoCHIP] asignarDatosCodeudor...");
        }
        this.asignarDatosCodeudor(resultadoCHIP, sdf);
        resultadoCHIP.setCodCanalCredito(null);
        resultadoCHIP.setGlosaCanalCredito(null);
        

        Date fechaSimulacion = resultadoSimulacion.getSimulacionTO().getDatosOperacion().getFecha();
        resultadoCHIP.setFechaSimulacion(fechaSimulacion);
        resultadoCHIP.setValorUF(configuracionSimuladorTO.getValorUF());
        resultadoCHIP.setResultadoFiltros(0);
        resultadoCHIP.setContactar(false);

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[construyeTOResultSimulacionProcesoCHIP] [resultadoSimulacion] 2:" + resultadoSimulacion);
            getLogger().info(
                "[construyeTOResultSimulacionProcesoCHIP] [resultadoCHIP] 2:" + resultadoCHIP);            
        }
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[construyeTOResultSimulacionProcesoCHIP] asignarDatosInmobiliaria...");
        }
        this.asignarDatosInmobiliaria(resultadoCHIP);
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[construyeTOResultSimulacionProcesoCHIP] asginarDatosVivienda...");
        }
        this.asginarDatosVivienda(resultadoCHIP);
        resultadoCHIP.setCodSeguroIncendioSismoOpcional(seguro);
        resultadoCHIP.setGlosaSeguroIncendioSismoOpcional(configuracionSimuladorTO.getTablaSeguros().get(seguro)
            .getDescripcion());

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[construyeTOResultSimulacionProcesoCHIP] asignarDatosGeograficos...");
        }
        this.asignarDatosGeograficos(resultadoCHIP);
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[construyeTOResultSimulacionProcesoCHIP] asignarDatosFinanciamiento...");
        }
        this.asignarDatosFinanciamiento(resultadoCHIP);
        
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[construyeTOResultSimulacionProcesoCHIP] [resultadoSimulacion] 3:" + resultadoSimulacion);
            getLogger().info(
                "[construyeTOResultSimulacionProcesoCHIP] [resultadoCHIP] 3:" + resultadoCHIP);            
        }
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[construyeTOResultSimulacionProcesoCHIP] asignarSegurosAdicionales...");
        }
        this.asignarSegurosAdicionales(resultadoCHIP, resultadoSimulacion);
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[construyeTOResultSimulacionProcesoCHIP] asignarSegurosIndividuales...");
        }
        this.asignarSegurosIndividuales(resultadoCHIP);
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[construyeTOResultSimulacionProcesoCHIP] asignarGastosOperacionales...");
        }
        this.asignarGastosOperacionales(resultadoCHIP, resultadoSimulacion);

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[construyeTOResultSimulacionProcesoCHIP] codEjecutivo, codCanalVenta, glosaCanalVenta...");
        }
        resultadoCHIP.setCodEjecutivo(colaboradorModelMB.getUsuario());
        resultadoCHIP.setCodCanalVenta(canal);
        resultadoCHIP.setGlosaCanalVenta(TablaValores.getValor(HIPOTECARIOS, canal, "Desc"));

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[construyeTOResultSimulacionProcesoCHIP] [resultadoSimulacion] 4:" + resultadoSimulacion);
            getLogger().info(
                "[construyeTOResultSimulacionProcesoCHIP] [resultadoCHIP] 4:" + resultadoCHIP);
            getLogger().info("[construyeTOResultSimulacionProcesoCHIP] obtieneDetalleCreditoTOCHIP...");
        }
        resultadoCHIP = this.obtieneDetalleCreditoTOCHIP(resultadoCHIP);
        resultadoCHIP.setGlosaOficinaEje(colaboradorModelMB.getNombreOficina());
        resultadoCHIP.setCodOficinaEje(colaboradorModelMB.getCodOficina());

        resultadoCHIP.setOrigenSimulacion(ORIGEN_TELECANAL);
        if (origenComercial) {
            resultadoCHIP.setOrigenSimulacion(ORIGEN_EVEREST);
        }

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[construyeTOResultSimulacionProcesoCHIP] [resultadoSimulacion] 5:" + resultadoSimulacion);            
        }

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[construyeTOResultSimulacionProcesoCHIP] [" + resultadoCHIP.toString()
                    + "] [BCI_FINOK]: Fin.");
        }

        return resultadoCHIP;
    }

    /**
     * Metodo para obtener el detalle de una simulacion colectiva o individual
     * para el proceso chip .
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @param resultSimulacionProcesoCHIP
     *            Simulacion del Proceso CHIP.
     * @return Simulacion del Proceso CHIP con el detalle de los costos de la
     *         simulacion.
     * @since 1.3
     */

    private ResultSimulaProcesoCHIPTO obtieneDetalleCreditoTOCHIP(
            ResultSimulaProcesoCHIPTO resultSimulacionProcesoCHIP) {
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtieneDetalleCreditoTOCHIP] [BCI_INI]: Inicio.");
            getLogger().info(
                "[obtieneDetalleCreditoTOCHIP] [resultSimulacionProcesoCHIP]:" + resultSimulacionProcesoCHIP);
        }  
        ResultadoSimulacionTO resultado = simulacionCreditoHipotecarioModelMB
                .getResultadoSimulacionTO();
        if (producto == PRODUCTO_TASA_FIJA_VARIABLE) {
            if (productoAnosTasaFija == 0) {
                resultSimulacionProcesoCHIP.setAnnosTasaFija(1);
            }
            else {
                resultSimulacionProcesoCHIP.setAnnosTasaFija(0);
                if (producto == PAGA_LA_MITAD) {
                    resultSimulacionProcesoCHIP.setMesesGracia(0);          
                } 
            }
        }
        resultSimulacionProcesoCHIP
                .setSimulacionPlazoPagaLaMitadColectivo(resultado
                        .getSimulacionPlazosPagaLaMitadTO());
        resultSimulacionProcesoCHIP
                .setSimulacionPlazoPagaLaMitadIndividual(resultado
                        .getSimulacionPlazosPagaLaMitadIndividualesTO());
        if (resultSimulacionProcesoCHIP
                .getSimulacionPlazoPagaLaMitadColectivo() == null) {
            resultSimulacionProcesoCHIP
                    .setSimulacionPlazoPagaLaMitadColectivo(new SimulacionPlazoTO());
            resultSimulacionProcesoCHIP
                    .setSimulacionPlazoPagaLaMitadIndividual(new SimulacionPlazoTO());
        }
        resultSimulacionProcesoCHIP.setSimulacionPlazoColectivo(resultado
                .getSimulacionPlazosTO());
        resultSimulacionProcesoCHIP.setSimulacionPlazoIndividual(resultado
                .getSimulacionPlazosIndividualesTO());
        resultSimulacionProcesoCHIP.setTasaCAEIndividual(NumerosUtil.redondearADosDecimales(resultado
                .getCalculoCAEIndividualesTO().getTasaCAE()));
        resultSimulacionProcesoCHIP
                .setCostoTotalCreditoIndividual(NumerosUtil.redondearADosDecimales(resultado
                        .getCalculoCAEIndividualesTO().getCostoTotal()));
        resultSimulacionProcesoCHIP.setTasaCAEColectivo(NumerosUtil.redondearADosDecimales(resultado
                .getCalculoCAETO().getTasaCAE()));
        resultSimulacionProcesoCHIP.setCostoTotalCreditoColectivo(NumerosUtil.redondearADosDecimales(
            resultado.getCalculoCAETO().getCostoTotal()));

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[obtieneDetalleCreditoTOCHIP] [" + resultSimulacionProcesoCHIP + "] [BCI_FINOK].");
        }
        return resultSimulacionProcesoCHIP;
    }

    /**
     * Metodo para indicar que el cliente desea ser contactado por un ejecutivo.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @since 1.3
     */
    public void marcarSimulacionProcesoCHIP() {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                    "[marcarSimulacionProcesoCHIP] [BCI_INI]: Backing.");
        }
        try {
            simulacionCreditoHipotecarioModelMB
                    .marcarSimulacionProcesoCHIP(RUTUtil.extraeRUT(rutCliente));
            String key = "mensajeOKAgendarContactoCliente";
            generarMensajeExito(key);
            
            if (getLogger().isEnabledFor(Level.INFO)) {
                getLogger().info(
                        "[marcarSimulacionProcesoCHIP] [BCI_FINOK]: Backing.");
            }           
        } 
        catch (GeneralException e) {
            if (logger.isEnabledFor(Level.ERROR)) {
                logger.error("[marcarSimulacionProcesoCHIP] [GeneralException] [rut: "
                + rutCliente + "] [BCI_FINEX] : Error al marcar simulacion CHIP. ", e);
            }
            String msg = obtenerMensajeTablaErrores("SIMCHIP003");
            generarMensajeError(msg, null);
        }
    }

    /**
     * Permite obtener la descripcion de un mensaje de error en la tabla
     * errores.codigos dada la clavede error y el canal donde se ejecuta la aplicación. 
     * Si el mensaje de error no está inscrito con el código de canal, intenta obtener
     * la descripción sólo con la clave de error. Ejemplos válidos de inscripción
     * de clave de error SIMCHIP001 en canal 151 en errores.codigos:
     * 
     * SIMCHIP001151;Desc=Mensaje de error 1;
     * SIMCHIP001-151;Desc=Mensaje de error 1;
     * SIMCHIP001;Desc=Mensaje de error 1;
     * 
     * 
     * <p>
     * Registro de versiones:
     * <ul>
     * 
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): Versión inicial.
     * </ul>
     * <p>
     * @param claveError
     *            identificador del mensaje.
     * @return descripcion del mensaje de error obtenido.
     * @since 1.3
     */
    private String obtenerMensajeTablaErrores(String claveError) {

        if (getLogger().isInfoEnabled()) {
            getLogger().info("[obtenerMensajeTablaErrores] [BCI_INI] Inicio");
            getLogger().info(
                    "[obtenerMensajeTablaErrores] claveError[" + claveError + "]");
            getLogger().info(
                    "[obtenerMensajeTablaErrores] canal[" + canal + "]");
        }

        String msj = null;
        msj = TablaValores.getValor(TABLA_ERRORES, claveError + canal, "Desc");
        if (msj == null) {
            msj = TablaValores.getValor(TABLA_ERRORES, claveError + "-" + canal,
                    "Desc");
        }
        if (msj == null) {
            msj = TablaValores.getValor(TABLA_ERRORES, claveError, "Desc");
        }
        if (getLogger().isInfoEnabled()) {
            getLogger().info("[obtenerMensajeTablaErrores] [BCI_FINOK] Fin[" 
                    + msj + "]");
        }
        return msj;
    }

    /**
     * Método que permite generar los mensajes de error que se despliegan en la
     * vista.
     * Si el parámetro claveMensaje es null, en la vista se despliega el parámetro
     * msgError. Por el contrario se despliega claveMensaje obtienendo la descripción
     * desde el archivo .properties del Simulador Hipotecario.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): Versión inicial.
     * </ul>
     * <p>
     * 
     * @param msgError
     *            mensaje de error devuelto por una excepcion.
     * @param claveMensaje
     *            codigo del mensaje perteneciente al properties de
     *            simulador CHIP.
     * @since 1.3
     */
    private void generarMensajeError(String msgError, String claveMensaje) {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("[generarMensajeError] [BCI_INI] Inicio");
            getLogger()
                    .info("[generarMensajeError] msgError[" + msgError + "]");
            getLogger().info("[generarMensajeError] claveMensaje[" + claveMensaje + "]");
        }

        FacesContext contexto = FacesContext.getCurrentInstance();
        ResolutorResourceBundleMB resolutorResourceBundleMB = encuentraBean("resolutorResourceBundleMB");
        ResourceBundle mensajes = PropertyResourceBundle
                .getBundle(resolutorResourceBundleMB
                        .obtieneResourceBundle(RECURSO_MENSAJE));
        FacesMessage mensajeF = null;
        if (claveMensaje != null) {
            mensajeF = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    mensajes.getString(claveMensaje), mensajes.getString(claveMensaje));
        } 
        else {
            mensajeF = new FacesMessage(FacesMessage.SEVERITY_ERROR, msgError,
                    msgError);
        }
        contexto.addMessage("ERROR", mensajeF);
        if (getLogger().isInfoEnabled()) {
            getLogger().info(
                    "[generarMensajeError][BCI_FINOK][" + mensajeF + "]");
        }
    }

    /**
     * Generar un mensaje de éxito a desplegar en la página.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): Versión inicial.
     * </ul>
     * <p>
     * @param claveMensaje
     *            codigo del mensaje perteneciente al properties de
     *            Simulador Hipotecario.
     * @since 1.3
     */
    private void generarMensajeExito(String claveMensaje) {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("[generarMensajeExito] [BCI_INI] Inicio");
            getLogger().info("[generarMensajeExito] keyTTFF[" + claveMensaje + "]");
        }
        FacesContext contexto = FacesContext.getCurrentInstance();
        ResolutorResourceBundleMB resolutorResourceBundleMB = encuentraBean("resolutorResourceBundleMB");
        ResourceBundle mensajes = PropertyResourceBundle
                .getBundle(resolutorResourceBundleMB
                        .obtieneResourceBundle(RECURSO_MENSAJE));
        FacesMessage mensajeF = null;
        mensajeF = new FacesMessage(FacesMessage.SEVERITY_INFO,
                mensajes.getString(claveMensaje), mensajes.getString(claveMensaje));
        contexto.addMessage("ERROR", mensajeF);
        if (getLogger().isInfoEnabled()) {
            getLogger().info(
                    "[generarMensajeExito][BCI_FINOK][" + mensajeF + "]");
        }
    }
    
    
    /**
     * Obtiene la glosa de la antiguedad indicada en la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @return Glosa de la antiguedad.
     * @since 1.3
     */    
    private String obtenerGlosaAntiguedadVivienda() {

        String glosa = "";
        for (int i = 0; i < configuracionSimuladorTO.getTablaAntiguedad().size(); i++) {
            TablaTO tabla = (TablaTO) configuracionSimuladorTO.getTablaAntiguedad().get(i);
            if (tabla.getCodigo().equals(antiguedadVivienda)) {
                glosa = tabla.getDescripcion();
                break;
            }
        }
        return glosa;
    }
    
    /**
     * Obtiene la glosa de la ciudad indicada en la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @return Glosa de la ciudad.
     * @since 1.3
     */
    private String obtenerGlosaCiudad() {

        String glosa = "";        
        if (ciudadesRegion != null && ciudadesRegion.length > 0) {
            for (int i = 0; i < ciudadesRegion.length; i++) {
                if (ciudadesRegion[i].getCodigoCiudad().equals(ciudad)) {
                    glosa = ciudadesRegion[i].getNombreCiudad().trim();
                    break;
                }
            }
        }
        return glosa;
    }

    /**
     * Obtiene la glosa de la comuna indicada en la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @return Glosa de la comuna. 
     * @since 1.3
     */
    private String obtenerGlosaComuna() {

        String glosa = "";
        if (comunasCiudad != null && comunasCiudad.length > 0) {
            for (int i = 0; i < comunasCiudad.length; i++) {
                if (comunasCiudad[i].getCodigoComuna().equals(comuna)) {
                    glosa = comunasCiudad[i].getNombreComuna().trim();
                    break;
                }
            }
        }
        return glosa;
    }

    /**
     * Obtiene la glosa del producto indicado en la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @return Glosa del producto. 
     * @since 1.3
     */    
    private String obtenerGlosaProducto() {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtenerGlosaProducto] [BCI_INI]: Inicio.");
            getLogger().info(
                "[obtenerGlosaProducto] [productosHipotecarios]:" + StringUtil.contenidoDe(productosHipotecarios));
        }          
        String glosa = "";
        if (productosHipotecarios != null && productosHipotecarios.length > 0) {
            for (int i = 0; i < productosHipotecarios.length; i++) {
                if (Integer.parseInt(String.valueOf(productosHipotecarios[i].getValue())) == producto) {
                    glosa = productosHipotecarios[i].getLabel().trim();
                    break;
                }
            }
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[obtenerGlosaProducto] [" + glosa + "] [BCI_FINOK].");
        }        
        return glosa;
    }

    /**
     * Obtiene la glosa del del tipo de moneda indicado en la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @return Glosa tipo de moneda.
     * @since 1.3
     */
    private String obtenerGlosaTipoMoneda() {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtenerGlosaTipoMoneda] [BCI_INI]: Inicio.");
        }        
        String glosa = "";
        for (int i = 0; i < configuracionSimuladorTO.getTablaMonedas().size(); i++) {
            TablaTO tabla = (TablaTO) configuracionSimuladorTO.getTablaMonedas().get(i);
            if (Integer.parseInt(tabla.getCodigo()) == productoMoneda) {
                glosa = tabla.getDescripcion().trim();
                break;
            }
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[obtenerGlosaTipoMoneda] [" + glosa + "] [BCI_FINOK].");
        }        
        return glosa;
    }    

    
    /**
     * Obtiene la glosa del del tipo de financiamiento indicado en la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @return Glosa tipo de financiamiento.
     * @since 1.3
     */    
    private String obtenerGlosaTipoFinanciamiento() {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtenerGlosaTipoFinanciamiento] [BCI_INI]: Inicio.");
        }         
        String glosa = "";
        for (int i = 0; i < configuracionSimuladorTO.getTablaTipoFinanciamiento().size(); i++) {
            TablaTO tabla = (TablaTO) configuracionSimuladorTO.getTablaTipoFinanciamiento().get(i);
            if (Integer.parseInt(tabla.getCodigo()) == (tipoFinanciamiento)) {
                glosa = tabla.getDescripcion().trim();
                break;
            }
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[obtenerGlosaTipoFinanciamiento] [" + glosa + "] [BCI_FINOK].");
        }        
        return glosa;
    }

    /**
     * Obtiene la glosa del destino del crédito solicitado en la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @return Glosa destino
     * @since 1.3
     */
    private String obtenerGlosaDestino() {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtenerGlosaDestino] [BCI_INI]: Inicio.");
        }           
        String glosa = "";
        for (int i = 0; i < configuracionSimuladorTO.getTablaObjetivos().size(); i++) {
            TablaTO tabla = (TablaTO) configuracionSimuladorTO.getTablaObjetivos().get(i);
            if (Integer.parseInt(tabla.getCodigo()) == destino) {
                glosa = tabla.getDescripcion().trim();
                break;
            }
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[obtenerGlosaDestino] [" + glosa + "] [BCI_FINOK].");
        }        
        return glosa;
    }

    /**
     * Asigna los datos del solicitante de la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @param resultadoCHIP Objeto al que se asignan los datos del solicitante de la simulación.
     * @param formatoFecha Formato de fecha utilizado para asignar los datos tipo fecha.
     * @since 1.3
     */
    private void asignarDatosSolicitante(ResultSimulaProcesoCHIPTO resultadoCHIP, SimpleDateFormat formatoFecha) {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[asignarDatosSolicitante] [BCI_INI]: Inicio.");
            getLogger().info("[asignarDatosSolicitante] [resultadoCHIP]:" + resultadoCHIP);
        }
        resultadoCHIP.setRutCliente(RUTUtil.extraeRUT(rutCliente));
        resultadoCHIP.setDvCliente(RUTUtil.extraeDigitoVerificador(rutCliente));
        resultadoCHIP.setNombreCliente(clienteMB.getNombres());
        resultadoCHIP.setApellidoPaterno(clienteMB.getApellidoPaterno());
        resultadoCHIP.setApellidoMaterno(clienteMB.getApellidoMaterno());
        resultadoCHIP.setFechaNacimientoCliente(FechasUtil.convierteStringADate(fechaNacimientoCliente,
            formatoFecha));
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[asignarDatosSolicitante] [clienteMB.getCodEjecutivo()]:" + clienteMB.getCodEjecutivo());
        }       
        resultadoCHIP.setIndCliente(clienteMB.esClienteBCI(clienteMB.getCodEjecutivo()));
        resultadoCHIP.setEmail(clienteMB.getEmail().trim());
        String fono = clienteMB.getFono().trim();
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[asignarDatosSolicitante] [fono]:" + fono);
        }      
        if (fono != null && !"".equals(fono)) {
            try {
                resultadoCHIP.setFono(Integer.parseInt(fono));
            }
            catch (NumberFormatException e) {
                if (getLogger().isEnabledFor(Level.WARN)) {
                    getLogger().warn("[asignarDatosSolicitante] excepción: ", e);
                }
            }
        }
        resultadoCHIP.setRentaLiquida(clienteMB.getRentaLiquida());

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[asignarDatosSolicitante] [" + resultadoCHIP + "] [BCI_FINOK].");
        }

    }

    /**
     * Asigna los datos de la inmobiliaria indicada en la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @param resultadoCHIP Objeto al que se asignan los datos de la inmobiliaria indicada en la simulación.
     * @since 1.3
     */
    private void asignarDatosInmobiliaria(ResultSimulaProcesoCHIPTO resultadoCHIP) {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[asignarDatosInmobiliaria] [BCI_INI]: Inicio.");
            getLogger().info("[asignarDatosInmobiliaria] [resultadoCHIP]:" + resultadoCHIP);
        }         

        if (inmobiliariaSeleccionada != null) {
            resultadoCHIP.setCodInmobiliaria(inmobiliariaSeleccionada.getCodInmobiliaria());
            resultadoCHIP.setGlosaInmobiliaria(inmobiliariaSeleccionada.getNombreInmobiliaria());            
        }
        else {
            resultadoCHIP.setCodInmobiliaria(inmobiliaria);
            resultadoCHIP.setGlosaInmobiliaria(inmobiliaria);
        }

        
        if (proyectoSeleccionado != null) {
            resultadoCHIP.setCodigoProyecto(proyectoSeleccionado.getCodProyecto());
            resultadoCHIP.setGlosaProyecto(proyectoSeleccionado.getDescripcionProyecto());            
            resultadoCHIP.setCodigoConvenio(proyectoSeleccionado.getConvenioProyecto());
        }
        else {
            resultadoCHIP.setCodigoProyecto(proyecto);
            resultadoCHIP.setGlosaProyecto(proyecto);
            resultadoCHIP.setCodigoConvenio(proyecto);
        }
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[asignarDatosInmobiliaria] [" + resultadoCHIP + "] [BCI_FINOK].");
        }        
    }

    /**
     * Asigna los datos del codeudor indicado en la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @param resultadoCHIP Objeto al que se asignan los datos del codeudor indicado en la simulación.
     * @param formatoFecha Formato de fecha utilizado para asignar los datos tipo fecha.
     * @since 1.3
     */
    private void asignarDatosCodeudor(ResultSimulaProcesoCHIPTO resultadoCHIP, SimpleDateFormat formatoFecha) {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[asignarDatosCodeudor] [BCI_INI]: Inicio.");
            getLogger().info("[asignarDatosCodeudor] [resultadoCHIP]:" + resultadoCHIP);
        }        
        resultadoCHIP.setCodeudor(tieneCodeudor);
        resultadoCHIP.setFechaNacimientoCodeudor(null);
        resultadoCHIP.setPorcentajeSegDesgravamenCodeudor(0);
        if (tieneCodeudor) {
            resultadoCHIP.setFechaNacimientoCodeudor(FechasUtil.convierteStringADate(fechaNacimientoCodeudor,
                formatoFecha));
            resultadoCHIP.setPorcentajeSegDesgravamenCodeudor(porcentajeSegDesgravamenCodeudor);
        }
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[asignarDatosCodeudor] [" + resultadoCHIP + "] [BCI_FINOK].");
        }
    }

    /**
     * Asigna los datos de la vivienda indicada en la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @param resultadoCHIP Objeto al que se asignan los datos de la vivienda indicada en la simulación.
     * @since 1.3
     */
    private void asginarDatosVivienda(ResultSimulaProcesoCHIPTO resultadoCHIP) {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[asginarDatosVivienda] [BCI_INI]: Inicio.");
            getLogger().info("[asginarDatosVivienda] [resultadoCHIP]:" + resultadoCHIP);
        }         
        resultadoCHIP.setDfl2(dfl2 == 1);
        resultadoCHIP.setGlosaTipoVivienda(descripcionVivienda);
        resultadoCHIP.setCodTipoVivienda(tipoVivienda);
        resultadoCHIP.setMaterialAdobe(materialAdobe == 1);
        resultadoCHIP.setCodAntiguedadVivienda(antiguedadVivienda);
        resultadoCHIP.setGlosaAntiguedadVivienda(this.obtenerGlosaAntiguedadVivienda());

        if (!resultadoCHIP.getGlosaAntiguedadVivienda().equals(VIVIENDA_NUEVA)) {
            resultadoCHIP.setGlosaAntiguedadVivienda(VIVIENDA_USADA);
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[asginarDatosVivienda] [" + resultadoCHIP + "] [BCI_FINOK].");
        }        
    }

    /**
     * Asigna los datos del financiamiento indicado en la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @param resultadoCHIP Objeto al que se asignan los datos del financiamiento indicado en la simulación.
     * @since 1.3
     */
    private void asignarDatosFinanciamiento(ResultSimulaProcesoCHIPTO resultadoCHIP) {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[asignarDatosFinanciamiento] [BCI_INI]: Inicio.");
            getLogger().info("[asignarDatosFinanciamiento] [resultadoCHIP]:" + resultadoCHIP);
            getLogger().info("[asignarDatosFinanciamiento] [mesExclusion]:" + mesExclusion);
            getLogger().info(
                "[asignarDatosFinanciamiento] [mesesExclusion]:" + StringUtil.contenidoDe(mesesExclusion));
        }  
        resultadoCHIP.setPrecioViviendaUF(NumerosUtil.redondearADosDecimales(precioViviendaUF));
        resultadoCHIP.setSubsidioUF(NumerosUtil.redondearADosDecimales(subsidioUF));
        resultadoCHIP.setCuotaContadoUF(NumerosUtil.redondearADosDecimales(cuotaContadoUF));
        resultadoCHIP.setCreditoUF(NumerosUtil.redondearADosDecimales(creditoUF));
        resultadoCHIP.setPorcentajeFinanciamiento(NumerosUtil.redondearADosDecimales(porcentajeFinanciamiento));
        resultadoCHIP.setPlazo(plazo);
        resultadoCHIP.setCodProducto(producto);
        resultadoCHIP.setGlosaProducto(this.obtenerGlosaProducto());
        resultadoCHIP.setCodTipoMoneda(productoMoneda);
        resultadoCHIP.setGlosaTipoMoneda(this.obtenerGlosaTipoMoneda());
        resultadoCHIP.setAnnosTasaFija(productoAnosTasaFija);
        resultadoCHIP.setCodTipoFinanciamiento(tipoFinanciamiento);
        resultadoCHIP.setGlosaTipoFinanciamiento(this.obtenerGlosaTipoFinanciamiento());
        resultadoCHIP.setSuscribePAC(suscribePac == 1);
        resultadoCHIP.setCodDestino(destino);
        resultadoCHIP.setGlosaDestino(this.obtenerGlosaDestino());
        resultadoCHIP.setMesesGracia(mesGracia);
        resultadoCHIP.setDiaVencimiento(diaVencimiento);
        resultadoCHIP.setCodMesExclusion(mesExclusion);
        resultadoCHIP.setGlosaMesExclusion(mesesExclusion[mesExclusion].getLabel());
        resultadoCHIP.setTasaCredito(simulacionCreditoHipotecarioModelMB.getResultadoSimulacionTO()
            .getSimulacionPlazosTO().getTasa());
        resultadoCHIP.setTasaCostoFondo(simulacionCreditoHipotecarioModelMB.getResultadoSimulacionTO()
            .getSimulacionPlazosTO().getTasaBase());
        resultadoCHIP.setTasaSpread(simulacionCreditoHipotecarioModelMB.getResultadoSimulacionTO()
            .getSimulacionPlazosTO().getTasaSpread());
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[asignarDatosFinanciamiento] [" + resultadoCHIP + "] [BCI_FINOK].");
        }        

    }

    /**
     * Asigna los datos geográficos de la vivienda indicada en la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @param resultadoCHIP Objeto al que se asignan los datos geográficos de la vivienda indicada en la
     *            simulación.
     * @since 1.3
     */
    private void asignarDatosGeograficos(ResultSimulaProcesoCHIPTO resultadoCHIP) {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[asignarDatosGeograficos] [BCI_INI]: Inicio.");
            getLogger().info("[asignarDatosGeograficos] [resultadoCHIP]:" + resultadoCHIP);
            getLogger().info("[asignarDatosGeograficos] [region]:" + region);
            getLogger().info("[asignarDatosGeograficos] [regiones]:" + StringUtil.contenidoDe(regiones));
        }          
        resultadoCHIP.setGlosaRegion(regiones[Integer.parseInt(region.trim()) - 1].getNombre().trim());
        resultadoCHIP.setCodRegion(Integer.parseInt(region));
        resultadoCHIP.setGlosaCiudad(this.obtenerGlosaCiudad());
        resultadoCHIP.setCodCiudad(Integer.parseInt(ciudad));
        resultadoCHIP.setGlosaComuna(this.obtenerGlosaComuna());
        resultadoCHIP.setCodComuna(Integer.parseInt(comuna));
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[asignarDatosGeograficos] [" + resultadoCHIP + "] [BCI_FINOK].");
        }        
    }

    /**
     * Asigna los datos de los seguros adicionales de la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @param resultadoCHIP Objeto al que se asignan los seguros adicionales.
     * @param resultadoSimulacion Objeto con el resultado de la simulación.
     * @since 1.3
     */
    private void asignarSegurosAdicionales(ResultSimulaProcesoCHIPTO resultadoCHIP,
        ResultadoSimulacionTO resultadoSimulacion) {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[asignarSegurosAdicionales] [BCI_INI]: Inicio.");
            getLogger().info("[asignarSegurosAdicionales] [resultadoCHIP]:" + resultadoCHIP);
            getLogger().info("[asignarSegurosAdicionales] [resultadoSimulacion]:" + resultadoSimulacion);
            getLogger().info(
                "[asignarSegurosAdicionales] [segurosAdicionales]:" + StringUtil.contenidoDe(segurosAdicionales));
        }           
        if (segurosAdicionales != null && segurosAdicionales.length > 0) {
            resultadoCHIP.setCodSeguroAdicional(segurosAdicionales[0]);
            for (int i = 0; i < segurosAdicionalesTO.length; i++) {
                if (Integer.parseInt(segurosAdicionalesTO[i].getCodigo()) == segurosAdicionales[0]) {
                    resultadoCHIP.setGlosaSeguroAdicional(segurosAdicionalesTO[i].getNombre());
                }
            }
            resultadoCHIP.setSeguroCesantiaInv(segurosAdicionales[0] == SEGURO_CESANTIA_INV);
            resultadoCHIP.setSeguroCesantiaDS01(segurosAdicionales[0] == SEGURO_CESANTIA_DS01);
            resultadoCHIP.setSeguroCesantiaDS40(segurosAdicionales[0] == SEGURO_CESANTIA_DS40);
            resultadoCHIP
                .setSeguroCesantiaDobleProteccion(segurosAdicionales[0] == SEGURO_CESANTIA_DOBLE_PROTECCION);
        }
        resultadoCHIP.setTotalPrimaSegurosEscogidosUF(NumerosUtil.redondearADosDecimales(resultadoSimulacion
            .getTotalPrimaSegurosAdicionales()));
        resultadoCHIP.setTotalPrimaSegurosEscogidosPesos((int) resultadoSimulacion
            .getTotalPrimaSegurosAdicionalesPesos());
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[asignarSegurosAdicionales] [" + resultadoCHIP + "] [BCI_FINOK].");
        }        

    }

    /**
     * Asigna los datos de los seguros individuales de la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @param resultadoCHIP Objeto al que se asignan los seguros individuales.
     * @since 1.3
     */
    private void asignarSegurosIndividuales(ResultSimulaProcesoCHIPTO resultadoCHIP) {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[asignarSegurosIndividuales] [BCI_INI]: Inicio.");
            getLogger().info("[asignarSegurosIndividuales] [resultadoCHIP]:" + resultadoCHIP);
            getLogger().info(
                "[asignarSegurosIndividuales] [segurosIndividualesTO]:"
                    + StringUtil.contenidoDe(segurosIndividualesTO));
        }          
        if (segurosIndividualesTO != null && segurosIndividualesTO.length > 0) {
            for (int i = 0; i < segurosIndividualesTO.length; i++) {
                if ((Integer.parseInt(segurosIndividualesTO[i].getCodigo()) == (simulacionCreditoHipotecarioModelMB
                    .getParametrosSimulacion().getSeguroIndividualDesgravamen()))
                    && (segurosIndividualesTO[i].getTipo().equals("D"))) {
                    resultadoCHIP.setGlosaSeguroDesgravamenIndividual(segurosIndividualesTO[i].getNombre());
                    resultadoCHIP.setCodSeguroDesgravamenIndividual(segurosIndividualesTO[i].getCodigo());
                }
                if ((Integer.parseInt(segurosIndividualesTO[i].getCodigo()) == (simulacionCreditoHipotecarioModelMB
                    .getParametrosSimulacion().getSeguroIndividualIncendio()))
                    && (segurosIndividualesTO[i].getTipo().equals("IS"))) {
                    resultadoCHIP.setGlosaSeguroIncendioSismoIndividual(segurosIndividualesTO[i].getNombre());
                    resultadoCHIP.setCodSeguroIncendioSismoIndividual(segurosIndividualesTO[i].getCodigo());
                }
            }
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[asignarSegurosIndividuales] [" + resultadoCHIP + "] [BCI_FINOK].");
        }        
    }

    /**
     * Asigna los datos de los gastos operacionales de la simulación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @param resultadoCHIP Objeto al que se asignan los gastos operacionales.
     * @param resultadoSimulacion Objeto con el resultado de la simulación.
     * @since 1.3
     */
    private void asignarGastosOperacionales(ResultSimulaProcesoCHIPTO resultadoCHIP,
        ResultadoSimulacionTO resultadoSimulacion) {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[asignarGastosOperacionales] [BCI_INI]: Inicio.");
            getLogger().info("[asignarGastosOperacionales] [resultadoCHIP]:" + resultadoCHIP);
            getLogger().info("[asignarGastosOperacionales] [resultadoSimulacion]:" + resultadoSimulacion);
        }          
        resultadoCHIP.setGastosOperacionalesPesos(resultadoSimulacion.getSimulacionTO().getGastosOperacionales());
        int totalGastosOperacionales = (int) (tasacionPesos + estudioTitulosPesos + borradorEscrituraPesos
            + notariaPesos + impuestoPesos + conservadorBRUFPesos + gestoriaPesos);
        resultadoCHIP.setTotalGastosOperacionalesPesos(totalGastosOperacionales);

        GastosOperacionalesTO gastosUF = new GastosOperacionalesTO();

        gastosUF.setBorradorEscritura(NumerosUtil.redondearADosDecimales(this.borradorEscritura));
        gastosUF.setEstudioTitulos(NumerosUtil.redondearADosDecimales(this.estudioTitulos));
        gastosUF.setGestoria(NumerosUtil.redondearADosDecimales(this.gestoria));
        gastosUF.setImptoAlMutuo(NumerosUtil.redondearADosDecimales(this.impuestoUF));
        gastosUF.setInscripcionConservador(NumerosUtil.redondearADosDecimales(this.conservadorBRUF));
        gastosUF.setNotariales(NumerosUtil.redondearADosDecimales(this.notaria));
        gastosUF.setTasacion(NumerosUtil.redondearADosDecimales(this.tasacion));
        resultadoCHIP.setGastosOperacionalesUF(gastosUF);
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[asignarGastosOperacionales] [" + resultadoCHIP + "] [BCI_FINOK].");
        }        

    }
    
    /**
     * Encuentra un Managed Bean.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): Versión inicial.
     * </ul>
     * <p>
     * @param beanName
     *            nombre del bean.
     * @param <T>
     *          Clase del bean encontrado.
     * @return bean.
     *            bean encontrado.
     * @since 1.3
     */
    private static <T> T encuentraBean(String beanName) {
        FacesContext context = FacesContext.getCurrentInstance();
        return (T) context.getApplication().evaluateExpressionGet(context,
                "#{" + beanName + "}", Object.class);
    }
    
    /**
     * Guarda los datos de la simulación en la base de datos.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * 
     * @since 1.3
     */
    private void guardarSimulacionProcesoCHIP() {

        try {
            simulacionCreditoHipotecarioModelMB.guardarSimulacionProcesoCHIP(this
                .construyeTOResultSimulacionProcesoCHIP());
        }
        catch (GeneralException e) {
            if (logger.isEnabledFor(Level.ERROR)) {
                logger.error("[guardarSimulacionProcesoCHIP] [GeneralException] [rut: " + rutCliente
                    + "] [BCI_FINEX] : Error al guardar simulacion CHIP. ", e);
            }
            String msg = obtenerMensajeTablaErrores("SIMCHIP002");
            generarMensajeError(msg, null);
        }
    }
}
