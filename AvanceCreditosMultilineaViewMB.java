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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

import wcorp.bprocess.boletadegarantia.SvcBoletaDeGarantiaImpl;
import wcorp.bprocess.creditosgenerales.InvocacionServicioException;
import wcorp.bprocess.creditosgenerales.LocalizadorDeServicios;
import wcorp.bprocess.creditosglobales.SvcCreditosGlobales;
import wcorp.bprocess.creditosglobales.SvcCreditosGlobalesImpl;
import wcorp.bprocess.multilinea.Multilinea;
import wcorp.bprocess.multilinea.MultilineaException;
import wcorp.bprocess.multilinea.MultilineaHome;
import wcorp.bprocess.multilinea.to.DatosAvalesTO;
import wcorp.bprocess.multilinea.to.DatosDisclaimerTO;
import wcorp.bprocess.multilinea.to.DatosParaAvanceTO;
import wcorp.bprocess.multilinea.to.DatosParaCondicionesCreditoTO;
import wcorp.bprocess.multilinea.to.DatosParaCorreoTO;
import wcorp.bprocess.multilinea.to.DatosParaOperacionesFirmaTO;
import wcorp.bprocess.multilinea.to.DatosPlantillaProductoTO;
import wcorp.bprocess.multilinea.to.DatosResultadoOperacionesTO;
import wcorp.bprocess.multilinea.to.DatosSegurosTO;
import wcorp.bprocess.precioscontextos.PreciosContextos;
import wcorp.bprocess.precioscontextos.PreciosContextosHome;
import wcorp.bprocess.tables.TableManagerService;
import wcorp.gestores.validador.utils.ValidaCuentasUtility;
import wcorp.model.actores.DireccionClienteBci;
import wcorp.serv.boletadegarantia.RowConsultaMasivaDeOficinas;
import wcorp.serv.creditosglobales.CalendarioPago;
import wcorp.serv.creditosglobales.EstructuraVencimiento;
import wcorp.serv.creditosglobales.ResultConsultaCalendarioPago;
import wcorp.serv.creditosglobales.ResultConsultaOperacionCredito;
import wcorp.serv.direcciones.DescComuna;
import wcorp.serv.direcciones.DireccionesException;
import wcorp.serv.direcciones.ServiciosDirecciones;
import wcorp.serv.direcciones.ServiciosDireccionesHome;
import wcorp.serv.misc.ServiciosMiscelaneos;
import wcorp.serv.misc.ServiciosMiscelaneosHome;
import wcorp.serv.precios.IteracionConsultaCgr;
import wcorp.serv.precios.ResultConsultaCgr;
import wcorp.serv.seguridad.SeguridadException;
import wcorp.util.EnhancedServiceLocator;
import wcorp.util.EnvioDeCorreo;
import wcorp.util.ErroresUtil;
import wcorp.util.FechasUtil;
import wcorp.util.Feriados;
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
import cl.bci.aplicaciones.firma.mb.ServicioAutorizacionyFirmaModelMB;
import cl.bci.aplicaciones.firma.to.ResultadoFirmaTO;
import cl.bci.aplicaciones.iconos.util.mb.GeneraComprobanteUtilityMB;
import cl.bci.aplicaciones.infraestructuradenegocios.dimensiones.ComunaSupportMB;
import cl.bci.aplicaciones.infraestructuradenegocios.dimensiones.RegionSupportMB;
import cl.bci.aplicaciones.infraestructuradenegocios.dimensiones.to.ComunaTO;
import cl.bci.aplicaciones.infraestructuradenegocios.dimensiones.to.RegionTO;
import cl.bci.aplicaciones.productos.mb.ProductosMB;
import cl.bci.aplicaciones.servicios.multilinea.pyme.to.DatosComprobantePdfTO;
import cl.bci.infraestructura.web.journal.Journalist;
import cl.bci.infraestructura.web.seguridad.mb.SesionMB;
import cl.bci.infraestructura.web.seguridad.segundaclave.SegundaClaveUIInput;

/** 
 * ManagedBean
 * 
 * <pre><b>AvanceCreditosMultilineaViewMB</b>
 *
 * Componente encargado de interactuar con la vista para realizar un
 * avance de créditos, a través de diferentes pasos.
 * </pre>
 *
 * Registro de versiones:<ul>
 *
 * <li>1.0  09/12/2014, Manuel Escárate R. (BEE S.A.)  - Jimmy Muñoz D. (ing.Soft.BCI): Versión inicial. </li>
 * <li>1.1  11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI):Se modifica el método volverPaso,
 *                                           se agregan los siguientes atributos: mensajeError, esErrorGenerico, excepciones
 *                                           ,condicionGarantiaIni. Se modifican los métodos iniciarAvance,mostrarCondicionesCredito,continuarAvance
 *                                           cursarAvance,enviaEmailEjecutivo,enviaEmailAval,enviaEmailBackOffice,detalleSeguros,volverPaso.
 *                                           Se modifican méotodos enviarSolicitud, detalleSeguros, enviaEmailBackOffice y iniciarAvance para enviar correo a backoffice.
 *                                           Se complementa metodo journalizar.</li>
 * <li>1.2  11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI):Se modifican los métodos iniciarAvance,mostrarCondicionesCredito,continuarAvance
 *                                           cursarAvance.</li>
 * <li>1.3 10/06/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI):Se modifican los métodos cursarAvance y enviarCorreoAvales.</li>
 * <li>1.4  14/06/2016, Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): Se modifican los métodos iniciarAvance,continuarAvance,mostrarCondicionesCredito,enviarSolicitud
 *                                                             ,enviaEmailEjecutivo,enviaEmailBackOffice,generarComprobanteCargaPDF,detalleSeguros
 *                                                             ,armaEmailParaDestinatarioGenerico,journalizar.
 *                                                             Se elimina atributo VALOR_OCHO, y se agregan los siguientes codMoneda,tipoDePlazo.</li>
 * <li>1.5 20/07/2016, Manuel Ecárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI):  Se modifican los siguientes métodos mostrarCondiciones,continuarAvance,cursarAvance,enviaEmailAval,
 *                                                              ingresarOperacionFirma,enviarCorreoAvales,generarComprobanteCargaPDF,resumenCredito,calendarioPago,armaEmailParaDestinatarioAval,enviarSolicitud.
 *                                                              Se agregan los siguientes métodos enviarCorreoContactoEmpresa,enviarEmailContactoEmpresa,armaEmailParaDestinatarioContacto,obtenerGastoNotarial,
 *                                                              getValor,obtenerTablaDescripciones.</li>
 * </ul> 
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 * 
 */
@ManagedBean
@ViewScoped
public class AvanceCreditosMultilineaViewMB implements Serializable {

    /**
     * Atributo Logger.
     */
    private static transient Logger logger  = (Logger)Logger.getLogger(AvanceCreditosMultilineaViewMB.class);

    /**
     * serialVersionUID de la clase.
     */ 
    private static final long serialVersionUID = 1L;
 
    /**
     * Nombre JNDI del EJB Multilinea.
     */
    private static final String JNDI_NAME_MULTILINEA = "wcorp.bprocess.multilinea.Multilinea";
     
    /**
     * Nombre JNDI del EJB PreciosContextos.
     */
    private static final String JNDI_NAME_PRECIOS = "wcorp.bprocess.precioscontextos.PreciosContextos";    
    
    /**
     * sistema tablas generales.
     */
    private static final String TABLE_MANANGER_SISTEMA_TAB = "TAB";
    
    /**
     * sistema tablas generales de colocaciones.
     */
    private static final String TABLE_MANANGER_TABLA_COM = "COM";
    
    /**
     * Resultado para oferta crediticia activa.
     */
    private static final int CURSE_ACT_MTL = 1;
    
    /**
     * Resultado para oferta crediticia inactiva.
     */
    private static final int SIM_CUR_CLI  = 2;
    
    /**
     * Error generico.
     */
    private static final int ERROR_GENERICO = -2;
    
    /**
     * Resultado para oferta crediticia inactiva.
     */
    private static final int SIM_EJE_COM = 3;
    
     /**
     * Estado pendiente para FyP.
     */
    private static final int ESTADO_PENDIENTE = 0;
    
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
     * Respuesta monto superado.
     */
    private static final int RESP_MONTO_SUPERADO = 999;
    
    /**
     * Atributo Númerico.
     */
    private static final int VALOR_MIL = 1000;
     
    /**
     * Monto supera el maximo permitido.
     */
    private static final int MONTO_SUPERADO = -1;
    
    /**
     * Atributo numérica.
     */
    private static final int VALOR_DOS = 2;
    
    /**
     * Atributo numérica.
     */
    private static final int VALOR_TRES = 3;
    
    /**
     * Atributo numérica.
     */
    private static final int VALOR_CUATRO = 4;
    
    /** 
    * Codigo tipo email.
    */
	private static final char TIPOMAIL = '7';
    
    /**
     * Atributo numérica.
     */
    private static final int VALOR_CINCO = 5;
    
    /**
     * Atributo numérica.
     */
    private static final int VALOR_SEIS = 6;
    
     /**
     * Atributo numérica.
     */
    private static final int VALOR_DIEZ = 10;
    
	/**
	 * Largo del código de moneda.
	 */
	private static final int LARGO_CODIGO_MONEDA =  6;
    
    /**
     * Tabla fimraYPoderes.
     */
    private static final String TABLA_FYP = "firmasYPoderes.parametros";
    
	/**
     * Tabla multilinea.
     */
    private static final String TABLA_MULTILINEA = "multilinea.parametros";
    
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
     * Constante para identificar el usuario a utilizar en la construccin del objeto MultiEnvironment.
     */
    private static final String USUARIO = TablaValores.getValor("multilinea.parametros",
            "tipoUsuarioParaMulti", "usuario");
    
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
     * Oferta crediticia activa.
     */
    private static final String ACTIVA = TablaValores.getValor("multilinea.parametros", 
            "ofertaCrediticia", "activa");
    
    /**
     * Oferta crediticia inactiva.
     */
    private static final String INACTIVA = TablaValores.getValor("multilinea.parametros", 
            "ofertaCrediticia", "inactiva");
    
    /**
     * Estilo de la oferta crediticia activa.
     */
    private static final String OFERTA_VERDE =  TablaValores.getValor("multilinea.parametros", 
            "estiloOfertaCrediticia", "ofertaVerde");
    
    /**
     * Estilo de la oferta crediticia inactiva.
     */
    private static final String OFERTA_AMARILLA =  TablaValores.getValor("multilinea.parametros", 
            "estiloOfertaCrediticia", "ofertaAmarilla");
    
    /**
     * Estilo de la oferta crediticia inactiva.
     */
    private static final String OFERTA_NARANJA =  TablaValores.getValor("multilinea.parametros", 
            "estiloOfertaCrediticia", "ofertaNaranja");

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
     * Formato date a ocupar.
     */
    private final SimpleDateFormat formatoFecha1  = new SimpleDateFormat("ddMMyyyy HH:mm:ss");
    
    /**
     * Formato date a ocupar.
     */
    private final SimpleDateFormat formatoFecha2 = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    
    /**
     * Hora definida.
     */
    private String hhmmss = " 23:59:59";
    
    /**    
     * Referencia al componente que maneja la segunda clave.
     */
    private SegundaClaveUIInput segundaClaveAplicativo;

    /**
     * Atributo para identificar si se tiene multilinea vigente.
     */
    private boolean tieneMultilineaVigente;

    /**
     * Atributo para indentificar si el cliente tiene multilinea.
     */
    private boolean tieneMultilinea;
    
     /**
     * Atributo para identificar si se debe enviar el correo a backoffice.
     */
    private boolean enviaCorreoBackOffice;

    /**
     * Banca envio email ejecutivo. 
     */
    private boolean bancaEnvioEmailEjecutivo;
    
    /**
     * Atributo para registrar si el cliente posee o no mandato.
     */
    private boolean tieneMandato;

    /**
     * Banca permitida empresario email.
     */
    private boolean bancaPermitidasEmpresarioEmail;

    /**
     * Atributo para identificar si tiene monto multilinea.
     */
    private boolean tieneMultilineaMonto;
    
    /**
     * Total cuotas.
     */
    private int totalCuotas;
    
    /**
     * Atributo que sirve para identificar si es necesario cargar los métodos
     * iniciales.
     */
    private boolean noCargaMetodosInicio;
    
    /**
     * Hora permitida para poder simular y cursar. 
     */
    private boolean horaPermitida;
    
    /**
     * Cuota seleccionada.
     */
    private int cuotaSeleccionada;

    /**
     * Tipo de crédito seleccionado.
     */
    private String tipoDeCreditoSeleccionado; 

    /**
     * Identificador del paso de avance.
     */
    private int paso;  

    /**
     * Total requieren dps.
     */
    private int requierenDPS;

    /**
     * Monto solicitado. 
     */
    private String montoSolicitado;
    
    /**
     * Monto correo.
     */
    private double montoCorreo;

    /**
     * Variable para setear el error traducido.
     */
    private String mensajeError;

    /**
     * Variable para identificar si es un error general.
     */
    private boolean esErrorGenerico;

    /**
     * Monto final del crédito.
     */
    private double montoFinalCredito;

    /**
     * Monto disponible.
     */
    private double montoDisponible;

    /**
     * Costo final del crédito.
     */
    private double costoFinalCredito;

    /**
     * Fecha vencimiento seleccionada.
     */
    private String fechaVencimientoSeleccionada;

    /**
     * Código de la banca.
     */
    private String codBanca;
    
    /**
     * Atributo para identificar si se muestran los seguros en el comprobante.
     */
    private boolean muestraSeguros;
    
    /**
     * Atributo para identificar si se muestra el resumen en el comprobante.    
     */
    private boolean muestraResumen;
    
    /**
     * Atributo para identificar si se muestra el calendario en el comprobante.
     */
    private boolean muestraCalendario;

    /**
     * Atributo para identificar la moneda.
     */
    private String codMoneda;
    
    /**
     * Atributo para identificar el tipo de plazo.
     */
    private String tipoDePlazo;

	/**
     * Atributo que representa las Comunas.
     */
    private ComunaTO[] comunasPorRegion;
    
    /**
     * Arreglo con errores.
     */
    private ArrayList<String> excepciones;
    
    /**
     * Plan.
     */
    private char plan;

    /**
     * Oficina con la que ingreso.
     */
    private String oficinaIngreso;

    /**
     * Código del ejecutivo.
     */
    private String codigoEjecutivo;

    /**
     * Código de comuna.
     */
    private String codigoComuna;
    
    /**
     * Código de región.
     */
    private String codigoRegion;

    /**
     * Código del segmento.
     */
    private String codSegmento;

    /**
     * valor de la cuota.
     */
    private double valorCuota;

    /**
     * Tasa de interés internet.
     */
    private double tasaInteresInternet;

    /**
     * Factor cae.
     */
    private double factorCae;

    /**
     * Impuestos.
     */
    private double impuestos;
    
    /**
     * Valor Gastos Notariales.
     */
    private double gastosNotariales;    
    
    /**
     * Telefono cliente.
     */
    private String telefonoCliente;
    
    /**
     * Código de teléfono.
     */
    private String codTelefonoCliente;
    
    /**
     * Celular cliente.
     */
    private String celularCliente;
    
    /**
     * Código celular.
     */
    private String codCelularCliente;
    
    /**
     * Email cliente.
     */
    private String emailCliente;
    
    /**
     * Region cliente.
     */
    private String regionCliente;

	/**
     * Comuna cliente.
     */
    private String comunaCliente;
    
    
    /**
     * HashMap con listado de feriados.
     */
    private HashMap<String, String> listaFeriados = null;
    
    /**
     * Números de cuotas.
     */
    private ArrayList<SelectItem> cuotas;

    /**
     * Números de cuotas.
     */
    private ArrayList<SelectItem> cuotasMod;

    /**
     * Tipos de créditos.
     */
    private ArrayList<SelectItem> tiposDeCreditos;

    /**
     * Tipos de créditos a mostrar.
     */
    private ArrayList<SelectItem> tiposDeCreditosAmostrar;

    /**
     * Arreglo con seguros.
     */
    private DatosSegurosTO[] segurosObtenidos;
    
    /**
     * Arreglo con datos de la plantilla.
     */
    private DatosPlantillaProductoTO[] datosPlantilla;
    
    /**
     * Arreglo con datos de avales.
     */
    private DatosAvalesTO[] datosAvales;
    
    /**
     * Arreglo con datos de avales para correos.
     */
    private DatosAvalesTO[] datosAvalesCorreo;

    /**
     * Atributo para registrar informacin para combobox de cuentas corrientes y primas.
     */
    private SelectItem[] cuentasCorrientesYPrimas;

    /**
     * Regiones.
     */
    private ArrayList<SelectItem> regiones;

    /**
     * Comunas.
     */
    private ArrayList<SelectItem> comunas;

    /**
     * Atributo para registrar la cuenta de abono seleccionada.
     */
    private String cuentaSeleccionadaAbono;

    /**
     * Atributo para registrar la cuenta de cargo seleccionada.
     */
    private String cuentaSeleccionadaCargo; 
    
    /**
     * Parte de 3 dígitos del número de la operación.
     */
    private String caiOperacion;
    
    /**
     * Parte de 6 dígitos del número de la operación.
     */
    private int iicOperacion;
    
    /**
     * Condición de garantía.
     */
    private String condicionGarantia;

    /**
     * Condición de garantía iniciar avance.
     */
    private String condicionGarantiaIni;

    /**
     * Fecha máxima primer vencimiento.
     */
    private String fechaMaximaPrimerVencimiento;

    /**
     * Monto mínimo para poder cursar.
     */
    private double montoMinimoParaCursar;
    
    /**
     * Monto máximo para poder cursar.
     */
    private double montoMaximoParaCursar;
    
    /**
     * Fecha vencimiento de la multilínea.
     */
    private String fechaVencimientoMLT;
    
    /**
     * Oferta crediticia.
     */
    private String ofertaCrediticia;
    
    /**
     * Estilo para diferentes ofertas crediticias.
     */
    private String estiloOfertaCrediticia;
    
    /**
     * Días tope para primer vencimiento.
     */
    private String diasTopePrimerVencimiento;
    
    /**
     * Días iniciales para primer vencimiento.
     */
    private String diasIniPrimerVencimiento;
    
    /**
     * Valor con la cuota inicial.
     */
    private int cuotaInicial;
    
    /**
     * Valor con la cuota final.
     */
    private int cuotaFinal;
    
    /**
     * Atributo para identificar si el seguro ha sido seleccionado.
     */
    private boolean seguroSeleccionado;
    
    /**
     * Monto total de seguros seleccionados.
     */
    private double montoTotalSegurosSeleccionados;
    
    /**
     * Atributo que guardar el check de acepta condiciones.
     */
    private boolean aceptaCondiciones;
    
    /**
     * Atributo que guardar el check de acepta condiciones.
     */
    private boolean aceptaCondicionesMandato;

    /**
     * Atributo que guardar el check de acepta condiciones para mandato.
     */

    private boolean mostrarCondicionesMandato;

    /**
     * Glosa con tipo de crédito.
     */
    private String glosaTipoCredito;
    
    /**
     * Calendario de pago para comprobante.
     */
    private CalendarioPago[] calendarioPago;
    
    /**
     * Salida Calendario de pago.
     */
    private CalendarioPago[] calendarioPagoSalida;
    
    /**
     * Objeto con resultados específicos de la operación del crédito.
     */
    private ResultConsultaOperacionCredito operacionCredito;
    
    /**
     * Atributo para identificar el resultado del componentes de firmas.
     */
    private String resultadoFirmaAvance;
    
    /**
     * Fecha de curse.
     */
    private String fechaCurse;

    /**
     * Razón social empresa.
     */
    private String razonSocial;

    /**
     * Nombre fantasía empresa.
     */
    private String nombreFantasia;
    
    /**
     * Largo comuna.
     */
    private int largoComuna;
    
    /**
     * Disclaimer para mandato.
     */
    private String disclaimerMandato;

    /**
     * Atributo representa Managed Bean inyectado ClienteMB.
     */
    @ManagedProperty(value = "#{clienteMB}")
    private ClienteMB clienteMB;

    /**
     * Atributo representa Managed Bean inyectado UsuarioModelMB.
     */
    @ManagedProperty(value = "#{usuarioModelMB}")
    private UsuarioModelMB usuarioModelMB;

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
     * Atributo para inyectar MB ComunaSupportMB.
     */
    @ManagedProperty(value = "#{comunaSupportMB}")
    private ComunaSupportMB comunaSupportMB;
    
    /**
     * Atributo para inyectar MB RegionSupportMB.
     */
    @ManagedProperty(value = "#{regionSupportMB}")
    private RegionSupportMB regionSupportMB;
    
    /**
     * Atributo para inyectar MB ServicioAutorizacionyFirmaModelMB.
     */
    @ManagedProperty(value = "#{servicioAutorizacionyFirmaModelMB}")
    private ServicioAutorizacionyFirmaModelMB servicioAutorizacionyFirmaModelMB;

