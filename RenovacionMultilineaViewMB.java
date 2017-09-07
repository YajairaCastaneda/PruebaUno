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
import java.util.Vector;

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
import wcorp.bprocess.multilinea.to.DatosParaRenovacionTO;
import wcorp.bprocess.multilinea.to.DatosPlantillaProductoTO;
import wcorp.bprocess.multilinea.to.DatosResultadoOperacionesTO;
import wcorp.bprocess.multilinea.to.DatosSegurosTO;
import wcorp.bprocess.precioscontextos.PreciosContextos;
import wcorp.bprocess.precioscontextos.PreciosContextosHome;
import wcorp.bprocess.tables.TableManagerService;
import wcorp.gestores.validador.utils.ValidaCuentasUtility;
import wcorp.model.actores.DireccionClienteBci;
import wcorp.serv.boletadegarantia.RowConsultaMasivaDeOficinas;
import wcorp.serv.controlriesgocrediticio.Aval;
import wcorp.serv.controlriesgocrediticio.ResultConsultaAvales;
import wcorp.serv.creditosglobales.CalendarioPago;
import wcorp.serv.creditosglobales.EstructuraVencimiento;
import wcorp.serv.creditosglobales.OperacionCreditoSuperAmp;
import wcorp.serv.creditosglobales.ResultCalculoValoresCancelacion;
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
 * <pre><b>RenovacionMultilineaViewMB</b>
 *
 * Componente encargado de interactuar con la vista para realizar una
 * renovación de créditos multilinea, a través de diferentes pasos.
 * </pre>
 *
 * Registro de versiones:<ul>
 *
 * <li>1.0  29/01/2015, Eduardo Pérez G. (BEE S.A.): Versión inicial. </li>
 * <li>1.1  12/11/2015, Eduardo Pérez G. (BEE S.A.): Se agrega validación de cuentas y journal en método cursarRenovacion.
 * <li>1.2  03/03/2016, Eduardo Pérez G. (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI)  : Se agregan atributos para el correo, se modifca lógica de 
 *                                                     correo backoffice, se agrega manejo de excepciones para añadir al correo, se modifican los 
 *                                                     siguientes métodos iniciarSimulacion,enviarSolicitud,enviaEmailEjecutivo,enviaEmailBackOffice,ingresarOperacionFirma,
 *                                                     enviarCorreoEjecutivo,armaEmailParaDestinatarioGenerico,mostrarCondicionesCredito,continuarRenovacion.
 * <li>1.3  11/02/2016, Eduardo Pérez G. (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se agrega validación y verificacion de tenencia de mandato.
 *                                       Se modifica metodo obtenerOperaciones, iniciarSimulacion, mas get y set correspondientes. se mejora javadoc.
 * </li>
 * <li>1.4  18/05/2016, Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI) : Se modifican los métodos generarComprobanteCargaPDF,iniciarSimulacion,obtenerValoresCancelacion
 *                                                   ,mostrarCondicionesCredito,iniciarSimulacion,cargarDatosBasicos,enviarSolicitud,continuarRenovacion,enviaEmailEjecutivo,obtieneDetalleTabla,enviaEmailBackOffice,cursarRenovacion,ingresarOperacionFirma
 *                                                   ,enviarCorreoEjecutivo,armaEmailParaDestinatarioGenerico.
 *                                                   Se agregan los métodos enviarCorreoAvales,enviaEmailAval,armaEmailParaDestinatarioAval.
 *                                                   Se agrega atributo TIPOMAIL y se eliminan VALOR_DOS,VALOR_TRES,VALOR_CINCO,VALOR_SEIS,VALOR_OCHO.
 * </li>
 * <li>1.5  18/08/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se modifican los métodos obtenerValoresCancelacion,generarComprobanteCargaPDF,mostrarCondicionesCredito,continuarRenovacion,enviarSolicitud,ingresarOperacionFirma
 *                                                   ,enviarCorreoEjecutivo,enviarCorreoAvales,enviaEmailAval,armaEmailParaDestinatarioAval,resumenCredito y se agregan los siguientes obtenerGastoNotarial, obtenerSeguros, obtenerTablaDescripciones,getValor
 *                                                   ,enviarCorreoContactoEmpresa,enviarEmailContactoEmpresa,armaEmailParaDestinatarioContacto,crearEJBprecios.</li>
 * </ul> 
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 */
@ManagedBean
@ViewScoped
public class RenovacionMultilineaViewMB implements Serializable {

	/**
	 * serialVersionUID de la clase.
	 */ 
	private static final long serialVersionUID = 1L;

	/**
	 * Atributo Logger.
	 */
	private static transient Logger logger  = (Logger)Logger.getLogger(RenovacionMultilineaViewMB.class);

	/**
	 * Nombre JNDI del EJB Multilinea.
	 */
	private static final String JNDI_NAME_MULTILINEA = "wcorp.bprocess.multilinea.Multilinea";
	
	   /**
     * Nombre JNDI del EJB PreciosContextos.
     */
    private static final String JNDI_NAME_PRECIOS = "wcorp.bprocess.precioscontextos.PreciosContextos";  
	
	/**
     * Atributo Númerico.
     */
    private static final int VALOR_MIL = 1000;
    
    /**
     * Atributo Númerico.
     */
    private static final int ERRORFIRMA999 = 999;
    
    /** 
     * Codigo tipo email.
     */
 	private static final char TIPOMAIL = '7';
    
    /**
	 * Resultado para oferta crediticia activa.
	 */
	private static final int CURSE_ACT_MTL = 1;
	
	/**
     * Error generico.
     */
    private static final int ERROR_GENERICO = -2;

	/**
	 * Resultado para oferta crediticia inactiva.
	 */
	private static final int SIM_CUR_CLI  = 2;

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
	 * Monto supera el maximo permitido.
	 */
	private static final int MONTO_SUPERADO = -1;

	/**
	 * Tabla fimraYPoderes.
	 */
	private static final String TABLA_FYP = "firmasYPoderes.parametros";
	
	/**
     * Tabla multilinea.
     */
    private static final String TABLA_MULTILINEA = "multilinea.parametros";
    
    /**
     * Tabla renmultilinea.
     */
    private static final String TABLA_RENMULTILINEA = "renmultilinea.parametros";