    /**
     * Método que permite obtener una instancia de Logger de la clase.
     *
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0 09/12/2014, Manuel Escárate R. (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): Versión inicial. </li>
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
     * Método que realiza las validaciones pertinentes para destinar a la página
     * de inicio del avance, o hacia una página de error en caso que no cumpla
     * con ciertas condiciones.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 09/12/2014 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li>
     * <li>1.1 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega lógica para simular en
     *                                                caso que existan problema de servicios con la consulta de avales.
     *                                            Se agrega atributo para correo backoffice.</li>
     * <li>1.2 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega lógica validar mandatos.</li>
     * <li>1.3 18/05/2016, Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): Se quita seteo de flag para correoBackOffice.</li>
     *
     * </ul>
     * @throws Exception controlado.
     * @since 1.0
     */  
    public void iniciarAvance() throws Exception{
        if (getLogger().isDebugEnabled()) {
             getLogger().debug("[iniciarAvance][" + clienteMB.getRut() + "][BCI_INI]");
        }
    	codTelefonoCliente = "";
    	telefonoCliente = "";
    	codCelularCliente = "";
    	celularCliente = "";
        emailCliente = "";
    	regionCliente = "";
    	comunaCliente = "";
   
        if (!noCargaMetodosInicio){
            tipoDeCreditoSeleccionado = "";
            caiOperacion = "";
            iicOperacion = 0;
            codigoRegion = "1";
            long rutCliente = clienteMB.getRut();
            char dvCliente = clienteMB.getDigitoVerif();
            codBanca        = null;
            plan            = ' ';
            oficinaIngreso  = null;
            codigoEjecutivo = null;
            codSegmento     = null;
            excepciones = null;
            DatosParaAvanceTO datosCliente = null;
            MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
                    sesion.getCanalId(), USUARIO);
            datosPlantilla = null;
            horaPermitida = cumpleHorarios();
            validaHorarioDia(horaPermitida);

	            if (getLogger().isDebugEnabled()) {
	                 getLogger().debug("[iniciarAvance][obtieneDetalleTabla para feriados]");
	            }
	            listaFeriados = obtieneDetalleTabla(multiEnvironment, "GNS", "FER", null);
	            try {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("[iniciarAvance][antes de llamar a iniciarAvance en EJB]");
                    }
	                datosCliente = crearEJBmultilinea().iniciarAvance(multiEnvironment,rutCliente,dvCliente);
	                if (datosCliente != null) {
	                    if (getLogger().isDebugEnabled()) {
	                        getLogger().debug("[iniciarAvance][datosCliente !=null]");
	                    }

                        razonSocial = datosCliente.getRazonSocial();
                        nombreFantasia = datosCliente.getNombreFantasia();

            		if (datosCliente.getCondicionGarantia() != null ){
            			condicionGarantiaIni = datosCliente.getCondicionGarantia();
            		}

            		if (getLogger().isDebugEnabled()) {
            			getLogger().debug("[iniciarAvance][condicionGarantiaIni] :" +condicionGarantiaIni);
            		}
            	
            		if (datosCliente.getEsEmpresario().equalsIgnoreCase("N")){
            			if (getLogger().isDebugEnabled()) {
            				getLogger().debug("[iniciarAvance][Tipo banca cliente no inscrita]");
            			}
            			paso = -1;
            			mensajeError = TablaValores.getValor(TABLA_MULTILINEA, "TIPOBANCACLIENTENOINSCRITA", "desc");
            		}
            		else {
            			if (datosCliente.getExcepciones() != null){
            				excepciones  = new ArrayList<String>(Arrays.asList(datosCliente.getExcepciones()));
            			}
                        tieneMultilinea = datosCliente.isTieneMultilinea();
	                    codBanca        = datosCliente.getCodBanca();
            			plan            = ' ';
	                    oficinaIngreso  = datosCliente.getOficinaIngreso();
	                    codigoEjecutivo = datosCliente.getCodigoEjecutivo();
            			codSegmento     = " ";
	                    montoDisponible = datosCliente.getMontoDisponible();
	                    bancaEnvioEmailEjecutivo = verificaPertenenciaBanca(
	                            codBanca, TablaValores.getValor("multilinea.parametros",
	                                    "bancaEnvioEmailEjecutivo", "desc"));
	                    if (getLogger().isDebugEnabled()) {
	                            getLogger().debug("Multinea - bancaEnvioEmailEjecutivo:"
	                                   + "[" + bancaEnvioEmailEjecutivo + "]");
	                    }
	                    bancaPermitidasEmpresarioEmail = verificaPertenenciaBanca(
	                            codBanca, TablaValores.getValor("multilinea.parametros", 
	                                    "bancapermitidasEmpresarioEmail", "desc"));
	                    if (getLogger().isDebugEnabled()){
	                           getLogger().debug("Multinea - bancapermitidasEmpresarioEmail:"
	                                 + "[" + bancaPermitidasEmpresarioEmail + "]");
	                    }
	                   if (datosCliente.getOfertaCrediticia() == CURSE_ACT_MTL){
	                        ofertaCrediticia = ACTIVA;  
	                        estiloOfertaCrediticia =OFERTA_VERDE; 
	                    }
	                    else if (datosCliente.getOfertaCrediticia() == SIM_CUR_CLI) {
	                        ofertaCrediticia = INACTIVA; 
            				estiloOfertaCrediticia = OFERTA_AMARILLA; //Cond. Garantía no constituída
            				enviaCorreoBackOffice = true;
	                    }
	                    else if (datosCliente.getOfertaCrediticia() == SIM_EJE_COM) {
	                        ofertaCrediticia = INACTIVA; 
	                        estiloOfertaCrediticia =OFERTA_NARANJA; 
	                    } 
	                    if (getLogger().isDebugEnabled()) {
	                    	getLogger().debug("[iniciarAvance] envia correoBackOffice [ "+ enviaCorreoBackOffice  + "]");
	                    } 

                        boolean revisionMandato = Boolean.parseBoolean(TablaValores.getValor(TABLA_MULTILINEA
                                , "revisionMandato", "valor"));
                        mostrarCondicionesMandato = Boolean.parseBoolean(TablaValores.getValor(TABLA_MULTILINEA
                                , "activaCondicionesMandato", "valor"));
                        if (getLogger().isInfoEnabled()) {
                            getLogger().info("[iniciarAvance][" + rutCliente + "] habilitada revisionMandato: "
                                    + revisionMandato);
                        }
                        if (getLogger().isInfoEnabled()) {
                            getLogger().info("[iniciarAvance][" + rutCliente + "] habilitada activaCondicionesMandato: "
                                    + mostrarCondicionesMandato);
                        }
                        if (revisionMandato){
                            try {
                                tieneMandato = clienteMB.poseeMandatoMulticanalAutorizado();
                                if(!tieneMandato) {
                                	ofertaCrediticia = INACTIVA;
                                    estiloOfertaCrediticia =OFERTA_NARANJA;
                                }
                                if (getLogger().isInfoEnabled()) {
                                    getLogger().info("[iniciarAvance][" + rutCliente + "] tieneMandato: "
                                            + tieneMandato);
                                }
                            }
                            catch (Exception e){
							    if(getLogger().isEnabledFor(Level.ERROR)){
                                getLogger().debug("[iniciarAvance][" + rutCliente + "] Exception mandato: "
                                        + ErroresUtil.extraeStackTrace(e));
								}
                                tieneMandato = false;
                                ofertaCrediticia = INACTIVA;
                                estiloOfertaCrediticia =OFERTA_NARANJA;
                            }
                        }
                        else{
                            tieneMandato = true;
                        }
		
	                    if (montoDisponible > 0){
	                        tieneMultilineaMonto = true;
	                    }
	                    fechaVencimientoMLT = datosCliente.getFechaVencimientoMLT();
	                    if (getLogger().isDebugEnabled()) {
	                        getLogger().debug("[iniciarAvance]codBanca =<" 
	                                + codBanca + ">,plan=<"
	                                + plan + ">, oficinaIngreso=<"
	                                + oficinaIngreso + ">,codigoEjecutivo=<" 
	                                + codigoEjecutivo + ">, codSegmento=<"
	                                + codSegmento + ">, montoDisponible=<"
	                                + montoDisponible + "> fechaVencimientoMLT=<"
	                                + fechaVencimientoMLT + ">");
	                    }
	                    if (datosCliente.getDatosPlantilla() != null && datosCliente.getDatosPlantilla().length >0){
	                        datosPlantilla = new DatosPlantillaProductoTO[datosCliente.getDatosPlantilla().length];
	                        for (int i = 0; i < datosCliente.getDatosPlantilla().length; i++) {
	                            datosPlantilla[i] = new DatosPlantillaProductoTO();
	                            datosPlantilla[i] = datosCliente.getDatosPlantilla()[i];
	                        }
	                    }
	                    diasTopePrimerVencimiento = "0";
	                    diasIniPrimerVencimiento = "0";
	                    cuotas = new ArrayList<SelectItem>();
	                    paso = 0;
		            }
	            }
            }
	            catch (Exception ex){
	            	if(getLogger().isEnabledFor(Level.ERROR)){
	            		getLogger().error("[iniciarAvance] [" + rutCliente + "] [BCI_FINEX] [datosBasicosGenerales] "
	            				+ "mensaje=< " + ex.getMessage() + ">", ex);
	            	}
	            	esErrorGenerico = true;
	            	paso = -1;
	            }
        }
        if (getLogger().isDebugEnabled()) {
        	getLogger().debug("[iniciarAvance][" + clienteMB.getRut() + "][BCI_FINOK]");
        }
    }
    
    /**
     * Método encargado de verificar dia hábil.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 09/12/2014 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li>
     * </ul>
     * @param cumple variable que identifica si cumple horario.
     * @since 1.0
     */  
    public void validaHorarioDia(boolean cumple){
        boolean esdiahabil       = Feriados.esHabil(new Date());
        if(esdiahabil){
            if (cumple){
            	horaPermitida = true;
            }
            else{
                horaPermitida = false;
            }
        }
    }
    
    /**
     * cumpleHorarios verifica si la operacion de credito esta en los horarios establecidos.
     *
     * Registro de versiones:<ul>
     * <li>1.1 09/12/2014   Manuel Escárate   (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): version inicial.
     * </ul>
     * <p>
     *
     * @return true o false
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
              getLogger().debug("[iniciarAvance][" + clienteMB.getRut() + "][BCI_FIN]");
         }
         return retorno;
     }
    
    
    /**
     * Método que obtiene la fecha maxima de vencimiento.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 09/12/2014 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li>
     * </ul>
     * @param fecha fecha actual.
     * @param dias fecha máxima primer pago.
     * @return devuelve el objeto date con los nuevos días añadidos.
     */
    public static Date obtenerFechaMaximaVencimiento(Date fecha, int dias){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha); 
        calendar.add(Calendar.DAY_OF_YEAR, dias);  
        return calendar.getTime(); 
    }

    /**
     * Rescata valores de la primera pantalla para el avance.
     * luego ejecuta el método avanceMultilinea para obtener los valores
     * para el paso 2 .
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 09/12/2014 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li>
     * <li>1.1 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega atributos para simular en
     *                                                caso que existan problema de servicios con la consulta de avales.</li>
     * <li>1.2 18/05/2016, Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): Se agrega codMoneda y tipoDePlazo.</li>
     * <li>1.3 20/07/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega seteo para valor de gasto notarial y monto para correo.</li>
     * </ul>
     * @throws ParseException 
     * @throws GeneralException 
     * @throws RemoteException 
     * @throws EJBException  
     * @throws MultilineaException 
     * @since 1.0
     */  
    public void mostrarCondicionesCredito() throws ParseException,
    MultilineaException, EJBException, RemoteException, GeneralException{
        if (getLogger().isDebugEnabled()) {
             getLogger().debug("[mostrarCondicionesCredito][" + clienteMB.getRut() + "][BCI_INI]");
        }
        if(getLogger().isDebugEnabled()){
            getLogger().debug("[mostrarCondicionesCredito]  <=tipoDeCreditoSeleccionado]"
                    + tipoDeCreditoSeleccionado);
        }
        String tipoOperacion = "";
        String codigoAuxiliar = "";
        int indicadorNP01 = 0;
        int indicadorNP02 = 0;
        int rutDeudor = (int) clienteMB.getRut();
        char digitoVerificador = clienteMB.getDigitoVerif();
        int cuentaAbono = 0;
        int cuentaCargo = 0;
        MultiEnvironment multiEnvironment = null;
        String[] segurosSeleccionados = null;
        String fechaPrimerVcto = null;
        double tasaPropuesta  = 0.0D;
        double  montoCredito= 0;
        EstructuraVencimiento[] vencimientos = new EstructuraVencimiento[1];
        try{
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[mostrarCondicionesCredito][condicionGarantiaIni] :" +condicionGarantiaIni);
            }
        	tipoOperacion = tipoDeCreditoSeleccionado.substring(0, VALOR_TRES);
	        codigoAuxiliar = tipoDeCreditoSeleccionado.substring(VALOR_TRES, VALOR_SEIS);
	        String montoSolicitadoCliente = montoSolicitado.replace(".","").replace(",","");
	        montoCredito = Double.parseDouble(montoSolicitadoCliente);
	        montoCorreo = montoCredito;
	        cuentaAbono = Integer.parseInt((String) productosMB.getCuentasCorrientesYPrimas()[0].getValue());
	        cuentaCargo = Integer.parseInt((String)productosMB.getCuentasCorrientesYPrimas()[0].getValue());
	 
	        fechaPrimerVcto = fechaVencimientoSeleccionada.substring(0,VALOR_DOS) 
	                + fechaVencimientoSeleccionada.substring(VALOR_TRES,VALOR_CINCO)
	                + fechaVencimientoSeleccionada.substring(VALOR_SEIS,VALOR_DIEZ);
	        noCargaMetodosInicio = true;
	        multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
	        		sesion.getCanalId(), USUARIO);
            if(getLogger().isDebugEnabled()){
                getLogger().debug("[mostrarCondicionesCredito] cuota Seleccionada antes" + cuotaSeleccionada);
            }
	        if (codigoAuxiliar.equals("010") || codigoAuxiliar.equals("326")){
	            cuotaSeleccionada = 1;
	        }
            if(getLogger().isDebugEnabled()){
                getLogger().debug("[mostrarCondicionesCredito] cuota Seleccionada" + cuotaSeleccionada);
            }
	        vencimientos[0] = new EstructuraVencimiento(0, ' ', 0, cuotaSeleccionada, formatoFecha1.parse(
	                fechaPrimerVcto + hhmmss), 0, 0, ' ', "", 0.0, 1, 'M', ' ');
	        int contSegurosSeleccionados = 0;
	        int largoArregloChecks = 0;
	        if (segurosObtenidos != null){
	            for (int i = 0; i < segurosObtenidos.length; i++) {
	                if (segurosObtenidos[i].isCheckeado()){
	                    largoArregloChecks++;
	                }
	            }
	            segurosSeleccionados = new String[largoArregloChecks];
	            for (int i = 0; i < segurosObtenidos.length; i++) {
	                if (segurosObtenidos[i].isCheckeado()){
	                    segurosSeleccionados[contSegurosSeleccionados] = segurosObtenidos[i].getCodigoSubConcepto()
	                            +segurosObtenidos[i].getIndCobro();
	                    contSegurosSeleccionados++;
	                }
	            } 
	        }
	        else{
	            if(getLogger().isDebugEnabled()){
	                 getLogger().debug("[mostrarCondicionesCredito] ingreso por primera vez SIN_SEGUROS");
	            }
	            segurosSeleccionados = new String[]{"SIN_SEGUROS"};
	        }
        }   
        catch (Exception ex){
            if(getLogger().isEnabledFor(Level.ERROR)){
                getLogger().error("[mostrarCondicionesCredito]" +  "mensaje=< "+ ex.getMessage() +">", ex);
            }
            String codEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codEventoSimulaNOOK");
            String codSubEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codSubEventoSimulaNOOK");
            String producto = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "productoSimulaNOOK");
            journalizar(codEvento,codSubEvento,producto);
            esErrorGenerico = true;
            paso = -1;
        }
        
        DatosResultadoOperacionesTO datosCondiciones = null;
        DatosParaCondicionesCreditoTO datosConCred = new DatosParaCondicionesCreditoTO();
        try{
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[mostrarCondicionesCredito]entre a setear para [obtenerCondicionesCredito] ");
            }
        	String moneda = StringUtils.rightPad(TablaValores.getValor(TABLA_MULTILINEA,
      				tipoDeCreditoSeleccionado.trim(),"moneda"), LARGO_CODIGO_MONEDA);
            codMoneda = moneda;
            tipoDePlazo = TablaValores.getValor(TABLA_MULTILINEA, "tipoDePlazo", "desc");
            datosConCred.setTipoOperacion(tipoOperacion);
            datosConCred.setMoneda(moneda);
            datosConCred.setCodigoAuxiliar(codigoAuxiliar);
            datosConCred.setOficinaIngreso("");
            datosConCred.setRutDeudor(rutDeudor);
            datosConCred.setDigitoVerificador(digitoVerificador);
            datosConCred.setMontoCredito(montoCredito);
            datosConCred.setAbono("CCMA");
            datosConCred.setCargo("AUT");
            datosConCred.setCtaAbono(cuentaAbono);
            datosConCred.setCtaCargo(cuentaCargo);
            datosConCred.setIndicadorNP01(indicadorNP01);
            datosConCred.setIndicadorNP02(indicadorNP02);
            datosConCred.setCodBanca(codBanca);
            datosConCred.setPlan(plan);
            datosConCred.setCodSegmento(codSegmento);
            datosConCred.setCaiNumOpe(caiOperacion);
            datosConCred.setIicNumOpe(iicOperacion);
            datosConCred.setTasaPropuesta(tasaPropuesta);
            datosConCred.setBandera("N");
            datosConCred.setEjecutivo("");
            datosConCred.setSeguros(segurosSeleccionados);
            datosConCred.setCuotaSeleccionada(cuotaSeleccionada);
            datosConCred.setFechaPrimerVcto(fechaPrimerVcto);
            datosConCred.setCondicionGarantia(condicionGarantiaIni);
            
            if (datosAvales != null && datosAvales.length > 0){
            	datosConCred.setTieneAvales(true);
            }
            
            datosCondiciones =  crearEJBmultilinea().obtenerCondicionesCredito(
                    multiEnvironment, 
                    vencimientos, 
                    datosConCred);
            if (datosCondiciones != null){
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[mostrarCondicionesCredito]datosCondiciones distinto de null] ");
                }
                
            	if (datosCondiciones.getExcepciones() != null){
            		if (excepciones != null){
            			int largoExcepciones = datosCondiciones.getExcepciones().length;
                		for (int i = 0; i < largoExcepciones; i++) {
                			excepciones.add(datosCondiciones.getExcepciones()[i]);
    					}
            		}
            		else {
            			excepciones  = new ArrayList<String>(Arrays.asList(datosCondiciones.getExcepciones()));
            		}
    			}
                
                valorCuota = datosCondiciones.getValorCuota();
                tasaInteresInternet = datosCondiciones.getTasaInteresInternet();
                impuestos =  datosCondiciones.getImpuestos();
                gastosNotariales =  datosCondiciones.getValorGastoNotarial();
                costoFinalCredito = datosCondiciones.getCostoFinalCredito();
                factorCae = datosCondiciones.getFactorCae();
                segurosObtenidos = datosCondiciones.getDatosSeguros();
                montoTotalSegurosSeleccionados = datosCondiciones.getCostoTotalSeguros();
                String fechaCurseDatos = FechasUtil.convierteDateAString(datosCondiciones.getFechaCurse(),
                        "yyyyMMdd"); 
                fechaCurse = fechaCurseDatos;
                calendarioPagoSalida = datosCondiciones.getCalendario();
                requierenDPS = datosCondiciones.getRequiereDPS();
                montoFinalCredito = datosCondiciones.getMontoFinalCredito(); 
                condicionGarantia = datosCondiciones.getCondicionGarantia();
                
                if (condicionGarantia.trim().equals("4")){
                    String codEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codEventoSinGarantia");
                    String codSubEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codSubEventoSinGarantia");
                    String producto = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "productoSinGarantia");
                    journalizar(codEvento,codSubEvento,producto);
                }
                
                caiOperacion = datosCondiciones.getCaiOperacion();
                iicOperacion =datosCondiciones.getIicOperacion();
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[mostrarCondicionesCredito ]valorCuota =<" 
                            + valorCuota + ">,tasaInteresInternet=<"
                            + tasaInteresInternet + ">, impuestos=<"
                            + factorCae + ">, factorCae=<"
                            + impuestos + ">,costoFinalCredito=<" 
                            + costoFinalCredito + ">,montoFinalCredito=<"+ montoFinalCredito +">, condicionGarantgia=<"
                            + condicionGarantia +">");
                }
                if (datosCondiciones.getAvales() != null &&  datosCondiciones.getAvales().length >0){
                	datosAvales = datosCondiciones.getAvales();
                	datosAvalesCorreo = datosCondiciones.getAvalesCorreo();
                }
                String codEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codEventoSimulaOK");
                String codSubEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codSubEventoSimulaOK");
                String producto = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "productoSimulaOK");
                journalizar(codEvento,codSubEvento,producto);
            }
            paso = 1;
        }
        catch (Exception ex){
            if(getLogger().isEnabledFor(Level.ERROR)){
                getLogger().error("[mostrarCondicionesCredito] error :BCI_FINEX" +  "mensaje=< "+ ex.getMessage() +">", ex);
            }
            String codEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codEventoSimulaNOOK");
            String codSubEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codSubEventoSimulaNOOK");
            String producto = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "productoSimulaNOOK");
            journalizar(codEvento,codSubEvento,producto);
            esErrorGenerico = true;
            paso = -1;
        }
        if (getLogger().isDebugEnabled()) {
             getLogger().debug("[mostrarCondicionesCredito][" + clienteMB.getRut() + "][BCI_FINOK]");
        }
    }
    
    /**
     * Método utilizado obtener los datos ingresados por el cliente en el avance,
     * y revisar si se envia a cuarto clic o pasa a realizar el avance.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 09/12/2014 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li>
     * <li>1.1 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se modifica cambio de paso.</li>
     * <li>1.2 22/06/2016, Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): Se agrega condición con tipo de usuario y perfil.</li>
     * <li>1.3 17/08/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega lógica de discleimer.</li>
     * </ul>
     * @throws Exception en caso de error. 
     * @since 1.0
     */  
    public void continuarAvance() throws Exception {
        if (getLogger().isDebugEnabled()) {
             getLogger().debug("[continuarAvance][" + clienteMB.getRut() + "][BCI_INI]");
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[continuarAvance][tipoDeCreditoSeleccionado]" + tipoDeCreditoSeleccionado);
            getLogger().debug("[continuarAvance][cuotaSeleccionada]" + cuotaSeleccionada);
       }
    	if (getLogger().isInfoEnabled()) {
    		getLogger().info( "[continuarAvance]" 
    				+ "Datos Usuario perfil<" + usuarioModelMB.getPerfil()+ "> ,"
    				+ "tipoUsuario<" + usuarioModelMB.getTipoUsuario() + ">.");
    	}
    	if (usuarioModelMB.getTipoUsuario().trim().equalsIgnoreCase(TIPO_USUARIO) && usuarioModelMB.getPerfil().trim().equalsIgnoreCase(PERFIL_USUARIO)){
        try{
	        setCuentasCorrientesYPrimas(productosMB.getCuentasCorrientesYPrimas());
	        if (getCuentasCorrientesYPrimas()!=null){
	            if (getLogger().isDebugEnabled()) {
	            	getLogger().debug("[continuarAvance] cuentas: "
	                        + getCuentasCorrientesYPrimas().length);
	                for (int i = 0; i< getCuentasCorrientesYPrimas().length; i++){
	                	getLogger().debug("[continuarAvance] cuentas: "
	                            + getCuentasCorrientesYPrimas()[i].getLabel()
	                            + "    " + getCuentasCorrientesYPrimas()[i]
	                                    .getValue());
	                }
	            }   
	        }
	       
	        if (excepciones != null && excepciones.size() > 0){
	        	if (getLogger().isDebugEnabled()) {
	        		getLogger().debug("[continuarAvance][tengo excepciones]");
	        	}
	        	paso = VALOR_TRES;
	        	this.cargarRegiones();
	        }
            else {
	        if (ofertaCrediticia.equals(ACTIVA) && tieneMultilinea && tieneMandato){
	        	
	        	if (getLogger().isDebugEnabled()) {
	        		getLogger().debug("[continuarAvance] condicionGarantia: " + condicionGarantia);
	        	}
					if (condicionGarantia != null && !condicionGarantia.trim().equals(GARANTIA_AVALES)){ 
						mostrarCondicionesMandato = false;
					}
					else{ 
				        String montoSolicitadoCliente = montoSolicitado.replace(".","").replace(",","");
						DatosDisclaimerTO datos = new DatosDisclaimerTO();
						datos.setRutCliente(String.valueOf(usuarioModelMB.getRut() +"-"+ usuarioModelMB.getDigitoVerif()));
						datos.setRutEmpresa(String.valueOf(clienteMB.getRut() +"-"+  clienteMB.getDigitoVerif()));
						datos.setNombreCliente(usuarioModelMB.getNombres() + " " + usuarioModelMB.getApPaterno() + " " + usuarioModelMB.getApMaterno());
						datos.setNombreempresa(clienteMB.getRazonSocial());
						datos.setMonto(formatearMonto(Double.parseDouble(montoSolicitadoCliente), 0, "#,###"));
						disclaimerMandato = crearEJBmultilinea().obtieneDisclaimerMandato(datos);
					}
					
	            paso = VALOR_DOS;
	        } 
	        else {  
	            paso = VALOR_TRES;    
	            this.cargarRegiones(); 
	        }
            }
        }
        catch (Exception ex){
        	 if(getLogger().isEnabledFor(Level.ERROR)){
                 getLogger().error("[continuarAvance]" 
                         +  "mensaje=< "+ ex.getMessage() +">", ex);
             }	
    			esErrorGenerico = true;
    			paso = -1;
    		}
    	}
    	else {
    		if(getLogger().isEnabledFor(Level.ERROR)){
    			getLogger().error("[continuarAvance]" 
    					+  "mensaje=< "+ "Usuario no Autorizado para esta operación >");
    		}
    		mensajeError =  TablaValores.getValor(TABLA_MULTILINEA,
    				"usuarioAutorizado","mensaje"); 
    		esErrorGenerico = false;
        	  paso = -1;
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[continuarAvance][" + clienteMB.getRut() + "][BCI_FIN]");
       }
    }


    /**
     * Método encargado de realizar el curse del avance.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 09/12/2014 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li>
     * <li>1.1 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega validacion de cuenta.</li>
     * <li>1.2 03/03/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agregan excepciones. </li>
     * <li>1.3 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega validacion de mandato.</li>
     * <li>1.4 10/06/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega seteo de condición de garantía obtenida.</li>
     * <li>1.5 10/06/2016, Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): Se agregan logs.</li>
     * <li>1.6 20/07/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega seteo para valor de gasto notarial.</li>
     * </ul>
     * @throws Exception en caso de error.
     * @since 1.0
     */  
    public void cursarAvance() throws Exception{
        if (getLogger().isDebugEnabled()) {
             getLogger().debug("[cursarAvance][" + clienteMB.getRut() + "][BCI_INI]");
        }
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[cursarAvance][caiOperacion] [" + caiOperacion + "] [iicOperacion] " + iicOperacion + "]");
    	}

        if(aceptaCondicionesMandato){
            String codEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codEventoAceptaCondicionesMandato");
            String codSubEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codSubEventoAceptaCondicionesMandato");
            String producto = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "productoAceptaCondicionesMandato");
            journalizar(codEvento,codSubEvento,producto);
        }

        try {
             if (!ValidaCuentasUtility.validarCuentaBCI( String.valueOf(Integer.parseInt(cuentaSeleccionadaAbono)), "CCT", (int) clienteMB.getRut(), clienteMB.getDigitoVerif())) {
                if (getLogger().isEnabledFor(Level.ERROR)) {
                    getLogger().error("[cursarAvance][" + clienteMB.getRut() + "] ha fallado la validacion de la cuenta de abono");
                }
                throw new Exception("Problemas al validar la cuenta de abono del cliente");
            }
          
            boolean autenticacionSegundaClave = false;
            try {
            	autenticacionSegundaClave = segundaClaveAplicativo.verificarAutenticacion();
            	if (autenticacionSegundaClave){
            		if (getLogger().isDebugEnabled()) {
            			getLogger().debug("[cursarAvance] [" + clienteMB.getRut() + "] Autenticacion OK");
            		}
            		
            		String codEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codEventoAutorizaCurse");
            		String codSubEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codSubEventoAutorizaCurse");
            		String producto = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "productoAceptaCondicionesMandato");
            		journalizar(codEvento,codSubEvento,producto);
            	}
            }
            catch (SeguridadException ex) {
            	if (getLogger().isEnabledFor(Level.ERROR)) {
            		getLogger().error("[cursarAvance] [BCI_FINEX] [" + clienteMB.getRut()  + "] [BCI_FINEX] "
            				+ "[SeguridadException] Error con mensaje " +ex.getInfoAdic());
            	}
            	FacesMessage mensaje = new FacesMessage(ex.getInfoAdic());
            	mensaje.setSeverity(FacesMessage.SEVERITY_ERROR);
            	FacesContext.getCurrentInstance().addMessage("segundaClave", mensaje);
            	esErrorGenerico = true;
            	paso = -1;
            }   
            if (autenticacionSegundaClave){
                        String iccOpe = String.valueOf(iicOperacion);
                        String numOperacion = caiOperacion + iccOpe;
                        MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
                                sesion.getCanalId(), USUARIO);
                        String[] segurosSeleccionados = null;
                        String tipoOperacion = tipoDeCreditoSeleccionado.substring(0, VALOR_TRES);
                        String codigoAuxiliar = tipoDeCreditoSeleccionado.substring(VALOR_TRES, VALOR_SEIS);
                        
                        glosaTipoCredito = TablaValores.getValor(TABLA_MULTILINEA,
                                tipoDeCreditoSeleccionado,"glosa"); 
                        int indicadorNP01 = 0;
                        int indicadorNP02 = 0;
                        int rutDeudor = (int) clienteMB.getRut();
                        char digitoVerificador = clienteMB.getDigitoVerif();
                        double tasaPropuesta  = 0.0D;
                        String fechaPrimerVcto = null;
                        fechaPrimerVcto = fechaVencimientoSeleccionada.substring(0,VALOR_DOS) 
                                + fechaVencimientoSeleccionada.substring(VALOR_TRES,VALOR_CINCO) 
                                + fechaVencimientoSeleccionada.substring(VALOR_SEIS,VALOR_DIEZ);
                        int contSegurosSeleccionados = 0;
                        int largoArregloChecks = 0;
                        String montoSolicitadoCliente = montoSolicitado.replace(".","").replace(",","");
                        double  montoCredito= Double.parseDouble(montoSolicitadoCliente);
            
                        if (segurosObtenidos != null){
                            for (int i = 0; i < segurosObtenidos.length; i++) {
                                if (segurosObtenidos[i].isCheckeado()){
                                    largoArregloChecks++;
                                }
                            }
                            segurosSeleccionados = new String[largoArregloChecks];
                            for (int i = 0; i < segurosObtenidos.length; i++) {
                                if (segurosObtenidos[i].isCheckeado()){
                                    segurosSeleccionados[contSegurosSeleccionados] 
                                            = segurosObtenidos[i].getCodigoSubConcepto()
                                            +segurosObtenidos[i].getIndCobro();
                                    contSegurosSeleccionados++;
                                }
                            }
                        }
            
                        EstructuraVencimiento[] vencimientos = new EstructuraVencimiento[1];
                        vencimientos[0] = new EstructuraVencimiento(0, ' ', 0, 
                                cuotaSeleccionada, formatoFecha1.parse(
                                fechaPrimerVcto + hhmmss), 0, 0, ' ', "", 0.0, 1, 'M', ' ');
            
                        DatosResultadoOperacionesTO datosCondiciones = null;
                        int cuentaAbono = Integer.parseInt(cuentaSeleccionadaAbono);
                        int cuentaCargo = Integer.parseInt(cuentaSeleccionadaCargo);
                        String moneda = StringUtils.rightPad(TablaValores.getValor(TABLA_MULTILINEA,
                  				tipoDeCreditoSeleccionado.trim(),"moneda"), LARGO_CODIGO_MONEDA);
                        try{
                            DatosParaCondicionesCreditoTO datosConCred = new DatosParaCondicionesCreditoTO();
                            datosConCred.setTipoOperacion(tipoOperacion);
                            datosConCred.setMoneda(moneda);
                            datosConCred.setCodigoAuxiliar(codigoAuxiliar);
                            datosConCred.setOficinaIngreso(oficinaIngreso);
                            datosConCred.setRutDeudor(rutDeudor);
                            datosConCred.setDigitoVerificador(digitoVerificador);
                            datosConCred.setMontoCredito(montoCredito);
                            datosConCred.setAbono("CCMA");
                            datosConCred.setCargo("AUT");
                            datosConCred.setCtaAbono(cuentaAbono);
                            datosConCred.setCtaCargo(cuentaCargo);
                            datosConCred.setIndicadorNP01(indicadorNP01);
                            datosConCred.setIndicadorNP02(indicadorNP02);
                            datosConCred.setCodBanca(codBanca);
                            datosConCred.setPlan(plan);
                            datosConCred.setCodSegmento(codSegmento);
                            datosConCred.setCaiNumOpe(caiOperacion);
                            datosConCred.setIicNumOpe(iicOperacion);
                            datosConCred.setTasaPropuesta(tasaPropuesta);
                            datosConCred.setBandera("N");
                            datosConCred.setEjecutivo(codigoEjecutivo);
                            datosConCred.setSeguros(segurosSeleccionados);
                            datosConCred.setCuotaSeleccionada(cuotaSeleccionada);
                            datosConCred.setFechaPrimerVcto(fechaPrimerVcto);
                            datosConCred.setCondicionGarantia(condicionGarantiaIni);
                                        
                            datosCondiciones =  crearEJBmultilinea().obtenerCondicionesCredito(
                                    multiEnvironment, 
                                    vencimientos, 
                                    datosConCred);
            
                            if (datosCondiciones != null){
                            	if (datosCondiciones.getExcepciones() != null){
                            		if (excepciones != null){
                            			int largoExcepciones = datosCondiciones.getExcepciones().length;
                            			for (int i = 0; i < largoExcepciones; i++) {
                            				excepciones.add(datosCondiciones.getExcepciones()[i]);
                            			}
                            		}
                            		else {
                            			excepciones  = new ArrayList<String>(Arrays.asList(datosCondiciones.getExcepciones()));
                            		}
                            	}
                                valorCuota = datosCondiciones.getValorCuota();
                                tasaInteresInternet = datosCondiciones.getTasaInteresInternet();
                                impuestos =  datosCondiciones.getImpuestos();
                                gastosNotariales =  datosCondiciones.getValorGastoNotarial();
                                costoFinalCredito = datosCondiciones.getCostoFinalCredito();
                                factorCae = datosCondiciones.getFactorCae();
                                segurosObtenidos = datosCondiciones.getDatosSeguros();
                                montoTotalSegurosSeleccionados = datosCondiciones.getCostoTotalSeguros();
                                montoFinalCredito = datosCondiciones.getMontoFinalCredito(); 
                                condicionGarantia = datosCondiciones.getCondicionGarantia();
                                caiOperacion = datosCondiciones.getCaiOperacion();
                                iicOperacion = datosCondiciones.getIicOperacion();
                                if (getLogger().isDebugEnabled()) {
                                    getLogger().debug("[mostrarCondicionesCredito ]valorCuota =<" 
                                            + valorCuota + ">,tasaInteresInternet=<"
                                            + tasaInteresInternet + ">, impuestos=<"
                                            + factorCae + ">, factorCae=<"
                                            + impuestos + ">,costoFinalCredito=<" 
                            				+ costoFinalCredito + ">,montoFinalCredito=<"+ montoFinalCredito +">,"
                            						+ "condicionGarantia=<" +condicionGarantia +">");
                                }
                            }
                            
                            String fecExpiracion      = "01/01/1900";       
                            String autoTransac        = "8";
                
                            DatosParaOperacionesFirmaTO datosOperacionesFirma = new DatosParaOperacionesFirmaTO();
                            datosOperacionesFirma.setAutoTransac(autoTransac);
                            datosOperacionesFirma.setAuxiliarCredito(iccOpe);
                            datosOperacionesFirma.setAuxiliarOpe(codigoAuxiliar);
                            datosOperacionesFirma.setCodAuxiliarCredito(" ");
                            datosOperacionesFirma.setCodigoMoneda(moneda);
                            datosOperacionesFirma.setCodigoMoneda2(" ");
                            datosOperacionesFirma.setCuentaAbono(cuentaSeleccionadaAbono);
                            datosOperacionesFirma.setCuentaCargo(cuentaSeleccionadaCargo);
                            datosOperacionesFirma.setDigitoVerifEmp(clienteMB.getDigitoVerif());
                            datosOperacionesFirma.setDvUsuario(String.valueOf(usuarioModelMB.getDigitoVerif()));
                            datosOperacionesFirma.setEstadoSolicitud("PEN");
                            datosOperacionesFirma.setFechaExpiracion(fecExpiracion);
                            datosOperacionesFirma.setFechaExpiracion2(" ");
                            datosOperacionesFirma.setFechaFin(fechaVencimientoSeleccionada.substring(
                                    VALOR_SEIS,VALOR_DIEZ)+fechaVencimientoSeleccionada.substring(
                                    		VALOR_TRES,VALOR_CINCO)
                                    +fechaVencimientoSeleccionada.substring(0,VALOR_DOS));
                            datosOperacionesFirma.setFechaInicio(formatoFecha2.format(new Date()));
                            datosOperacionesFirma.setFechaPrimerVencimiento(fechaVencimientoSeleccionada.substring(
                                    VALOR_SEIS,VALOR_DIEZ)+fechaVencimientoSeleccionada.substring(
                                    		VALOR_TRES,VALOR_CINCO)
                                    +fechaVencimientoSeleccionada.substring(0,VALOR_DOS));
                            datosOperacionesFirma.setGlosaTipoCredito(glosaTipoCredito);
                            datosOperacionesFirma.setIdConvenio(clienteMB.getNumeroConvenio());
                            datosOperacionesFirma.setIndicadorAplic("0"); 
                            datosOperacionesFirma.setMonedaLinea(" ");
                            datosOperacionesFirma.setMontoAbonado("0");
                            datosOperacionesFirma.setMontoCredito(String.valueOf(montoFinalCredito)); 
                            datosOperacionesFirma.setNumeroAutorizacion("");
                            datosOperacionesFirma.setNumOperacion(caiOperacion);
                            datosOperacionesFirma.setNumOperacionCan(" ");
                            datosOperacionesFirma.setOficinaIngreso(oficinaIngreso);
                            datosOperacionesFirma.setProcesoNegocio("AVC");
                            datosOperacionesFirma.setRutEmpresa(String.valueOf(clienteMB.getRut()));
                            datosOperacionesFirma.setRutUsuario(String.valueOf(usuarioModelMB.getRut()));
                            datosOperacionesFirma.setTipoAbono("CCMA");
                            datosOperacionesFirma.setTipoCargoAbono("AUT");
                            datosOperacionesFirma.setTipoOperacion(tipoOperacion);
                            datosOperacionesFirma.setTotalVencimientos(String.valueOf(cuotaSeleccionada));
                            datosOperacionesFirma.setNumOperacionValidacionFirma(numOperacion);
                            datosOperacionesFirma.setCondicionGarantia(condicionGarantia);
                            datosOperacionesFirma.setQuienVisa(VALOR_DOS);
                            int resultadoFirma = ingresarOperacionFirma(multiEnvironment,datosOperacionesFirma);
                            if (getLogger().isDebugEnabled()){
                                getLogger().debug("[cursarAvance] [ingresarOperacionFirma] resultadoFirma:"+resultadoFirma);
                            }
                         
                            if (resultadoFirma == VALOR_MIL){
                            	if (getLogger().isDebugEnabled()){
                                      getLogger().debug("[cursarAvance] [correo a ejecutivo]");
                                }
                            	String codEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codEventoCurseNOOK");
                            	String codSubEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codSubEventoCurseNOOK");
                            	String producto = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "productoCurseNOOK");
                            	journalizar(codEvento,codSubEvento,producto);
                                paso = VALOR_TRES;
                                this.cargarRegiones(); 
                            }
                            else {
                                resultadoFirmaAvance = String.valueOf(resultadoFirma);
                                if (getLogger().isDebugEnabled()) {
                                	getLogger().debug("[cursarAvance][resultadoFirmaAvance]:" +resultadoFirmaAvance);
                                }
                                paso = VALOR_CUATRO;
                                if (resultadoFirma == 1){
                                String codEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codEventoCurseOK");
                                String codSubEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codSubEventoCurseOK");
                                String producto = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "productoCurseOK");
                                journalizar(codEvento,codSubEvento,producto);
                                }
                                else {
                                	String codEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codEventoCurseNOOK");
                                	String codSubEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codSubEventoCurseNOOK");
                                	String producto = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "productoCurseNOOK");
                                	journalizar(codEvento,codSubEvento,producto);
                                }
                            }
                        }
                        catch (Exception ex){
                            if(getLogger().isEnabledFor(Level.ERROR)){
                                getLogger().error("[mostrarCondicionesCredito]" 
                                        +  "[BCI_FINEX] mensaje=< "+ ex.getMessage() +">", ex);
                            }
                            paso = VALOR_TRES;
                            this.cargarRegiones(); 
                        }
            }
            else {
                paso = VALOR_DOS;
            }
        }
        catch (Exception ex){
            if(getLogger().isEnabledFor(Level.ERROR)){
                getLogger().error("[cursarAvance]" +  " [BCI_FINEX] mensaje=< "+ ex.getMessage() +">", ex);
            }
            String codEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codEventoCurseNOOK");
            String codSubEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "codSubEventoCurseNOOK");
            String producto = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "productoCurseNOOK");
            journalizar(codEvento,codSubEvento,producto);
            esErrorGenerico = true;
    		paso = -1;
        }
        if (getLogger().isDebugEnabled()) {
             getLogger().debug("[cursarAvance]["+clienteMB.getRut()+"][BCI_FIN]");
        }
    }

    /**
     * Método encargado de realizar el curse del avance.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 09/12/2014 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li>
     * </ul>
     * @throws Exception 
     * @since 1.0
     */  
    public void cambiarValoresTipoCredito() throws Exception{
        String fechaFormateada = "";
        Date fechaActual = new Date();
        String formato = TablaValores.getValor(TABLA_MULTILINEA, "formatoFecha", "formato");
        Date fechaSuma = null;
        
        try{
        if (datosPlantilla != null){ 
        	if (getLogger().isDebugEnabled()) {
        		getLogger().debug("tipoDeCreditoSeleccionado " + tipoDeCreditoSeleccionado);
        	}
            if (tipoDeCreditoSeleccionado != ""){
                cuotas.clear();
                String codigoAuxiliarPlantilla = tipoDeCreditoSeleccionado.substring(VALOR_TRES, VALOR_SEIS);
                if (getLogger().isDebugEnabled()) {
                	getLogger().debug("codigoAuxiliarPlantilla" + codigoAuxiliarPlantilla);
                }
                for (int i = 0; i < datosPlantilla.length; i++) {
                    if (codigoAuxiliarPlantilla.equals(datosPlantilla[i].getCodigoAuxiliar())){
                        montoMinimoParaCursar = datosPlantilla[i].getCreditoMinimo();
                        montoMaximoParaCursar = datosPlantilla[i].getCreditoMaximo();
                        cuotaInicial = datosPlantilla[i].getCuotaInicial();
                        cuotaFinal =  datosPlantilla[i].getCuotaFinal();
                        fechaSuma = obtenerFechaMaximaVencimiento(
                                fechaActual,datosPlantilla[i].getMaximoPrimerPago());
                        fechaFormateada = FechasUtil.convierteDateAString(fechaSuma, formato);
                        diasTopePrimerVencimiento = String.valueOf(datosPlantilla[i].getMaximoPrimerPago());
                        diasIniPrimerVencimiento = String.valueOf(datosPlantilla[i].getMinimoPrimerPago());
                        if (getLogger().isDebugEnabled()) {
                        	getLogger().debug("diasTopePrimerVencimiento  " + diasTopePrimerVencimiento);
                        	getLogger().debug("diasIniPrimerVencimiento   " + diasIniPrimerVencimiento);
                        }
                    }
                } 
                for (int i = cuotaInicial; i <= cuotaFinal; i++) {
                    String cuota = String.valueOf(i);
                    cuotas.add(new SelectItem(i,cuota));
                } 
            }
            else {
                cuotas.clear();
                fechaFormateada = "";  
                montoMinimoParaCursar = 0;
                montoMaximoParaCursar = 0;
                diasIniPrimerVencimiento = "0";
                diasTopePrimerVencimiento = "0";
            } 
        } 
        fechaMaximaPrimerVencimiento =fechaFormateada;
        noCargaMetodosInicio = true;
        }
        catch (Exception ex){
        	 if(getLogger().isEnabledFor(Level.ERROR)){
                 getLogger().error("[cambiarValoresTipoCredito]" 
                         +  "mensaje=< "+ ex.getMessage() +">", ex);
             }
        }
    }

    /**
     * Metodo que se encarga de cargar las obtener y cargar las regiones.
     * 
     * Registro de versiones:
     * <ul>
     * <li>1.0 (19/03/2015, Manuel Escárate R. (BEE)) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.
     * </ul>
     * 
     * @since 1.0
     */
    public void cargarRegiones() {
        if (getLogger().isDebugEnabled()){
            getLogger().debug("[cargarRegiones][BCI_INI]");    
        }
        if ((this.regiones == null) || (this.regiones.size() == 0)){
            try {
                this.regiones = new ArrayList<SelectItem>();
                RegionTO[] regionesObtenidas= regionSupportMB.obtenerRegiones();
                if ((regionesObtenidas != null) && (regionesObtenidas.length > 0)){
                    for (int i = 0; i < regionesObtenidas.length; i++) {
                        this.regiones.add(new SelectItem(regionesObtenidas[i].getCodigo()
                        		,regionesObtenidas[i].getNombre()));
                    }
                }
            }
            catch (Exception ex) {
                if (getLogger().isEnabledFor(Level.ERROR)) {
                    getLogger().debug("[cargaCiudades] Exception:" + ErroresUtil.extraeStackTrace(ex));
                }
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[cargarRegiones][BCI_FINOK] retornando objeto : " + this.regiones);
            }
        }
    }    
    
    /**
     * Método que envía la solictud a ejecutivo.
     * 
     * Registro de versiones:
     * <ul>
     * <li>1.0 (19/03/2015, Manuel Escárate R. (BEE)) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.
     * <li>1.1 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): se agrega atributo para correo backoffice.</li>
     * <li>1.2 03/03/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agregan datos para correo. </li>
     * <li>1.3 05/08/2016, Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): Se cambia flag de envio de correo a backoffice por true. </li>
     * <li>1.4 18/08/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se modifica monto dentro de los datos de correo. </li>
     * </ul>
     * 
     * @throws Exception en caso de error.
     * @since 1.0
     */
    public void enviarSolicitud() throws Exception{
    	if (getLogger().isDebugEnabled()) {
			getLogger().debug("[enviarSolicitud][BCI_INI]");
		}
    	try{
    		boolean enviaBackOffice = true;
    		MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
					sesion.getCanalId(), USUARIO);
    		if (getLogger().isDebugEnabled()) {
    			getLogger().debug("[enviarSolicitud][enviaCorreoBackOffice]" + enviaCorreoBackOffice);
    		}
            if (enviaCorreoBackOffice) { 
    			String destinoDelCredito = "";
    	    	try {
    	    		if (getLogger().isDebugEnabled()) getLogger().debug("[enviarSolicitud] consultando destinoDelCredito      [" + caiOperacion + iicOperacion + "]" );
    	    		ResultConsultaOperacionCredito resConOpe = crearEJBmultilinea().consultaOperacionCredito(multiEnvironment, caiOperacion, iicOperacion);
    	    		destinoDelCredito  = resConOpe.getGlosaDestinoEspecifico();
    	    		if (getLogger().isDebugEnabled()) getLogger().debug("[enviarSolicitud] despues destinoDelCredito          [" + caiOperacion + iicOperacion + "]" );
    	    	}
    	    	catch (Exception ex) {
    	    		if (getLogger().isDebugEnabled()) getLogger().debug("[enviarSolicitud] Error en consultaOperacionCredito ::"+ ex.getMessage());
    	    		destinoDelCredito = " ";
    	    	}
    			String estado =TablaValores.getValor(TABLA_MULTILINEA, "multilineaBackOffice", "estado");
    			String asunto =TablaValores.getValor(TABLA_MULTILINEA, "multilineaBackOffice", "descripcion");
    			DatosParaCorreoTO datosCorreo = new DatosParaCorreoTO();
    	    	datosCorreo.setCaiOperacion(caiOperacion);
    	    	datosCorreo.setIicOperacion(iicOperacion);
    	    	datosCorreo.setFechaVencimientoSeleccionada(fechaVencimientoSeleccionada);
    	    	datosCorreo.setRut(clienteMB.getRut());
    	    	datosCorreo.setDv(clienteMB.getDigitoVerif());
    	    	datosCorreo.setRazonSocial(clienteMB.getRazonSocial());
    	    	datosCorreo.setMontoFinalCredito(montoCorreo);
    	    	datosCorreo.setCuotaSeleccionada(cuotaSeleccionada);
    	    	datosCorreo.setDestinoDelCredito(destinoDelCredito);
    	    	datosCorreo.setTasaInteres(formatearMonto((tasaInteresInternet),VALOR_DOS,"#,##0"));
    	    	datosCorreo.setEstado(estado);
    	    	datosCorreo.setAsunto(asunto);
    	    	datosCorreo.setEnviaBackOffice(enviaBackOffice);
                datosCorreo.setRutOperador(usuarioModelMB.getRut());
    	    	datosCorreo.setDvOperador(usuarioModelMB.getDigitoVerif());
    	    	datosCorreo.setCodTelefonoCliente(codTelefonoCliente);
    	    	datosCorreo.setTelefonoCliente(telefonoCliente);
    	    	datosCorreo.setCodCelularCliente(codCelularCliente);
    	    	datosCorreo.setCelularCliente(celularCliente);
    	    	datosCorreo.setEmailCliente(emailCliente);
    	    	datosCorreo.setRegionCliente(regionCliente);
    	    	datosCorreo.setComunaCliente(comunaCliente);
    	    	
    			enviaEmailBackOffice(multiEnvironment,datosCorreo);
      		}
            else {
            	enviarCorreoEjecutivo(multiEnvironment,"opeNoCursada","asuntoEmailProblemaAvance",enviaBackOffice);
            }
    		paso = VALOR_CINCO;        
    		if (getLogger().isDebugEnabled()) {
    			getLogger().debug("[enviarSolicitud][BCI_FINOK]");
    		}
    	}
    	catch (Exception ex){
    		 if (getLogger().isEnabledFor(Level.ERROR)) {
                 getLogger().debug("[enviarSolicitud] [BCI_FINEX] Exception:" + ErroresUtil.extraeStackTrace(ex));
             }
    	}
    }  
   
    /**
     * Método que envía correo a ejecutivo.
     * 
     * Registro de versiones:
     * <ul>
     * <li>1.0 19/03/2015, Manuel Escárate R. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li>
     * <li>1.1 03/03/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agregan datos para correo. </li>
     * <li>1.2 18/08/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se modifica monto dentro de los datos de correo. </li>
     * </ul>
     * 
     * @param multiEnvironment multiambiente.
     * @param estado estado.
     * @param asunto asunto.
     * @param enviaBackOffice flag envio a backoffice.
     * 
     * @throws GeneralException excepcion general.
     * @since 1.0
     */    
    public void enviarCorreoEjecutivo(MultiEnvironment multiEnvironment,String estado, String asunto,boolean enviaBackOffice) throws GeneralException{
    	if (getLogger().isDebugEnabled()){
    		getLogger().debug("[enviarCorreoEjecutivo][BCI_INI]");    
    	}
    	String destinoDelCredito = "";
    	try {
    		if (getLogger().isDebugEnabled()) getLogger().debug("consultando destinoDelCredito      [" + caiOperacion + iicOperacion + "]" );
    		ResultConsultaOperacionCredito resConOpe = crearEJBmultilinea().consultaOperacionCredito(multiEnvironment, caiOperacion, iicOperacion);
    		destinoDelCredito  = resConOpe.getGlosaDestinoEspecifico();
    		if (getLogger().isDebugEnabled()) getLogger().debug("despues destinoDelCredito          [" + caiOperacion + iicOperacion + "]" );
    	}
    	catch (Exception ex) {
    		if (getLogger().isDebugEnabled()) getLogger().debug("Error en consultaOperacionCredito ::"+ ex.getMessage());
    		destinoDelCredito = " ";
    	}
    	boolean cumple = cumpleHorarios();
    	DatosParaCorreoTO datosCorreo = new DatosParaCorreoTO();
    	datosCorreo.setCaiOperacion(caiOperacion);
    	datosCorreo.setIicOperacion(iicOperacion);
    	datosCorreo.setFechaVencimientoSeleccionada(fechaVencimientoSeleccionada);
    	datosCorreo.setRut(clienteMB.getRut());
    	datosCorreo.setDv(clienteMB.getDigitoVerif());
    	datosCorreo.setRazonSocial(clienteMB.getRazonSocial());
    	datosCorreo.setMontoFinalCredito(montoCorreo);
    	datosCorreo.setCuotaSeleccionada(cuotaSeleccionada);
    	datosCorreo.setDestinoDelCredito(destinoDelCredito);
    	datosCorreo.setTasaInteres(formatearMonto((tasaInteresInternet),VALOR_DOS,"#,##0"));
    	datosCorreo.setEstado(estado);
    	datosCorreo.setAsunto(asunto);
    	datosCorreo.setEnviaBackOffice(enviaBackOffice);
    	datosCorreo.setRutOperador(usuarioModelMB.getRut());
    	datosCorreo.setDvOperador(usuarioModelMB.getDigitoVerif());
    	datosCorreo.setCodTelefonoCliente(codTelefonoCliente);
    	datosCorreo.setTelefonoCliente(telefonoCliente);
    	datosCorreo.setCodCelularCliente(codCelularCliente);
    	datosCorreo.setCelularCliente(celularCliente);
    	datosCorreo.setEmailCliente(emailCliente);
    	datosCorreo.setRegionCliente(regionCliente);
    	datosCorreo.setComunaCliente(comunaCliente);

    	if (cumple) enviaEmailEjecutivo(multiEnvironment,datosCorreo);
    	if (getLogger().isDebugEnabled()){
    		getLogger().debug("[enviarCorreoEjecutivo][BCI_FIN]");    
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
     * Método que envía email a ejecutivo.
     * 
     * Registro de versiones:
     * <ul>
     * <li>1.0 19/03/2015, Manuel Escárate R. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li>
     * <li>1.1 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agregan excepciones posibles en el correo.</li>
     * <li>1.2 18/05/2016, Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): Se agregan excepciones de mensajes de errores de sistema.</li>
     * </ul>
     * 
     * @param multiEnvironment multiambiente.
     * @param datosParaCorreo datos necesarios para el envío de correo.
     * @throws GeneralException excepcion general.
     * @since 1.0
     */
    public void enviaEmailEjecutivo(MultiEnvironment multiEnvironment, 
    		DatosParaCorreoTO datosParaCorreo) throws GeneralException {
        
    	if (getLogger().isDebugEnabled()) {
             getLogger().debug("[enviaEmailEjecutivo] [BCI_INI]");
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
        fechaPrimerVencimiento = fechaPrimerVcto.substring(0,VALOR_DOS)
        		+ "/" +fechaPrimerVcto.substring(VALOR_TRES,VALOR_CINCO) + "/" 
        		+fechaPrimerVcto.substring(VALOR_SEIS);
        if (getLogger().isDebugEnabled()) getLogger().debug("[enviaEmailEjecutivo] - Antes de emailEjecutivo");
        emailEjecutivo = obtieneCorreoEjecutivo(codigoEjecutivo);
        if (getLogger().isDebugEnabled()) getLogger().debug("[enviaEmailEjecutivo] - emailEjecutivo               [" + emailEjecutivo +"]");

        if (datosParaCorreo.isEnviaBackOffice()){
        	if (getLogger().isDebugEnabled()) getLogger().debug("[enviaEmailEjecutivo] - emailsbackoffice         [true]");
            emailsbackoffice = TablaValores.getValor(TABLA_MULTILINEA, "emailsbackoffice", "desc");
        }

        if (!oficinaIngreso.trim().equals("")) emailJefeOficina = buscaMailJefeOficina(
                multiEnvironment, oficinaIngreso);

        if (getLogger().isDebugEnabled()) getLogger().debug("[enviaEmailEjecutivo] - Antes de obtener glosa oficina");
        glosaOficinaIngreso = TablaValores.getValor("TabOfic.parametros", oficinaIngreso, "desc");
        if (getLogger().isDebugEnabled()) getLogger().debug("[enviaEmailEjecutivo] - oficinaIngreso               [" + oficinaIngreso        + "]");
        if (getLogger().isDebugEnabled()) getLogger().debug("[enviaEmailEjecutivo] - glosaOficinaIngreso          [" + glosaOficinaIngreso   + "]");

        de                  = TablaValores.getValor(TABLA_MULTILINEA, "emailsEjecutivoDe", "desc");
        direccionOrigen     = TablaValores.getValor(TABLA_MULTILINEA, "emailsEjecutivoFrom", "desc");
        direccionDestino    = emailEjecutivo + (emailJefeOficina.trim().equals("") ? "" : "," + emailJefeOficina);
        direccionDestino    += emailsbackoffice.trim().equals("") ? "" : "," + emailsbackoffice.trim();
        String modulo = TablaValores.getValor(TABLA_MULTILINEA, "modulo", "moduloAvance");
        asunto              = modulo +TablaValores.getValor(TABLA_MULTILINEA, datosParaCorreo.getAsunto(), "desc");
        asunto              = asunto + " - "+ datosParaCorreo.getRut() + "-" + datosParaCorreo.getDv() + " - " + datosParaCorreo.getRazonSocial();
        String erroresObtenidos = "";
        if (excepciones != null && excepciones.size() > 0){
            for (int i = 0; i < excepciones.size(); i++) {
            	if (!erroresObtenidos.contains(excepciones.get(i))){
            		if (TablaValores.getValor(TABLA_MULTILINEA, "excepciones", excepciones.get(i)) == null){
            			estado = estado + excepciones.get(i) + ";";		
            		}
            		else {
            		estado = estado + TablaValores.getValor(TABLA_MULTILINEA, "excepciones", excepciones.get(i)) + ";";
            		}
            		erroresObtenidos = erroresObtenidos + excepciones.get(i);
            	}
            }
        }
        else {
        estado              = datosParaCorreo.getEstado().trim().equals("") ? " " : TablaValores.getValor(
                TABLA_MULTILINEA, datosParaCorreo.getEstado(), "desc");
        }
        cuerpoDelEmail      = armaEmailParaDestinatarioGenerico(
                 datosParaCorreo, codBanca, codigoEjecutivo, fechaPrimerVencimiento, 
                 oficinaIngreso, glosaOficinaIngreso, 
                 montoCreditoCorreo, estado);
        enviaMensajeCorreo(de, direccionOrigen, direccionDestino, asunto, cuerpoDelEmail, firma);
        if (getLogger().isDebugEnabled()) {
             getLogger().debug("enviaEmailEjecutivo [BCI_FINOK]");
        }
    }
   
    
    /**
     * Método que envía email al aval.
     * 
     * Registro de versiones:
     * <ul>
     * <li>1.0 19/03/2015, Manuel Escárate R. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li>
     * <li>1.1 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se modifica largo para la fecha.</li>
     * <li>1.2 17/08/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se cambia seteo de correo de aval.</li>
     * </ul>
     * 
     * @param multiEnvironment multiambiente.
     * @param datosCorreo datos necesarios para el envío de correo.
     * 
     * @throws GeneralException excepcion general.
     * @since 1.0
     */
    public  void enviaEmailAval(MultiEnvironment multiEnvironment, 
            DatosParaCorreoTO datosCorreo) throws GeneralException {
        
    	if (getLogger().isDebugEnabled()) {
            getLogger().debug("[enviaEmailAval] [BCI INI]");
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
        fechaPrimerVencimiento = fechaPrimerVcto.substring(0,VALOR_DOS) 
        		+ "/" +fechaPrimerVcto.substring(VALOR_TRES,VALOR_CINCO) + "/" 
                +fechaPrimerVcto.substring(VALOR_SEIS);
        de                  = TablaValores.getValor(TABLA_MULTILINEA, "emailMultilinea", "desc");
        direccionOrigen     = TablaValores.getValor(TABLA_MULTILINEA, "emailsEjecutivoFrom", "desc");
        direccionDestino    = datosCorreo.getDatosAvales().getMailAval();
        String modulo = TablaValores.getValor(TABLA_MULTILINEA, "modulo", "moduloAvance");
        asunto              = modulo + TablaValores.getValor(TABLA_MULTILINEA, datosCorreo.getAsunto(), "desc");
        asunto              = asunto + " - "+ datosCorreo.getRut() + "-" + datosCorreo.getDv() + " - " + datosCorreo.getRazonSocial();
        estado              = datosCorreo.getEstado().trim().equals("") ? " " : TablaValores.getValor(
                TABLA_MULTILINEA, datosCorreo.getEstado(), "desc");
       
        cuerpoDelEmail      = armaEmailParaDestinatarioAval(
        		datosCorreo, codBanca,fechaPrimerVencimiento
               , montoCreditoCorreo,estado);

          enviaMensajeCorreo(de, direccionOrigen, direccionDestino, asunto, cuerpoDelEmail, firma);
          if (getLogger().isDebugEnabled()) {
        	  getLogger().debug("enviaEmailAval FIN");
          }
    }
    
    /**
     * Metodo que envia email a Back Office.
     *
     * Registro de versiones:<ul>
     * <li>1.0 19/03/2015  Manuel Escárate   (BEE)  - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li>
     * <li>1.1 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se quita valor de empresario por defecto.</li>
     * <li>1.2 03/03/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agregan excepciones. </li>
     * <li>1.3 18/05/2016, Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): Se quita VALOR_OCHO y Se agregan excepciones de mensajes de errores de sistema.</li>
     * </ul>
     * <p>
     * @param multiEnvironment multiambiente utilizado.
     * @param datosCorreo datos para correo.
     * @throws Exception en caso de error.
     * @since 1.0
     */
    public void enviaEmailBackOffice(MultiEnvironment multiEnvironment, 
           DatosParaCorreoTO datosCorreo)  throws Exception{
        if (getLogger().isDebugEnabled()) {
             getLogger().debug("[enviaEmailBackOffice]["+clienteMB.getRut()+"][BCI_INI]");
        }
        String bancaPermitidasEmpresa = "" ;

        bancaPermitidasEmpresa = TablaValores.getValor(TABLA_MULTILINEA,
                "bancapermitidasEmpresarioEmail", "desc");
        String fechaPrimerVcto = datosCorreo.getFechaVencimientoSeleccionada();
        
        boolean esEmpresario = verificaPertenenciaBanca(codBanca, bancaPermitidasEmpresa);
        fechaPrimerVcto = fechaPrimerVcto.substring(0,VALOR_DOS) 
        		+"/"+fechaPrimerVcto.substring(VALOR_TRES,VALOR_CINCO) +"/"+fechaPrimerVcto.substring(
        				VALOR_SEIS);

        String emailjefeOficina     = "";
        String emailEjecutivo       = "";
        String emailBackOffice      = "";
        String notesEjecutivo       = "";
        String destinoCredito       = "";
        String asunto               = "";
        String direccionDestino     = "";
        String direccionOrigen      = "";
        String de                   = "";
        String cuerpoDelEmail       = "";
        String firma                = "";
        String estado               = "";
        try{
            if (getLogger().isDebugEnabled()) {
                 getLogger().debug("[enviaEmailBackOffice] codigo ejecutivo:" + codigoEjecutivo);
            }
            emailEjecutivo = obtieneCorreoEjecutivo(codigoEjecutivo);

            if (getLogger().isDebugEnabled()){
                 getLogger().debug("[enviaEmailBackOffice] esEmpresario:" + esEmpresario);
            }

           if (esEmpresario){
                if (getLogger().isDebugEnabled()){
                     getLogger().debug("[enviaEmailBackOffice] antes de la consulta a tablaOficinas");
                }
                String glosaOficina = TablaValores.getValor("TabOfic.parametros", oficinaIngreso, "desc");
                if (getLogger().isDebugEnabled()){
                     getLogger().debug("[enviaEmailBackOffice] después de la consulta a tablaOficinas");
                }
                
                String emailJefeOficina = buscaMailJefeOficina(multiEnvironment, oficinaIngreso);

                if (getLogger().isDebugEnabled()){
                    getLogger().debug("Antes de obtener nombreEmpresa ");
                }
                String nombreEmpresa     = datosCorreo.getRazonSocial();
                if (getLogger().isDebugEnabled()){
                     getLogger().debug("[enviaEmailBackOffice] Antes de "
                            + "obtener nombreEmpresa [" + nombreEmpresa + "]");
                }

                emailBackOffice  = TablaValores.getValor(TABLA_MULTILINEA, "emailsbackoffice", "desc");
                emailjefeOficina = emailJefeOficina.trim().equals("") ? "" : ","+ emailJefeOficina.trim();
                if (getLogger().isDebugEnabled()){
                     getLogger().debug("[enviaEmailBackOffice] emailEjecutivo ["+  emailEjecutivo +"]");
                }
                notesEjecutivo   = codigoEjecutivo;
                direccionOrigen  = TablaValores.getValor(TABLA_MULTILINEA, "emailsbackofficefrom", "desc");

                de              = TablaValores.getValor(TABLA_MULTILINEA, "emailMultilinea", "desc");
                String modulo = TablaValores.getValor(TABLA_MULTILINEA, "modulo", "moduloAvance");
                asunto          = modulo + datosCorreo.getAsunto()+ " "+notesEjecutivo+ " "+glosaOficina+ " "+ destinoCredito;
                emailEjecutivo  = emailEjecutivo.trim().equals("") ? "" : ","+ emailEjecutivo.trim();
                direccionDestino = emailBackOffice.trim() + emailEjecutivo + emailjefeOficina;
               
                String montoCreditoCorreo = formatearMonto(datosCorreo.getMontoFinalCredito(),0,"#,###");

                if (getLogger().isDebugEnabled()){
                     getLogger().debug("[enviaEmailBackOffice] Armando cuerpoDelEmail al BackOffice");
                }
                String erroresObtenidos = "";
                if (excepciones != null && excepciones.size() > 0){
                    for (int i = 0; i < excepciones.size(); i++) {
                    	if (!erroresObtenidos.contains(excepciones.get(i))){
                    		if (TablaValores.getValor(TABLA_MULTILINEA, "excepciones", excepciones.get(i)) == null){
                    			estado = estado + excepciones.get(i) + ";";		
                    		}
                    		else {
                    		estado = estado + TablaValores.getValor(TABLA_MULTILINEA, "excepciones", excepciones.get(i)) + ";";
                    		}
                    		erroresObtenidos = erroresObtenidos + excepciones.get(i);
                    	}
                    }
                }
                else {
                	estado   = datosCorreo.getEstado();
                			
                }
                cuerpoDelEmail = armaEmailParaDestinatarioGenerico(
                		 datosCorreo, codBanca, codigoEjecutivo, fechaPrimerVcto, 
                         oficinaIngreso, glosaOficina, 
                         montoCreditoCorreo, estado);
              
                if (getLogger().isDebugEnabled()){
                     getLogger().debug("[enviaEmailBackOffice] Antes de enviar email al BackOffice");
                }

                enviaMensajeCorreo(de,
                        direccionOrigen,
                        direccionDestino,
                        asunto,
                        cuerpoDelEmail,
                        firma);
                if (getLogger().isDebugEnabled()){
                     getLogger().debug("[enviaEmailBackOffice] [BCI_FINOK]");
                }
            }
            else{
                if (getLogger().isDebugEnabled()){
                     getLogger().debug("[enviaEmailBackOffice] No es Banca permitida"
                    		+ " (no se envia email a BackOffice)");
                }
            }
        }
        catch (Exception e){
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().debug("[enviaEmailBackOffice][BCI_FINEX] El Email al BackOffice No"
                        + " pudo ser enviado :" + ErroresUtil.extraeStackTrace(e));
            }
        }
    }
    
    /**
	 * Metodo que envia email a destinatario.
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 19/03/2015, Manuel Escárate. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): version inicial</li>
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
		    	 getLogger().debug("FIN enviaMensajeCorreo ");
		    }
		} 
		catch (Exception e) {
	         if (getLogger().isEnabledFor(Level.ERROR)) {
	        	 getLogger().error("Error en el servicio de correo : "
					+ ErroresUtil.extraeStackTrace(e));
	         }
		}
	}
    
    
    /**
     * Metodo para transformar las comunas obtenidas desde servicios miscelaneos
     * en ComunaTO.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 19/03/2015, Manuel Escárate. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): version inicial</li>
     * </ul>
     * 
     * @param comunasServicio comunas de servicio.
     * @return ComunaTO[] Lista de comunas.
     * @since 1.0
     */
    public ComunaTO[] transformaAComunaTO(DescComuna[] comunasServicio) {
        if (getLogger().isEnabledFor(Level.INFO)) {
             getLogger().debug("[transformaAComunaTO]: Inicio.");
        }
        ArrayList<ComunaTO> tempComunas = new ArrayList<ComunaTO>();
        for (int i = 0; i < comunasServicio.length; i++) {
            try {
                ComunaTO comuna = new ComunaTO();
                comuna.setCodigo(Integer.parseInt(comunasServicio[i].getCodComuna()));
                comuna.setNombre(comunasServicio[i].getNomComuna());
                comuna.setCodCiudad(Integer.parseInt(comunasServicio[i].getCodCiudad()));
                tempComunas.add(comuna);
            }
            catch (Exception e) {
                    getLogger().error(
                        "[transformaAComunaTO] comuna con problemas:"
                            + comunasServicio[i].getCodComuna() + "-"
                            + comunasServicio[i].getNomComuna() + "-"
                            + comunasServicio[i].getCodCiudad() + " " + e.toString());
            }
        }
        comunasPorRegion = new ComunaTO[tempComunas.size()];
        for (int i = 0; i < tempComunas.size(); i++) {
            comunasPorRegion[i] = tempComunas.get(i);
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
             getLogger().debug("[transformaAComunaTO]: Fin.");
        }
        return comunasPorRegion;
    }

    /**
     * Metodo que utiliza el componente de firmas.
     *
     * Registro de versiones:<ul>
     * <li>1.0 19/03/2015  Manuel Escárate   (BEE)  - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial</li>
     * <li>1.2 03/03/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agregan excepciones. </li>
     * <li>1.3 10/06/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agregan mensajes en catch y se modifica log.</li> 
     * <li>1.4 17/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega llamado a método de envio de correo contacto.
     *                                                            se quita llamada a correo de avales en caso de pendientes de firmas.</li>
     * </ul>
     * <p>
     * @param multiEnvironment multiambiente de la consulta.
     * @param datosOperacionesFirma datos necesarios para utilizar el componente de firmas.
     * @return int resultado resultadoValorFirma.
     * @throws Exception en caso de error.
     * @since 1.0
     */
    public int ingresarOperacionFirma(MultiEnvironment multiEnvironment,
            DatosParaOperacionesFirmaTO datosOperacionesFirma) 
                    throws Exception{
    	if (getLogger().isEnabledFor(Level.INFO)) {
    		getLogger().debug("[ingresarOperacionFirma]: [BCI_INI]");
    	}
        int pasoFirma = 0;
        int resultadoValorFirma = 0;
        DatosResultadoOperacionesTO resultadoOpe = null;
        ResultadoFirmaTO resultadoFirma = null;
        try{
            resultadoOpe = crearEJBmultilinea().ingresarOperacionFirma(multiEnvironment,
                    datosOperacionesFirma,pasoFirma);
            if (getLogger().isDebugEnabled()){
                getLogger().debug("[ingresarOperacionFirma] :" + resultadoOpe.isRespuesta());
            }
            if (resultadoOpe != null){
            	
            	if (resultadoOpe != null && resultadoOpe.getExcepciones() != null){
            		if (excepciones != null){
            			int largoExcepciones = resultadoOpe.getExcepciones().length;
                		for (int i = 0; i < largoExcepciones; i++) {
                			excepciones.add(resultadoOpe.getExcepciones()[i]);
    					}
            		}
            		else {
            			excepciones  = new ArrayList<String>(Arrays.asList(resultadoOpe.getExcepciones()));
            		}
    			}
            	
            	 if (getLogger().isDebugEnabled()){
                     getLogger().debug("[ingresarOperacionFirma] [resultadoOpe.isRespuesta()]:" + resultadoOpe.isRespuesta());
                }
                if (resultadoOpe.isRespuesta()){
                    datosOperacionesFirma.setIdentificadorFirma(resultadoOpe.getNumOperacion());
                    pasoFirma = 1;
                    String numOperacion =datosOperacionesFirma.getNumOperacionValidacionFirma();
                    double montoFinalCreditoFirma = new Double(datosOperacionesFirma.getMontoCredito());
                    String firma ="";
                    Hashtable    htTablaCreditos = null;
                    if (getLogger().isDebugEnabled()) getLogger().debug("TablaValores... ");
                    TablaValores tablaCreditos   = new TablaValores();
                    htTablaCreditos = tablaCreditos.getTabla("simmultilinea.parametros");
                    Enumeration codigos = htTablaCreditos.keys();
                    Object elem         = codigos.nextElement();
               
                    Hashtable htDesc    = (Hashtable)htTablaCreditos.get(elem);

                    String lsf          = (String)htDesc.get("AVCSIF");
                    String lnf          = (String)htDesc.get("AVCNOF");
                    if (getLogger().isDebugEnabled()) {
                    	getLogger().debug("Verificando si debe firmar (Avance) ... ");
                    	getLogger().debug("codBanca =[" + codBanca + "]");
                    	getLogger().debug("lsf =[" + lsf + "]");
                    	getLogger().debug("lnf =[" + lnf + "]");
                    }                  
                    firma = necesitaFirmar(codBanca, lsf, lnf, String.valueOf(clienteMB.getRut()));
                    if (firma.equals("S")){            
                    			resultadoFirma = servicioAutorizacionyFirmaModelMB.firmar(
			                            numOperacion,montoFinalCreditoFirma,false);
                    			if (getLogger().isDebugEnabled()) getLogger().debug("[ingresarOperacionFirma] resultado firma primera");
                  
		                       if (resultadoFirma != null){
		                    	  if (getLogger().isDebugEnabled()){
		                               getLogger().debug("[ingresarOperacionFirma] :" + resultadoFirma.getCodigoRespuesta());
		                          }
		                        if ((resultadoFirma.getCodigoRespuesta() == ERROR_GENERICO) 
		                        		&& (resultadoFirma.getGlosa().equalsIgnoreCase(TablaValores.getValor(
		                                        TABLA_FYP, "registroFyP", "errorGenerico")))){
		                            resultadoValorFirma = VALOR_MIL;
		                        }
		                        else if (resultadoFirma.getCodigoRespuesta() == 1){
		                            if (getLogger().isDebugEnabled()) {
		                                 getLogger().debug("[ingresoOperacionFirma]["+clienteMB.getRut()+"][BCI_INI]");
		                            }
		                            resultadoValorFirma = VALOR_MIL;
		                            resultadoOpe = crearEJBmultilinea().ingresarOperacionFirma(
		                                    multiEnvironment,datosOperacionesFirma,pasoFirma);
		                        	if (resultadoOpe != null && resultadoOpe.getExcepciones() != null){
	                            		if (excepciones != null){
	                            			int largoExcepciones = resultadoOpe.getExcepciones().length;
	                                		for (int i = 0; i < largoExcepciones; i++) {
	                                			excepciones.add(resultadoOpe.getExcepciones()[i]);
	                    					}
	                            		}
	                            		else {
	                            			excepciones  = new ArrayList<String>(Arrays.asList(resultadoOpe.getExcepciones()));
	                            		}
	                    			}
		                            if (resultadoOpe.isRespuesta()){
		                                 if (getLogger().isDebugEnabled()){
		                                     getLogger().debug("[ingresarOperacionFirma] resultadoOpe:" + resultadoOpe.isRespuesta());
		                                }
		                                resultadoOpe = crearEJBmultilinea().activarAvanceMultilinea(multiEnvironment,datosOperacionesFirma);
		                                if (resultadoOpe != null && resultadoOpe.getExcepciones() != null){
	                                		 if (excepciones != null){
	                                			 int largoExcepciones = resultadoOpe.getExcepciones().length;
	                                			 for (int i = 0; i < largoExcepciones; i++) {
	                                				 excepciones.add(resultadoOpe.getExcepciones()[i]);
	                                			 }
	                                		 }
	                                		 else {
	                                			 excepciones  = new ArrayList<String>(Arrays.asList(resultadoOpe.getExcepciones()));
	                                		 }
	                                	 }
		                                if (resultadoOpe.isRespuesta()){
		                                	 if (getLogger().isDebugEnabled()){
		                                         getLogger().debug("[ingresarOperacionFirma] activarAvanceMultilinea:" + resultadoOpe.isRespuesta());
		                                	 }

		                                	 try {
		                                		 resultadoValorFirma = 1;
		                                		 if (condicionGarantia.trim().equals("2")){
		                                			 enviarCorreoAvales(multiEnvironment,"opeExitoAvance","asuntoEmailExito");
		                                		 }
		                                		 if (bancaEnvioEmailEjecutivo || bancaPermitidasEmpresarioEmail) {
		                                			 enviarCorreoEjecutivo(multiEnvironment,"opeExitoAvance","asuntoEmailExito",false);
		                                		 }
		                                		 enviarCorreoContactoEmpresa(multiEnvironment, "opeExitoAvance", "opeExitoAvance");
		                                	 }
		                                	 catch (Exception ex){
		                                		 if (getLogger().isEnabledFor(Level.ERROR)) { 
		                                			 getLogger().error("[ingresarOperacionFirma][error enviando correos][" + clienteMB.getRut() 
		                                					 +"][BCI_FINEX][ingresarOperacionFirma]" +" error con mensaje: " + ex.getMessage(), ex);
		                                		 }  
		                                	 }
		                                }
		                            }  
		                        }
		                        else  {
		                            switch(resultadoFirma.getCodigoRespuesta()){
		                            case ESTADO_PENDIENTE:
		                                resultadoValorFirma = ESTADO_PENDIENTE;
		                                try{
			                           	    if (bancaEnvioEmailEjecutivo || bancaPermitidasEmpresarioEmail) {
			                           	    	enviarCorreoEjecutivo(multiEnvironment,"opePendienteFirmas","asuntoPendienteFirma",false);
			                           	    }
		                                }
		                           	    catch (Exception ex){
		                                	if (getLogger().isEnabledFor(Level.ERROR)) { 
		                                		getLogger().error("[ingresarOperacionFirma][error enviando correos][" + clienteMB.getRut() 
		                                				+"][BCI_FINEX][ingresarOperacionFirma]" +" error con mensaje: " + ex.getMessage(), ex);
		                                	}  
		                           	    }
		                                break;
		                            case NO_POSEE_APO_IDN:   
		                                resultadoValorFirma = NO_POSEE_APO_IDN;
		                                break;
		                            case FIRMO_APO_IDN:   
		                                resultadoValorFirma = FIRMO_APO_IDN;
		                                break;
		                            case PROBLEMAS_CONEXION:   
		                                resultadoValorFirma = PROBLEMAS_CONEXION;
		                                break;
		                            case MONTO_SUPERADO:   
		                                resultadoValorFirma = RESP_MONTO_SUPERADO;
		                                break;
		                            }
		                        }
		                    }
                    }
                    else  {
                    	pasoFirma = 1;
						resultadoValorFirma = VALOR_MIL;
						resultadoOpe = crearEJBmultilinea().ingresarOperacionFirma(
								multiEnvironment,datosOperacionesFirma,pasoFirma);
						if (resultadoOpe.isRespuesta()){
							try{
							  resultadoOpe = crearEJBmultilinea().activarAvanceMultilinea(multiEnvironment,
									datosOperacionesFirma);
							  if (resultadoOpe != null && resultadoOpe.getExcepciones() != null){
								  if (excepciones != null){
									  int largoExcepciones = resultadoOpe.getExcepciones().length;
									  for (int i = 0; i < largoExcepciones; i++) {
										  excepciones.add(resultadoOpe.getExcepciones()[i]);
									  }
								  }
								  else {
									  excepciones  = new ArrayList<String>(Arrays.asList(resultadoOpe.getExcepciones()));
								  }
							  }
							  if (resultadoOpe.isRespuesta()){
								     try{
                                         resultadoValorFirma = 1;
			                             if (condicionGarantia.trim().equals("2")){
			                            	 enviarCorreoAvales(multiEnvironment,"opeExitoAvance","asuntoEmailExito");
			                             }
			                     		 if (bancaEnvioEmailEjecutivo || bancaPermitidasEmpresarioEmail) {
			                     			 enviarCorreoEjecutivo(multiEnvironment,"opeExitoAvance","asuntoEmailExito",false);
		                        		 }
			                     		 enviarCorreoContactoEmpresa(multiEnvironment, "opeExitoAvance", "asuntoEmailExito");
								     }
								     catch (Exception ex){
									  if (getLogger().isEnabledFor(Level.ERROR)) { 
        				                  getLogger().error("[ingresarOperacionFirma][error enviando correos][" + clienteMB.getRut() 
        				                          +"][BCI_FINEX][ingresarOperacionFirma]" +" error con mensaje: " + ex.getMessage(), ex);
        				              }  
								     }
								}
								else {
									resultadoValorFirma = VALOR_MIL;
								}
							}
							catch (Exception ex){
								if (getLogger().isEnabledFor(Level.ERROR)) {
									getLogger().error("[ingresoOperacionFirma] Exception:" 
											+ ErroresUtil.extraeStackTrace(ex));
								}
								resultadoValorFirma = VALOR_MIL;
							}
						}  
                    }
                }
                else {
                	resultadoValorFirma = VALOR_MIL;
                }
            }
            else {
            	resultadoValorFirma = VALOR_MIL;
            }
        }
        catch (Exception e){
            if (getLogger().isEnabledFor(Level.ERROR)) {
            	getLogger().error("[ingresoOperacionFirma] [[BCI_FINEX]] Exception:" + ErroresUtil.extraeStackTrace(e));
            }
            esErrorGenerico = true;
            paso = -1;
        }
    	if (getLogger().isEnabledFor(Level.INFO)) {
    		getLogger().debug("[ingresarOperacionFirma]: [BCI_FINOK]");
    	}
        return resultadoValorFirma;
    }
    
    /**
     * Método encargado de enviar correo a avales.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 12/06/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
     * <li>1.1 10/06/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega try catch.</li> 
     * <li>1.2 17/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega lógica para obtener nuevos datos en el correo.</li> 
     * </ul>
     * @param multiEnvironment multiambiente.
     * @param estadoAvance estado del avance.
     * @param asuntoAvance asunto del avance.
     *
     * @throws Exception excepcion en el metodo.
     */    
    public void enviarCorreoAvales(MultiEnvironment multiEnvironment, String estadoAvance,String asuntoAvance) throws Exception{
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
		int diaDeVencimiento = 0;
		double segurosCorreo = 0;
		double valorNotarioCorreo = 0;
		double impuestosCorreo = 0;
	    if (calendarioPagoSalida != null && calendarioPagoSalida[cuotaSeleccionada-1].getFecVencPago() != null){
	    	fechaUltimaCuota =  FechasUtil.convierteDateAString(calendarioPagoSalida[cuotaSeleccionada-1].getFecVencPago(),"dd/MM/yyyy"); 
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
				getLogger().error("[enviarCorreoAvales][" + clienteMB.getRut() 
						+"][BCI_FINEX][enviarCorreoAvales] [obteniendo comunas]" +" error con mensaje: " + ex.getMessage(), ex);
			}  
		}
		
		try {
			if (getLogger().isDebugEnabled()){
				getLogger().debug("[enviarCorreoAvales] consultando destinoDelCredito " + "  [" + caiOperacion + iicOperacion + "]" );
			}
			ResultConsultaOperacionCredito resConOpe = crearEJBmultilinea().consultaOperacionCredito(multiEnvironment, caiOperacion, iicOperacion);
			destinoDelCredito  = resConOpe.getGlosaDestinoEspecifico();
				
			ResultConsultaCgr seguros = obtenerSeguros(multiEnvironment, caiOperacion, String.valueOf(iicOperacion));
			double sumaSeguros = 0;
			if (seguros != null && seguros.getInstanciaDeConsultaCgr() != null){
				for(int j = 0; j < seguros.getInstanciaDeConsultaCgr().length; j++){
					IteracionConsultaCgr cgr = seguros.getInstanciaDeConsultaCgr()[j];
					if (cgr.getIndVigenciaInst() == 'S'){
						sumaSeguros = sumaSeguros + cgr.getTasaMontoFinal();
					}

				}
			}
			if (getLogger().isDebugEnabled()){
				getLogger().debug("[enviarCorreoAvales] Total Seguros: " + sumaSeguros);
			}
			segurosCorreo = sumaSeguros;		
			ResultConsultaCgr gastos = obtenerGastoNotarial(multiEnvironment, caiOperacion, String.valueOf(iicOperacion));
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
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[enviarCorreoAvales] Gastos Notariales: " + gastoNotario);
			}
			valorNotarioCorreo = gastoNotario;		
			impuestosCorreo = resConOpe.getImpuestos();
			if (tipoDeCreditoSeleccionado.equals("AVC721")){
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
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[enviarCorreoAvales] despues destinoDelCredito " + "[" + caiOperacion + iicOperacion + "]" );
			}
				
		}
		catch (Exception ex) {
			if (getLogger().isEnabledFor(Level.ERROR)) { 
				getLogger().error("[enviarCorreoAvales][" + clienteMB.getRut() 
						+"][BCI_FINEX][enviarCorreoAvales]" +" error con mensaje: " + ex.getMessage(), ex);
			}  
			destinoDelCredito = " ";
		}
		
    	try {
    		if (getLogger().isDebugEnabled()) {
				getLogger().debug("[enviarCorreoAvales] [antes de recorrer datos de avales]");
			}
    		
    		if (datosAvalesCorreo != null && datosAvalesCorreo.length > 0){
    			for (int i = 0; i < datosAvalesCorreo.length; i++) {
    				if (datosAvalesCorreo[i] != null){
    					DireccionClienteBci[] direcciones = instanciaEJBServicioDirecciones().getAddressBci(Long.parseLong(datosAvalesCorreo[i].getRutAval()));
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
    					datosAval.setRutAval(datosAvalesCorreo[i].getRutAval());
    					datosAval.setDvAval(datosAvalesCorreo[i].getDvAval());

    					DatosParaCorreoTO datosCorreo = new DatosParaCorreoTO();
    					datosCorreo.setCaiOperacion(caiOperacion);
    					datosCorreo.setIicOperacion(iicOperacion);
    					datosCorreo.setFechaVencimientoSeleccionada(fechaVencimientoSeleccionada);
    					datosCorreo.setRut(clienteMB.getRut());
    					datosCorreo.setDv(clienteMB.getDigitoVerif());
    					datosCorreo.setRazonSocial(clienteMB.getRazonSocial());
    					datosCorreo.setMontoFinalCredito(montoCorreo);
    					datosCorreo.setCuotaSeleccionada(cuotaSeleccionada);
    					datosCorreo.setDestinoDelCredito(destinoDelCredito);
    					datosCorreo.setEstado(estadoAvance);
    					datosCorreo.setAsunto(asuntoAvance);
    					datosCorreo.setTasaInteres(formatearMonto((tasaInteresInternet),VALOR_DOS,"#,##0"));
    					String montoEnPalabras = TextosUtil.numeroEnPalabras(montoCorreo);
    					datosCorreo.setMontoExpresadoEnPalabras(montoEnPalabras);
    					datosCorreo.setValorCuota(valorCuota);
    					datosCorreo.setDatosAvales(datosAval);
    					datosCorreo.setComunaEmpresa(comunaEmpresa);
    					datosCorreo.setCiudadEmpresa(ciudadEmpresa);
    					datosCorreo.setDireccionEmpresa(direccionEmpresa);
    					datosCorreo.setFechaUltimaCuota(fechaUltimaCuota);
    					datosCorreo.setDiaVencimiento(diaDeVencimiento);
    					datosCorreo.setSeguros(segurosCorreo);
    					datosCorreo.setImpuestos(impuestosCorreo);
    					datosCorreo.setValorNotario(valorNotarioCorreo);
    					enviaEmailAval(multiEnvironment,datosCorreo);
    				}
    			}	
    		}
    		if(getLogger().isDebugEnabled()){
    			getLogger().debug("[enviarCorreoAvales][BCI_FINOK]");
    		}
    	}
    	catch (Exception ex){
    		if (getLogger().isEnabledFor(Level.ERROR)) { 
    			getLogger().error("[enviarCorreoAvales][" + clienteMB.getRut() 
    					+"][BCI_FINEX][enviarCorreoAvales]" +" error con mensaje: " + ex.getMessage(), ex);
    		}  
    	}
    }
    
    /**
     * Obtiene el detalle de las tablas llamando al servicio de tablas de IBM.
     * <p>
     * Registro de versiones:
     * <ul>
     *   <li>1.0 15/05/2015 Manuel Escárate R. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): Version Inicial</li>
     * </ul>
     * </p>
     * @param tabla tabla que será consultado por el servicio.
     * @param codigoTabla código en la tabla a consultar.
     * @param codigo código asociado a la consulta.
     * @param multiambiente objeto para seteo de ambiente.
     * @return detalle de la tabla.
     * @throws RemoteException captura excepción producida por comunicación con EJB.
     * @throws EJBException excepción lanzada al ocurrir un error en el EJB.
     * @throws TableAccessException captura excepción producida por tabla de acceso.
     * @since 1.0
     */ 
    private HashMap<String, String> obtieneDetalleTabla(MultiEnvironment multiambiente, String tabla, 
            String codigoTabla, String codigo) throws RemoteException,EJBException,TableAccessException{
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[obtieneDetalleTabla] Obteniendo valores de tablas: " 
                  + tabla + ", " + codigoTabla + ", " + codigo);
        }
        HashMap<String, String> viewTab = new HashMap<String, String>();
        try {
            TableManagerService tableManagerService = (TableManagerService)
                      LocalizadorDeServicios.obtenerInstanciaEJB(
                              LocalizadorDeServicios.JNDI_MANEJADOR_DE_TABLAS);
            TableSpec table = new TableSpecImpl(tabla, codigoTabla, "");
            List lsViewTab = tableManagerService.query(multiambiente, table, true);
            for (Iterator iterator = lsViewTab.iterator(); iterator.hasNext();) {
                Row row = (Row) iterator.next();
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[obtieneDetalleTabla] row: "  + row);
                }                
                viewTab.put(row.getLongDescription(), (row.getCode()));
            }
        } 
        catch (InvocacionServicioException e) {
            if (getLogger().isEnabledFor(Level.ERROR)) { 
                getLogger().error("[obtieneDetalleTabla][" + clienteMB.getRut() 
                        +"][BCI_FINEX][InvocacionServicioException]" +" error con mensaje: " + e.getMessage(), e);
            }  
        } 
        catch (RemoteException e) {
            if (getLogger().isEnabledFor(Level.ERROR)) { 
                getLogger().error("[obtieneDetalleTabla][" + clienteMB.getRut() 
                        +"][BCI_FINEX][RemoteException]" +" error con mensaje: " + e.getMessage(), e);
            }  
        }
        catch (EJBException e) {
            if (getLogger().isEnabledFor(Level.ERROR)) { 
                getLogger().error("[obtieneDetalleTabla][" + clienteMB.getRut() 
                        +"][BCI_FINEX][EJBException]" +" error con mensaje: " + e.getMessage(), e);
            }  
        } 
        catch (TableAccessException e) {
            if (getLogger().isEnabledFor(Level.ERROR)) { 
                getLogger().error("[obtieneDetalleTabla][" + clienteMB.getRut() 
                        +"][BCI_FINEX][TableAccessException]" +" error con mensaje: " + e.getMessage(), e);
            }  
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[obtieneDetalleTabla] Valores recuperados: " + viewTab);
        }        
        return viewTab; 
    }   
    
    /**
     * <p> Método que genera el comprobante del avance. </p>
     * 
     * Registro de versiones:
     * <ul>
     *     <li>1.0 25/05/2015 Manuel Escárate (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): version inicial.</li>
     *     <li>1.1 18/05/2016 Manuel Escárate (BEE) - Pablo Paredes (ing.Soft.BCI): Se deja en blanco la cuenta de cargo
     *                                                          en caso de no obtenerla.</li>
     *     <li>1.2 17/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se modifica nombre de pdf.</li>                                                          
     * </ul>
     * 
     * @since 1.0
     */
    public void generarComprobanteCargaPDF(){
        if(getLogger().isEnabledFor(Level.INFO)){
             getLogger().debug("[generarComprobanteCargaPDF]["+usuarioModelMB.getRut()+"][BCI_INI]");
        }
        DatosComprobantePdfTO comprobanteTO = new DatosComprobantePdfTO();
        comprobanteTO.setNombreCompleto((this.usuarioModelMB.getNombres()
                + " " + this.usuarioModelMB.getApPaterno()));
        if (cuentaSeleccionadaCargo != null){
        comprobanteTO.setCuentaCargo(cuentaSeleccionadaCargo);
        }
        else {
        	comprobanteTO.setCuentaCargo("");
        }
        String montoCred = "";
        if (resultadoFirmaAvance !=null){
        	if (Integer.parseInt(resultadoFirmaAvance) == 1){
        		montoCred = formatearMonto(montoFinalCredito,0,"#,###");
        	}
        	else {
        		montoCred = montoSolicitado;
        	}
        }
        else {
        	montoCred = montoSolicitado;
        }
        
        comprobanteTO.setMontoCargo(montoCred);
        comprobanteTO.setNumeroOperacion(caiOperacion+iicOperacion);
        
        if (getLogger().isEnabledFor(Level.DEBUG)){
            getLogger().debug("[generarComprobanteCargaPDF][" + usuarioModelMB.getRut()
                    + "] cargaNominaTO: [" + comprobanteTO.toString() + "]");
        }
        FacesContext ctx = FacesContext.getCurrentInstance();
        ServletContext servletContext = (ServletContext) ctx.getExternalContext().getContext();
        String rutaRecursosSello = servletContext.getRealPath("/").replace("\\", "/").concat("/");
        String rutaRecursosPDF = TablaValores.getValor(TABLA_MULTILINEA, "crearPDF","rutaRecursosPDF");
        
        if (getLogger().isEnabledFor(Level.DEBUG)){
            getLogger().debug("[generarComprobanteCargaPDF][" + usuarioModelMB.getRut()
                    + "] rutaRecursosPDF: [" + rutaRecursosPDF + "]");
        }
        String comprobateIngresar = TablaValores.getValor(
                TABLA_MULTILINEA, "crearPDF","comprobanteAvanceMultilinea");

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
            getLogger().debug("[generarComprobanteCargaPDF]["+usuarioModelMB.getRut()+"] cambiado!!: [" 
                    + html + "]");
        }
        try{
            if (!ctx.getResponseComplete()) {
                String contentType = "application/pdf";
                HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();
                response.setContentType(contentType);
                response.setHeader("Content-disposition","attachment;filename=comprobanteAvanceCreditoComercial.pdf");
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
                getLogger().error("[generarComprobanteCargaPDF][[BCI_FINEX]]["+usuarioModelMB.getRut()+"]"
                    + "[Exception]::" + ex.getMessage(), ex);
            }
        }
        if(getLogger().isEnabledFor(Level.INFO)){
             getLogger().debug("[generarComprobanteCargaPDF]["+usuarioModelMB.getRut()+"][BCI_FINOK]");
        }
    }
    
    /**
     * <p> Metodo para reemplazar los caracteres especiales en la creacion del pdf</p>
     *
     * Registro de versiones:
     * <ul>
     *     <li>1.0 25/05/2015 Manuel Escárate R. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): version inicial.</li>
     * </ul>
     * @param texto a reemplazar caracteres.
     * @return String texto con los caracteres cambiados.
     * @since 1.0
     */
    public String reemplazaCaractereEspeciales(String texto){
        if(getLogger().isEnabledFor(Level.INFO)){
             getLogger().debug("[reemplazaCaractereEspeciales][" + usuarioModelMB.getRut() + "][BCI_INI]");
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
             getLogger().debug("[reemplazaCaractereEspeciales][" + usuarioModelMB.getRut() + "][BCI_FINOK]");
        }
        return texto;
    }
    
    /**
     * retorna una instancia del EJB Multilinea.
     * <P>
     * Registro de versiones:
     * <ul>
     * <li> 1.0  09/12/2014 Manuel Escárate (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): Versión inicial.</li>
     * </ul>
     * <p>
     * @return Multilinea instancia del ejb Multilinea.
     * @throws GeneralException en caso de error.
     * @since 1.0
     */
    private Multilinea crearEJBmultilinea() throws GeneralException {
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
     * retorna una instancia del EJB Precios.
     * <P>
     * Registro de versiones:
     * <ul>
     * <li> 1.0  16/06/2014 Braulio Rivas S. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): Versión inicial.</li>
     * </ul>
     * <p>
     * @return PreciosContextos instancia del ejb Multilinea.
     * @throws GeneralException en caso de error.
     * @since 1.0
     */
    private PreciosContextos crearEJBprecios() throws GeneralException {
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
            return preciosContextos;

        }
        catch (Exception e) {
            if (getLogger().isEnabledFor(Level.ERROR)){
                getLogger().error("RemoteException: Error al crear instancia EJB", e);
            }
            throw new GeneralException("ESPECIAL", "Error al crear instancia EJB");
        }
    }  
    
    /**
     * Metodo para obtener instancia de ejb ServiciosMiscelaneos.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 09/12/2014 Manuel Escárate. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): version inicial</li>
     * </ul>
     * @return ServiciosMiscelaneos instancia de ejb ServiciosMiscelaneos.
     * @throws Exception En caso de error al consultar.
     * @since 1.0
     */
    private ServiciosMiscelaneos getServiciosMiscelaneos() throws Exception {
        if (getLogger().isEnabledFor(Level.INFO)) {
             getLogger().debug("[getServiciosMiscelaneos] [BCI_INI]");
        }
        ServiciosMiscelaneos serviciosMiscelaneos;
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[getServiciosMiscelaneos] Creando Instancia EJB.");
            }
            EnhancedServiceLocator locator = EnhancedServiceLocator.getInstance();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[getServiciosMiscelaneos] Seteando serviciosMiscelaneosHome.");
            }
            ServiciosMiscelaneosHome serviciosMiscelaneosHome = (ServiciosMiscelaneosHome) locator
                .getGenericService("wcorp.serv.misc.ServiciosMiscelaneos",
                    ServiciosMiscelaneosHome.class);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[getServiciosMiscelaneos] Seteando serviciosMiscelaneos.");
            }
            serviciosMiscelaneos = serviciosMiscelaneosHome.create();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[getServiciosMiscelaneos] Seteo Ok.");
            }
        }
        catch (Exception e) {
                getLogger().error("[getServiciosMiscelaneos] excepcion:" + e.toString());
            throw new Exception("Error al crear instancia de ejb.");
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
             getLogger().debug("[getServiciosMiscelaneos] [BCI_FINOK] ");
        }
        return serviciosMiscelaneos;
    }
    
    /**
     * Obtiene combo box con tipos de créditos.
     * <P>
     * Registro de versiones:
     * <ul>
     * <li> 1.0  09/12/2014 Manuel Escárate (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): Versión inicial.</li>
     * </ul>
     * <p>
     * @return tiposDeCreditos selecitem con tipos de creditos.
     * @since 1.0
     */
    public ArrayList<SelectItem> getTiposDeCreditos() {
        if (getLogger().isDebugEnabled()){
            getLogger().debug("[getTiposDeCreditos][BCI_INI]");
        }   
        if (tipoDeCreditoSeleccionado.equalsIgnoreCase("")){
            int totalTipoCreditos= Integer.parseInt(TablaValores.getValor(
                    TABLA_MULTILINEA,"cantidadTiposDeCreditos", "total"));
            if (tiposDeCreditos == null || tiposDeCreditos.size() == 0){
                tiposDeCreditos = new ArrayList<SelectItem>();
                for (int i = 1; i <= totalTipoCreditos; i++) {
                    String[]  resultado; 
                    resultado = StringUtil.divide(TablaValores.getValor(TABLA_MULTILINEA,
                            "tiposDeCreditos","valores"), "|");
                    String value = resultado[i-1];
                    String codigo = TablaValores.getValor(TABLA_MULTILINEA,
                            value,"valor");
                    String glosa = TablaValores.getValor(TABLA_MULTILINEA,
                            value,"glosa");
                    tiposDeCreditos.add(new SelectItem(codigo,glosa));
                } 
            }
        }
        if(getLogger().isDebugEnabled()){
            getLogger().debug("[getTiposDeCreditos][BCI_FINOK] retornando objeto : " + tiposDeCreditos);
        }
        return tiposDeCreditos;
    }
    
    
    /**
     * Método permite obtener los tipos de créditos a mostrar.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 02/04/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
     * </ul>
     * @return tiposDeCreditosAmostrar tipos de créditos a mostrar.
     * @since 1.0
     */
    public ArrayList<SelectItem> getTiposDeCreditosAmostrar() {
        if (getLogger().isDebugEnabled()){
            getLogger().debug("[getTiposDeCreditosAmostrar][BCI_INI]");
        }   
        if (!tipoDeCreditoSeleccionado.equalsIgnoreCase("")){
            tiposDeCreditosAmostrar = new ArrayList<SelectItem>();
            if(getLogger().isDebugEnabled()){
                getLogger().debug("[getTiposDeCreditosAmostrar]  <=tipoDeCreditoSeleccionado]"
                        +tipoDeCreditoSeleccionado);
            }
            String codigo = String.valueOf(tipoDeCreditoSeleccionado);
            String glosa = TablaValores.getValor(TABLA_MULTILINEA,
                    tipoDeCreditoSeleccionado,"glosa");
            tiposDeCreditosAmostrar.add(new SelectItem(codigo,glosa));
        }
        if(getLogger().isDebugEnabled()){
            getLogger().debug("[getTiposDeCreditosAmostrar][BCI_FINOK] retornando objeto : " 
                    + tiposDeCreditosAmostrar);
        }
        return tiposDeCreditosAmostrar;
    }
       
    /**
     * Método permite obtener el resumen del crédito.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 02/04/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
     * <li>1.1 17/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agregan datos para el despliegue de resumen.</li> 
     * </ul>
     * @param origen de que módulo viene.
     * @param cai cai de la operación.
     * @param iic iic de la operación.
     * @since 1.0
     */
    public void resumenCredito(String origen, String cai, String iic){
    	if (getLogger().isDebugEnabled()){
    		getLogger().debug("[resumenCredito][BCI_INI]");    
    	}
    	if (origen.equals("firmar")){
    		MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
    				sesion.getCanalId(), USUARIO);
    		try {
    			operacionCredito = crearEJBmultilinea().consultaOperacionCredito(multiEnvironment,
    					cai, new Integer(iic).intValue());

    			ResultConsultaCgr seguros = obtenerSeguros(multiEnvironment, cai, iic);
    			double sumaSeguros = 0;
    			if (seguros != null && seguros.getInstanciaDeConsultaCgr() != null){
    				for(int j = 0; j < seguros.getInstanciaDeConsultaCgr().length; j++){
    					IteracionConsultaCgr cgr = seguros.getInstanciaDeConsultaCgr()[j];
    					if (cgr.getIndVigenciaInst() == 'S'){
    						sumaSeguros = sumaSeguros + cgr.getTasaMontoFinal();
    					}

    				}
    			}
    			if (getLogger().isDebugEnabled()) getLogger().debug("[resumenCredito] Total Seguros: " + sumaSeguros);
    			operacionCredito.setValorOtroSeguro(sumaSeguros);
    			if (getLogger().isDebugEnabled()){
    				getLogger().debug("[resumenCredito] tasa de interes especial"+operacionCredito.getInteresEspecial());
    				getLogger().debug("[resumenCredito] valor seguro"+operacionCredito.getValorOtroSeguro());
    			}
    			
    			ResultConsultaCgr gastos = obtenerGastoNotarial(multiEnvironment, cai, iic);
    			double gastoNotario = 0;
    			
    			if (gastos != null && gastos.getTotOcurrencias() > 0){
    				if (gastos.getInstanciaDeConsultaCgr(0) != null){
    					gastoNotario = gastos.getInstanciaDeConsultaCgr(0).getTasaMontoFinal();
    					if (getLogger().isDebugEnabled()){ 
    						getLogger().debug("[resumenCredito] valorNotario: " +  gastoNotario);
    					}
    				}
    				else {
    					if (getLogger().isDebugEnabled()){ 
    						getLogger().debug("[resumenCredito] no setee valor de notario");
    					}
    				}
    			} 
    			else {
    				if (getLogger().isDebugEnabled()){
    					getLogger().debug("[resumenCredito] No existen ocurrencias en la consulta CGR para Gastos Notariales");
    				}
    	    	} 
    			if (getLogger().isDebugEnabled()) getLogger().debug("[resumenCredito] Gastos Notariales: " + gastoNotario);
    			operacionCredito.setValorGasto(gastoNotario);
    			
    		} 
    		catch (Exception ex) {
    			if (getLogger().isEnabledFor(Level.ERROR)) {
    				getLogger().debug("[resumenCredito] [BCI_FINEX] Exception:" + ErroresUtil.extraeStackTrace(ex));
    			}
    		}
    	}
        else if (origen.equals("avance")) {
            ResultConsultaOperacionCredito operacionCreditoCalendario = new ResultConsultaOperacionCredito();
            operacionCreditoCalendario.setMontoProxCuota(valorCuota);
            operacionCreditoCalendario.setTotalCuotasOrig(cuotaSeleccionada);
            SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
            Date fechaProximaCuota = FechasUtil.convierteStringADate(fechaVencimientoSeleccionada, ft);
            operacionCreditoCalendario.setFechaProxCuota(fechaProximaCuota);
            operacionCreditoCalendario.setMontoCredito(montoFinalCredito);
            operacionCreditoCalendario.setImpuestos(impuestos);
            this.setOperacionCredito(operacionCreditoCalendario);
            muestraCalendario = false;
            muestraSeguros = false;
            muestraResumen = true;
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[resumenCredito][BCI_FINOK]");
        }
    }
    
    /**los seguros del credito.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 16/06/2015 Braulio Rivas (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
     * </ul>
     * @param multiEnvironment multiambiente.
     * @param cai cai de la operación.
     * @param iic iic de la operación.
     * @return seguros del credito.
     * @since 1.0
     */    
    public ResultConsultaCgr obtenerSeguros(MultiEnvironment multiEnvironment, String cai, String iic){
    	
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
             if (getLogger().isDebugEnabled()) getLogger().debug("ERROR***Exception...Exception [" + e.getMessage() + "]");
         }         
         return obeanCGRSGS;
         
    }
    
    /**
     * Método permite obtener el calendario de pago.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 02/04/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
     * <li>1.1 18/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda. (ing.Soft.BCI): Se agrega validación para calendario de pago, y se agrega un log.</li> 
     * </ul>
     * @param origen de que módulo viene.
     * @param cai cai de la operación.
     * @param iic iic de la operación.
     * @since 1.0
     */
    public void calendarioPago(String origen, String cai, String iic){
	  if (getLogger().isDebugEnabled()){
          getLogger().debug("[calendarioPago][BCI_INI]");    
      }
        if (origen.equals("firmar")){
            MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
                    sesion.getCanalId(), USUARIO);
            try {
                SvcCreditosGlobales svcCreditos = new SvcCreditosGlobalesImpl();
                ResultConsultaCalendarioPago resultCalendario=  svcCreditos.consultaCalendarioPago(
                        multiEnvironment, cai, Integer.parseInt(iic));
                if (resultCalendario != null){
                    calendarioPago = resultCalendario.getCalendario();
                }
                operacionCredito = crearEJBmultilinea().consultaOperacionCredito(multiEnvironment, 
                        cai, new Integer(iic).intValue());
            } 
            catch (Exception ex) {
                if (getLogger().isEnabledFor(Level.ERROR)) {
                    getLogger().debug("[calendarioPago] Exception:" + ErroresUtil.extraeStackTrace(ex));
                }
            }
        }
        else if (origen.equals("avance")) {
            ResultConsultaOperacionCredito operacionCreditoCalendario = new ResultConsultaOperacionCredito();
            operacionCreditoCalendario.setMontoProxCuota(valorCuota);
            SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
            Date fechaProximaCuota = FechasUtil.convierteStringADate(fechaVencimientoSeleccionada, ft);
            operacionCreditoCalendario.setFechaProxCuota(fechaProximaCuota);
            this.setOperacionCredito(operacionCreditoCalendario);
            List<CalendarioPago> calendarioConCuotas = new ArrayList<CalendarioPago>();
            if (calendarioPago == null){
            	if (calendarioPagoSalida != null){
            		for (int i = 0; i < calendarioPagoSalida.length; i++) {
            			if (calendarioPagoSalida[i] != null){
            				calendarioConCuotas.add(calendarioPagoSalida[i]);
            			}
            		}

            		if (getLogger().isDebugEnabled()) {
            			getLogger().debug("[calendarioPago][seteando calendarioDePago]");
            		}
            		calendarioPago = calendarioConCuotas.toArray(new CalendarioPago[calendarioConCuotas.size()]);
            	}
            }
            muestraResumen = false;
            muestraSeguros = false;
            muestraCalendario = true;
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[calendarioPago][BCI_FINOK]");
        }
    }
    
    /**
     * Método que retorna si el rut necesita firmar.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 12/06/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
     * </ul>
     * @param codbanca codigo de banca.
     * @param lsf lista si firman.
     * @param lnf lista no firman.
     * @param rutEmpresa rut de empresa
     *
     * @return retorna indicador si necesita firmar.
     *
     *
     */
    private String necesitaFirmar(String codbanca, String lsf, String lnf, String rutEmpresa) {
    	if (getLogger().isDebugEnabled()){
             getLogger().debug("[necesitaFirma] [BCI_INI]");
        }
        String[] listaFirmables    = null;
        String[] listaNofirmables  = null;
        StringTokenizer st          = null;
        int ind                     = 0;
        String elem                 = "";
        String cp                = codbanca.trim();
        String retorno              = "E";
        boolean sigue               = true;
        int rutNoFirma = Integer.parseInt(TablaValores.getValor(TABLA_MULTILINEA, "rutMAxNoFirma", "valor"));                                 
        if (Integer.parseInt(rutEmpresa) < rutNoFirma) 
            return "N";

        if (lsf != null){
            st                  = new StringTokenizer(lsf, ",");
            listaFirmables     = new String[st.countTokens()];    
            while (st.hasMoreTokens()) {
                elem = st.nextToken().trim();
                listaFirmables[ind] = elem;
                ind++;
            }
        }

        if (lnf != null){
            st                  = new StringTokenizer(lnf, ",");
            listaNofirmables   = new String[st.countTokens()];    //iguala la cantidad
            ind                 = 0;
            while (st.hasMoreTokens()) {
                elem = st.nextToken().trim();
                listaNofirmables[ind] = elem;
                ind++;
            }
        }

        if (lsf != null && listaNofirmables != null){
            for (int i=0; i< listaNofirmables.length; i++){
                if (listaNofirmables[i].equals(cp) ) {
                    retorno = "N";                                 //esta en el conjunto => NO firma
                    sigue   = false;
                    break;
                }
            }
        }

        if (sigue){
            if (lsf != null){
                for (int i=0; i< listaFirmables.length; i++){
                    if (listaFirmables[i].equals(cp) ) {
                        retorno= "S";                          //esta en el conjunto => debe firmar
                        break;
                    }
                 }
            }
        }
        if (getLogger().isDebugEnabled()){
            getLogger().debug("[necesitaFirmar] [BCI_FIN]");
        }
        return retorno;
    }
    
    /**
     * Método permite mostrar los seguros en el último paso del avance.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 02/04/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
     * <li>1.1 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): se obtiene datos calendario pago.</li>
     * <li>1.2 18/05/2016, Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): se cambia firma al método y se agrega lógica para firmas en seguros.</li>
     * </ul>
     * @param origen de que módulo viene.
     * @param cai cai de la operación.
     * @param iic iic de la operación.
     * @since 1.0
     */
    public void detalleSeguros(String origen,String cai, String iic){
    	if (getLogger().isDebugEnabled()){
    		getLogger().debug("[detalleSeguros] [BCI_INI]");
    	}
        if (origen.equals("avance")){
          ResultConsultaOperacionCredito operacionCreditoCalendario = new ResultConsultaOperacionCredito();
          operacionCreditoCalendario.setMontoProxCuota(valorCuota);
          operacionCreditoCalendario.setTotalCuotasOrig(cuotaSeleccionada);
          SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
          Date fechaProximaCuota = FechasUtil.convierteStringADate(fechaVencimientoSeleccionada, ft);
          operacionCreditoCalendario.setFechaProxCuota(fechaProximaCuota);
          operacionCreditoCalendario.setMontoCredito(montoFinalCredito);
          operacionCreditoCalendario.setImpuestos(impuestos);
          this.setOperacionCredito(operacionCreditoCalendario);
           muestraResumen = false;
           muestraCalendario = false; 
           muestraSeguros = true;        
        }
        else if (origen.equals("firmar")){
        	  MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
                      sesion.getCanalId(), USUARIO);
              try {
                  operacionCredito = crearEJBmultilinea().consultaOperacionCredito(multiEnvironment,
                          cai, new Integer(iic).intValue());
                  
                  ResultConsultaCgr seguros = obtenerSeguros(multiEnvironment, cai, iic);
                  double sumaSeguros = 0;
                  for(int j = 0; j < seguros.getInstanciaDeConsultaCgr().length; j++){
                  	IteracionConsultaCgr cgr = seguros.getInstanciaDeConsultaCgr()[j];
                  	if (cgr.getIndVigenciaInst() == 'S'){
                  		sumaSeguros = sumaSeguros + cgr.getTasaMontoFinal();
                  	}
                  	
                  }
                  if (getLogger().isDebugEnabled()) getLogger().debug("Total Seguros: " + sumaSeguros);
                    montoTotalSegurosSeleccionados = sumaSeguros;
                  if (getLogger().isDebugEnabled()){
                  	getLogger().debug("operacion creditomonto1+++"+operacionCredito.getInteresEspecial());
                  	getLogger().debug("valor otro seguro++"+operacionCredito.getValorOtroSeguro());
                  }
              } 
              catch (Exception ex) {
          	   if (getLogger().isEnabledFor(Level.ERROR)) {
                     getLogger().debug("[detalleSeguros] [BCI_FINEX]:" + ErroresUtil.extraeStackTrace(ex));
                 }
              }
        	
        	
        }
        if (getLogger().isDebugEnabled()){
    		getLogger().debug("[detalleSeguros] [BCI_FINOK]");
    	}
    }    
    
	
	/**
	 * Verifica si la banca del cliente este en la lista de bancas permitidas.
	 * <p>
	 * Registro de Versiones
	 * <ul>
	 * <li>1.0 02/04/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
	 *
	 * </ul>
	 * </p>
	 *
	 * @param codbanca codigo banca cliente.
	 * @param listaBancas lista de Bancas permitidas.
	 * @return boolean
	 * @since 1.0
	 *
	 */
	public boolean verificaPertenenciaBanca(String codbanca,
			String listaBancas) {
		StringTokenizer st = null;
		String elem = "";
		String cp = codbanca.trim();
		boolean retorno = false;

		if (listaBancas != null) {
			st = new StringTokenizer(listaBancas, ",");
			while (st.hasMoreTokens()) {
				elem = st.nextToken().trim();
				if (elem.equals(cp)) {
					retorno = true;
				}
			}
		}
		return retorno;
	}
	
	/**
	 * Metodo que arma mensaje de email generico a Destinatario.
	 *
	 *
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 10/04/2015 Manuel Escárate(BEE - Jimmy Muñoz D. (ing.Soft.BCI) ): Version Inicial</li>
	 * <li>1.1 03/03/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agregan datos para correo. </li>
	 * <li>1.2 18/05/2016, Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): Se modifica dígito verificador para correo.</li>
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
				
				getLogger().debug("armaEmailParaDestinatario - [BCI_FINOK]");
			}
			
			String cuerpoMail = "";
			if(cuerpoDelEmail != null) {
				cuerpoMail = cuerpoDelEmail.toString();
			}
			return cuerpoMail;

		} 
		catch (Exception e) {
			if (getLogger().isEnabledFor(Level.ERROR)) {
				getLogger().error("Exception " + e.getMessage());
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
	 * <li>1.0 10/04/2015 Manuel Escárate(BEE - Jimmy Muñoz D. (ing.Soft.BCI) ): Version Inicial</li>
	 * <li>1.1 18/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda. (ing.Soft.BCI): Se agregan datos para el correo.</li>
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
				getLogger().debug("antes del archivo mail avales");
			}
			String archivoMail = "";
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("armaEmailParaDestinatario [tipoDeCreditoSeleccionado]" + tipoDeCreditoSeleccionado);
			}
			if (tipoDeCreditoSeleccionado.equals("AVC010")){
				archivoMail = TablaValores.getValor("multilinea.parametros",
						"archivoMailAvalesUnVencimiento", "desc");
			}
			else if (tipoDeCreditoSeleccionado.equals("AVC721")){
				archivoMail = TablaValores.getValor("multilinea.parametros",
						"archivoMailAvalesCuotaFija", "desc");
			}
			
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("archivo mail" + archivoMail);
				getLogger().debug("armaEmailParaDestinatarioAval - ...");
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
			paramsTemplate.put("${diaVencimiento}", String.valueOf(datosCorreo.getDiaVencimiento()));
			paramsTemplate.put("${impuestos}", formatearMonto(datosCorreo.getImpuestos(),0,"#,###"));
			paramsTemplate.put("${seguros}", formatearMonto(datosCorreo.getSeguros(),0,"#,###"));
			paramsTemplate.put("${valorNotario}", formatearMonto(datosCorreo.getValorNotario(),0,"#,###"));
			paramsTemplate.put("${fechaActual}", fechaActual);
			cuerpoDelEmail = plantillaCorreo(paramsTemplate, archivoMail);

			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[armaEmailParaDestinatarioAval] - ANTECEDENTES DEL CREDITO");
				getLogger().debug("[armaEmailParaDestinatarioAval] - Fecha y Hora de la Solicitud   : "
						+ fechaHora);
				getLogger().debug("[armaEmailParaDestinatarioAval] - Monto del Credito              : "
						+ montoCredito);
				getLogger().debug("[armaEmailParaDestinatarioAval] - Cuotas                         : "
						+ datosCorreo.getCuotaSeleccionada());
				getLogger().debug("[armaEmailParaDestinatarioAval] - Fecha 1° vencimiento           : "
						+ fechaPrimerVcto);
				getLogger().debug("[armaEmailParaDestinatarioAval] - Número de operacion            : "
						+ numeroOperacion);
				getLogger().debug("[armaEmailParaDestinatarioAval] - Destino del credito            : "
						+ datosCorreo.getDestinoDelCredito());
				getLogger().debug("[armaEmailParaDestinatarioAval] - Estado del Avance              : "
						+ datosCorreo.getEstado());
				getLogger().debug("[armaEmailParaDestinatarioAval] - FIN armaEmailParaDestinatario");
			}
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[armaEmailParaDestinatarioAval] [BCI_FINOK]");
			}
			String cuerpoMail = "";
			if(cuerpoDelEmail != null) {
				cuerpoMail = cuerpoDelEmail.toString();
			}
			return cuerpoMail;

		} 
		catch (Exception e) {
			if (getLogger().isEnabledFor(Level.ERROR)) {
				getLogger().error("[armaEmailParaDestinatarioAval] [BCI_FINEX] Exception " + e.getMessage());
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
	 * <li>1.0 (10/07/2013 Hector Carranza - BEE ): Version Inicial
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
			getLogger().debug("plantillaCorreo - [BCI_INI] ...");
		}
		StringBuffer buffer = null;
		if (archivo == null) {
			return null;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(archivo));
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
				getLogger().error("plantillaCorreo - IOException - Algo ocurrio ::"
					+ ErroresUtil.extraeStackTrace(e));
			}
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("plantillaCorreo - [BCI_FIN] ...");
		}
		return buffer;
	}
	
	/**
	 * Metodo que obtiene email a partir de username de ejecutivo y/o jefe de
	 * oficina.
	 *
	 *
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 02/04/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
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
			 if (getLogger().isEnabledFor(Level.ERROR)){
				 getLogger().error("Error en el servicio de correo : " + e.getMessage());
			 }
			mailEjecutivo = "";
		}
		mailEjecutivo = mailEjecutivo.trim().equals("") ? (codEjecutivoCorreo
				.trim().equals("") ? "" : codEjecutivoCorreo.toLowerCase().trim()
				+ "@bci.cl") : mailEjecutivo;

		return mailEjecutivo;
	}
	
	/**
	 * busca email de jefe de oficina de ejecutivo.
	 * <p>
	 * Registro de Versiones
	 * <ul>
	 * <li>1.0 02/04/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
	 *
	 * </ul>
	 * </p>
	 *
	 * @param multiEnvironment multiEnvironment.
	 * @param oficinaIngresoCorreo oficiana de ingreso.
	 * @return email de jefe oficina
	 * @since 1.0
	 *
	 */
	public String buscaMailJefeOficina(
			MultiEnvironment multiEnvironment, String oficinaIngresoCorreo) {
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
			result = svc.consultaMasivaDeOficinas(multiEnvironment,
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
				for (int i = 0; i < concurrencias; i++) {
					if (getLogger().isDebugEnabled()){
						getLogger().debug("buscaMailJefeOficina - i [" + i
							+ "] result[i].getCodigoOficina() ["
							+ result[i].getCodigoOficina() + "] ["
							+ result[i].getJefeOficina() + "]");
					}
					emailJefeOficina = obtieneCorreoEjecutivo(result[i].getJefeOficina());
					break;
				}
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
			getLogger().debug("buscaMailJefeOficina - [BCI_FIN]");
		}

		return emailJefeOficina.trim();
	}
		
    /**
     * <p>Método encargado de la journalización de un evento.</p>
     * <p>
     * Registro de versiones:
     * <ul>
     * 
     * <li>1.0 15/06/2015, Manuel Escárate  (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li>
     * <li>1.1 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): se agrega data a journal.</li>
     * <li>1.2 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): se modifica dígito verificador.</li>
     * <li>1.3 18/05/2016, Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): Se modifica monto a journalizar. </li>
     * </ul>
     * @param codEvento Código del evento.
     * @param subCodEvento Sub código del evento.
     * @param producto Código del producto.
     * 
     * @since 1.0
     */
    public void journalizar(String codEvento, String subCodEvento, String producto) {
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[journalizar] [BCI_INI] ["+usuarioModelMB.getRut()
    				+"] codEvento [" + codEvento + "]   subCodEvento ["+subCodEvento+"]   producto ["+producto+"]");
    	}
    	try{
            String estadoCanal = TablaValores.getValor(TABLA_MULTILINEA, "journalAvance", "estadoCanal");
            estadoCanal = estadoCanal == null ? "" : estadoCanal.trim();
    		HashMap datos = new HashMap();
    		datos.put("codEventoNegocio", codEvento);
    		datos.put("subCodEventoNegocio", subCodEvento);
    		datos.put("idProducto", producto);
            datos.put("rutCliente", clienteMB.getRut());
            datos.put("dvCliente", clienteMB.getDigitoVerif());
            datos.put("rutOperadorCliente", String.valueOf(usuarioModelMB.getRut()));
            datos.put("dvOperadorCliente", String.valueOf(usuarioModelMB.getDigitoVerif()));
            datos.put("idCanal", String.valueOf(sesion.getCanalTO().getNombre()));
            datos.put("estadoCanal", estadoCanal);
            String montoJournal =  montoSolicitado.replace(".","").replace(",","");
            datos.put("monto", Double.parseDouble(montoJournal)); 

    		Journalist.getInstance().publicar(datos);

    		if(getLogger().isEnabledFor(Level.DEBUG)){
    			getLogger().debug("[journalizacion][" + usuarioModelMB.getRut() + "] [BCI_FINOK]");
    		}
    	}
    	catch(Exception e){
    		if(getLogger().isEnabledFor(Level.ERROR)){
    			getLogger().error("[journalizacion][BCI_FINEX][" + usuarioModelMB.getRut() + "] [Exception] " 
    					+"No Journalizo, mensaje=<" 
    					+ e.getMessage() + "> ",e);
    		}
    	}

    }
    
    /**
     * Método para obtener instancia del servicio de direcciones.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 12/06/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
     * </ul>
     * @return servicio de direcciones.
     * @throws DireccionesException excepcion de direccion.
     * @since 1.0
     */     
    private ServiciosDirecciones instanciaEJBServicioDirecciones()
    		throws DireccionesException{
    	if (getLogger().isDebugEnabled()) getLogger().debug("[instanciaEJBServicioDirecciones] Instacia EJB direcciones");
    	ServiciosDireccionesHome localServiciosDireccionesHome = null;
    	try{
    		EnhancedServiceLocator localEnhancedServiceLocator1 = EnhancedServiceLocator.getInstance();
    		localServiciosDireccionesHome = (ServiciosDireccionesHome)localEnhancedServiceLocator1.getHome("wcorp.serv.direcciones.ServiciosDirecciones", ServiciosDireccionesHome.class);
    		return localServiciosDireccionesHome.create();
    	}
    	catch (Exception localException1){
    		if (getLogger().isEnabledFor(Level.ERROR)) {
    			getLogger().error("[instanciaEJBServicioDirecciones] Fallo 1er intento.", localException1);
    		}
    		try{
    			EnhancedServiceLocator localEnhancedServiceLocator2 = EnhancedServiceLocator.getInstance();
    			localServiciosDireccionesHome = (ServiciosDireccionesHome)localEnhancedServiceLocator2.getHome("wcorp.serv.direcciones.ServiciosDirecciones", ServiciosDireccionesHome.class);
    			return localServiciosDireccionesHome.create();
    		}
    		catch (Exception localException2){
    			if (getLogger().isEnabledFor(Level.ERROR)) {
    				getLogger().error("[instanciaEJBServicioDirecciones] Fallo 2o intento.", localException1);
    			}
    			throw new DireccionesException("003", localException1.getMessage());
    		}
    	}
    }
    
    /**
     * Método para regresar a vista anterior.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 12/06/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
     * <li>1.1 22/09/2015 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): se agrega limpieza de instancia view.</li>
     * </ul>
     * @param pasoVista al que vuelve.
     * @since 1.0
     */    
    public void volverPaso(int pasoVista){
        if (getLogger().isDebugEnabled()){
            getLogger().debug("[volverPaso][BCI_INI]");
        }
    	paso = pasoVista;
        FacesContext.getCurrentInstance().getViewRoot().getViewMap().clear();
        if (getLogger().isDebugEnabled()){
            getLogger().debug("[volverPaso][BCI_FINOK]");
        }
    }
    
    /**
     * Obtiene los gastos notariales.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 29/07/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): versión inicial.</li> 
     * </ul>
     * @param multiEnvironment multiambiente.
     * @param cai cai de la operación.
     * @param iic iic de la operación.
     * @return seguros del credito.
     * @since 1.5
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
             if (getLogger().isDebugEnabled()) getLogger().debug("[obtenerGastoNotarial][BCI_FINEX] ERROR***Exception...Exception [" + e.getMessage() + "]");
         }
         if (getLogger().isDebugEnabled()){
             getLogger().debug("[obtenerGastoNotarial][BCI_FINOK]");
         }
         return obeanCGRCGN;
    }
    
    /**
     * retorna texto con descripcion encontrada en la lista
     * <p>
     * Registro de versiones:
     * <ul>
     *
     * <li>1.0 04/08/2016, Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI): versión inicial.
     *
     * </ul>
     * </p>
     *
     * @param data Lista a recorrer para obtener descripcion.
     * @param campo campo de la descripción a obtener.
     * @return string con la descripcion larga del sistema.
     *
     * @since 1.5
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
                    return row.getLongDescription();
                }
            }
        }
    	if (getLogger().isDebugEnabled()){
            getLogger().debug("[getValor][BCI_FINOK]");    
        }
        return "";
    }
    
    /**
     * obtiene lista de filas que son la tabla consultada.
     * <p>
     *
     * Registro de versiones:
     * <ul>
     *
     * <li>1.0 04/08/2016, Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI): : versión inicial.
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
     * @since 1.5
     */
    public List obtenerTablaDescripciones(TableManagerService tableManagerService
                                          , MultiEnvironment multienvironment
                                          , String sistema
                                          , String tabla
                                          , String codigo) throws Exception, RemoteException,
                                          TableAccessException {
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[obtenerTablaDescripciones][" + clienteMB.getRut() + "][BCI_INI]");
    	}
        TableSpec tableSpec = new TableSpecImpl(sistema, tabla, codigo);
        if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[obtenerTablaDescripciones][" + clienteMB.getRut() + "][BCI_FINOK]");
    	}
        return tableManagerService.query(multienvironment, tableSpec, true);
    }
    
    /**
	 * Método encargado de enviar correo a contacto de empresa.
	 * <br>
	 * Registro de versiones:<ul>
	 * <li>1.0 11/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): versión inicial.</li> 
	 * </ul>
	 * @param multiEnvironment multiambiente.
	 * @param estadoAvance estado del avance.
	 * @param asuntoAvance asunto del avance.
	 * @throws Exception excepcion en el metodo.
	 * @since 1.5
	 */    
	public void enviarCorreoContactoEmpresa(MultiEnvironment multiEnvironment, String estadoAvance,String asuntoAvance) throws Exception{
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
		int diaDeVencimiento = 0;
		if (calendarioPagoSalida != null && calendarioPagoSalida[cuotaSeleccionada-1].getFecVencPago() != null){
			fechaUltimaCuota =  FechasUtil.convierteDateAString(calendarioPagoSalida[cuotaSeleccionada-1].getFecVencPago(),"dd/MM/yyyy"); 
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
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[enviarCorreoContactoEmpresa] consultando destinoDelCredito " + "  [" + caiOperacion + iicOperacion + "]" );
			}
					
			ResultConsultaOperacionCredito resConOpe = crearEJBmultilinea().consultaOperacionCredito(multiEnvironment, caiOperacion, iicOperacion);
			destinoDelCredito  = resConOpe.getGlosaDestinoEspecifico();
			ResultConsultaCgr seguros = obtenerSeguros(multiEnvironment, caiOperacion, String.valueOf(iicOperacion));
			double sumaSeguros = 0;
			if (seguros != null && seguros.getInstanciaDeConsultaCgr() != null){
				for(int j = 0; j < seguros.getInstanciaDeConsultaCgr().length; j++){
					IteracionConsultaCgr cgr = seguros.getInstanciaDeConsultaCgr()[j];
					if (cgr.getIndVigenciaInst() == 'S'){
						sumaSeguros = sumaSeguros + cgr.getTasaMontoFinal();
					}

				}
			}
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[enviarCorreoContactoEmpresa] Total Seguros: " + sumaSeguros);
			}
			segurosCorreo = sumaSeguros;	
			ResultConsultaCgr gastos = obtenerGastoNotarial(multiEnvironment, caiOperacion, String.valueOf(iicOperacion));
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
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[enviarCorreoContactoEmpresa] Gastos Notariales: " + gastoNotario);
			}
			valorNotarioCorreo = gastoNotario;
			impuestosCorreo = resConOpe.getImpuestos();
			if (tipoDeCreditoSeleccionado.equals("AVC721")){
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
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[enviarCorreoContactoEmpresa] despues destinoDelCredito " + "[" + caiOperacion + iicOperacion + "]" );
			}
					
		}
		catch (Exception ex) {
			if (getLogger().isEnabledFor(Level.ERROR)) { 
				getLogger().error("[enviarCorreoContactoEmpresa][" + clienteMB.getRut() 
						+"][BCI_FINEX][enviarCorreoContactoEmpresa]" +" error con mensaje: " + ex.getMessage(), ex);
			}  
			destinoDelCredito = " ";
		}
		try {
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[enviarCorreoContactoEmpresa] antes de recorrer datos avales para correo");
			}
			if (datosAvalesCorreo != null && datosAvalesCorreo.length > 0){
				textosParaAvales = crearEJBmultilinea().obtenerTextosParaAvales();
				for (int i = 0; i < datosAvalesCorreo.length; i++) {
					if (datosAvalesCorreo[i] != null){
						DireccionClienteBci[] direcciones = instanciaEJBServicioDirecciones().getAddressBci(Long.parseLong(datosAvalesCorreo[i].getRutAval()));
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
						datosAval.setRutAval(datosAvalesCorreo[i].getRutAval());
						datosAval.setDvAval(datosAvalesCorreo[i].getDvAval());
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
			datosCorreo.setCaiOperacion(caiOperacion);
			datosCorreo.setIicOperacion(iicOperacion);
			datosCorreo.setFechaVencimientoSeleccionada(fechaVencimientoSeleccionada);
			datosCorreo.setRut(clienteMB.getRut());
			datosCorreo.setDv(clienteMB.getDigitoVerif());
			datosCorreo.setRazonSocial(clienteMB.getRazonSocial());
			datosCorreo.setMontoFinalCredito(montoCorreo);
			datosCorreo.setCuotaSeleccionada(cuotaSeleccionada);
			datosCorreo.setDestinoDelCredito(destinoDelCredito);
			datosCorreo.setEstado(estadoAvance);
			datosCorreo.setAsunto(asuntoAvance);
			datosCorreo.setTasaInteres(formatearMonto((tasaInteresInternet),VALOR_DOS,"#,##0"));
			String montoEnPalabras = TextosUtil.numeroEnPalabras(montoCorreo);
			datosCorreo.setMontoExpresadoEnPalabras(montoEnPalabras);
			datosCorreo.setValorCuota(valorCuota);
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
			enviarEmailContactoEmpresa(multiEnvironment,datosCorreo);
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
     * <li>1.0 19/03/2015, Manuel Escárate R. (BEE) - Felipe Ojeda. (ing.Soft.BCI): versión inicial.</li>
     *
     * </ul>
     * 
     * @param multiEnvironment multiambiente.
     * @param datosCorreo datos necesarios para el envío de correo.
     * 
     * @throws GeneralException excepcion general.
     * @since 1.5
     */
    public  void enviarEmailContactoEmpresa(MultiEnvironment multiEnvironment, 
            DatosParaCorreoTO datosCorreo) throws GeneralException {
        
    	if (getLogger().isDebugEnabled()) {
            getLogger().debug("[enviarEmailContactoEmpresa] [BCI INI]");
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
        fechaPrimerVencimiento = fechaPrimerVcto.substring(0,VALOR_DOS) 
        		+ "/" +fechaPrimerVcto.substring(VALOR_TRES,VALOR_CINCO) + "/" 
                +fechaPrimerVcto.substring(VALOR_SEIS);
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
	 * <li>1.0 09/08/2016 Manuel Escárate(BEE) - Felipe Ojeda. (ing.Soft.BCI) )
	 * : Version Inicial</li>
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
	 * @since 1.5
	 */
	public String armaEmailParaDestinatarioContacto(
			DatosParaCorreoTO datosCorreo, String codBancaCorreo,
			String fechaPrimerVcto, String montoCredito, String estado)
			throws GeneralException {
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
				getLogger().debug("[armaEmailParaDestinatarioContacto] [tipoDeCreditoSeleccionado]" + tipoDeCreditoSeleccionado);
			}
			if (tipoDeCreditoSeleccionado.equals("AVC010")){
				archivoMail = TablaValores.getValor("multilinea.parametros",
						"archivoMailAvalesUnVencimientoContacto", "desc");
			}
			else if (tipoDeCreditoSeleccionado.equals("AVC721")){
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
			return cuerpoMail;

		} 
		catch (Exception e) {
			if (getLogger().isEnabledFor(Level.ERROR)) {
				getLogger().error("[armaEmailParaDestinatarioContacto] [BCI_FINEX] Exception " + e.getMessage());
			}
			throw new GeneralException("UNKNOW", e.getMessage());
		}

	}
    
    /**
     * Método que permite obtener arreglo para el combobox de comunas, las cuales corresponden a la ciudad
     * seleccionada.
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0 19/03/2015 Manuel Escárate R. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI): Versin Inicial
     * </ul>
     * <p>
     * @return arreglo con comunas de una ciudad en particular.
     * @since 1.0
     */
    public ArrayList<SelectItem> getComunas() {
        if (getLogger().isDebugEnabled()){
            getLogger().debug("[getcomunas][BCI_INI]");    
        }
            try {
                this.comunas = new ArrayList<SelectItem>();
                ServiciosMiscelaneos serviciosMiscelaneos = getServiciosMiscelaneos();
                DescComuna[] comunasRegion = serviciosMiscelaneos.obtenerComunasPorRegion(
                   (codigoRegion), " ", " ");
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[obtenerComunasCiudad] transformando a ComunaTO: "+codigoRegion);
                }
                this.comunasPorRegion = transformaAComunaTO(comunasRegion);
                
                if ((comunasPorRegion != null) && (comunasPorRegion.length > 0)){
                    for (int i = 0; i < comunasPorRegion.length; i++) {
                        this.comunas.add(new SelectItem(comunasPorRegion[i].getCodigo(),
                                comunasPorRegion[i].getNombre()));
                    }
                }
            }
            catch (Exception ex){
                if (getLogger().isEnabledFor(Level.ERROR)) {
                    getLogger().debug("[getcomunas] Exception:" + ErroresUtil.extraeStackTrace(ex));
                }
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[getcomunas][BCI_FINOK] retornando objeto : " + this.comunas);
            }
        
        return comunas;
    }
    

    /**
     * Método permite obtener las cuotas para modificar.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 02/04/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI): versión inicial.</li> 
     * </ul>
     * @since 1.0
     * @return cuotasMod objeto para cuotas si se van a modificar.
     */
    public ArrayList<SelectItem> getCuotasMod() {
	    if (getLogger().isDebugEnabled()){
              getLogger().debug("[getCuotasMod][BCI_INI]");    
        }
        cuotasMod = new ArrayList<SelectItem>();
        for (int i = 0; i < datosPlantilla.length; i++) {
            if (datosPlantilla[i].getCodigoAuxiliar().equals("721")){
                cuotaInicial = datosPlantilla[i].getCuotaInicial();
                cuotaFinal =  datosPlantilla[i].getCuotaFinal();
            }
        } 
        totalCuotas = cuotaFinal-cuotaInicial;
        for (int i = cuotaInicial; i <= totalCuotas; i++) {
            String cuota = String.valueOf(i);
            cuotasMod.add(new SelectItem(i,cuota));
        } 
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[getCuotasMod][BCI_FINOK]");
        }
        return cuotasMod;
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


    public boolean isTieneMultilineaVigente() {
        return tieneMultilineaVigente;
    }

    public void setTieneMultilineaVigente(boolean tieneMultilineaVigente) {
        this.tieneMultilineaVigente = tieneMultilineaVigente;
    }

    public boolean isTieneMultilinea() {
        return tieneMultilinea;
    }

    public void setTieneMultilinea(boolean tieneMultilinea) {
        this.tieneMultilinea = tieneMultilinea;
    }

    public boolean isTieneMultilineaMonto() {
        return tieneMultilineaMonto;
    }

    public void setTieneMultilineaMonto(boolean tieneMultilineaMonto) {
        this.tieneMultilineaMonto = tieneMultilineaMonto;
    }

    public int getCuotaSeleccionada() {
        return cuotaSeleccionada;
    }

    public void setCuotaSeleccionada(int cuotaSeleccionada) {
        this.cuotaSeleccionada = cuotaSeleccionada;
    }

    public ArrayList<SelectItem> getCuotas() {
        return cuotas;
    }

    public void setCuotas(ArrayList<SelectItem> cuotas) {
        this.cuotas = cuotas;
    }

    public void setTiposDeCreditos(ArrayList<SelectItem> tiposDeCreditos) {
        this.tiposDeCreditos = tiposDeCreditos;
    }
    
    public String getTipoDeCreditoSeleccionado() {
        return tipoDeCreditoSeleccionado;
    }

    public void setTipoDeCreditoSeleccionado(String tipoDeCreditoSeleccionado) {
        this.tipoDeCreditoSeleccionado = tipoDeCreditoSeleccionado;
    }

    public int getPaso() {
        return paso;
    }

    public void setPaso(int paso) {
        this.paso = paso;
    }

    public boolean isNoCargaMetodosInicio() {
        return noCargaMetodosInicio;
    }

    public void setNoCargaMetodosInicio(boolean noCargaMetodosInicio) {
        this.noCargaMetodosInicio = noCargaMetodosInicio;
    }

    public String getMontoSolicitado() {
        return montoSolicitado;
    }

    public void setMontoSolicitado(String montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
    }
    
    public DatosPlantillaProductoTO[] getDatosPlantilla() {
        return datosPlantilla;
    }

    public void setDatosPlantilla(DatosPlantillaProductoTO[] datosPlantilla) {
        this.datosPlantilla = datosPlantilla;
    }

    public void setTiposDeCreditosAmostrar(
            ArrayList<SelectItem> tiposDeCreditosAmostrar) {
        this.tiposDeCreditosAmostrar = tiposDeCreditosAmostrar;
    }

    public String getFechaVencimientoSeleccionada() {
        return fechaVencimientoSeleccionada;
    }

    public void setFechaVencimientoSeleccionada(String fechaVencimientoSeleccionada) {
        this.fechaVencimientoSeleccionada = fechaVencimientoSeleccionada;
    }

    public String getCodBanca() {
        return codBanca;
    }

    public void setCodBanca(String codBanca) {
        this.codBanca = codBanca;
    }

    public char getPlan() {
        return plan;
    }

    public void setPlan(char plan) {
        this.plan = plan;
    }

    public String getOficinaIngreso() {
        return oficinaIngreso;
    }

    public void setOficinaIngreso(String oficinaIngreso) {
        this.oficinaIngreso = oficinaIngreso;
    }

    public String getCodigoEjecutivo() {
        return codigoEjecutivo;
    }

    public void setCodigoEjecutivo(String codigoEjecutivo) {
        this.codigoEjecutivo = codigoEjecutivo;
    }

    public String getCodSegmento() {
        return codSegmento;
    }

    public void setCodSegmento(String codSegmento) {
        this.codSegmento = codSegmento;
    }

    public ProductosMB getProductosMB() {
        return productosMB;
    }

    public void setProductosMB(ProductosMB productosMB) {
        this.productosMB = productosMB;
    }

    public double getValorCuota() {
        return valorCuota;
    }

    public void setValorCuota(double valorCuota) {
        this.valorCuota = valorCuota;
    }

    public double getTasaInteresInternet() {
        return tasaInteresInternet;
    }

    public void setTasaInteresInternet(double tasaInteresInternet) {
        this.tasaInteresInternet = tasaInteresInternet;
    }

    public double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(double impuestos) {
        this.impuestos = impuestos;
    }

    

    public double getGastosNotariales() {
		return gastosNotariales;
	}

	public void setGastosNotariales(double gastosNotariales) {
		this.gastosNotariales = gastosNotariales;
	}

	public double getMontoFinalCredito() {
        return montoFinalCredito;
    }

    public void setMontoFinalCredito(double montoFinalCredito) {
        this.montoFinalCredito = montoFinalCredito;
    }

    public double getFactorCae() {
        return factorCae;
    }

    public void setFactorCae(double factorCae) {
        this.factorCae = factorCae;
    }

    public double getMontoDisponible() {
        return montoDisponible;
    }

    public void setMontoDisponible(double montoDisponible) {
        this.montoDisponible = montoDisponible;
    }

    public double getCostoFinalCredito() {
        return costoFinalCredito;
    }

    public void setCostoFinalCredito(double costoFinalCredito) {
        this.costoFinalCredito = costoFinalCredito;
    }

    public DatosSegurosTO[] getSegurosObtenidos() {
        return segurosObtenidos;
    }

    public void setSegurosObtenidos(DatosSegurosTO[] segurosObtenidos) {
        this.segurosObtenidos = segurosObtenidos;
    }

    public SelectItem[] getCuentasCorrientesYPrimas() {
        return cuentasCorrientesYPrimas;
    }

    public void setCuentasCorrientesYPrimas(SelectItem[] cuentasCorrientesYPrimas) {
        this.cuentasCorrientesYPrimas = cuentasCorrientesYPrimas;
    }

    public String getCuentaSeleccionadaAbono() {
        return cuentaSeleccionadaAbono;
    }

    public void setCuentaSeleccionadaAbono(String cuentaSeleccionadaAbono) {
        this.cuentaSeleccionadaAbono = cuentaSeleccionadaAbono;
    }

    public String getCuentaSeleccionadaCargo() {
        return cuentaSeleccionadaCargo;
    }

    public void setCuentaSeleccionadaCargo(String cuentaSeleccionadaCargo) {
        this.cuentaSeleccionadaCargo = cuentaSeleccionadaCargo;
    }

    public ServicioAutorizacionyFirmaModelMB getServicioAutorizacionyFirmaModelMB() {
        return servicioAutorizacionyFirmaModelMB;
    }

    public void setServicioAutorizacionyFirmaModelMB(
            ServicioAutorizacionyFirmaModelMB servicioAutorizacionyFirmaModelMB) {
        this.servicioAutorizacionyFirmaModelMB = servicioAutorizacionyFirmaModelMB;
    }

    public String getCaiOperacion() {
        return caiOperacion;
    }

    public void setCaiOperacion(String caiOperacion) {
        this.caiOperacion = caiOperacion;
    }

    public int getIicOperacion() {
        return iicOperacion;
    }

    public void setIicOperacion(int iicOperacion) {
        this.iicOperacion = iicOperacion;
    }

    public String getFechaMaximaPrimerVencimiento() {
        return fechaMaximaPrimerVencimiento;
    }

    public void setFechaMaximaPrimerVencimiento(String fechaMaximaPrimerVencimiento) {
        this.fechaMaximaPrimerVencimiento = fechaMaximaPrimerVencimiento;
    }

    public double getMontoMinimoParaCursar() {
        return montoMinimoParaCursar;
    }

    public void setMontoMinimoParaCursar(double montoMinimoParaCursar) {
        this.montoMinimoParaCursar = montoMinimoParaCursar;
    }

    public String getFechaVencimientoMLT() {
        return fechaVencimientoMLT;
    }

    public void setFechaVencimientoMLT(String fechaVencimientoMLT) {
        this.fechaVencimientoMLT = fechaVencimientoMLT;
    }

    public String getOfertaCrediticia() {
        return ofertaCrediticia;
    }

    public void setOfertaCrediticia(String ofertaCrediticia) {
        this.ofertaCrediticia = ofertaCrediticia;
    }

    public String getEstiloOfertaCrediticia() {
        return estiloOfertaCrediticia;
    }

    public void setEstiloOfertaCrediticia(String estiloOfertaCrediticia) {
        this.estiloOfertaCrediticia = estiloOfertaCrediticia;
    }

    public String getDiasTopePrimerVencimiento() {
        return diasTopePrimerVencimiento;
    }

    public void setDiasTopePrimerVencimiento(String diasTopePrimerVencimiento) {
        this.diasTopePrimerVencimiento = diasTopePrimerVencimiento;
    }

    public int getCuotaInicial() {
        return cuotaInicial;
    }

    public void setCuotaInicial(int cuotaInicial) {
        this.cuotaInicial = cuotaInicial;
    }

    public int getCuotaFinal() {
        return cuotaFinal;
    }

    public void setCuotaFinal(int cuotaFinal) {
        this.cuotaFinal = cuotaFinal;
    }

    public int getTotalCuotas() {
        return totalCuotas;
    }

    public void setTotalCuotas(int totalCuotas) {
        this.totalCuotas = totalCuotas;
    }

    public String getDiasIniPrimerVencimiento() {
        return diasIniPrimerVencimiento;
    }

    public void setDiasIniPrimerVencimiento(String diasIniPrimerVencimiento) {
        this.diasIniPrimerVencimiento = diasIniPrimerVencimiento;
    }

    public ArrayList<SelectItem> getRegiones() {
        return regiones;
    }

    public void setRegiones(ArrayList<SelectItem> regiones) {
        this.regiones = regiones;
    }
    
    public void setComunas(ArrayList<SelectItem> comunas) {
        this.comunas = comunas;
    }

    public String getCodigoComuna() {
        return codigoComuna;
    }

    public void setCodigoComuna(String codigoComuna) {
        this.codigoComuna = codigoComuna;
    }

    public boolean isSeguroSeleccionado() {
        return seguroSeleccionado;
    }

    public void setSeguroSeleccionado(boolean seguroSeleccionado) {
        this.seguroSeleccionado = seguroSeleccionado;
    }

    public void setCuotasMod(ArrayList<SelectItem> cuotasMod) {
        this.cuotasMod = cuotasMod;
    }

    public SegundaClaveUIInput getSegundaClaveAplicativo() {
        return segundaClaveAplicativo;
    }

    public void setSegundaClaveAplicativo(SegundaClaveUIInput segundaClaveAplicativo) {
        this.segundaClaveAplicativo = segundaClaveAplicativo;
    }

    public boolean isAceptaCondiciones() {
        return aceptaCondiciones;
    }

    public void setAceptaCondiciones(boolean aceptaCondiciones) {
        this.aceptaCondiciones = aceptaCondiciones;
    }

    public double getMontoTotalSegurosSeleccionados() {
        return montoTotalSegurosSeleccionados;
    }

    public void setMontoTotalSegurosSeleccionados(
            double montoTotalSegurosSeleccionados) {
        this.montoTotalSegurosSeleccionados = montoTotalSegurosSeleccionados;
    }
    
    public double getMontoMaximoParaCursar() {
        return montoMaximoParaCursar;
    }

    public void setMontoMaximoParaCursar(double montoMaximoParaCursar) {
        this.montoMaximoParaCursar = montoMaximoParaCursar;
    }

    public UsuarioModelMB getUsuarioModelMB() {
        return usuarioModelMB;
    }

    public void setUsuarioModelMB(UsuarioModelMB usuarioModelMB) {
        this.usuarioModelMB = usuarioModelMB;
    }
    
    public boolean isBancaEnvioEmailEjecutivo() {
        return bancaEnvioEmailEjecutivo;
    }

    public void setBancaEnvioEmailEjecutivo(boolean bancaEnvioEmailEjecutivo) {
        this.bancaEnvioEmailEjecutivo = bancaEnvioEmailEjecutivo;
    }

    
    public String getGlosaTipoCredito() {
        return glosaTipoCredito;
    }

    public void setGlosaTipoCredito(String glosaTipoCredito) {
        this.glosaTipoCredito = glosaTipoCredito;
    }
    
    public String getCondicionGarantia() {
        return condicionGarantia;
    }

    public void setCondicionGarantia(String condicionGarantia) {
        this.condicionGarantia = condicionGarantia;
    }
    
    public CalendarioPago[] getCalendarioPago() {
        return calendarioPago;
    }

    public void setCalendarioPago(CalendarioPago[] calendarioPago) {
        this.calendarioPago = calendarioPago;
    }

    public ResultConsultaOperacionCredito getOperacionCredito() {
        return operacionCredito;
    }

    public void setOperacionCredito(ResultConsultaOperacionCredito operacionCredito) {
        this.operacionCredito = operacionCredito;
    }
    
    public String getResultadoFirmaAvance() {
        return resultadoFirmaAvance;
    }

    public void setResultadoFirmaAvance(String resultadoFirmaAvance) {
        this.resultadoFirmaAvance = resultadoFirmaAvance;
    }
    
    public ComunaSupportMB getComunaSupportMB() {
        return comunaSupportMB;
    }

    public void setComunaSupportMB(ComunaSupportMB comunaSupportMB) {
        this.comunaSupportMB = comunaSupportMB;
    }

    public RegionSupportMB getRegionSupportMB() {
        return regionSupportMB;
    }

    public void setRegionSupportMB(RegionSupportMB regionSupportMB) {
        this.regionSupportMB = regionSupportMB;
    }
    
    public String getCodigoRegion() {
        return codigoRegion;
    }

    public void setCodigoRegion(String codigoRegion) {
        this.codigoRegion = codigoRegion;
    }

    public ComunaTO[] getComunasPorRegion() {
        return comunasPorRegion;
    }

    public void setComunasPorRegion(ComunaTO[] comunasPorRegion) {
        this.comunasPorRegion = comunasPorRegion;
    }

    public String getFechaCurse() {
        return fechaCurse;
    }

    public void setFechaCurse(String fechaCurse) {
        this.fechaCurse = fechaCurse;
    }

    public boolean isMuestraSeguros() {
        return muestraSeguros;
    }

    public void setMuestraSeguros(boolean muestraSeguros) {
        this.muestraSeguros = muestraSeguros;
    }

    public boolean isMuestraResumen() {
        return muestraResumen;
    }

    public void setMuestraResumen(boolean muestraResumen) {
        this.muestraResumen = muestraResumen;
    }

    public boolean isMuestraCalendario() {
        return muestraCalendario;
    }

    public void setMuestraCalendario(boolean muestraCalendario) {
        this.muestraCalendario = muestraCalendario;
    }

    public CalendarioPago[] getCalendarioPagoSalida() {
        return calendarioPagoSalida;
    }

    public void setCalendarioPagoSalida(CalendarioPago[] calendarioPagoSalida) {
        this.calendarioPagoSalida = calendarioPagoSalida;
    }

    public boolean isBancaPermitidasEmpresarioEmail() {
        return bancaPermitidasEmpresarioEmail;
    }

    public void setBancaPermitidasEmpresarioEmail(
            boolean bancaPermitidasEmpresarioEmail) {
        this.bancaPermitidasEmpresarioEmail = bancaPermitidasEmpresarioEmail;
    }
    
    public int getRequierenDPS() {
        return requierenDPS;
    }

    public void setRequierenDPS(int requierenDPS) {
        this.requierenDPS = requierenDPS;
    }

    public HashMap getListaFeriados() {
        return listaFeriados;
    }

    public void setListaFeriados(HashMap listaFeriados) {
        this.listaFeriados = listaFeriados;
    }

	public DatosAvalesTO[] getDatosAvales() {
		return datosAvales;
	}

	public void setDatosAvales(DatosAvalesTO[] datosAvales) {
		this.datosAvales = datosAvales;
	}

	public boolean isHoraPermitida() {
		return horaPermitida;
	}

	public void setHoraPermitida(boolean horaPermitida) {
		this.horaPermitida = horaPermitida;
	}
	
	public String getFormatoUf() {
		return FORMATO_UF;
	}

	public String getFormatoPesos() {
		return FORMATO_PESOS;
	}	
    
    public String getCondicionGarantiaIni() {
        return condicionGarantiaIni;
    }

    public void setCondicionGarantiaIni(String condicionGarantiaIni) {
        this.condicionGarantiaIni = condicionGarantiaIni;
    }

    public ArrayList<String> getExcepciones() {
        return excepciones;
    }

    public void setExcepciones(ArrayList<String> excepciones) {
        this.excepciones = excepciones;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }

    public boolean isEsErrorGenerico() {
        return esErrorGenerico;
    }

    public void setEsErrorGenerico(boolean esErrorGenerico) {
        this.esErrorGenerico = esErrorGenerico;
    }
     
    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getNombreFantasia() {
        return nombreFantasia;
    }

    public void setNombreFantasia(String nombreFantasia) {
        this.nombreFantasia = nombreFantasia;
    }
    
    public String getTelefonoCliente() {
		return telefonoCliente;
	}

	public void setTelefonoCliente(String telefonoCliente) {
		this.telefonoCliente = telefonoCliente;
	}

	public String getCodTelefonoCliente() {
		return codTelefonoCliente;
	}

	public void setCodTelefonoCliente(String codTelefonoCliente) {
		this.codTelefonoCliente = codTelefonoCliente;
	}

	public String getCelularCliente() {
		return celularCliente;
	}

	public void setCelularCliente(String celularCliente) {
		this.celularCliente = celularCliente;
	}
	
	public String getCodCelularCliente() {
		return codCelularCliente;
	}

	public void setCodCelularCliente(String codCelularCliente) {
		this.codCelularCliente = codCelularCliente;
	}

	public String getEmailCliente() {
		return emailCliente;
	}

	public void setEmailCliente(String emailCliente) {
		this.emailCliente = emailCliente;
	}

	public String getRegionCliente() {
		return regionCliente;
	}

	public void setRegionCliente(String regionCliente) {
		this.regionCliente = regionCliente;
	}

	public String getComunaCliente() {
		return comunaCliente;
	}

	public void setComunaCliente(String comunaCliente) {
		this.comunaCliente = comunaCliente;
	}     

    public boolean isTieneMandato() {
        return tieneMandato;
    }

    public void setTieneMandato(boolean tieneMandato) {
        this.tieneMandato = tieneMandato;
    }

    public boolean isMostrarCondicionesMandato() {
        return mostrarCondicionesMandato;
    }

    public void setMostrarCondicionesMandato(boolean mostrarCondicionesMandato) {
        this.mostrarCondicionesMandato = mostrarCondicionesMandato;
    }

    public boolean isAceptaCondicionesMandato() {
        return aceptaCondicionesMandato;
    }

    public void setAceptaCondicionesMandato(boolean aceptaCondicionesMandato) {
        this.aceptaCondicionesMandato = aceptaCondicionesMandato;
    }

    public String getCodMoneda() {
		return codMoneda;
	}

	public void setCodMoneda(String codMoneda) {
		this.codMoneda = codMoneda;
	}

	public String getTipoDePlazo() {
		return tipoDePlazo;
	}
	
	public void setTipoDePlazo(String tipoDePlazo) {
		this.tipoDePlazo = tipoDePlazo;
	}

	public DatosAvalesTO[] getDatosAvalesCorreo() {
		return datosAvalesCorreo;
	}

	public void setDatosAvalesCorreo(DatosAvalesTO[] datosAvalesCorreo) {
		this.datosAvalesCorreo = datosAvalesCorreo;
	}

	public int getLargoComuna() {
		return largoComuna;
	}

	public void setLargoComuna(int largoComuna) {
		this.largoComuna = largoComuna;
	}
	
	public String getDisclaimerMandato() {
		return disclaimerMandato;
	}

	public void setDisclaimerMandato(String disclaimerMandato) {
		this.disclaimerMandato = disclaimerMandato;
	}

	public double getMontoCorreo() {
		return montoCorreo;
	}

	public void setMontoCorreo(double montoCorreo) {
		this.montoCorreo = montoCorreo;
	}
	
}