	/**
	 * Constante para identificar el usuario a utilizar en la construccin del objeto MultiEnvironment.
	 */
	private static final String USUARIO = TablaValores.getValor(TABLA_MULTILINEA,
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
	private static final String ACTIVA = TablaValores.getValor(TABLA_MULTILINEA, 
			"ofertaCrediticia", "activa");

	/**
	 * Oferta crediticia inactiva.
	 */
	private static final String INACTIVA = TablaValores.getValor(TABLA_MULTILINEA, 
			"ofertaCrediticia", "inactiva");

	/**
	 * Estilo de la oferta crediticia activa.
	 */
	private static final String OFERTA_VERDE =  TablaValores.getValor(TABLA_MULTILINEA, 
			"estiloOfertaCrediticia", "ofertaVerde");

	/**
	 * Estilo de la oferta crediticia inactiva.
	 */
	private static final String OFERTA_AMARILLA =  TablaValores.getValor(TABLA_MULTILINEA, 
			"estiloOfertaCrediticia", "ofertaAmarilla");

	/**
	 * Estilo de la oferta crediticia inactiva.
	 */
	private static final String OFERTA_NARANJA =  TablaValores.getValor(TABLA_MULTILINEA, 
			"estiloOfertaCrediticia", "ofertaNaranja");
	
	/**
	 * Valor máximo de descuento para renovación.
	 */
	private static final String DESCTO_VENC_REN =  TablaValores.getValor(TABLA_MULTILINEA, 
			"descuentoVencimientoRenovacion", "valor");
	
	 /**
     * sistema tablas generales.
     */
    private static final String TABLE_MANANGER_SISTEMA_TAB = "TAB";
    
    /**
     * sistema tablas generales de colocaciones.
     */
    private static final String TABLE_MANANGER_TABLA_COM = "COM";
	
	/**
	 * Largo del código de moneda.
	 */
	private static final int LARGO_CODIGO_MONEDA =  6;
	
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
	 * Atributo numérico.
	 */
	private static final int WHOVISA2 =  2;
	
	/**
	 * Atributo numérico.
	 */
	private static final int VALOR0 =  0;
	
	/**
	 * Atributo numérico.
	 */
	private static final int VALOR2 =  2;
	
	/**
	 * Atributo numérico.
	 */
	private static final int VALOR3 =  3;
	
	/**
	 * Atributo numérico.
	 */
	private static final int VALOR4 =  4;
	
	/**
	 * Atributo numérico.
	 */
	private static final int VALOR5 =  5;
	
	/**
	 * Atributo numérico.
	 */
	private static final int VALOR6 =  6;
	
	/**
	 * Atributo numérico.
	 */
	private static final int VALOR7 =  7;
	
	/**
	 * Atributo numérico.
	 */
	private static final int VALOR10 =  10;
	
	/**
	 * Atributo numérico.
	 */
	private static final int PASO2 =  2;
	
	/**
	 * Atributo numérico.
	 */
	private static final int PASO3 =  3;
	
	/**
	 * Atributo numérico.
	 */
	private static final int PASO4 =  4;
	
	/**
	 * Atributo numérico.
	 */
	private static final int PASO5 =  5;
	
	/**
	 * Atributo numérico.
	 */
	private static final int PASO6 =  6;
	
	/**
	 * Atributo numérico.
	 */
	private static final int RES_FIRMA_1000 =  1000;
	
	/**
	 * Horario de inicio de la renovación.
	 */
	private static final String HORARIO_INICIO = StringUtil.eliminaUnaVez(
			TablaValores.getValor(TABLA_MULTILINEA, "inihorariorenovacion", "desc"), ":");
	
	/**
	 * Horario de fin de la renovación.
	 */
	private static final String HORARIO_FIN = StringUtil.eliminaUnaVez(
			TablaValores.getValor(TABLA_MULTILINEA, "finhorariorenovacion", "desc"), ":");
	
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
     * Formato date a ocupar.
     */
	private final SimpleDateFormat ddMMyyyyHHmmssForm  = new SimpleDateFormat("ddMMyyyy HH:mm:ss");

	/**
     * Formato date a ocupar.
     */
	private final SimpleDateFormat timestampForm = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	
    /**    
	 * Referencia al componente que maneja la segunda clave.
	 */
	private SegundaClaveUIInput segundaClaveAplicativo;
	
	/**
	 * EJB Multilinea.
	 */
	private Multilinea multilineaBean = null;

	/**
	 * Operación seleccionada.
	 */
	private OperacionCreditoSuperAmp operacionSeleccionada;

	/**
	 * Operación seleccionada.
	 */
	private ResultCalculoValoresCancelacion operacionSeleccionadaCancelacion;

	/**
	 * TO para el manejo de datos de renovación.
	 */
	private DatosParaRenovacionTO datosRenovacion = null;

	/**
	 * Condicion de garantía.
	 */
	private int paso = 1;

	/**
	 * Total requieren dps.
	 */
	private int requierenDPS;

	/**
	 * Monto disponible.
	 */
	private double montoDisponible;

	/**
	 * Atributo para identificar si se tiene multilinea vigente.
	 */
	private boolean tieneMultilineaVigente;

	/**
	 * Atributo para indentificar si el cliente tiene multilinea.
	 */
	private boolean tieneMultilinea;

    /**
     * Atributo para registrar si el cliente posee o no mandato.
     */
    private boolean tieneMandato;

    /**
	 * Banca envio email ejecutivo. 
	 */
	private boolean bancaEnvioEmailEjecutivo;

	/**
	 * Banca permitida empresario email.
	 */
	private boolean bancaPermitidasEmpresarioEmail;

	/**
	 * Atributo para identificar si tiene monto multilinea.
	 */
	private boolean tieneMultilineaMonto;
	
	/**
     * Atributo para identificar si se debe enviar el correo a backoffice.
     */
    private boolean enviaCorreoBackOffice;
	
	/**
	 * Total cuotas.
	 */
	private int totalCuotas;
	
    /**
     * Hora definida.
     */
	private String hhmmss = " 23:59:59";

	/**
	 * Atributo que sirve para identificar si es necesario cargar los métodos
	 * iniciales.
	 */
	private boolean noCargaMetodosInicio;

	/**
	 * Cuota seleccionada.
	 */
	private int cuotaSeleccionada;

	/**
	 * Tipo de crédito seleccionado.
	 */
	private String tipoDeCreditoSeleccionado; 
	
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
	 * Monto final del crédito.
	 */
	private double montoFinalCredito;

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
	 * Intereses.
	 */
	private double intereses;
	
	/**
     * Variable para setear el error traducido.
     */
    private String mensajeError;

    /**
     * Variable para identificar si es un error general.
     */
    private boolean esErrorGenerico;
    
    /**
     * Valor Gastos Notariales.
     */
    private double gastosNotariales;    

	/**
	 * HashMap con listado de feriados.
	 */
	private HashMap listaFeriados = null;

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
	 * Cai operación.
	 */
	private String caiOperacion;

	/**
	 * Iic operación.
	 */
	private int iicOperacion;
	
	/**
	 * Condición de garantía.
	 */
	private String condicionGarantia;

	/**
	 * Fecha de primer vencimiento.
	 */
	private String fechaMaximaPrimerVencimiento;

	/**
	 * Fecha de vencimiento para MLT.
	 */
	private String fechaVencimientoMLT;

	/**
	 * Oferta Crediticia.
	 */
	private String ofertaCrediticia;

	/**
	 * Estilo de oferta crediticia.
	 */
	private String estiloOfertaCrediticia;
	
	/**
	 * Tope de días para primer vencimiento.
	 */
	private String diasTopePrimerVencimiento;

	/**
	 * Días de inicio para primer vencimiento.
	 */
	private String diasIniPrimerVencimiento;

	/**
	 * Cuota inicial.
	 */
	private int cuotaInicial;

	/**
	 * Cuota final.
	 */
	private int cuotaFinal;

	/**
	 * Seguros seleccionados.
	 */
	private boolean seguroSeleccionado;

	/**
	 * Monto total de seguros seleccionados.
	 */
	private double montoTotalSegurosSeleccionados;
	
	/**
	 * Indica si se aceptan las condiciones.
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
	 * Glosa para el tipo de crédito seleccionado.
	 */
	private String glosaTipoCredito;

	/**
	 * Calendario de pago.
	 */
	private CalendarioPago[] calendarioPago;

	/**
	 * Calendario de pago de salida.
	 */
	private CalendarioPago[] calendarioPagoSalida;

	/**
	 * Operación de crédito.
	 */
	private ResultConsultaOperacionCredito operacionCredito;

	/**
	 * Resultado de la firma.
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
     * Atributo para identificar la moneda.
     */
    private String codMoneda;
    
    /**
     * Atributo para identificar el tipo de plazo.
     */
    private String tipoDePlazo;

    /**
     * Disclaimer para mandato.
     */
    private String disclaimerMandato;
    
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
	 * Atributo para inyectar MB ServicioAutorizacionFirmaModelMB.
	 */
	@ManagedProperty(value = "#{servicioAutorizacionyFirmaModelMB}")
	private ServicioAutorizacionyFirmaModelMB servicioAutorizacionyFirmaModelMB;

	/**
	 * <p>Método que obtiene las operaciones a renovar.
	 * 
	 * </p>  Registro de Versiones:<ul>
	 * <li> 1.0 26/12/2014 Eduardo Pérez (BEE): versión inicial.</li>
	 * <li> 1.1 09/03/2016 Eduardo Pérez (BEE) - Felipe Ojeda (ing.Soft.BCI): Se modifica documentación y se agrega 
	 *                                              seteo a atributo razonSocial.</li>
     * <li> 1.2 11/02/2016 Eduardo Pérez (BEE) - Felipe Ojeda (ing.Soft.BCI) : Se agrega validación y verificacion de tenencia de mandato.
	 * </ul>
	 *
     * @throws EJBException Exception de EJB.
     * @throws ParseException Exception de parseo de variables.
     * @throws MultilineaException Exception de parseo de variables.
     * @throws GeneralException general.
	 */
	private void obtenerOperaciones() throws EJBException, ParseException, MultilineaException, GeneralException{

		if(paso!=1) return;
		if(datosRenovacion != null &&  datosRenovacion.getOperaciones()!=null) return;
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[obtenerOperaciones]["+clienteMB.getRut()+"][BCI_INI]");
		}

        boolean revisionMandato = Boolean.parseBoolean(TablaValores.getValor(TABLA_MULTILINEA
                    , "revisionMandato", "valor"));
        mostrarCondicionesMandato = Boolean.parseBoolean(TablaValores.getValor(TABLA_MULTILINEA
                , "activaCondicionesMandato", "valor"));
        if (getLogger().isInfoEnabled()) {
            getLogger().info("[obtenerOperaciones]habilitada revisionMandato: "
                    + revisionMandato);
        }
        if (getLogger().isInfoEnabled()) {
            getLogger().info("[obtenerOperaciones][" + clienteMB.getRut() + "-" + clienteMB.getDigitoVerif()+ "] habilitada activaCondicionesMandato: "
                    + mostrarCondicionesMandato);
        }
        if (revisionMandato){
            try {
                tieneMandato = clienteMB.poseeMandatoMulticanalAutorizado();
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("[obtenerOperaciones] tieneMandato: "
                            + tieneMandato);
                }
            }
            catch (Exception e){
                getLogger().debug("[obtenerOperaciones] Exception mandato: "
                        + ErroresUtil.extraeStackTrace(e));
                tieneMandato = false;
                ofertaCrediticia = INACTIVA;
            }
        }
        else{
            tieneMandato = true;
        }
        razonSocial = clienteMB.getRazonSocial();
		int numCliente = (int) clienteMB.getRut();
		char vrfCliente = clienteMB.getDigitoVerif();
		MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
				sesion.getCanalId(),USUARIO);

		try {

			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[obtenerOperaciones][invocando Multilinea.obtenerOperaciones]");
			}

			datosRenovacion = crearEJBmultilinea().iniciarRenovacion(multiEnvironment,numCliente, vrfCliente);
		} 
		catch (Exception e) {
			if (getLogger().isEnabledFor(Level.ERROR)) {
				getLogger().error("[obtenerOperaciones]["+clienteMB.getRut()+"][BCI_FINEX][Exception]"
						+" error con mensaje: " + e.getMessage(), e);
			}
			throw new MultilineaException("UNKNOW", e.getMessage());
		} 

		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[obtenerOperaciones]["+clienteMB.getRut()+"][BCI_FINOK]");
		}

	}
	
	/**
	 * <p>Método que obtiene la información de cancelación para la operación
	 * seleccionada.
	 * 
	 * </p>  Registro de Versiones:<ul>
	 * <li> 1.0 26/12/2014 Eduardo Pérez (BEE): versión inicial.</li>
	 * <li> 1.1 22/06/2016 Eduardo Pérez (BEE) - Pablo Paredes (ing.Soft.BCI) : Se agrega seteo para atributo esErrorGenerico.</li>
	 * <li> 1.2 17/08/2016 Eduardo Pérez (BEE) - Felipe Ojeda (ing.Soft.BCI) : Se agrega monto para correo.</li>
	 * </ul>
	 *
	 * @throws EJBException Exception de EJB.
	 * @throws RemoteException Exception de parseo de variables.
	 * @throws GeneralException  GeneralException.
	 */
	public void obtenerValoresCancelacion() throws EJBException, RemoteException, GeneralException{
		
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[obtenerValoresCancelacion]["+clienteMB.getRut()+"][BCI_INI]");
		}
		montoCorreo = 0;
		operacionSeleccionadaCancelacion = null;
		if(operacionSeleccionada == null) return;
		MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
				sesion.getCanalId(),USUARIO);

		try {
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[obtenerValoresCancelacion][invocando consultaCancelacionMultilinea]");
				getLogger().debug("[obtenerValoresCancelacion][getIdOperacion()]" 
				+ operacionSeleccionada.getIdOperacion());
				getLogger().debug("[obtenerValoresCancelacion][getNumOperacion()]" 
				+ operacionSeleccionada.getNumOperacion());
				getLogger().debug("[obtenerValoresCancelacion][getCodMonedaCred()]" 
				+ operacionSeleccionada.getCodMonedaCred());
				getLogger().debug("[obtenerValoresCancelacion][getOficinaIngreso()]" 
				+ operacionSeleccionada.getOficinaIngreso());
			}

			Hashtable datosLog = new Hashtable();
			operacionSeleccionadaCancelacion = (ResultCalculoValoresCancelacion) 
					crearEJBmultilinea().consultaCancelacionMultilinea(multiEnvironment,
					operacionSeleccionada.getIdOperacion(),
					operacionSeleccionada.getNumOperacion(),
					operacionSeleccionada.getCodMonedaCred(),
					operacionSeleccionada.getOficinaIngreso(),
					datosLog);
			
			if (operacionSeleccionadaCancelacion != null){
				montoCorreo = operacionSeleccionadaCancelacion.getValorRenovado();
			}
			
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[obtenerValoresCancelacion][OK  Multilinea.consultaCancelacionMultilinea]");
			}

		} 
		catch (MultilineaException e) {
			if (getLogger().isEnabledFor(Level.ERROR)) {
				getLogger().error("[obtenerValoresCancelacion]["+ operacionSeleccionada.getIdOperacion() 
						+ operacionSeleccionada.getNumOperacion()+"][BCI_FINEX][Exception]"
						+" error con mensaje: " + e.getMessage(), e);
			}
			esErrorGenerico = true;
			paso = -1;

		}
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[obtenerValoresCancelacion]["+clienteMB.getRut()+"][BCI_FINOK]");
		}

	}

	/**
	 * <p>Método de tipo prerender que carga los datos iniciales para la renovacion.
	 * 
	 * </p>  Registro de Versiones:<ul>
	 * <li> 1.0 25/12/2014 Eduardo Pérez (BEE): versión inicial.</li>
	 *<li> 1.1 22/06/2016 Eduardo Pérez (BEE) - Pablo Paredes (ing.Soft.BCI) : Se agrega seteo para atributo esErrorGenerico.</li>
	 * </ul>
	 *
	 * @throws MultilineaException Exception de Precios
	 * @throws EJBException Exception EJB
	 * @throws ParseException Exception de parseo de variables
	 */
	public void cargarDatosBasicos() throws MultilineaException, EJBException, ParseException {

		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[cargarDatosBasicos]["+clienteMB.getRut()+"][BCI_INI]");
		}

		try {
			clienteMB.setApellidoPaterno(null);
			clienteMB.setDatosBasicos();
			obtenerOperaciones();
			setCuentasCorrientesYPrimas(productosMB.getCuentasCorrientesYPrimas()); 
		}
		catch (Exception e) {
			if (getLogger().isEnabledFor(Level.ERROR)) {
				getLogger().error("[cargarDatosBasicos]["+clienteMB.getRut()+"][BCI_FINEX][Exception]"
						+" error con mensaje: " + e.getMessage(), e);
			}
			esErrorGenerico = true;
			paso = -1;
		}

		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[cargarDatosBasicos]["+clienteMB.getRut()+"][BCI_FINOK]");
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
	 * Obtiene el detalle de las tablas llamando al servicio de tablas de IBM.
	 * <p>
	 * Registro de versiones:
	 * <ul>
	 *   <li>1.0 15/05/2015 Manuel Escárate R. (BEE): Version Inicial</li>
	 *   <li> 1.1 22/06/2016 Manuel Escárate (BEE) - Pablo Paredes (ing.Soft.BCI) : Se agrega seteo para atributo esErrorGenerico.</li>
	 * </ul>
	 * </p>
	 * @param multiambiente objeto para seteo de ambiente.
	 * @param tabla tabla que será consultado por el servicio.
	 * @param codigoTabla código en la tabla a consultar.
	 * @param codigo código asociado a la consulta.
	 * @return HashMap detalle de la tabla.
	 * @since 1.0
	 */ 
	private HashMap obtieneDetalleTabla(MultiEnvironment multiambiente, String tabla, 
    		String codigoTabla, String codigo) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[obtieneDetalleTabla] Obteniendo valores de tablas: " 
                  + tabla + ", " + codigoTabla + ", " + codigo);
        }
        HashMap viewTab = new HashMap();
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
            esErrorGenerico = true;
            paso = -1;
        } 
        catch (RemoteException e) {
            if (getLogger().isEnabledFor(Level.ERROR)) { 
                getLogger().error("[obtieneDetalleTabla][" + clienteMB.getRut() 
                        +"][BCI_FINEX][RemoteException]" +" error con mensaje: " + e.getMessage(), e);
            }
            esErrorGenerico = true;
            paso = -1;
        }
        catch (EJBException e) {
            if (getLogger().isEnabledFor(Level.ERROR)) { 
                getLogger().error("[obtieneDetalleTabla][" + clienteMB.getRut() 
                        +"][BCI_FINEX][EJBException]" +" error con mensaje: " + e.getMessage(), e);
            }
            esErrorGenerico = true;
            paso = -1;
        } 
        catch (TableAccessException e) {
            if (getLogger().isEnabledFor(Level.ERROR)) { 
                getLogger().error("[obtieneDetalleTabla][" + clienteMB.getRut() 
                        +"][BCI_FINEX][TableAccessException]" +" error con mensaje: " + e.getMessage(), e);
            }
            esErrorGenerico = true;
            paso = -1;
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[obtieneDetalleTabla] Valores recuperados: " + viewTab);
        }        
        return viewTab; 
    }
	
	/**
     * <p> Método que genera el comprobante. </p>
     * 
     * Registro de versiones:
     * <ul>
     *     <li>1.0 25/05/2015 Manuel Escárate (BEE) : version inicial.</li>
     *     <li>1.1 18/05/2016 Manuel Escárate (BEE) - Pablo Paredes (ing.Soft.BCI) : Se deja deja en blanco la cuenta 
     *                          en caso de no obtenerla y se cambia nombre de archivo de salida. </li>
     *     <li>1.2 18/08/2016 Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI) : Se cambia nombre de archivo PDF.</li>
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
          
        String montoCred = formatearMonto(montoFinalCredito,0,"#,###");
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
                TABLA_MULTILINEA, "crearPDF","comprobanteRenovacionMultilinea");

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
                response.setHeader("Content-disposition","attachment;filename=comprobanteRenovacionCreditoComercial.pdf");
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
                    + "[[BCI_FINEX]]::" + ex.getMessage(), ex);
            }
        }
        if(getLogger().isEnabledFor(Level.INFO)){
             getLogger().debug("[generarComprobanteCargaPDF]["+usuarioModelMB.getRut()+"][BCI_FINOK]");
        }
    }
    
    
    /**
     * <p> Metodo para reemplazar los caracteres especiales en la creacion del pdf.</p>
     *
     * Registro de versiones:
     * <ul>
     *     <li>1.0 25/05/2015 Manuel Escárate R. (BEE): version inicial.</li>
     * </ul>
     * @param texto a reemplazar caracteres.
     * @return String texto con los caracteres cambiados.
     * @since 1.0
     */
    public String reemplazaCaractereEspeciales(String texto){
        if(getLogger().isDebugEnabled()){
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
        
        if(getLogger().isDebugEnabled()){
            getLogger().debug("[reemplazaCaractereEspeciales][" + usuarioModelMB.getRut() + "][BCI_FINOK]");
        }
        return texto;
    }
    
	/**
	 * retorna una instancia del EJB Multilinea.
	 * <P>
	 * Registro de versiones:
	 * <ul>
	 * <li> 1.0  09/12/2014 Manuel Escárate (BEE): Versión inicial.</li>
	 * </ul>
	 * <p>
	 * @return Multilinea instancia del ejb Multilinea.
	 * @throws GeneralException en caso de error.
	 * @since 1.0
	 */
	private Multilinea crearEJBmultilinea() throws GeneralException {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[crearEJBmultilinea]["+clienteMB.getRut()+"][BCI_INI]");
		}
		if(multilineaBean!=null) return multilineaBean;
		try {
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
				getLogger().debug("[crearEJBmultilinea]["+clienteMB.getRut()+"][BCI_FINOK]");
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
	 * Obtiene servicios.
	 * <P>
	 * Registro de versiones:
	 * <ul>
	 * <li> 1.0  09/12/2014 Manuel Escárate (BEE): Versión inicial.</li>
	 * </ul>
	 * <p>
	 * @return ServiciosMiscelaneos instancia del ejb ServiciosMiscelaneos.
	 * @throws Exception en caso de error.
	 * @since 1.0
	 */
	private ServiciosMiscelaneos getServiciosMiscelaneos() throws Exception {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[getServiciosMiscelaneos] Inicio.");
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
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[getServiciosMiscelaneos] Fin del método.");
        }
        return serviciosMiscelaneos;
    }

	/**
	 * Método para obtener la fecha máxima de primer vencimiento.
	 * <br>
	 * Registro de versiones:<ul>
	 * <li>1.0 10/03/2015 Manuel Escárate (BEE S.A.): versión inicial.</li>
	 * </ul>
	 * @param fecha fecha.
	 * @param dias dias.
	 * @return Date
	 * @since 1.0
	 */  
	public static Date obtenerFechaMaximaVencimiento(Date fecha, int dias){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fecha); // Configuramos la fecha que se recibe
		calendar.add(Calendar.DAY_OF_YEAR, dias);  // numero de días a añadir, o restar en caso de días<0
		return calendar.getTime(); // Devuelve el objeto Date con los nuevos días añadidos
	}

	/**
	 * Método que una vez seleccionada una operación para renovar, realiza las 
	 * validaciones pertinentes para destinar a la página
	 * de inicio de la renovación  o hacia una página de error en caso que no cumpla
	 * con ciertas condiciones.
	 * <br>
	 * Registro de versiones:<ul>
	 * <li>1.0 10/03/2015 Eduard Pérez (BEE S.A.): versión inicial.</li>
     * <li>1.1 11/02/2016 Eduardo Perez (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se agrega inicización de atributos para correo y backoffice, además
     *                                               se agregan excepciones para los correos.
     * <li>1.2 11/02/2016 Eduardo Perez (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se agrega validación y verificacion de tenencia de mandato.</li>
     * <li>1.3 18/05/2016 Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI) : Se quita flag en false para enviaCorreoBackOffice</li>
	 * <li> 1.4 22/06/2016 Manuel Escárate (BEE) - Pablo Paredes (ing.Soft.BCI) : Se agrega seteo para atributo esErrorGenerico.</li>
	 * </ul>
	 * @throws Exception controlado.
	 * @since 1.0
	 */  
	public void iniciarSimulacion() throws Exception{
		if(paso != 1 || operacionSeleccionada ==null) return;
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[iniciarSimulacion]["+clienteMB.getRut()+"][BCI_INI]");
		}
		tipoDeCreditoSeleccionado = "";
		caiOperacion = operacionSeleccionada.getIdOperacion();
		iicOperacion = operacionSeleccionada.getNumOperacion();
		codigoRegion = "1";
		long rutCliente = clienteMB.getRut();
		char dvCliente = clienteMB.getDigitoVerif();
		codBanca        = null;
		plan            = ' ';
		oficinaIngreso  = null;
		codigoEjecutivo = null;
		codSegmento     = null;
		codTelefonoCliente = "";
    	telefonoCliente = "";
    	codCelularCliente = "";
    	celularCliente = "";
        emailCliente = "";
    	regionCliente = "";
    	comunaCliente = "";
		DatosParaAvanceTO datosCliente = null;
		MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
				sesion.getCanalId(), USUARIO);
		datosPlantilla = null;
		if (getLogger().isDebugEnabled()) {
            getLogger().debug("[iniciarSimulacion][obtieneDetalleTabla para feriados]");
		}
		listaFeriados = obtieneDetalleTabla(multiEnvironment, "GNS", "FER", null);

		try {
			datosCliente = crearEJBmultilinea().iniciarAvance(multiEnvironment,rutCliente,dvCliente);
			if (datosCliente != null) {
				
				if (datosCliente.getExcepciones() != null){
    				excepciones  = new ArrayList<String>(Arrays.asList(datosCliente.getExcepciones()));
    			}
				
                razonSocial = datosCliente.getRazonSocial();
                nombreFantasia = datosCliente.getNombreFantasia();

				if (datosCliente.isTieneMultilinea()){
					codBanca        = datosCliente.getCodBanca();
					plan            = datosCliente.getPlan();
					oficinaIngreso  = datosCliente.getOficinaIngreso();
					codigoEjecutivo = datosCliente.getCodigoEjecutivo();
					codSegmento     = datosCliente.getCodSegmento();
					montoDisponible = datosCliente.getMontoDisponible();
					bancaEnvioEmailEjecutivo = verificaPertenenciaBanca(codBanca, 
							TablaValores.getValor(TABLA_MULTILINEA, "bancaEnvioEmailEjecutivo", "desc"));
					if (getLogger().isDebugEnabled()){
						getLogger().debug("Multinea - bancaEnvioEmailEjecutivo      :[" 
								+ bancaEnvioEmailEjecutivo + "]");
					}

					bancaPermitidasEmpresarioEmail = verificaPertenenciaBanca(codBanca, 
							TablaValores.getValor(TABLA_MULTILINEA, "bancapermitidasEmpresarioEmail", "desc"));
					if (getLogger().isDebugEnabled()){
						getLogger().debug("Multinea - bancapermitidasEmpresarioEmail:[" 
								+ bancaPermitidasEmpresarioEmail + "]");
					}
					
					if (datosCliente.getOfertaCrediticia() == CURSE_ACT_MTL){
						ofertaCrediticia = ACTIVA;  
						estiloOfertaCrediticia = OFERTA_VERDE; 
					}
					else if (datosCliente.getOfertaCrediticia() == SIM_CUR_CLI) {
						ofertaCrediticia = INACTIVA; 
						estiloOfertaCrediticia = OFERTA_AMARILLA; 
						enviaCorreoBackOffice = true;
					}
					else if (datosCliente.getOfertaCrediticia() == SIM_EJE_COM) {
						ofertaCrediticia = INACTIVA; 
						estiloOfertaCrediticia = OFERTA_NARANJA; 
					} 
					
					if (getLogger().isDebugEnabled()) {
    					getLogger().debug("[iniciarAvance] envia correoBackOffice [ "+ enviaCorreoBackOffice  + "]");
    				}
                    if(!tieneMandato) {
                        ofertaCrediticia = INACTIVA;
                        estiloOfertaCrediticia =OFERTA_NARANJA;
                    }

					
					if (montoDisponible > 0){
						tieneMultilineaMonto = true;
					}
					fechaVencimientoMLT = datosCliente.getFechaVencimientoMLT();
					if (getLogger().isDebugEnabled()) {
						getLogger().debug("[iniciarAvance ]codBanca =<" 
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
				}
				else {
					esErrorGenerico = true;
					paso = -1;
				}
			}
			paso = PASO2;
		}
		catch (Exception ex){
			if(getLogger().isEnabledFor(Level.ERROR)){
				getLogger().error("[iniciarSimulacion] ["+rutCliente+"] [[BCI_FINEX]] [datosBasicosGenerales] "
						+ "mensaje=< "+ ex.getMessage() +">", ex);
			}
			esErrorGenerico = true;
			paso = -1;
		}

		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[iniciarSimulacion]["+clienteMB.getRut()+"][BCI_FINOK]");
		}
	}


	/**
	 * Rescata valores de la primera pantalla para la renovación.
	 * luego ejecuta el método avanceMultilinea para obtener los valores
	 * para el paso 2.
	 * <br>
	 * Registro de versiones:<ul>
	 * <li>1.0 25/03/2015 Eduardo Pérez (BEE S.A.): versión inicial.</li>
	 * <li>1.1 11/02/2016 Eduardo Perez (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se agregan excepciones para los correos.</li>
	 * <li>1.2 18/05/2016 Manuel Escárate R. (BEE S.A.) - Pablo Paredes (ing.Soft.BCI) : Se agregan los atributos codMoneda y tipoDePlazo.</li>
	 * <li>1.3 22/06/2016 Eduardo Pérez (BEE) - Pablo Paredes (ing.Soft.BCI) : Se agrega seteo para atributo esErrorGenerico.</li>
	 * <li>1.4 18/08/2016 Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI) : Se agrega seteo para atributo gastoNotarial.</li>* 
	 * </ul>
     * @throws ParseException de formato.
     * @throws GeneralException general.
     * @throws RemoteException de servicio.
     * @throws EJBException de ejb.
     * @throws MultilineaException multilinea.
	 * @since 1.0
	 */  
	public void mostrarCondicionesCredito() throws ParseException,
	MultilineaException, EJBException, RemoteException, GeneralException{
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[mostrarCondicionesCredito]["+clienteMB.getRut()+"][BCI_INI]");
		}
		if(getLogger().isDebugEnabled()){
			getLogger().debug("[mostrarCondicionesCredito]  <=tipoDeCreditoSeleccionado]"
					+tipoDeCreditoSeleccionado);
		}
		String key = StringUtils.rightPad(operacionSeleccionada.getCodMonedaCred(), LARGO_CODIGO_MONEDA)  
				+ operacionSeleccionada.getCodigo10();
	    String moneda = StringUtils.rightPad(TablaValores.getValor(TABLA_MULTILINEA,
				tipoDeCreditoSeleccionado.trim(),"moneda"), LARGO_CODIGO_MONEDA);
	    codMoneda = moneda;
	    tipoDePlazo = TablaValores.getValor(TABLA_MULTILINEA, "tipoDePlazo", "desc");
		int topeMora = Integer.parseInt(TablaValores.getValor(TABLA_RENMULTILINEA, key, "topeMora"));
		String tipoOperacion = tipoDeCreditoSeleccionado.substring(VALOR0, VALOR3);
		String codigoAuxiliar = tipoDeCreditoSeleccionado.substring(VALOR3, VALOR6);
		int rutDeudor = (int) clienteMB.getRut();
		char digitoVerificador = clienteMB.getDigitoVerif();
		double montoCredito = 0;
		if (operacionSeleccionadaCancelacion !=null) {
			  montoCredito= operacionSeleccionadaCancelacion.getValorRenovado();
		}
		int cuentaAbono = Integer.parseInt((String) productosMB.getCuentasCorrientesYPrimas()[0].getValue());
		int cuentaCargo = Integer.parseInt((String)productosMB.getCuentasCorrientesYPrimas()[0].getValue());
		String[] segurosSeleccionados = null;
		String fechaPrimerVcto = null;
		fechaPrimerVcto = fechaVencimientoSeleccionada.substring(VALOR0,VALOR2) 
				+ fechaVencimientoSeleccionada.substring(VALOR3,VALOR5)
				+ fechaVencimientoSeleccionada.substring(VALOR6,VALOR10);
		noCargaMetodosInicio = true;
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("canal+"+sesion.getCanalId()+"+");
		}
		MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
				sesion.getCanalId(), USUARIO);
		multiEnvironment.setCartera("");
		if (codigoAuxiliar.equals("010")){
			cuotaSeleccionada = 1;
		}

		EstructuraVencimiento[] vencimientos = new EstructuraVencimiento[1];
		vencimientos[0] = new EstructuraVencimiento(0, ' ', 0, cuotaSeleccionada, ddMMyyyyHHmmssForm.parse(
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

		DatosResultadoOperacionesTO datosCondiciones = null;
		DatosParaCondicionesCreditoTO datosConCred = new DatosParaCondicionesCreditoTO();
		try{
			datosConCred.setIdRequerimiento("096"); //Simulación
			datosConCred.setCaiNumOpe(caiOperacion);
			datosConCred.setIicNumOpe(iicOperacion);
			datosConCred.setOficinaIngreso(oficinaIngreso);
			datosConCred.setEjecutivo("");
			datosConCred.setMontoCredito(montoCredito);
			datosConCred.setTipoOperacion(tipoOperacion);
			datosConCred.setMoneda(moneda);
			datosConCred.setCodigoAuxiliar(codigoAuxiliar);
			datosConCred.setCtaAbono(cuentaAbono);
			datosConCred.setCtaCargo(cuentaCargo);
			datosConCred.setCodBanca(codBanca);			
			datosConCred.setPlan(plan);
			datosConCred.setRutDeudor(rutDeudor);
			datosConCred.setDigitoVerificador(digitoVerificador);
			datosConCred.setFecVencimiento2(operacionSeleccionada.getFecVencimiento2());
			datosConCred.setWhoVisa(-1); //Simulación
			if(operacionSeleccionadaCancelacion != null){
				datosConCred.setTotalPagado(operacionSeleccionadaCancelacion.getTotalPagado());
			}
			datosConCred.setCodSegmento(codSegmento);
			datosConCred.setSeguros(segurosSeleccionados);
			datosConCred.setCodMonedaOrig(operacionSeleccionada.getCodMonedaCred());
			datosConCred.setTopeMora(topeMora);
			datosConCred.setFechaPrimerVcto(fechaPrimerVcto);
			datosConCred.setConvenio(clienteMB.getNumeroConvenio());
			datosConCred.setRutUsuario(String.valueOf(usuarioModelMB.getRut()));
			datosConCred.setDigitoVerificadorUsuario(String.valueOf(usuarioModelMB.getDigitoVerif()).charAt(0));
			 
			datosCondiciones =  crearEJBmultilinea().obtenerCondicionesRenovacion(
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
				String fechaCurseDatos = FechasUtil.convierteDateAString(datosCondiciones.getFechaCurse(),
						"yyyyMMdd"); 
				fechaCurse = fechaCurseDatos;
				calendarioPagoSalida = datosCondiciones.getCalendario();
				requierenDPS = datosCondiciones.getRequiereDPS();
				montoFinalCredito = operacionSeleccionadaCancelacion.getValorRenovado();
				if (getLogger().isDebugEnabled()) {
					getLogger().debug("[mostrarCondicionesCredito ]valorCuota =<" 
							+ valorCuota + ">,tasaInteresInternet=<"
							+ tasaInteresInternet + ">, impuestos=<"
							+ factorCae + ">, factorCae=<"
							+ impuestos + ">,costoFinalCredito=<" 
							+ costoFinalCredito + ">,montoFinalCredito=<"+ montoFinalCredito +">");
				}
	            String codEvento = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "codEventoSimulaOK");
	            String codSubEvento = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "codSubEventoSimulaOK");
	            String producto = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "productoSimulaOK");
	            journalizar(codEvento,codSubEvento,producto);
				
			}
			paso = PASO3;
		}
		catch (Exception ex){
			if(getLogger().isEnabledFor(Level.ERROR)){
				getLogger().error("[mostrarCondicionesCredito]" +  "[BCI_FINEX] mensaje=< "+ ex.getMessage() +">", ex);
			}
			String codEvento = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "codEventoSimulaNOOK");
			String codSubEvento = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "codSubEventoSimulaNOOK");
			String producto = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "productoSimulaNOOK");
			journalizar(codEvento,codSubEvento,producto);
			esErrorGenerico = true;
			paso = -1;
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[mostrarCondicionesCredito]["+clienteMB.getRut()+"][BCI_FINOK]");
		}
	}

	/**
	 * Método que modifica las condiciones según tipo de moneda.
	 * <br>
	 * Registro de versiones:<ul>
	 * <li>1.0 09/12/2014 Manuel Escárate (BEE S.A.): versión inicial.</li>
	 * </ul>
	 * @since 1.0
	 */  
	public void cambiarValoresTipoCredito(){
		String fechaFormateada = "";
		Date fechaActual = new Date();
		String formato = TablaValores.getValor(TABLA_MULTILINEA, "formatoFecha", "formato");
		Date fechaSuma = null;
		if (datosPlantilla != null){ 
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("tipoDeCreditoSeleccionadotipoDeCreditoSeleccionado"+tipoDeCreditoSeleccionado);
			}
			if (tipoDeCreditoSeleccionado != ""){
				cuotas.clear();
				if (getLogger().isDebugEnabled()) {
					getLogger().debug("Multilinea largo Vector "+ datosPlantilla.length);
				}
				String codigoAuxiliarPlantilla = tipoDeCreditoSeleccionado.substring(VALOR3, VALOR6);
				if (getLogger().isDebugEnabled()) {
					getLogger().debug("cambio datos plantilla codigoauxilioar" +codigoAuxiliarPlantilla);
				}
				for (int i = 0; i < datosPlantilla.length; i++) {
					if (getLogger().isDebugEnabled()) {
						getLogger().debug("cambio datos arrelgo " + datosPlantilla[i].getCodigoAuxiliar());
					}
					if (codigoAuxiliarPlantilla.equals(datosPlantilla[i].getCodigoAuxiliar())){
						if (getLogger().isDebugEnabled()) {
							getLogger().debug("datosPlantilla[i].getCuotaInicial()"+datosPlantilla[i].getCuotaInicial());
							getLogger().debug("datosPlantilla[i].getCuotaFinal()"+datosPlantilla[i].getCuotaFinal());
						}
						cuotaInicial = datosPlantilla[i].getCuotaInicial();
						cuotaFinal =  datosPlantilla[i].getCuotaFinal();
						if (getLogger().isDebugEnabled()) {
							getLogger().debug("MONTO MINIMO" + datosPlantilla[i].getCreditoMinimo());
							getLogger().debug("MONTO MAXIMO" + datosPlantilla[i].getCreditoMaximo());
						}
						
						fechaSuma = obtenerFechaMaximaVencimiento(
								fechaActual,datosPlantilla[i].getMaximoPrimerPago());
						fechaFormateada = FechasUtil.convierteDateAString(fechaSuma, formato);
						diasTopePrimerVencimiento = String.valueOf(datosPlantilla[i].getMaximoPrimerPago());
						diasIniPrimerVencimiento = String.valueOf(datosPlantilla[i].getMinimoPrimerPago());
					}
				} 
				for (int i = cuotaInicial; i <= cuotaFinal; i++) {
					if (getLogger().isDebugEnabled()) {
						getLogger().debug("cuota "+ i + " de" + cuotaFinal);
					}
					String cuota = String.valueOf(i);
					cuotas.add(new SelectItem(i,cuota));
				} 
			}
			else {
				cuotas.clear();
				fechaFormateada = "";  
				diasIniPrimerVencimiento = "0";
				diasTopePrimerVencimiento = "0";
			} 
		} 
		fechaMaximaPrimerVencimiento =fechaFormateada;
		noCargaMetodosInicio = true;
	}
	
	/**
	 * Realiza validaciones previas al curse.
	 * <br>
	 * Registro de versiones:<ul>
	 * <li>1.0 09/12/2014 Eduardo Pérez (BEE S.A.): versión inicial.</li>
	 * <li>1.1 11/02/2016 Eduardo Perez (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se agregan excepciones para los correos.
	 * <li>1.2 22/02/2016 Eduardo Pérez (BEE) - Felipe Ojeda (ing.Soft.BCI) : Se agrega validación y verificacion de tenencia de mandato.
	 * <li>1.3 22/06/2016, Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): Se agrega condición con tipo de usuario y perfil.</li>
	 * <li>1.4 18/08/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega lógica de discleimer.</li>
	 * </ul>
	 * @throws Exception en caso de error. 
	 * @since 1.0
	 */  
	public void continuarRenovacion() throws Exception{
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[continuarRenovacion]["+clienteMB.getRut()+"][BCI_INI]");
		}
		if (getLogger().isInfoEnabled()) {
			getLogger().info( "[continuarRenovacion]" 
					+ "Datos Usuario perfil<" + usuarioModelMB.getPerfil()+ "> ,"
					+ "tipoUsuario<" + usuarioModelMB.getTipoUsuario() + ">.");
		}
		if (usuarioModelMB.getTipoUsuario().trim().equalsIgnoreCase(TIPO_USUARIO) && usuarioModelMB.getPerfil().trim().equalsIgnoreCase(PERFIL_USUARIO)){
			try{
				setCuentasCorrientesYPrimas(productosMB.getCuentasCorrientesYPrimas());
				if (getCuentasCorrientesYPrimas()!=null){
					if (getLogger().isDebugEnabled()) {
						getLogger().debug("[continuarRenovacion] cuentas: "
								+ getCuentasCorrientesYPrimas().length);
						for (int i = 0; i< getCuentasCorrientesYPrimas().length; i++){
							getLogger().debug("[continuarRenovacion] cuentas: "
									+ getCuentasCorrientesYPrimas()[i].getLabel()
									+ "    " + getCuentasCorrientesYPrimas()[i]
											.getValue());
						}
					}   
				}
				if (excepciones != null && excepciones.size() > 0){
					if (getLogger().isDebugEnabled()) {
						getLogger().debug("[continuarRenovacion][tengo excepciones]");
					}
					paso = PASO5;
					this.cargarRegiones();

				} 
				else {

					if (ofertaCrediticia.equals(ACTIVA) && tieneMandato){
						
	                    ResultConsultaOperacionCredito operacionDeCredito = new ResultConsultaOperacionCredito();
						try {
							MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
									sesion.getCanalId(),USUARIO);
							operacionDeCredito = crearEJBmultilinea().consultaOperacionCredito(multiEnvironment, operacionSeleccionada.getIdOperacion(), operacionSeleccionada.getNumOperacion());
						}
						catch (Exception e) {
			    			if (getLogger().isEnabledFor(Level.ERROR)) {
			    				getLogger().error("[continuarRenovacion][" + clienteMB.getRut() + "][BCI_FINEX] Error al consultar operacion de credito: " + e);
			    			}
			    			throw new Exception("Error al obtener operacion de credito");
						}
						

						if (operacionDeCredito != null && operacionDeCredito.getCondicionGar() != null
								&& !operacionDeCredito.getCondicionGar().trim().equals(GARANTIA_AVALES)){ 
							mostrarCondicionesMandato = false;
						}
						else{ 
							DatosDisclaimerTO datos = new DatosDisclaimerTO();
							datos.setRutCliente(String.valueOf(usuarioModelMB.getRut() +"-"+ usuarioModelMB.getDigitoVerif()));
							datos.setRutEmpresa(String.valueOf(clienteMB.getRut() +"-"+  clienteMB.getDigitoVerif()));
							datos.setNombreCliente(usuarioModelMB.getNombres() + " " + usuarioModelMB.getApPaterno() + " " + usuarioModelMB.getApMaterno());
							datos.setNombreempresa(clienteMB.getRazonSocial());
							datos.setMonto(formatearMonto(operacionSeleccionadaCancelacion.getValorRenovado(), 0, "#,###"));
							disclaimerMandato = crearEJBmultilinea().obtieneDisclaimerMandato(datos);

						}
						paso = PASO4;
					} 
					else {
						paso = PASO5;	
						this.cargarRegiones(); 
					}
				}
			}
			catch (Exception ex){
				if(getLogger().isEnabledFor(Level.ERROR)){
					getLogger().error("[continuarRenovacion]" 
							+  "mensaje=< "+ ex.getMessage() +">", ex);
				}
				esErrorGenerico = true;
				paso = -1;
			}
		}
		else {
			if(getLogger().isEnabledFor(Level.ERROR)){
				getLogger().error("[continuarRenovacion]" 
						+  "mensaje=< "+ "Usuario no Autorizado para esta operación >");
			}	
			mensajeError =  TablaValores.getValor(TABLA_MULTILINEA,
					"usuarioAutorizado","mensaje"); 
			esErrorGenerico = false;
			paso = -1;
		}

		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[continuarRenovacion]["+clienteMB.getRut()+"][BCI_FIN]");
		}
	}

	/**
	 * Metodo que se encarga de cargar las obtener y cargar las regiones.
	 * 
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 (19/03/2015, Manuel Escárate R. (BEE)): versión inicial.
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
				RegionTO[] regionesArr = regionSupportMB.obtenerRegiones();
				if ((regionesArr != null) && (regionesArr.length > 0)){
					for (int i = 0; i < regionesArr.length; i++) {
						this.regiones.add(new SelectItem(regionesArr[i].getCodigo(),regionesArr[i].getNombre()));
					}
				}
			}
			catch (Exception ex){
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
     * <li>1.0 (19/03/2015, Eduardo Pérez. (BEE)): versión inicial.
     * <li>1.1 11/02/2016 Eduardo Perez (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se agregan atributos para el correo y se maneja backoffice.
     * <li>1.2 18/05/2016 Manuel Escárate R. (BEE S.A.) - Pablo Paredes (ing.Soft.BCI) : Se cambia el atributo enviaBackOffice a true.</li>
     * <li>1.3 18/08/2016 Manuel Escárate R. (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se cambia monto para correo.</li>     
     * </ul>
     * 
     * @throws Exception en caso de error.
     * @since 1.0
     */
	public void enviarSolicitud() throws Exception{
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[enviarSolicitud][BCI_INI]");
		}
		try {
			boolean enviaBackOffice = true;
			MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
					sesion.getCanalId(),USUARIO);
			if (getLogger().isDebugEnabled()) {
    			getLogger().debug("[enviarSolicitud][enviaCorreoBackOffice]" + enviaCorreoBackOffice);
    		}
			
			if (enviaCorreoBackOffice) {
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
				String estado =TablaValores.getValor("multilinea.parametros", "multilineaBackOffice", "estado");
				String asunto =TablaValores.getValor("multilinea.parametros", "multilineaBackOffice", "descripcion");
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
				datosCorreo.setTasaInteres(formatearMonto((tasaInteresInternet),VALOR2,"#,##0"));
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
			paso = VALOR7;   
			if (getLogger().isDebugEnabled()) {
    			getLogger().debug("[enviarSolicitud][BCI_FINOK]");
    		}
		}
		catch (Exception ex){
			if (getLogger().isEnabledFor(Level.ERROR)) {
				getLogger().debug("[enviarSolicitud] [BCI_FINEX]:" + ErroresUtil.extraeStackTrace(ex));
			}
		}
	}  
	
	
	/**
     * Método que envía email a ejecutivo.
     * 
     * Registro de versiones:
     * <ul>
     * <li>1.0 (19/03/2015, Manuel Escárate R. (BEE)): versión inicial.
     * <li>1.1 11/02/2016 Eduardo Perez (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se agregan excepciones para los correos. 
     * <li>1.2 18/05/2016 Manuel Escárate R. (BEE S.A.) - Pablo Paredes (ing.Soft.BCI) : Se modifica formato de fecha 
     *                                                           y se agregan errores de sistema.</li>
     * </ul>
     * @param multiEnvironment multiEnvironment.
     * @param datosParaCorreo datosParaCorreo.
     * @throws GeneralException GeneralException.
     * @since 1.0
     */
    public void enviaEmailEjecutivo(MultiEnvironment multiEnvironment,
    		DatosParaCorreoTO datosParaCorreo) throws GeneralException {

    	if (getLogger().isDebugEnabled()) {
			getLogger().debug("enviaEmailEjecutivo [BCI_INI");
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
			getLogger().debug("enviaEmailEjecutivo - caiOperacionNro              [" + datosParaCorreo.getCaiOperacion() +"]");
        }
        String montoCreditoCorreo = formatearMonto(datosParaCorreo.getMontoFinalCredito(),0,"#,###");
        getLogger().debug("enviaEmailEjecutivo - iicOperacionNro              [" + datosParaCorreo.getIicOperacion() +"]");
        String fechaPrimerVcto = datosParaCorreo.getFechaVencimientoSeleccionada();
        fechaPrimerVencimiento = fechaPrimerVcto.substring(VALOR0,VALOR2) 
        		+ "/" + fechaPrimerVcto.substring(VALOR3,VALOR5) + "/" +fechaPrimerVcto.substring(VALOR6);
        
        if (getLogger().isDebugEnabled()) {
			getLogger().debug("enviaEmailEjecutivo - Antes de emailEjecutivo");
        }
        emailEjecutivo = obtieneCorreoEjecutivo(codigoEjecutivo);
        if (getLogger().isDebugEnabled()) {
			getLogger().debug("enviaEmailEjecutivo - emailEjecutivo               [" + emailEjecutivo +"]");
        }
  
        if (datosParaCorreo.isEnviaBackOffice()){
        	if (getLogger().isDebugEnabled()) {
				getLogger().debug("enviaEmailEjecutivo - emailsbackoffice         [true]");
        	}
            emailsbackoffice = TablaValores.getValor(TABLA_MULTILINEA, "emailsbackoffice", "desc");
        }

        if (!oficinaIngreso.trim().equals("")) emailJefeOficina = buscaMailJefeOficina(
        		multiEnvironment, oficinaIngreso);

        if (getLogger().isDebugEnabled()) {
			getLogger().debug("enviaEmailEjecutivo - Antes de obtener glosa oficina");
        }
        glosaOficinaIngreso = TablaValores.getValor("TabOfic.parametros", oficinaIngreso, "desc");
        if (getLogger().isDebugEnabled()) {
			getLogger().debug("enviaEmailEjecutivo - oficinaIngreso               [" + oficinaIngreso        + "]");
			getLogger().debug("enviaEmailEjecutivo - glosaOficinaIngreso          [" + glosaOficinaIngreso   + "]");
        }
        

        de                  = TablaValores.getValor(TABLA_MULTILINEA, "emailsEjecutivoDe", "desc");
        direccionOrigen     = TablaValores.getValor(TABLA_MULTILINEA, "emailsEjecutivoFrom", "desc");
        direccionDestino    = emailEjecutivo + (emailJefeOficina.trim().equals("") ? "" : "," + emailJefeOficina);
        direccionDestino    += emailsbackoffice.trim().equals("") ? "" : "," + emailsbackoffice.trim();
        String modulo = TablaValores.getValor(TABLA_MULTILINEA, "modulo", "moduloRenovacion");
        asunto              = modulo + TablaValores.getValor(TABLA_MULTILINEA, datosParaCorreo.getAsunto(), "desc");
        asunto              = asunto + " - "+ datosParaCorreo.getRut() + "-" + datosParaCorreo.getDv() + " - " + datosParaCorreo.getRazonSocial();
        String erroresObtenidos = "";
        if (excepciones != null && excepciones.size() > 0){
            for (int i = 0; i < excepciones.size(); i++) {
            	if (!erroresObtenidos.contains(excepciones.get(i))){
            		if (TablaValores.getValor(TABLA_MULTILINEA, "excepciones", excepciones.get(i)) == null){
            			estado = estado + excepciones.get(i) + ";";		
            		}
            		else{
            			estado = estado + TablaValores.getValor(TABLA_MULTILINEA, "excepciones", excepciones.get(i)) + ";";
            		}
            		erroresObtenidos = erroresObtenidos + excepciones.get(i);
            	}
            }
        }
        else {
        	estado = datosParaCorreo.getEstado().trim().equals("") ? " " : TablaValores.getValor(
        			TABLA_MULTILINEA, datosParaCorreo.getEstado(), "desc");
        }

        cuerpoDelEmail      = armaEmailParaDestinatarioGenerico(datosParaCorreo,codBanca,
        		codigoEjecutivo, fechaPrimerVencimiento, 
                oficinaIngreso, glosaOficinaIngreso,montoCreditoCorreo, estado);
        

        enviaMensajeCorreo(de, direccionOrigen, direccionDestino, asunto, cuerpoDelEmail, firma);

        if (getLogger().isDebugEnabled()) {
			getLogger().debug("[enviaEmailEjecutivo] [BCI_FINOK]");
        }

    }
	
	/**
	 * Metodo que envia email a Back Office.
	 *
	 * Registro de versiones:<ul>
	 * <li>1.0 19/03/2015  Manuel Escárate   (BEE) - versión inicial. </li>
	 * <li>1.1 11/02/2016 Eduardo Perez (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se agregan excepciones para los correos.</li>
	 * <li>1.2 18/05/2016 Manuel Escárate R. (BEE S.A.) - Pablo Paredes (ing.Soft.BCI) : Se cambia formato de fecha 
	 *                                             y se agregan excepciones con errores de sistema</li>
	 * </ul>
	 * <p>
	 *
	 * @param multiEnvironment multiEnvironment.
	 * @param datosCorreo datos para el correo.
     * @throws Exception en caso de error.
     * @since 2.1
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
        fechaPrimerVcto = fechaPrimerVcto.substring(0,VALOR2) 
        		+"/"+fechaPrimerVcto.substring(VALOR3,VALOR5) +"/"+fechaPrimerVcto.substring(
        				VALOR6);

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

                emailBackOffice  = TablaValores.getValor("multilinea.parametros", "emailsbackoffice", "desc");
                emailjefeOficina = emailJefeOficina.trim().equals("") ? "" : ","+ emailJefeOficina.trim();
                if (getLogger().isDebugEnabled()){
                     getLogger().debug("[enviaEmailBackOffice] emailEjecutivo ["+  emailEjecutivo +"]");
                }
                notesEjecutivo   = codigoEjecutivo;
                direccionOrigen  = TablaValores.getValor("multilinea.parametros", "emailsbackofficefrom", "desc");

                de              = TablaValores.getValor("multilinea.parametros", "emailMultilinea", "desc");
                String modulo = TablaValores.getValor(TABLA_MULTILINEA, "modulo", "moduloRenovacion");
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
                getLogger().debug("[enviaEmailBackOffice] [BCI_FINEX] El Email al BackOffice No"
                        + " pudo ser enviado :" + ErroresUtil.extraeStackTrace(e));
            }
        }
    }

	/**
	 * Método encargado de realizar el curse de la renovación.
	 * <br>
	 * Registro de versiones:<ul>
	 * <li>1.0 09/12/2014 Eduardo Pérez G. (BEE S.A.): versión inicial.</li>
	 * <li>1.1 12/11/2015, Eduardo Pérez G. (BEE S.A.): Se agrega validación de cuentas y journal.</li>
	 * <li>1.2 09/03/2016, Eduardo Pérez G. (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se modifica documentación y se agregan códigos para journal.</li>
     * <li>1.3 22/02/2016 Eduardo Pérez (BEE) - Felipe Ojeda (ing.Soft.BCI) : Se agrega journals referentes al mandato y nuevas condiciones. </li>
     * <li>1.4 18/05/2016 Manuel Escárate R. (BEE) - Pablo Paredes (ing.Soft.BCI) : Se agregan envíos de correos. </li>
	 * </ul>
     * @throws Exception de control.
	 * @since 1.0
	 */  
	public void cursarRenovacion() throws Exception{
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[cursarRenovacion]["+clienteMB.getRut()+"][BCI_INI]");
		}
		if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[cursarRenovacion][caiOperacion] [" + caiOperacion + "] [iicOperacion] " + iicOperacion + "]");
    	}

        if(aceptaCondicionesMandato){
            String codEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalRenovacion", "codEventoAceptaCondicionesMandato");
            String codSubEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalRenovacion", "codSubEventoAceptaCondicionesMandato");
            String producto = TablaValores.getValor(TABLA_MULTILINEA, "journalRenovacion", "productoAceptaCondicionesMandato");
            journalizar(codEvento,codSubEvento,producto);
        }

		try {
			if (!ValidaCuentasUtility.validarCuentaBCI( String.valueOf(Integer.parseInt(cuentaSeleccionadaAbono)), "CCT", (int) clienteMB.getRut(), clienteMB.getDigitoVerif())) {
        		if (getLogger().isEnabledFor(Level.ERROR)) {
        			getLogger().error("[cursarRenovacion][" + clienteMB.getRut() + "] ha fallado la validacion de la cuenta de abono");
        		}
        		throw new Exception("Problemas al validar la cuenta de abono del cliente");    
        	}
			boolean autenticacionSegundaClave = false;
			try{
			  autenticacionSegundaClave = segundaClaveAplicativo.verificarAutenticacion();
			  if (autenticacionSegundaClave){
				  if (getLogger().isDebugEnabled()) {
					  getLogger().debug("[cursarRenovacion] [" + clienteMB.getRut() + "] Autenticacion OK");
				  }
				  String codEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalRenovacion", "codEventoAutorizaCurse");
				  String codSubEvento = TablaValores.getValor(TABLA_MULTILINEA, "journalRenovacion", "codSubEventoAutorizaCurse");
				  String producto = TablaValores.getValor(TABLA_MULTILINEA, "journalRenovacion", "productoAceptaCondicionesMandato");
				  journalizar(codEvento,codSubEvento,producto);
			  }
			}
			catch (SeguridadException ex) {
				if (getLogger().isEnabledFor(Level.ERROR)) {
					getLogger().error("[cursarRenovacion] [" + clienteMB.getRut()  + "] [BCI_FINEX] "
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
						getLogger().debug("[cursarRenovacion] [num ope] " + numOperacion);
						MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
								sesion.getCanalId(), USUARIO);
						
						SvcCreditosGlobales svcCreditos = new SvcCreditosGlobalesImpl();
		            	ResultConsultaOperacionCredito operacion = null;
		            	try {
		            		operacion = svcCreditos.consultaOperacionCredito(multiEnvironment, 
		            				caiOperacion, iicOperacion);
		            	}
		            	catch (Exception e) {
		            		if (getLogger().isEnabledFor(Level.ERROR)) {
		            			getLogger().error("[firmarOperacion] [BCI_FINEX]  consultaOperacionCredito Exception:" 
		            					+ ErroresUtil.extraeStackTrace(e));
		            		}
		            		try{
		            			enviarCorreoEjecutivo(multiEnvironment,e.getMessage(),"asuntoEmailProblemaAvance",true);
		            		}
		            		catch (Exception ex){
		            			  if (getLogger().isEnabledFor(Level.ERROR)){
		                    		  getLogger().error("[firmarOperacion] ERROR al enviar correos ejecutivo: " + ex);
		                    	  }
		            		}
		            	}
		            	if (getLogger().isDebugEnabled()){
		            		getLogger().debug("[firmarOperacion] condición de garantía ["+ operacion.getCondicionGar()  +"]");
		                }
		            	condicionGarantia = operacion.getCondicionGar() != null ? operacion.getCondicionGar() : null;
						
						String segurosSeleccionados = "";
						String moneda = StringUtils.rightPad(TablaValores.getValor(TABLA_MULTILINEA,
								tipoDeCreditoSeleccionado.trim(),"moneda"), LARGO_CODIGO_MONEDA);
						if (getLogger().isDebugEnabled()) {
							getLogger().debug("tipoDeCreditoSeleccionado.trim()"+ tipoDeCreditoSeleccionado.trim());
							getLogger().debug("moneda"+ moneda);							
						}
						String tipoOperacion = tipoDeCreditoSeleccionado.substring(VALOR0, VALOR3);
						if (getLogger().isDebugEnabled()) {
							getLogger().debug("tipoOperacionnnn"+ tipoOperacion);
						}
						String codigoAuxiliar = tipoDeCreditoSeleccionado.substring(VALOR3, VALOR6);
						
						glosaTipoCredito = TablaValores.getValor(TABLA_MULTILINEA,
								tipoDeCreditoSeleccionado,"glosa"); 
						if (getLogger().isDebugEnabled()) {
							getLogger().debug("tipo de credito"+  glosaTipoCredito);
						}
						
						String fechaPrimerVcto = null;
						fechaPrimerVcto = fechaVencimientoSeleccionada.substring(VALOR0,VALOR2) 
								+ fechaVencimientoSeleccionada.substring(VALOR3,VALOR5) 
								+ fechaVencimientoSeleccionada.substring(VALOR6,VALOR10);
						int contSegurosSeleccionados = 0;
						int largoArregloChecks = 0;
						double  montoCredito= operacionSeleccionadaCancelacion.getValorRenovado();
			
						String  tipoCobroSegurosCodigo = ((String) TablaValores.getValor(TABLA_MULTILINEA, 
								"tipoCobroSegurosCodigo" , "lista"));
						String  tipoCobroSegurosNumero = ((String) TablaValores.getValor(TABLA_MULTILINEA, 
								"tipoCobroSegurosNumero" , "lista"));
						Vector listadoTipoCobroSegurosCodigo = buscaListaCodigos(
								tipoCobroSegurosCodigo, ",");
						Vector listadoTipoCobroSegurosNumero = buscaListaCodigos(
								tipoCobroSegurosNumero, ",");
		                
						if (segurosObtenidos != null){
							for (int i = 0; i < segurosObtenidos.length; i++) {
								if (segurosObtenidos[i].isCheckeado()){
									largoArregloChecks++;
								}
							}
							StringBuilder sbSeguros = new StringBuilder();
							for (int i = 0; i < segurosObtenidos.length; i++) {
								if (segurosObtenidos[i].isCheckeado()){
									int index = listadoTipoCobroSegurosCodigo.indexOf(
											segurosObtenidos[i].getIndCobro());
									String seguro = segurosObtenidos[i].getCodigoSubConcepto()
											+listadoTipoCobroSegurosNumero.get(index);
									sbSeguros.append(seguro);
									contSegurosSeleccionados++;
								}
							}
							if (sbSeguros != null) {
								segurosSeleccionados = sbSeguros.toString();
							}
						}
						
						if (codigoAuxiliar.equals("010")){
							cuotaSeleccionada = 1;
						}
						
						EstructuraVencimiento[] vencimientos = new EstructuraVencimiento[1];
						
						vencimientos[0] = new EstructuraVencimiento(0, ' ', 0, cuotaSeleccionada, 
								ddMMyyyyHHmmssForm.parse(
								fechaPrimerVcto + hhmmss), 0, 0, ' ', "", 0.0, 1, 'M', ' ');
			
						String autoTransac        = "8";
						
						SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyyMMdd");
		                String fecVencimiento2 = sdfYMD.format(operacionSeleccionada.getFecVencimiento2());
						
						DatosParaOperacionesFirmaTO datosOperacionesFirma = new DatosParaOperacionesFirmaTO();
						datosOperacionesFirma.setAutoTransac(autoTransac);
						datosOperacionesFirma.setNumOperacion(caiOperacion);
						datosOperacionesFirma.setAuxiliarCredito(iccOpe);
						datosOperacionesFirma.setNumOperacionCan(caiOperacion);
						datosOperacionesFirma.setCodAuxiliarCredito(iccOpe);
						datosOperacionesFirma.setAuxiliarOpe(codigoAuxiliar);
						datosOperacionesFirma.setCodigoMoneda(moneda);
						datosOperacionesFirma.setCodigoMoneda2(StringUtils.rightPad(
								operacionSeleccionada.getCodMonedaCred(), LARGO_CODIGO_MONEDA));
						datosOperacionesFirma.setCuentaAbono(cuentaSeleccionadaAbono);
						datosOperacionesFirma.setCuentaCargo(cuentaSeleccionadaCargo);
						datosOperacionesFirma.setDigitoVerifEmp(clienteMB.getDigitoVerif());
						datosOperacionesFirma.setDvUsuario(String.valueOf(usuarioModelMB.getDigitoVerif()));
						datosOperacionesFirma.setEstadoSolicitud("PEN");
						datosOperacionesFirma.setFechaExpiracion2(fecVencimiento2);
						datosOperacionesFirma.setFechaFin(fechaVencimientoSeleccionada.substring(VALOR6,VALOR10)
								+ fechaVencimientoSeleccionada.substring(VALOR3,VALOR5)
								+ fechaVencimientoSeleccionada.substring(VALOR0,VALOR2));
						datosOperacionesFirma.setFechaInicio(timestampForm.format(new Date()));
						datosOperacionesFirma.setFechaPrimerVencimiento(
								fechaVencimientoSeleccionada.substring(VALOR6,VALOR10)
								+ fechaVencimientoSeleccionada.substring(VALOR3,VALOR5)
								+ fechaVencimientoSeleccionada.substring(VALOR0,VALOR2));
						datosOperacionesFirma.setGlosaTipoCredito(segurosSeleccionados);
						datosOperacionesFirma.setIdConvenio(clienteMB.getNumeroConvenio());
						datosOperacionesFirma.setRutUsuario(String.valueOf(usuarioModelMB.getRut()));
						datosOperacionesFirma.setDvUsuario(String.valueOf(usuarioModelMB.getDigitoVerif()));
						datosOperacionesFirma.setIndicadorAplic("0"); 
						datosOperacionesFirma.setMonedaLinea(" ");
						datosOperacionesFirma.setMontoAbonado("0");
						datosOperacionesFirma.setMontoCredito(String.valueOf(montoFinalCredito)); 
						datosOperacionesFirma.setNumeroAutorizacion("");
						datosOperacionesFirma.setNumOperacionCan(" ");
						datosOperacionesFirma.setOficinaIngreso(oficinaIngreso);
						datosOperacionesFirma.setProcesoNegocio("REN");
						datosOperacionesFirma.setRutEmpresa(String.valueOf(clienteMB.getRut()));
						datosOperacionesFirma.setDigitoVerifEmp(clienteMB.getDigitoVerif());
						datosOperacionesFirma.setTipoAbono("CCMA");
						datosOperacionesFirma.setTipoCargoAbono("AUT");
						datosOperacionesFirma.setTipoOperacion(tipoOperacion);
						datosOperacionesFirma.setTotalVencimientos(String.valueOf(cuotaSeleccionada));
						datosOperacionesFirma.setNumOperacionValidacionFirma(numOperacion);
						datosOperacionesFirma.setCondicionGarantia(condicionGarantia);
						datosOperacionesFirma.setQuienVisa(WHOVISA2);
						int resultadoFirma = ingresarOperacionFirma(multiEnvironment,datosOperacionesFirma);
						if (getLogger().isDebugEnabled()) {
							getLogger().debug("[cursarRenovacion][resultadoFirma]["+ resultadoFirma +"]");
						}

						if (resultadoFirma == RES_FIRMA_1000){
							String codEvento = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "codEventoCurseNOOK");
				            String codSubEvento = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "codSubEventoCurseNOOK");
				            String producto = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "productoCurseNOOK");
				            journalizar(codEvento,codSubEvento,producto);
				          		paso = PASO5;
							this.cargarRegiones(); 
						}
						else {
							resultadoFirmaAvance = String.valueOf(resultadoFirma);
							paso = PASO6;
					if (resultadoFirma == 1){
				            String codEvento = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "codEventoCurseOK");
                            String codSubEvento = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "codSubEventoCurseOK");
                            String producto = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "productoCurseOK");
                            journalizar(codEvento,codSubEvento,producto);
						 }
					else {
						String codEvento = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "codEventoCurseNOOK");
						String codSubEvento = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "codSubEventoCurseNOOK");
						String producto = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "productoCurseNOOK");
						journalizar(codEvento,codSubEvento,producto);
					}
				}
			}
		
		}
		catch (Exception ex){
			if(getLogger().isEnabledFor(Level.ERROR)){
				getLogger().error("[cursarRenovacion]" +  " [BCI_FINEX]mensaje=< "+ ex.getMessage() +">", ex);
			}
	        String codEvento = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "codEventoCurseNOOK");
            String codSubEvento = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "codSubEventoCurseNOOK");
            String producto = TablaValores.getValor("multilinea.parametros", "journalRenovacion", "productoCurseNOOK");
            journalizar(codEvento,codSubEvento,producto);
            esErrorGenerico = true;
			paso = -1;
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[cursarRenovacion]["+clienteMB.getRut()+"][BCI_FINOK]");
		}
	}
	
	/**
	 * Metodo que utiliza el componente de firmas.
	 *
	 * Registro de versiones:<ul>
	 * <li>1.0 19/03/2015  Eduardo Pérez G.   (BEE) - versión inicial</li>
     * <li>1.1 11/02/2016 Eduardo Perez (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se agregan excepciones para los correos.</li>
     * <li>1.2 18/05/2016 Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI) : Se agregan envíos de correos.</li>
	 * <li>1.3 18/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se agregan envíos de correo a contacto de empresa 
	 *                                                                              y se quita envio a avales en pendientes de firma.</li>
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
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[ingresarOperacionFirma]["+clienteMB.getRut()+"][BCI_INI]");
		}
		int pasoFirma = 0;
		int resultadoValorFirma = 0;
		DatosResultadoOperacionesTO resultadoOpe = null;
		ResultadoFirmaTO resultadoFirma = null;
		try{
			resultadoOpe = crearEJBmultilinea().ingresarOperacionFirma(multiEnvironment,
					datosOperacionesFirma,pasoFirma);
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[ingresarOperacionFirma][resultadoOpe]" + resultadoOpe.toString());
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
				
				if (resultadoOpe.isRespuesta()){
					datosOperacionesFirma.setIdentificadorFirma(resultadoOpe.getNumOperacion());
					pasoFirma = 1;
					String numOperacion = datosOperacionesFirma.getNumOperacionValidacionFirma();
					double montoFinalDelCredito = new Double(datosOperacionesFirma.getMontoCredito());
				    String firma ="";
                    Hashtable    htTablaCreditos = null;
                    // Tabla de Productos para simular avance
                    if (getLogger().isDebugEnabled()) {
            			getLogger().debug("TablaValores... ");
                    }
                        TablaValores tablaCreditos   = new TablaValores();
                                     htTablaCreditos = tablaCreditos.getTabla("simmultilinea.parametros");
                    Enumeration codigos = htTablaCreditos.keys();
                    Object elem         = codigos.nextElement();

                    Hashtable htDesc    = (Hashtable)htTablaCreditos.get(elem);

                    String lsf          = (String)htDesc.get("AVCSIF");
                    String lnf          = (String)htDesc.get("AVCNOF");

                    if (getLogger().isDebugEnabled()) {
            			getLogger().debug("Verificando si debe firmar (Avance) ... ");
                    }
                    if (getLogger().isDebugEnabled()) {
            			getLogger().debug("codBanca =[" + codBanca + "]");
            			getLogger().debug("lsf =[" + lsf + "]");
            			getLogger().debug("lnf =[" + lnf + "]");
                    }
                    firma = necesitaFirmar(codBanca, lsf, lnf, String.valueOf(clienteMB.getRut())); 
                  
                    if (firma.equals("S")){
							resultadoFirma = servicioAutorizacionyFirmaModelMB.firmar(
									numOperacion,montoFinalDelCredito,false);
							if (resultadoFirma != null){
								if ((resultadoFirma.getCodigoRespuesta() == ERROR_GENERICO) && (resultadoFirma.
										getGlosa().equalsIgnoreCase(TablaValores.getValor(
												TABLA_FYP, "registroFyP", "errorGenerico")))){
									resultadoValorFirma = RES_FIRMA_1000;
								}
								else if (resultadoFirma.getCodigoRespuesta() == 1){
									if (getLogger().isDebugEnabled()) {
										getLogger().debug("[ingresoOperacionFirma]["
												+ clienteMB.getRut()+"][BCI_INI]");
									}
									resultadoValorFirma = VALOR_MIL;
									try{
										resultadoOpe = crearEJBmultilinea().ingresarOperacionFirma(
												multiEnvironment,datosOperacionesFirma,pasoFirma);
										if (getLogger().isDebugEnabled()) {
											getLogger().debug("Resultado operación" + resultadoOpe.getNumOperacion());
										}
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

										if (resultadoOpe != null  && resultadoOpe.isRespuesta()){
											resultadoValorFirma = 1;
											try{
												if (getLogger().isDebugEnabled()) {
													getLogger().debug("[ingresoOperacionFirma] [voy a enviar correo ejecutivo]");
												}
												if (bancaEnvioEmailEjecutivo || bancaPermitidasEmpresarioEmail) {
													enviarCorreoEjecutivo(multiEnvironment,"estadoAvanceEfectuadoRen","asuntoEmailExitoRen",false);
												}
												if (getLogger().isDebugEnabled()) {
													getLogger().debug("[ingresoOperacionFirma] [envié correo de ejecutivo]");
												}
												if (getLogger().isDebugEnabled()) {
													getLogger().debug("[ingresoOperacionFirma] [voy a enviar correo de avales]");
												}
												if (condicionGarantia.trim().equals("2")){
													enviarCorreoAvales(multiEnvironment,"estadoAvanceEfectuadoRen","asuntoEmailExitoRen");
												}
												if (getLogger().isDebugEnabled()) {
													getLogger().debug("[ingresoOperacionFirma] [envié correo de avales]");
												}
												if (getLogger().isDebugEnabled()) {
													getLogger().debug("[ingresoOperacionFirma] [voy a enviar correo de empresa]");
												}
												enviarCorreoContactoEmpresa(multiEnvironment, "estadoAvanceEfectuadoRen", "asuntoEmailExitoRen");
												if (getLogger().isDebugEnabled()) {
													getLogger().debug("[ingresoOperacionFirma] [envié correo de empresa]");
												}
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
								else  {
									switch(resultadoFirma.getCodigoRespuesta()){
									case ESTADO_PENDIENTE:
										resultadoValorFirma = ESTADO_PENDIENTE;
										try{
											if (getLogger().isDebugEnabled()) {
												getLogger().debug("[ingresoOperacionFirma] [voy a enviar correo ejecutivo]");
											}
											if (bancaEnvioEmailEjecutivo || bancaPermitidasEmpresarioEmail) {
												enviarCorreoEjecutivo(multiEnvironment,"opePendienteFirmas","asuntoPendienteFirma",false);
											}
											if (getLogger().isDebugEnabled()) {
												getLogger().debug("[ingresoOperacionFirma] [envié correo de ejecutivo]");
											}
										}
										catch (Exception ex){
											if (getLogger().isEnabledFor(Level.ERROR)){
		                         				getLogger().error("[firmarOperacion] ERROR al enviar correos " + ex);
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
										resultadoValorFirma = ERRORFIRMA999;
										break;
									}
								}
							}
                    }
                    else {
                    	if (getLogger().isDebugEnabled()) {
							getLogger().debug("[ingresoOperacionFirma]["+clienteMB.getRut()+"]");
						}
						resultadoValorFirma = VALOR_MIL;
						try{
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
							
							 if (resultadoOpe != null && resultadoOpe.isRespuesta()){
								 resultadoValorFirma = 1;
								 try {
									 if (bancaEnvioEmailEjecutivo || bancaPermitidasEmpresarioEmail) {
										 enviarCorreoEjecutivo(multiEnvironment,"estadoAvanceEfectuadoRen","asuntoEmailExitoRen",false);
									 }
									 if (getLogger().isDebugEnabled()) {
										 getLogger().debug("[ingresoOperacionFirma] [voy a enviar correo de avales]");
									 }
									 if (condicionGarantia.trim().equals("2")){
										 enviarCorreoAvales(multiEnvironment,"estadoAvanceEfectuadoRen","asuntoEmailExitoRen");
									 }
									 if (getLogger().isDebugEnabled()) {
										 getLogger().debug("[ingresoOperacionFirma] [envié correo de avales]");
									 }
									 if (getLogger().isDebugEnabled()) {
										 getLogger().debug("[ingresoOperacionFirma] [voy a enviar correo de empresa]");
									 }
									 enviarCorreoContactoEmpresa(multiEnvironment, "estadoAvanceEfectuadoRen", "asuntoEmailExitoRen");
									 if (getLogger().isDebugEnabled()) {
										 getLogger().debug("[ingresoOperacionFirma] [envié correo de empresa]");
									 }
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
				getLogger().debug("[ingresoOperacionFirma] [BCI_FINEX]:" + ErroresUtil.extraeStackTrace(e));
			}
		}
		
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[ingresoOperacionFirma]["+clienteMB.getRut()+"][BCI_FINOK]");
		}

		return resultadoValorFirma;
	}
	
	/**
     * Método que retorna si el rut necesita firmar.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 12/06/2015 Manuel Escárate (BEE S.A.): versión inicial.</li> 
     * </ul>
     * @param codbanca codigo de banca.
     * @param lsf lsf.
     * @param lnf lnf.
     * @param rutEmpresa rut de empresa.
     * @return retorna indicador si necesita firmar.
     */
    private String necesitaFirmar(String codbanca, String lsf, String lnf, String rutEmpresa) {
    	if (getLogger().isDebugEnabled()) {
			getLogger().debug("[necesitaFirmar]["+clienteMB.getRut()+"][BCI_INI]");
		}
        // requiere firma ?}
        // S  = si        // N = no        // E = Error
        String[] listaFirmables    = null;
        String[] listaNofirmables  = null;
        StringTokenizer st          = null;
        int ind                     = 0;
        String elem                 = "";
        String cp                = codbanca.trim();
        String retorno              = "E";
        boolean sigue               = true;
                                          
        if (Integer.parseInt(rutEmpresa) < Integer.parseInt(TablaValores.getValor(TABLA_MULTILINEA, 
        		"rutMAxNoFirma","valor"))) //si rut empresa es menor a 50MM no firma Edoerner/CPH
            return "N";

        if (lsf != null){
            st                  = new StringTokenizer(lsf, ",");
            listaFirmables     = new String[st.countTokens()];    //iguala la cantidad
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

        if (getLogger().isDebugEnabled()) {
			getLogger().debug("[necesitaFirmar]["+clienteMB.getRut()+"][BCI_FIN]");
		}
        return retorno;
    }
	
	
	
	   /**
     * Método que envía correo a ejecutivo.
     * 
     * Registro de versiones:
     * <ul>
     * <li>1.0 (19/03/2015, Manuel Escárate R. (BEE)): versión inicial. </li>
     * <li>1.1 11/02/2016 Eduardo Perez (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se agregan datos para el correo.
     * <li>1.2 18/05/2016 Manuel Escárate R. (BEE S.A.) - Pablo Paredes (ing.Soft.BCI) : Se modifica nombre de variable. </li>
     * <li>1.3 18/08/2016 Manuel Escárate R. (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se modifica monto de correo. </li>
     * </ul>
     * @param multiEnvironment multiEnvironment.
     * @param estado estado.
     * @param asunto asunto.
     * @param enviaBackOffice enviaBackOffice.
     * 
     * @throws GeneralException excepcion general.
     * @since 1.0
     */    
    public void enviarCorreoEjecutivo(MultiEnvironment multiEnvironment,
    		String estado, String asunto,boolean enviaBackOffice) throws GeneralException{

    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[enviarCorreoEjecutivo]["+clienteMB.getRut()+"][BCI_INI]");
    	}
    	String destinoDelCredito = "";
    	try {
    		if (getLogger().isDebugEnabled()) {
    			getLogger().debug("consultando destinoDelCredito      [" + caiOperacion + iicOperacion + "]" );
    		}
    		ResultConsultaOperacionCredito resConOpe = crearEJBmultilinea().consultaOperacionCredito(
    				multiEnvironment, caiOperacion, iicOperacion);
    		destinoDelCredito  = resConOpe.getGlosaDestinoEspecifico();
    		if (getLogger().isDebugEnabled()) {
    			getLogger().debug("despues destinoDelCredito          [" + caiOperacion + iicOperacion + "]" );
    		}
    	}
    	catch (Exception ex) {
    		if (getLogger().isEnabledFor(Level.ERROR)) {
    			getLogger().error("Error en consultaOperacionCredito ::"+ ex.getMessage());
    		}
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
    	datosCorreo.setTasaInteres(formatearMonto((tasaInteresInternet),VALOR2,"#,##0"));
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
    		getLogger().debug("[enviarCorreoEjecutivo][BCI_FINOK]");    
    	}
    }
	
    
    /**
     * Método encargado de enviar correo a avales.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 12/06/2015 Manuel Escárate (BEE S.A.) - Pablo Paredes (ing.Soft.BCI): versión inicial.</li> 
     * <li>1.1 18/08/2015 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agregan lógica para obtener datos en el envio.</li>
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
		String tipoOperacion = " ";
     	DatosAvalesTO[] avalesMultilinea = null;
     	
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
     	
     	try{
			ResultConsultaAvales obeanAval = new ResultConsultaAvales();
			obeanAval = crearEJBmultilinea().consultaAvales(multiEnvironment,(int)clienteMB.getRut(),clienteMB.getDigitoVerif(), ' ', " "," ", " ", 0, 0, "AVL", "AVC");
			if (getLogger().isDebugEnabled()) { getLogger().debug("[enviarCorreoAvales] despues consultaAvales");}
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
				getLogger().debug("[enviarCorreoAvales] antes del seteo de avales"); 
			}
		}
		catch(Exception e) {
			if(getLogger().isEnabledFor(Level.ERROR)){
				getLogger().error("[enviarCorreoAvales]ERROR [consultaAvales] Cliente  [BCI_FINEX] [ " + e.getMessage() + "]");
			}
		}
     	
        try {
            if (getLogger().isDebugEnabled()) getLogger().debug("consultando destinoDelCredito    "
            		+ "  [" + caiOperacion + iicOperacion + "]" );
            ResultConsultaOperacionCredito resConOpe = crearEJBmultilinea().consultaOperacionCredito(multiEnvironment
           		 , caiOperacion, iicOperacion);
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
			if (getLogger().isDebugEnabled()) getLogger().debug("[enviarCorreoAvales] Total Seguros: " + sumaSeguros);
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
							diaDeVencimiento = Integer.parseInt(fechaProxConvert.substring(0,VALOR2));
						}
					}
				}
			}
            if (getLogger().isDebugEnabled()) { 
            	getLogger().debug("despues destinoDelCredito "+ "[" + caiOperacion + iicOperacion + "]" );
            }
        }
        catch (Exception ex) {
       	  if (getLogger().isEnabledFor(Level.ERROR)) { 
                 getLogger().error("[enviarCorreoAvales][" + clienteMB.getRut() 
                         +"][BCI_FINEX][enviarCorreoAvales]" +" error con mensaje: " + ex.getMessage(), ex);
             }  
            destinoDelCredito = " ";
        }
        if (getLogger().isDebugEnabled()) { 
        	getLogger().debug("[enviarCorreoAvales] avalesMultilinea largo" + avalesMultilinea.length);
        }
        if (avalesMultilinea != null && avalesMultilinea.length > 0){
        	for (int k = 0; k < avalesMultilinea.length; k++) {
        		if (avalesMultilinea[k] != null){
        			DireccionClienteBci[] direcciones = instanciaEJBServicioDirecciones().getAddressBci(Long.parseLong(avalesMultilinea[k].getRutAval()));
        			if (direcciones != null) {
        				for (int j = 0; j < direcciones.length; j++) {
        					nombreAval =direcciones[0].getNombres();
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
        			datosCorreo.setTasaInteres(String.valueOf(tasaInteresInternet));
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
        if (getLogger().isDebugEnabled()){
    		getLogger().debug("[enviarCorreoAvales][BCI_FINOK]");    
    	}
    }
    
    /**
     * Método que envía email al aval.
     * 
     * Registro de versiones:
     * <ul>
     * <li>1.0 10/05/2016, Manuel Escárate R. (BEE) - Pablo Paredes (ing.Soft.BCI): versión inicial.</li>
     * <li>1.1 18/08/2016, Manuel Escárate R. (BEE) - Felipe Ojeda (ing.Soft.BCI): Se modifica seteo de dirección de destino. </li>
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
        fechaPrimerVencimiento = fechaPrimerVcto.substring(0,VALOR2) 
        		+ "/" +fechaPrimerVcto.substring(VALOR3,VALOR5) + "/" 
                +fechaPrimerVcto.substring(VALOR6);
        
        de                  = TablaValores.getValor(TABLA_MULTILINEA, "emailMultilinea", "desc");
        direccionOrigen     = TablaValores.getValor(TABLA_MULTILINEA, "emailsEjecutivoFrom", "desc");
        direccionDestino    = datosCorreo.getDatosAvales().getMailAval();
        String modulo = TablaValores.getValor(TABLA_MULTILINEA, "modulo", "moduloRenovacion");
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
 	 * Metodo que arma mensaje de email para Destinatario aval.
 	 *
 	 *
 	 * Registro de versiones:
 	 * <ul>
 	 * <li>1.0 10/05/2016 Manuel Escárate(BEE) - Pablo Paredes. (ing.Soft.BCI) ) : Version Inicial</li>
 	 * <li>1.0 18/08/2016 Manuel Escárate(BEE) - Felipe Ojeda. (ing.Soft.BCI) ) : Version Inicial</li>
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
 				getLogger().debug("[armaEmailParaDestinatarioAval]antes del archivo mail avales");
 			}
 			String archivoMail = "";
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[armaEmailParaDestinatarioAval] [tipoDeCreditoSeleccionado]" + tipoDeCreditoSeleccionado);
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
 				getLogger().debug("[armaEmailParaDestinatarioAval] archivo mail" + archivoMail);
 				getLogger().debug("[armaEmailParaDestinatarioAval] - ...");
 			}
 			
 			String diaPrimerVenc = "";
			String mesPrimerVenc = "";
			String anioPrimerVenc = "";
			if (fechaPrimerVcto != null && !fechaPrimerVcto.equals("")){
				diaPrimerVenc = fechaPrimerVcto.substring(0,VALOR2); 
				mesPrimerVenc = fechaPrimerVcto.substring(VALOR3,VALOR5);
				anioPrimerVenc = fechaPrimerVcto.substring(VALOR6);      
			}
			
			String diaUltimoVenc = "";
			String mesUltimoVenc = "";
			String anioUltimoVenc = "";
			if (datosCorreo.getFechaUltimaCuota() != null && !datosCorreo.getFechaUltimaCuota().equals("")){
				diaUltimoVenc = datosCorreo.getFechaUltimaCuota().substring(0,VALOR2); 
				mesUltimoVenc = datosCorreo.getFechaUltimaCuota().substring(VALOR3,VALOR5);
				anioUltimoVenc = datosCorreo.getFechaUltimaCuota().substring(VALOR6);      
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
 				getLogger().error("[BCI_FINEX] Exception " + e.getMessage());
 			}
 			throw new GeneralException("UNKNOW", e.getMessage());
 		}

 	}
    
    /**
     * Método que formatea un monto.
     * 
     * Registro de versiones:
     * <ul>
     * <li>1.0 (19/03/2015, Manuel Escárate R. (BEE)): versión inicial.
     * </ul>
     * 
     * @param formatearMonto Monto monto a formatear.
     * @param decimales cantidad de decimales.
     * @param formato formato.
     * @return numero formateado.
     * @since 1.0
     */
    public String formatearMonto(double formatearMonto, int decimales, String formato){
    	if (getLogger().isDebugEnabled()) {
            getLogger().debug("[formatearMonto] [BCI INI]");
        }  
    	DecimalFormatSymbols decimal = new DecimalFormatSymbols(new Locale("es", "cl"));
          DecimalFormat localDecimalFormat = new DecimalFormat(formato, decimal);
          if (decimales > 0){
        	  localDecimalFormat.setMinimumFractionDigits(decimales);
          }
          else {
        	  decimal.setDecimalSeparator(',');
        	  decimal.setGroupingSeparator(('.'));
          }
          if (getLogger().isDebugEnabled()) {
              getLogger().debug("[formatearMonto] [BCI FIN]");
          }  
          return localDecimalFormat.format(formatearMonto);
    }
    
    /**
     * Método para obtener instancia del servicio de direcciones.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 12/06/2015 Manuel Escárate (BEE S.A.): versión inicial.</li> 
     * </ul>
     * @return servicio de direcciones.
     * @throws DireccionesException excepcion de direccion.
     * @since 1.0
     */     
    private ServiciosDirecciones instanciaEJBServicioDirecciones()
    		throws DireccionesException{
    	if (getLogger().isDebugEnabled()) {
            getLogger().debug("[instanciaEJBServicioDirecciones] Instacia EJB direcciones");
    	}
    	ServiciosDireccionesHome localServiciosDireccionesHome = null;
    	try{
    		EnhancedServiceLocator localEnhancedServiceLocator1 = EnhancedServiceLocator.getInstance();
    		localServiciosDireccionesHome = (ServiciosDireccionesHome)localEnhancedServiceLocator1.getHome(
    				"wcorp.serv.direcciones.ServiciosDirecciones", ServiciosDireccionesHome.class);
    		return localServiciosDireccionesHome.create();
    	}
    	catch (Exception localException1){
    		if (getLogger().isEnabledFor(Level.ERROR)) {
    			getLogger().error("[instanciaEJBServicioDirecciones] Fallo 1er intento.", localException1);
    		}
    		try{
    			EnhancedServiceLocator localEnhancedServiceLocator2 = EnhancedServiceLocator.getInstance();
    			localServiciosDireccionesHome = (ServiciosDireccionesHome)localEnhancedServiceLocator2.getHome(
    					"wcorp.serv.direcciones.ServiciosDirecciones", ServiciosDireccionesHome.class);
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
     * Metodo para transformar las comunas obtenidas desde servicios miscelaneos
     * en ComunaTO.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 19/03/2015, Manuel Escárate. (BEE): version inicial</li>
     * </ul>
     * 
     * @param comunasServicio comunas de servicio.
     * @return ComunaTO[] Lista de comunas.
     * @since 1.0
     */
    public ComunaTO[] transformaAComunaTO(DescComuna[] comunasServicio) {
        if (getLogger().isDebugEnabled()) {
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
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[transformaAComunaTO]: Fin.");
        }
        return comunasPorRegion;
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

	public int getPaso() {
		return paso;
	}

	public void setPaso(int paso) {
		this.paso = paso;
	}

	public OperacionCreditoSuperAmp getOperacionSeleccionada() {
		return operacionSeleccionada;
	}

	/**
     * Metodo para transformar las comunas obtenidas desde servicios miscelaneos
     * en ComunaTO.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 19/03/2015, Manuel Escárate. (BEE): version inicial</li>
     * </ul>
     * 
     * @param operacionSeleccionada comunas de servicio.
     * @throws GeneralException general.
     * @throws RemoteException de servicio.
     * @throws EJBException de ejb.
     * @since 1.0
     */
	public void setOperacionSeleccionada(
			OperacionCreditoSuperAmp operacionSeleccionada) 
					throws EJBException, RemoteException, GeneralException {
		this.operacionSeleccionada = operacionSeleccionada;
		obtenerValoresCancelacion();
		
	}

	public ResultCalculoValoresCancelacion getOperacionSeleccionadaCancelacion() {
		return operacionSeleccionadaCancelacion;
	}

	public void setOperacionSeleccionadaCancelacion(
			ResultCalculoValoresCancelacion operacionSeleccionadaCancelacion) {
		this.operacionSeleccionadaCancelacion = operacionSeleccionadaCancelacion;
	}

	public DatosParaRenovacionTO getDatosRenovacion() {
		return datosRenovacion;
	}

	public void setDatosRenovacion(DatosParaRenovacionTO datosRenovacion) {
		this.datosRenovacion = datosRenovacion;
	}

	public SegundaClaveUIInput getSegundaClaveAplicativo() {
		return segundaClaveAplicativo;
	}

	public void setSegundaClaveAplicativo(SegundaClaveUIInput segundaClaveAplicativo) {
		this.segundaClaveAplicativo = segundaClaveAplicativo;
	}

	public Multilinea getMultilineaBean() {
		return multilineaBean;
	}

	public void setMultilineaBean(Multilinea multilineaBean) {
		this.multilineaBean = multilineaBean;
	}

	public double getMontoDisponible() {
		return montoDisponible;
	}

	public void setMontoDisponible(double montoDisponible) {
		this.montoDisponible = montoDisponible;
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

	public boolean isBancaEnvioEmailEjecutivo() {
		return bancaEnvioEmailEjecutivo;
	}

	public void setBancaEnvioEmailEjecutivo(boolean bancaEnvioEmailEjecutivo) {
		this.bancaEnvioEmailEjecutivo = bancaEnvioEmailEjecutivo;
	}

	public boolean isBancaPermitidasEmpresarioEmail() {
		return bancaPermitidasEmpresarioEmail;
	}

	public void setBancaPermitidasEmpresarioEmail(
			boolean bancaPermitidasEmpresarioEmail) {
		this.bancaPermitidasEmpresarioEmail = bancaPermitidasEmpresarioEmail;
	}

	public boolean isTieneMultilineaMonto() {
		return tieneMultilineaMonto;
	}

	public void setTieneMultilineaMonto(boolean tieneMultilineaMonto) {
		this.tieneMultilineaMonto = tieneMultilineaMonto;
	}

	public int getTotalCuotas() {
		return totalCuotas;
	}

	public void setTotalCuotas(int totalCuotas) {
		this.totalCuotas = totalCuotas;
	}

	public String getHhmmss() {
		return hhmmss;
	}

	public void setHhmmss(String hhmmss) {
		this.hhmmss = hhmmss;
	}

	public boolean isNoCargaMetodosInicio() {
		return noCargaMetodosInicio;
	}

	public void setNoCargaMetodosInicio(boolean noCargaMetodosInicio) {
		this.noCargaMetodosInicio = noCargaMetodosInicio;
	}

	public int getCuotaSeleccionada() {
		return cuotaSeleccionada;
	}

	public void setCuotaSeleccionada(int cuotaSeleccionada) {
		this.cuotaSeleccionada = cuotaSeleccionada;
	}

	public String getTipoDeCreditoSeleccionado() {
		return tipoDeCreditoSeleccionado;
	}

	public void setTipoDeCreditoSeleccionado(String tipoDeCreditoSeleccionado) {
		this.tipoDeCreditoSeleccionado = tipoDeCreditoSeleccionado;
	}

	public double getMontoFinalCredito() {
		return montoFinalCredito;
	}

	public void setMontoFinalCredito(double montoFinalCredito) {
		this.montoFinalCredito = montoFinalCredito;
	}

	public double getCostoFinalCredito() {
		return costoFinalCredito;
	}

	public void setCostoFinalCredito(double costoFinalCredito) {
		this.costoFinalCredito = costoFinalCredito;
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

	public ComunaTO[] getComunasPorRegion() {
		return comunasPorRegion;
	}

	public void setComunasPorRegion(ComunaTO[] comunasPorRegion) {
		this.comunasPorRegion = comunasPorRegion;
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

	public String getCodigoComuna() {
		return codigoComuna;
	}

	public void setCodigoComuna(String codigoComuna) {
		this.codigoComuna = codigoComuna;
	}

	public String getCodigoRegion() {
		return codigoRegion;
	}

	public void setCodigoRegion(String codigoRegion) {
		this.codigoRegion = codigoRegion;
	}

	public String getCodSegmento() {
		return codSegmento;
	}

	public void setCodSegmento(String codSegmento) {
		this.codSegmento = codSegmento;
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

	public double getFactorCae() {
		return factorCae;
	}

	public void setFactorCae(double factorCae) {
		this.factorCae = factorCae;
	}

	public double getImpuestos() {
		return impuestos;
	}

	public void setImpuestos(double impuestos) {
		this.impuestos = impuestos;
	}

	public ArrayList<SelectItem> getCuotas() {
		return cuotas;
	}

	public void setCuotas(ArrayList<SelectItem> cuotas) {
		this.cuotas = cuotas;
	}

	/**
     * Método permite obtener las cuotas para modificar.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 02/04/2015 Manuel Escárate (BEE S.A.): versión inicial.</li> 
     * </ul>
     * @since 1.0
     * @return cuotasMod objeto para cuotas si se van a modificar.
     */
	public ArrayList<SelectItem> getCuotasMod() {
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

		return cuotasMod;
	}

	public void setCuotasMod(ArrayList<SelectItem> cuotasMod) {
		this.cuotasMod = cuotasMod;
	}

	/**
	 * Obtiene combo box con tipos de créditos.
	 * <P>
	 * Registro de versiones:
	 * <ul>
	 * <li> 1.0  09/12/2014 Manuel Escárate (BEE): Versión inicial.</li>
	 * </ul>
	 * <p>
	 * @return SelecItem opciones de cuotas.
	 * @since 1.0
	 */
	public ArrayList<SelectItem> getTiposDeCreditosAmostrar() {
		if (getLogger().isDebugEnabled()){
			getLogger().debug("[getTiposDeCreditosAmostrar][BCI_INI]");
		}   
		if (!tipoDeCreditoSeleccionado.equalsIgnoreCase("")){
			tiposDeCreditosAmostrar = new ArrayList<SelectItem>();
			if(getLogger().isDebugEnabled()){
				getLogger().debug("[getTiposDeCreditos]  <=tipoDeCreditoSeleccionado]"
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
     * <li>1.0 02/04/2015 Manuel Escárate (BEE S.A.): versión inicial.</li> 
     * <li>1.1 18/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se obtiene valor del gasto notarial.</li>
     * </ul>
     * @param cai cai de la operación.
     * @param iic iic de la operación.
     * @since 1.0
     */
	public void resumenCredito(String cai, String iic){
		if (getLogger().isDebugEnabled()) {
            getLogger().debug("[resumenCredito]["+clienteMB.getRut()+"][BCI_INI]");
		}
		ResultConsultaOperacionCredito operacionCreditoCalendario = new ResultConsultaOperacionCredito();
    	operacionCreditoCalendario.setMontoProxCuota(valorCuota);
    	operacionCreditoCalendario.setTotalCuotasOrig(cuotaSeleccionada);
        SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
    	Date fechaProximaCuota = FechasUtil.convierteStringADate(fechaVencimientoSeleccionada, ft);
    	operacionCreditoCalendario.setFechaProxCuota(fechaProximaCuota);
    	operacionCreditoCalendario.setMontoCredito(montoFinalCredito);
    	operacionCreditoCalendario.setImpuestos(impuestos);
    	
    	MultiEnvironment multiEnvironment = CrearMultiEnviroment.seteaMultiEnvironment(
				sesion.getCanalId(), USUARIO);
    	ResultConsultaCgr gastos = obtenerGastoNotarial(multiEnvironment, cai, iic);
		double gastoNotario = 0;
		
		if (gastos != null && gastos.getTotOcurrencias() > 0){
			gastoNotario = gastos.getInstanciaDeConsultaCgr(0).getTasaMontoFinal();
			if (getLogger().isDebugEnabled()){ 
				getLogger().debug("[resumenCredito] valorNotario: " +  gastoNotario);
			}
		} 
		else {
			if (getLogger().isDebugEnabled()){
				getLogger().debug("[resumenCredito] No existen ocurrencias en la consulta CGR para Gastos Notariales");
			}
    	} 
		if (getLogger().isDebugEnabled()) getLogger().debug("[resumenCredito] Gastos Notariales: " + gastoNotario);
		operacionCreditoCalendario.setValorGasto(gastoNotario);
    	
    	this.setOperacionCredito(operacionCreditoCalendario);
    	muestraCalendario = false;
    	muestraSeguros = false;
    	muestraResumen = true;
    	if (getLogger().isDebugEnabled()) {
            getLogger().debug("[resumenCredito]["+clienteMB.getRut()+"][BCI_FIN]");
		}
	}
	
	/**
     * Método permite obtener el calendario de pago.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 02/04/2015 Manuel Escárate (BEE S.A.): versión inicial.</li> 
     * </ul>
     * @param cai cai de la operación.
     * @param iic iic de la operación.
     * @since 1.0
     */
	public void calendarioPago(String cai, String iic){
		if (getLogger().isDebugEnabled()) {
            getLogger().debug("[calendarioPago]["+clienteMB.getRut()+"][BCI_INI]");
		}	
		ResultConsultaOperacionCredito operacionCreditoCalendario = new ResultConsultaOperacionCredito();
    	operacionCreditoCalendario.setMontoProxCuota(valorCuota);
        SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
    	Date fechaProximaCuota = FechasUtil.convierteStringADate(fechaVencimientoSeleccionada, ft);
    	operacionCreditoCalendario.setFechaProxCuota(fechaProximaCuota);
    	this.setOperacionCredito(operacionCreditoCalendario);
    	List<CalendarioPago> calendarioConCuotas = new ArrayList<CalendarioPago>();
    	for (int i = 0; i < calendarioPagoSalida.length; i++) {
			if (calendarioPagoSalida[i] != null){
				calendarioConCuotas.add(calendarioPagoSalida[i]);
			}
		}
    	calendarioPago = calendarioConCuotas.toArray(new CalendarioPago[calendarioConCuotas.size()]);
    	muestraResumen = false;
    	muestraSeguros = false;
    	muestraCalendario = true;
    	if (getLogger().isDebugEnabled()) {
            getLogger().debug("[calendarioPago]["+clienteMB.getRut()+"][BCI_FIN]");
		}
	}
	
	/**
     * Método permite mostrar los seguros en el último paso.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 02/04/2015 Manuel Escárate (BEE S.A.): versión inicial.</li> 
     * </ul>
     * @since 1.0
     */
	public void detalleSeguros(){
		   muestraResumen = false;
		   muestraCalendario = false; 
	       muestraSeguros = true;		
	}
	
	   /**
     * <p>Método encargado de la journalización de un evento.</p>
     * <p>
     * Registro de versiones:
     * <ul>
     * 
     * <li>1.0 15/06/2015, Manuel Escárate  (BEE): versión inicial.</li>
     * <li>1.1 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): se agrega data a journal.</li>
     *
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
            String estadoCanal = TablaValores.getValor(TABLA_MULTILINEA, "journalRenovacion", "estadoCanal");
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
            datos.put("monto", montoFinalCredito);

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
     * Obtiene combo box con tipos de créditos.
     * <P>
     * Registro de versiones:
     * <ul>
     * <li> 1.0  25/02/2015 Eduardo Pérez G. (BEE): Versión inicial.</li>
     * </ul>
     * <p>
     * @return tiposDeCreditos selecitem con tipos de creditos.
     * @since 1.0
     */
	public ArrayList<SelectItem> getTiposDeCreditos() {
		if (getLogger().isDebugEnabled()){
			getLogger().debug("[getTiposDeCreditos][BCI_INI]");
		}   
		String key = StringUtils.rightPad(operacionSeleccionada.getCodMonedaCred(), 
				LARGO_CODIGO_MONEDA)  + operacionSeleccionada.getCodigo10();
		String[]  resultado;
		resultado = StringUtil.divide(TablaValores.getValor(TABLA_RENMULTILINEA,
				key,"LC"), ",");
		tiposDeCreditos = new ArrayList<SelectItem>();
		for (int i = 0; i < resultado.length; i++) {
			String value = resultado[i];
			String codigo = value.substring(LARGO_CODIGO_MONEDA);
			String glosa = TablaValores.getValor(TABLA_MULTILINEA,
					codigo,"glosa");
			tiposDeCreditos.add(new SelectItem(codigo,glosa));
		} 
		
		if(getLogger().isDebugEnabled()){
			getLogger().debug("[getTiposDeCreditos][BCI_FINOK] retornando objeto : " + tiposDeCreditos);
		}
		return tiposDeCreditos;
	}
	
	public void setTiposDeCreditos(ArrayList<SelectItem> tiposDeCreditos) {
		this.tiposDeCreditos = tiposDeCreditos;
	}

	public void setTiposDeCreditosAmostrar(
			ArrayList<SelectItem> tiposDeCreditosAmostrar) {
		this.tiposDeCreditosAmostrar = tiposDeCreditosAmostrar;
	}

	public DatosSegurosTO[] getSegurosObtenidos() {
		return segurosObtenidos;
	}

	public void setSegurosObtenidos(DatosSegurosTO[] segurosObtenidos) {
		this.segurosObtenidos = segurosObtenidos;
	}

	public DatosPlantillaProductoTO[] getDatosPlantilla() {
		return datosPlantilla;
	}

	public void setDatosPlantilla(DatosPlantillaProductoTO[] datosPlantilla) {
		this.datosPlantilla = datosPlantilla;
	}

	public SelectItem[] getCuentasCorrientesYPrimas() {
		return cuentasCorrientesYPrimas;
	}

	public void setCuentasCorrientesYPrimas(SelectItem[] cuentasCorrientesYPrimas) {
		this.cuentasCorrientesYPrimas = cuentasCorrientesYPrimas;
	}

	public ArrayList<SelectItem> getRegiones() {
		return regiones;
	}

	public void setRegiones(ArrayList<SelectItem> regiones) {
		this.regiones = regiones;
	}

	/**
     * Método que permite obtener arreglo para el combobox de comunas, las cuales corresponden a la ciudad
     * seleccionada.
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0 19/03/2015 Manuel Escárate R. (BEE): Versin Inicial
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
			if (getLogger().isDebugEnabled()){
				getLogger().debug("en comunasmas codigo recion" + codigoRegion);
			}
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
			getLogger().debug("[cargarRegiones][BCI_FINOK] retornando objeto : " + this.regiones);
		}
		
		return comunas;
	}

	/**
     * Método para saber si se encuentra en la hora correcta para la renovación.
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0 20/03/2015 Eduardo Pérez G. (BEE): Versin Inicial
     * </ul>
     * <p>
     * @return Boolean si se encuentra o no dentro del horario de renovación.
     * @since 1.0
     */
	public boolean getHoraRenovacion(){
		if (getLogger().isDebugEnabled()){
			getLogger().debug("[getHoraRenovacion][BCI_INI]");	
		}
		boolean retorno = true;
		String considerarHorario = TablaValores.getValor(
				TABLA_MULTILINEA, "considerarhorariorenovacion", "desc");
		int comparaHorario    = Integer.parseInt(FechasUtil.convierteDateAString(new Date(), "HHmm"));
		if (considerarHorario.trim().equals("S")){  
			if(!((comparaHorario >= Integer.parseInt(
					HORARIO_INICIO)) && (comparaHorario <= Integer.parseInt(HORARIO_FIN)))){
	           retorno = false;            
			} 
	    }	    
		if (getLogger().isDebugEnabled()){
			getLogger().debug("[getHoraRenovacion][BCI_FIN]");	
		}
		return retorno;
	}
	
	/**
     * Método para cambiar el paso del flujo.
     * <br>
     * Registro de versiones:<ul>
     * <li>1.0 02/04/2015 Eduardo Pérez G. (BEE S.A.): versión inicial.</li> 
     * </ul>
     * @param pasoVista de que módulo viene.
     * @since 1.0
     */
	public void volverPaso(int pasoVista){
    	paso = pasoVista;
    }
	
	public String getDescuentoVencimiento(){
    	return DESCTO_VENC_REN;
    }
	
	/**
	 * Verifica si la banca del cliente este en la lista de bancas permitidas.
	 * <p>
	 * Registro de Versiones
	 * <ul>
     * <li>1.0 20/03/2015 Eduardo Pérez G. (BEE): Versin Inicial
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
	public static boolean verificaPertenenciaBanca(String codbanca,
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
	 * Método que obtiene email a partir de username de ejecutivo y/o jefe de
	 * oficina.
	 *
	 *
	 * Registro de versiones:
	 * <ul>
     * <li>1.0 20/03/2015 Eduardo Pérez G. (BEE): Versin Inicial
	 * </ul>
	 * <p>
	 *
	 * @param codigoEjecutivoCorreo codigo de ejecutivo/jefe de oficina.
	 * @return correo del ejecutivo.
	 * @since 1.1
	 */
	public String obtieneCorreoEjecutivo(String codigoEjecutivoCorreo) {

		String mailEjecutivo = "";
		try {
			if (getLogger().isDebugEnabled()) {
	            getLogger().debug("obtieneCorreoEjecutivo - Antes de mailEspecial");
			}
			String mailEspecial = TablaValores.getValor(
					"multiworkflowintegrator.parametros", "USERNAME_"
							+ codigoEjecutivoCorreo.trim(), "correo");
			if (getLogger().isDebugEnabled()) {
	            getLogger().debug("obtieneCorreoEjecutivo - mailEspecial [" + mailEspecial
					+ "]");
			}
			if (mailEspecial != null) {
				if (mailEspecial.length() > 0) {
					mailEjecutivo = mailEspecial;
				}
			}
			if (mailEjecutivo.trim().equals("")) {
				if (!codigoEjecutivoCorreo.trim().equals("")) {
					mailEjecutivo = codigoEjecutivoCorreo.toLowerCase().trim()
							+ "@bci.cl";
				}
			}
			if (getLogger().isDebugEnabled()) {
	            getLogger().debug("obtieneCorreoEjecutivo - mailEjecutivo ["
					+ mailEjecutivo + "]");
			}
		} 
		catch (Exception e) {
			if (getLogger().isEnabledFor(Level.ERROR)) {
	            getLogger().error("Error en el servicio de correo : " + e.getMessage());
			}
			mailEjecutivo = "";
		}
		mailEjecutivo = mailEjecutivo.trim().equals("") ? (codigoEjecutivoCorreo
				.trim().equals("") ? "" : codigoEjecutivoCorreo.toLowerCase().trim()
				+ "@bci.cl") : mailEjecutivo;

		return mailEjecutivo;
	}
	
	/**
	 * busca email de jefe de oficina de ejecutivo.
	 * <p>
	 * Registro de Versiones
	 * <ul>
     * <li>1.0 20/03/2015 Eduardo Pérez G. (BEE): Versin Inicial
	 *
	 * </ul>
	 * </p>
	 *
	 * @param multiEnvironment MultiEnvironment.
	 * @param oficinaIngresoCorreo oficiana de ingreso.
	 * @return email de jefe oficina
	 * @since 1.5
	 *
	 */
	public String buscaMailJefeOficina(
			MultiEnvironment multiEnvironment, String oficinaIngresoCorreo) {

		if (getLogger().isDebugEnabled()) {
            getLogger().debug("buscaMailJefeOficina [BCI_INI]");
		}

		if (getLogger().isDebugEnabled()) {
            getLogger().debug("buscaMailJefeOficina - aqui imprimo oficinaIngreso ["
				+ oficinaIngresoCorreo + "]");
		}
		RowConsultaMasivaDeOficinas[] result = null;

		boolean errorConsulta = false;
		try {
			SvcBoletaDeGarantiaImpl svc = new SvcBoletaDeGarantiaImpl();
			if (getLogger().isDebugEnabled()) {
	            getLogger().debug("buscaMailJefeOficina - DESPUES DE INSTANCIAR   SvcBoletaDeGarantiaImpl");
			}

			if (getLogger().isDebugEnabled()) {
	            getLogger().debug("buscaMailJefeOficina - ANTES DE LA CONSULTA DE LA CONSULTA MASIVA DE OFICINAS");
			}
			result = svc.consultaMasivaDeOficinas(multiEnvironment,
					oficinaIngresoCorreo, "").getRowConsultaMasivaDeOficinas();

		} 
		catch (Exception e) {
			if (getLogger().isEnabledFor(Level.ERROR)) {
				getLogger().error("buscaMailJefeOficina - Exception [" + e.toString() + "]");
			}
			errorConsulta = true;
		}

		if (getLogger().isDebugEnabled()) {
            getLogger().debug("buscaMailJefeOficina - aqui imprimo los datos de result de consulta Masiva De Oficinas ");
		}
		String emailJefeOficina = "";

		if (!errorConsulta) {
			int concurrencias = result.length;
			if (getLogger().isDebugEnabled()) {
	            getLogger().debug("buscaMailJefeOficina - cantidad de filas ["
					+ concurrencias + "]");
			}

			if (concurrencias > 0) {
				for (int i = 0; i < concurrencias; i++) {
					if (getLogger().isDebugEnabled()) {
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
			if (getLogger().isDebugEnabled()) {
	            getLogger().debug("buscaMailJefeOficina - Fallo en consulta emailJefeOficina []");
			}
		}
		if (getLogger().isDebugEnabled()) {
            getLogger().debug("buscaMailJefeOficina - emailJefeOficina ["
				+ emailJefeOficina.trim() + "]");
		}
		if (getLogger().isDebugEnabled()) {
            getLogger().debug("buscaMailJefeOficina [BCI_FIN]");
		}

		return emailJefeOficina.trim();
	}
	
	/**
	 * Metodo que arma mensaje de email generico a Destinatario.
	 *
	 *
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 10/04/2015 Manuel Escárate(BEE - Jimmy Muñoz D. (ing.Soft.BCI) ): Version Inicial</li>
	 * <li>1.1 11/02/2016 Eduardo Perez (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se agregan datos para los correos.</li>
	 * <li>1.2 18/02/2016 Manuel Escárate R. (BEE S.A.) - Pablo Paredes (ing.Soft.BCI) : Se modifica dv en el idc.</li>
	 * </ul>
	 * <p>
	 *
	 * @param datosCorreo datos para correo.
	 * @param codBancaCorreo codigo banca.
	 * @param codigoEjecutivoCorreo codigo ejecutivo.
	 * @param fechaPrimerVcto fecha primer vencimiento.
	 * @param codOficinaIngreso codigo oficina del cliente.
	 * @param glosaOficinaIngreso glosa oficina del cliente.
	 * @param montoCredito Monto del credito.
	 * @param estado estado.
	 * @throws GeneralException Excepcion general.
	 * @return cuerpo de correo.
	 * @since 2.0
	 */
	public String armaEmailParaDestinatarioGenerico(
			DatosParaCorreoTO datosCorreo, String codBancaCorreo,
			String codigoEjecutivoCorreo, String fechaPrimerVcto,
			String codOficinaIngreso, String glosaOficinaIngreso,
			String montoCredito, String estado) throws GeneralException {

		try {
			if (getLogger().isDebugEnabled()) {
	            getLogger().debug("[armaEmailParaDestinatario] [BCI_INI]");
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
			paramsTemplate.put("${codigoEjecutivo}", codigoEjecutivoCorreo);
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
						+ codigoEjecutivoCorreo);
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
	            
	            getLogger().debug("[armaEmailParaDestinatario] [BCI_FINOK]");
			}

			String cuerpoMail = "";
			if(cuerpoDelEmail != null) {
				cuerpoMail = cuerpoDelEmail.toString();
			}
			return cuerpoMail;

		} 
		catch (Exception e) {
			if (getLogger().isEnabledFor(Level.ERROR)) {
				getLogger().error("[BCI_FINEX] Exception " + e.getMessage());
			}
			throw new GeneralException("UNKNOW", e.getMessage());
		}

	}
	
	
	/**
	 * Metodo que envia email a destinatario.
	 *
	 *
	 * Registro de versiones:
	 * <ul>
     * <li>1.0 20/03/2015 Eduardo Pérez G. (BEE): Versin Inicial
	 * </ul>
	 * <p>
	 *
	 * @param de Nombre del emisor del mensaje.
	 * @param direccionOrigen Dirección de correo electrónico del emisor.
	 * @param direccionDestino Dirección de correo electrónico del destinatario.
	 * @param asunto Asunto del mensaje.
	 * @param cuerpo Cuerpo del mensaje.
	 * @param firma Firma del mensaje.
	 * @throws GeneralException GeneralException.
	 * @since 1.1
	 */
	public void enviaMensajeCorreo(String de, String direccionOrigen,
			String direccionDestino, String asunto, String cuerpo, String firma)
			throws GeneralException {

		try {

			if (getLogger().isDebugEnabled()) {
	            getLogger().debug("INI enviaMensajeCorreo ");
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
				if (getLogger().isDebugEnabled()) {
		            getLogger().debug("Mensaje de correo enviado !!!");
				}
			} 
			else {
				if (resultEnvioCorreo == EnvioDeCorreo.FALLOENELENVIO) {
					resultadoFinalEnvio += "Exception. Retorno de código de error ("
							+ resultEnvioCorreo + ")";
					if (getLogger().isDebugEnabled()) {
			            getLogger().debug(resultadoFinalEnvio);
					}
				}
				else if (resultEnvioCorreo == EnvioDeCorreo.FROMVACIO) {
					resultadoFinalEnvio += "From Vacio. Número de error ("
							+ resultEnvioCorreo + ")";
					if (getLogger().isDebugEnabled()) {
			            getLogger().debug(resultadoFinalEnvio);
					}
				} 
				else if (resultEnvioCorreo == EnvioDeCorreo.FROMILEGAL) {
					resultadoFinalEnvio += "From Ilegal. Número de error ("
							+ resultEnvioCorreo + ")";
					if (getLogger().isDebugEnabled()) {
			            getLogger().debug(resultadoFinalEnvio);
					}
				} 
				else if (resultEnvioCorreo == EnvioDeCorreo.TOVACIO) {
					resultadoFinalEnvio += "To Vacio. Número de error ("
							+ resultEnvioCorreo + ")";
					if (getLogger().isDebugEnabled()) {
			            getLogger().debug(resultadoFinalEnvio);
					}
				} 
				else if (resultEnvioCorreo == EnvioDeCorreo.SUBJECTVACIO) {
					resultadoFinalEnvio += "Subject Vacio. Número de error ("
							+ resultEnvioCorreo + ")";
					if (getLogger().isDebugEnabled()) {
			            getLogger().debug(resultadoFinalEnvio);
					}
				} 
				else if (resultEnvioCorreo == EnvioDeCorreo.CUERPOVACIO) {
					resultadoFinalEnvio += "Cuerpo Vacio. Número de error ("
							+ resultEnvioCorreo + ")";
					if (getLogger().isDebugEnabled()) {
			            getLogger().debug(resultadoFinalEnvio);
					}
				} 
				else {
					resultadoFinalEnvio += "Desconocido. Número de error ("
							+ resultEnvioCorreo + ")";
					if (getLogger().isDebugEnabled()) {
			            getLogger().debug(resultadoFinalEnvio);
					}
				}
				throw new GeneralException(resultadoFinalEnvio);
			}
			if (getLogger().isDebugEnabled()) {
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
	 * busca lista de codigos delimitada por separador y los agrupa en vector.
	 * <p>
	 * Registro de Versiones
	 * <ul>
     * <li>1.0 20/03/2015 Eduardo Pérez G. (BEE): Versin Inicial
	 *
	 * </ul>
	 * </p>
	 *
	 * @param lista de codigos.
	 * @param separador de codigos.
	 * @return Vector con codigos.
	 * @since 1.4
	 *
	 */
	public Vector buscaListaCodigos(String lista, String separador) {

		Vector vectorRetorno = new Vector();
		StringTokenizer st = null;
		String elem = "";

		if (lista != null) {
			st = new StringTokenizer(lista, separador);
			while (st.hasMoreTokens()) {
				elem = st.nextToken().trim();
				vectorRetorno.add(elem);
			}
		}

		return vectorRetorno;
	}
	
	/**
	 * cumpleHorarios verifica si la operacion de credito esta en los horarios
	 * establecidos.
	 *
	 * Registro de versiones:
	 * <ul>
     * <li>1.0 20/03/2015 Eduardo Pérez G. (BEE): Versin Inicial
	 *
	 * </ul>
	 * <p>
	 * @return true o false
	 * @since 2.1
	 */
	public boolean cumpleHorarios() {

		boolean retorno = false;
		String horaTardeInicial = TablaValores.getValor(
				"multilinea.parametros", "HorarioTardeini", "desc");
		String horaTardeFinal = TablaValores.getValor("multilinea.parametros",
				"HorarioTardefin", "desc");
		int horaIniTarde = Integer.parseInt(horaTardeInicial);
		int horaTerTarde = Integer.parseInt(horaTardeFinal);
		String hora = FechasUtil.fechaActualFormateada("HHmm");
		int horanow = Integer.parseInt(hora);

		if (getLogger().isDebugEnabled()) {
            getLogger().debug("cumpleHorarios  - El Horario es [" + horanow + "]");
            getLogger().debug("cumpleHorarios  - horaIniTarde  [" + horaIniTarde + "]");
            getLogger().debug("cumpleHorarios  - horaTerTarde  [" + horaTerTarde + "]");
		}

		if ((horanow > horaTerTarde) && (horanow < horaIniTarde)) {
			if (getLogger().isDebugEnabled()) {
	            getLogger().debug("cumpleHorarios  - [true]");
			}
			retorno = true;
		} 
		else {
			if (getLogger().isDebugEnabled()) {
	            getLogger().debug("cumpleHorarios  - [false]");
			}
			retorno = false;
		}

		return retorno;
	}
	
	/**
	 * Metodo que reemplaza en plantilla dada las campos dinamicos en correo de
	 * texto.
	 * Registro de versiones:
	 * <ul>
     * <li>1.0 20/03/2015 Eduardo Pérez G. (BEE): Versin Inicial
	 * </ul>
	 * <p>
	 * @param aVariables a reemplazar en plantilla dada.
	 * @param archivo plantilla.
	 * @return plantilla con reemplazos
	 * @since 1.7
	 */
	private  StringBuffer plantillaCorreo(Map aVariables, String archivo) {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("plantillaCorreo - [BCI_INI] ...");
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
				getLogger().error("plantillaCorreo - IOException - Algo ocurrio ::"
					+ ErroresUtil.extraeStackTrace(e));
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
			getLogger().debug("plantillaCorreo - [BCI_FIN] ...");
		}
		return buffer;
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
             if (getLogger().isDebugEnabled()) getLogger().debug("[BCI_FINEX] ERROR***Exception...Exception [" + e.getMessage() + "]");
         }
         if (getLogger().isDebugEnabled()){
             getLogger().debug("[obtenerGastoNotarial][BCI_FINOK]");
         }
         return obeanCGRCGN;
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
     * @since 1.5
     */    
    public ResultConsultaCgr obtenerSeguros(MultiEnvironment multiEnvironment, String cai, String iic){
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[obtenerSeguros][" + clienteMB.getRut() + "][BCI_INI]");
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
        	 getLogger().debug("[obtenerSeguros][" + clienteMB.getRut() + "][BCI_FINOK]");
         }
         return obeanCGRSGS;
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
                	if (getLogger().isDebugEnabled()){
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
			DatosAvalesTO[] avalesMultilinea = null;
		    if (calendarioPagoSalida != null && calendarioPagoSalida[cuotaSeleccionada-1].getFecVencPago() != null){
		    	fechaUltimaCuota =  FechasUtil.convierteDateAString(calendarioPagoSalida[cuotaSeleccionada-1].getFecVencPago(),"dd/MM/yyyy"); 
		    }
		    
		    try{
	    		 ResultConsultaAvales obeanAval = new ResultConsultaAvales();
	    		 obeanAval = crearEJBmultilinea().consultaAvales(multiEnvironment,(int)clienteMB.getRut(),clienteMB.getDigitoVerif(), ' ', " "," ", " ", 0, 0, "AVL", "AVC");
	    		 if (getLogger().isDebugEnabled()) {
	    			 getLogger().debug("[enviarCorreoContactoEmpresa] despues consultaAvales");
	    		 }
	    		 Aval[] avales = null;
	    		 if (obeanAval != null){
	    			 avales    = obeanAval.getAvales();
	    			 if (getLogger().isDebugEnabled()) { 
	    				 getLogger().debug("[enviarCorreoContactoEmpresa] avales.length ["+ avales.length +"]");
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
	    			 getLogger().debug("[enviarCorreoContactoEmpresa] antes del seteo de avales"); 
	    		 }
	    	 }
	    	 catch(Exception e) {
	    		 if(getLogger().isEnabledFor(Level.ERROR)){
	    			 getLogger().error("[enviarCorreoContactoEmpresa]ERROR [consultaAvales] [BCI_FINEX] Cliente  Exception [ " + e.getMessage() + "]");
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
						+ "  [" + caiOperacion + iicOperacion + "]" );
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
				if (getLogger().isDebugEnabled()) getLogger().debug("[enviarCorreoContactoEmpresa] Total Seguros: " + sumaSeguros);
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
								String fechaProxConvert = FechasUtil.convierteDateAString(fechaProxVenc,"ddMMyyyy"); 
								diaDeVencimiento = Integer.parseInt(fechaProxConvert.substring(0,VALOR2));
							}
						}
					}
					
				}
				if (getLogger().isDebugEnabled()) getLogger().debug("[enviarCorreoContactoEmpresa] despues destinoDelCredito "
						+ "[" + caiOperacion + iicOperacion + "]" );
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
						if (avalesMultilinea[i] != null) {
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
				datosCorreo.setTasaInteres(formatearMonto((tasaInteresInternet),VALOR2,"#,##0"));
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
     * <li>1.0 18/08/2016, Manuel Escárate R. (BEE) - Felipe Ojeda. (ing.Soft.BCI): versión inicial.</li>
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
        fechaPrimerVencimiento = fechaPrimerVcto.substring(0,VALOR2) 
        		+ "/" +fechaPrimerVcto.substring(VALOR3,VALOR5) + "/" 
                +fechaPrimerVcto.substring(VALOR6);
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
        	getLogger().debug("enviaEmailAval FIN");
        }
    }
    
    /**
	 * Metodo que arma mensaje de email para Destinatario contacto.
	 *
	 *
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 18/08/2016 Manuel Escárate(BEE) - Felipe Ojeda. (ing.Soft.BCI) ) : Version Inicial.</li>
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
				getLogger().debug("[armaEmailParaDestinatarioContacto]antes del archivo mail avales");
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
				getLogger().debug("[armaEmailParaDestinatarioContacto]archivo mail" + archivoMail);
				getLogger().debug("[armaEmailParaDestinatarioContacto]armaEmailParaDestinatario - ...");
			}
			
			String diaPrimerVenc = "";
			String mesPrimerVenc = "";
			String anioPrimerVenc = "";
			if (fechaPrimerVcto != null && !fechaPrimerVcto.equals("")){
				diaPrimerVenc = fechaPrimerVcto.substring(0,VALOR2); 
				mesPrimerVenc = fechaPrimerVcto.substring(VALOR3,VALOR5);
				anioPrimerVenc = fechaPrimerVcto.substring(VALOR6);      
			}
			
			String diaUltimoVenc = "";
			String mesUltimoVenc = "";
			String anioUltimoVenc = "";
			if (datosCorreo.getFechaUltimaCuota() != null && !datosCorreo.getFechaUltimaCuota().equals("")){
				diaUltimoVenc = datosCorreo.getFechaUltimaCuota().substring(0,VALOR2); 
				mesUltimoVenc = datosCorreo.getFechaUltimaCuota().substring(VALOR3,VALOR5);
				anioUltimoVenc = datosCorreo.getFechaUltimaCuota().substring(VALOR6);      
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
				getLogger().error("[armaEmailParaDestinatarioContacto] [BCI_FINEX]Exception " + e.getMessage());
			}
			throw new GeneralException("UNKNOW", e.getMessage());
		}

	}
	
    /**
     * retorna una instancia del EJB Precios.
     * <P>
     * Registro de versiones:
     * <ul>
     * <li> 1.0  18/8/2016 Manuel Escárate. (BEE) - Felipe Ojeda (ing.Soft.BCI): Versión inicial.</li>
     * </ul>
     * <p>
     * @return PreciosContextos instancia del ejb Multilinea.
     * @throws GeneralException en caso de error.
     * @since 1.5
     */
    private PreciosContextos crearEJBprecios() throws GeneralException {
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[crearEJBprecios] [BCI_INI]");
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
                getLogger().error("[crearEJBprecios] [BCI_FINEX] RemoteException: Error al crear instancia EJB", e);
            }
            throw new GeneralException("ESPECIAL", "Error al crear instancia EJB");
        }
    }    
	
	public void setComunas(ArrayList<SelectItem> comunas) {
		this.comunas = comunas;
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

	public String getCondicionGarantia() {
		return condicionGarantia;
	}

	public void setCondicionGarantia(String condicionGarantia) {
		this.condicionGarantia = condicionGarantia;
	}

	public String getFechaMaximaPrimerVencimiento() {
		return fechaMaximaPrimerVencimiento;
	}

	public void setFechaMaximaPrimerVencimiento(String fechaMaximaPrimerVencimiento) {
		this.fechaMaximaPrimerVencimiento = fechaMaximaPrimerVencimiento;
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

	public String getDiasIniPrimerVencimiento() {
		return diasIniPrimerVencimiento;
	}

	public void setDiasIniPrimerVencimiento(String diasIniPrimerVencimiento) {
		this.diasIniPrimerVencimiento = diasIniPrimerVencimiento;
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

	public boolean isSeguroSeleccionado() {
		return seguroSeleccionado;
	}

	public void setSeguroSeleccionado(boolean seguroSeleccionado) {
		this.seguroSeleccionado = seguroSeleccionado;
	}

	public double getMontoTotalSegurosSeleccionados() {
		return montoTotalSegurosSeleccionados;
	}

	public void setMontoTotalSegurosSeleccionados(
			double montoTotalSegurosSeleccionados) {
		this.montoTotalSegurosSeleccionados = montoTotalSegurosSeleccionados;
	}

	public boolean isAceptaCondiciones() {
		return aceptaCondiciones;
	}

	public void setAceptaCondiciones(boolean aceptaCondiciones) {
		this.aceptaCondiciones = aceptaCondiciones;
	}

	public String getGlosaTipoCredito() {
		return glosaTipoCredito;
	}

	public void setGlosaTipoCredito(String glosaTipoCredito) {
		this.glosaTipoCredito = glosaTipoCredito;
	}

	public CalendarioPago[] getCalendarioPago() {
		return calendarioPago;
	}

	public void setCalendarioPago(CalendarioPago[] calendarioPago) {
		this.calendarioPago = calendarioPago;
	}

	public CalendarioPago[] getCalendarioPagoSalida() {
		return calendarioPagoSalida;
	}

	public void setCalendarioPagoSalida(CalendarioPago[] calendarioPagoSalida) {
		this.calendarioPagoSalida = calendarioPagoSalida;
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

	public String getFechaCurse() {
		return fechaCurse;
	}

	public void setFechaCurse(String fechaCurse) {
		this.fechaCurse = fechaCurse;
	}

	public UsuarioModelMB getUsuarioModelMB() {
		return usuarioModelMB;
	}

	public void setUsuarioModelMB(UsuarioModelMB usuarioModelMB) {
		this.usuarioModelMB = usuarioModelMB;
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

	public ServicioAutorizacionyFirmaModelMB getServicioAutorizacionyFirmaModelMB() {
		return servicioAutorizacionyFirmaModelMB;
	}

	public void setServicioAutorizacionyFirmaModelMB(
			ServicioAutorizacionyFirmaModelMB servicioAutorizacionyFirmaModelMB) {
		this.servicioAutorizacionyFirmaModelMB = servicioAutorizacionyFirmaModelMB;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static void setLogger(Logger logger) {
		RenovacionMultilineaViewMB.logger = logger;
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

	public double getIntereses() {
		return intereses;
	}

	public void setIntereses(double intereses) {
		this.intereses = intereses;
	}

	public Date getToday() {
		return Calendar.getInstance().getTime();
	}

	public String getFormatoUf() {
		return FORMATO_UF;
	}

	public String getFormatoPesos() {
		return FORMATO_PESOS;
	}	

   	public ArrayList<String> getExcepciones() {
		return excepciones;
	}

	public void setExcepciones(ArrayList<String> excepciones) {
		this.excepciones = excepciones;
	}

	public boolean isEnviaCorreoBackOffice() {
		return enviaCorreoBackOffice;
	}

	public void setEnviaCorreoBackOffice(boolean enviaCorreoBackOffice) {
		this.enviaCorreoBackOffice = enviaCorreoBackOffice;
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

	public double getGastosNotariales() {
		return gastosNotariales;
	}

	public void setGastosNotariales(double gastosNotariales) {
		this.gastosNotariales = gastosNotariales;
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
	
	public int getLargoComuna() {
		return largoComuna;
	}

	public void setLargoComuna(int largoComuna) {
		this.largoComuna = largoComuna;
	}
	
}