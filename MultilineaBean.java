
package wcorp.bprocess.multilinea;


import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import wcorp.bprocess.cliente.Cliente;
import wcorp.bprocess.cliente.ClienteHome;
import wcorp.bprocess.multilinea.to.DatosAvalesTO;
import wcorp.bprocess.multilinea.to.DatosDisclaimerTO;
import wcorp.bprocess.multilinea.to.DatosOperacionTO;
import wcorp.bprocess.multilinea.to.DatosParaAvanceTO;
import wcorp.bprocess.multilinea.to.DatosParaCondicionesCreditoTO;
import wcorp.bprocess.multilinea.to.DatosParaDashboardTO;
import wcorp.bprocess.multilinea.to.DatosParaOperacionesFirmaTO;
import wcorp.bprocess.multilinea.to.DatosParaRenovacionTO;
import wcorp.bprocess.multilinea.to.DatosPlantillaProductoTO;
import wcorp.bprocess.multilinea.to.DatosResultadoOperacionesTO;
import wcorp.bprocess.multilinea.to.DatosSegurosTO;
import wcorp.bprocess.precioscontextos.PreciosContextos;
import wcorp.bprocess.precioscontextos.PreciosContextosHome;
import wcorp.bprocess.simulacion.InputIngresoRocAmpliada;
import wcorp.bprocess.simulacion.SvcSimulaCursaCredito;
import wcorp.bprocess.simulacion.SvcSimulaCursaCreditoImpl;
import wcorp.bprocess.utlhost.RutinasHost;
import wcorp.bprocess.utlhost.RutinasHostHome;
import wcorp.model.productos.CuentaCorriente;
import wcorp.serv.bciexpress.AutorizaPagoCtaCte;
import wcorp.serv.bciexpress.BCIExpress;
import wcorp.serv.bciexpress.BCIExpressHome;
import wcorp.serv.bciexpress.ResultActualizacionTablaLOG;
import wcorp.serv.bciexpress.ResultAutorizaPagoCuentaCorriente;
import wcorp.serv.bciexpress.ResultConsultaTasaMultilinea;
import wcorp.serv.bciexpress.ResultConsultarApoderadosquehanFirmado;
import wcorp.serv.clientes.RetornoTipCli;
import wcorp.serv.clientes.ServiciosCliente;
import wcorp.serv.colocaciones.PlantillaOperacionesCredito;
import wcorp.serv.colocaciones.ServiciosColocaciones;
import wcorp.serv.colocaciones.ServiciosColocacionesHome;
import wcorp.serv.controlriesgocrediticio.Aval;
import wcorp.serv.controlriesgocrediticio.ControlRiesgoCrediticioLocal;
import wcorp.serv.controlriesgocrediticio.ControlRiesgoCrediticioLocalHome;
import wcorp.serv.controlriesgocrediticio.InputConsultaAvales;
import wcorp.serv.controlriesgocrediticio.InputConsultaCln;
import wcorp.serv.controlriesgocrediticio.Linea;
import wcorp.serv.controlriesgocrediticio.ResultConsultaAvales;
import wcorp.serv.controlriesgocrediticio.ResultConsultaCln;
import wcorp.serv.creditosglobales.CalendarioPago;
import wcorp.serv.creditosglobales.ConsultaOperacionCreditoMultilinea;
import wcorp.serv.creditosglobales.EstructuraVencimiento;
import wcorp.serv.creditosglobales.InputActivacionDeOpcAlDia;
import wcorp.serv.creditosglobales.InputCalculoValoresCancelacion;
import wcorp.serv.creditosglobales.InputCambiaEstadoOperacionCreditoMultilinea;
import wcorp.serv.creditosglobales.InputConsultaOperClienteAmp;
import wcorp.serv.creditosglobales.InputConsultaOperClienteSuperAmp;
import wcorp.serv.creditosglobales.InputConsultaOperacionCredito;
import wcorp.serv.creditosglobales.InputConsultaOperacionCreditoMultilinea;
import wcorp.serv.creditosglobales.InputConsultaOperacionesProrrogadas;
import wcorp.serv.creditosglobales.InputContextualizacionIngrCancelacion;
import wcorp.serv.creditosglobales.InputEliminaOperacionCreditoMultilinea;
import wcorp.serv.creditosglobales.InputIngresoCancelacion;
import wcorp.serv.creditosglobales.InputIngresoDeDlcLlavesYCampos;
import wcorp.serv.creditosglobales.InputIngresoOperacionCreditoMultilinea;
import wcorp.serv.creditosglobales.InputIngresoRoc;
import wcorp.serv.creditosglobales.InputIngresoUnitarioCya;
import wcorp.serv.creditosglobales.InputIngresoUnitarioDeEvc;
import wcorp.serv.creditosglobales.InputIngresoUnitarioDeOperacionDeCreditoOpc;
import wcorp.serv.creditosglobales.InputIngresoUnitarioDeRdc;
import wcorp.serv.creditosglobales.InputIngresoUnitarioDeVen;
import wcorp.serv.creditosglobales.InputIngresoUnitarioIcg;
import wcorp.serv.creditosglobales.InputLiquidacionDeOperacionDeCreditoOpc;
import wcorp.serv.creditosglobales.InputModificaOperacionCreditoMultilinea;
import wcorp.serv.creditosglobales.InputOperaCredito;
import wcorp.serv.creditosglobales.InputSimulacionCredito;
import wcorp.serv.creditosglobales.OperacionCreditoAmp;
import wcorp.serv.creditosglobales.OperacionCreditoSuperAmp;
import wcorp.serv.creditosglobales.ResultActivacionDeOpcAlDia;
import wcorp.serv.creditosglobales.ResultCalculoValoresCancelacion;
import wcorp.serv.creditosglobales.ResultCambiaEstadoOperacionCreditoMultilinea;
import wcorp.serv.creditosglobales.ResultConsultaOperClienteAmp;
import wcorp.serv.creditosglobales.ResultConsultaOperClienteSuperAmp;
import wcorp.serv.creditosglobales.ResultConsultaOperacionCredito;
import wcorp.serv.creditosglobales.ResultConsultaOperacionCreditoMultilinea;
import wcorp.serv.creditosglobales.ResultConsultaOperacionesProrrogadas;
import wcorp.serv.creditosglobales.ResultContextualizacionIngrCancelacion;
import wcorp.serv.creditosglobales.ResultEliminaOperacionCreditoMultilinea;
import wcorp.serv.creditosglobales.ResultIngresoOperacionCreditoMultilinea;
import wcorp.serv.creditosglobales.ResultLiquidacionDeOperacionDeCreditoOpc;
import wcorp.serv.creditosglobales.ResultModificaOperacionCreditoMultilinea;
import wcorp.serv.creditosglobales.ResultSimulacionCredito;
import wcorp.serv.creditosglobales.ServiciosCreditosGlobalesLocal;
import wcorp.serv.creditosglobales.ServiciosCreditosGlobalesLocalHome;
import wcorp.serv.precios.DetalleConsultaSpr;
import wcorp.serv.precios.ResultConsultaCgr;
import wcorp.serv.precios.ResultConsultaSpr;
import wcorp.serv.riesgo.ServiciosRiesgo;
import wcorp.serv.riesgo.ServiciosRiesgoHome;
import wcorp.serv.utlhost.InputConsultaValoresCambio;
import wcorp.serv.utlhost.ResultConsultaValoresCambio;
import wcorp.util.FechasUtil;
import wcorp.util.Feriados;
import wcorp.util.GeneralException;
import wcorp.util.StrUtl;
import wcorp.util.StringUtil;
import wcorp.util.TablaValores;
import wcorp.util.bee.FactorCae;
import wcorp.util.bee.ManejoEvc;
import wcorp.util.bee.MultiEnvironment;
import wcorp.util.bee.UtilitarioCalculoCuota;
import wcorp.util.bee.Utils;
import wcorp.util.com.JNDIConfig;

/**
* <b>EJB DE MULTILINEA</b>
* <p>
* Este EJB contiene todas las acciones de consultas, actualizacion, modificacion,
* eliminacion y todos sus metodos para realizar a  avances, prorrogas y renovacion
* de multilineas.
*
* <p>
* Registro de versiones:<ul>
*
* <li> 1.0  (05/07/2004   Carlos Panozo         BEE ) : version inicial
*
* <li> 1.1  (12/07/2004   Carlos Panozo         BEE ) : +log en metodo consultaCancelacionMultilinea
*
* <li> 1.2  (26/07/2004   Carlos Panozo         BEE ) : Agrega parametro datosLog en (consultaCartolaMultilinea, consultaOperacionesParaRenovar, avanceMultilinea, activaAvanceMultilinea, consultaCancelacionMultilinea, renovacionMultilinea)
*                                                       nuevos metodos registraLogMultilinea, consultaOperacionCredito, wcorp.util.bee.Utils.hackle
*
* <li> 1.3  (28/07/2004   Waldo Iriarte         BEE ) : creeacion  una funcion nueva "obtieneTotalMontoUtilizadoEnMonedaEspecifica" para
*                                                       resolver el tema de la suma de monedas y transformarlas a una moneda indicada.
*                                                       Modificacion rutina "consultaCartolaMultilinea" para que los montos y las
*                                                       glosas mostraran lo solicitado +LOG.
*                                                       Modificacion de metodo obtieneConversionMoneda logica.
*
* <li> 1.4  (24/08/2004   Carlos Panozo         BEE ) : Sin consulta opc (MLT-3-0-0)
*                                                       Inhibe llamada de metodo ResultConsultaOperacionCredito en metodo renovacionMultilinea
*
* <li> 1.5  (15/09/2004   Carlos Panozo         BEE ) : En metodo avanceMultilinea mejora de logica
*                                                       En metodo activaAvanceMultilinea mejora de logica
*                                                       Modificación por cambio visación Rut (MLT-3-0-1)
*                                                       En metodo visacionRut cambio ServiciosRiesgo por RiesgoDelegate
*                                                       En metodo renovacionMultilinea, verificacion de si cliente tiene MLT + control errores
*
* <li> 1.6  (11/11/2004   Carlos Panozo         BEE ) : Correciones Certificación Avales y Mejoras (Cartola y Visación)
*                                                       LogFile por wcorp.util.LogFile
*                                                       En metodos (visacionRut, obtieneTotalMontoUtilizadoEnMonedaEspecifica) +LOG secuencia
*
* <li> 1.7  (30/11/2004   Carlos Panozo         BEE ) : Multilinea Empresas (precios y MCI)
*                                                       Inclusion de import de Precios
*                                                       Metodos de firmas (ingresoOperacionCreditoMultilinea, consultaOperacionCreditoMultilinea,modificaOperacionCreditoMultilinea, eliminaOperacionCreditoMultilinea, consultaTasaEmpresaMultilinea
*                                                       avanceMultilinea cambio por rutinas de Precios
*                                                       Nuevo ingresoDeDlcLlavesYCampos, ingresoUnitarioDeEvc, ingresoUnitarioCya, ingresoUnitarioDeRdc, ingresoUnitarioIcg, ingresoUnitarioDeVen
*                                                       Nuevo liquidacionDeOperacionDeCreditoOpc, ingresoCancelacion, ingresoUnitarioDeOperacionDeCreditoOpc, ingresoRoc, operaCredito, obtieneTasaMultilineaPrecios
*                                                       En metodo renovacionMultilinea nuevo parametro codSegmento
*
* <li> 1.8  (01/12/2004   Carlos Panozo         BEE ) : version link ful Bci SPR y MTI, sin PPC
*                                                       En metodo avanceMultilinea mejora de logica +LOG de secuencia
*                                                       En metodo renovacionMultilinea descMoraSt no va
*                                                       Nuevo metodos cambiaEstadoOperacionCreditoMultilinea, ingresoOperacionCreditoMultilineaporFirmar, autorizaFirmaUsuario, listaTodosFirmantes, existOperacionenFirmas
*
* <li> 1.9  (05/12/2004   Carlos Panozo         BEE ) : Multilinea Empresas (Precios, Firmas y MCI)
*                                                       En metodos (avanceMultilinea,renovacionMultilinea) +LOG secuencia
*                                                       En metodos renovacionMultilinea multiEnvironment.setIndreq() Solo BD Mañana?  --> '0'=No '1'=Si
*                                                       En metodo ingresoOperacionCreditoMultilineaporFirmar mejora logica
*
* <li> 1.10 (17/12/2004   Carlos Panozo         BEE ) : Multilinea Empresas (Precios, Firmas y MCI) toCertBci 2
*                                                       En metodo renovacionMultilinea secuencia de codigo de Morosidad
*
* <li> 1.11 (06/01/2005   Carlos Panozo         BEE ) : Multilinea Empresas (Precios, Firmas y MCI) toCertBci 5
*                                                       En metodo consultaCartolaMultilinea control de montos disponible ante moneda diferentes
*                                                       En metodo avanceMultilinea control de monto permitidos, obtencion de tasas web y sucursal
*                                                       En metodo renovacionMultilinea control de monto permitidos, obtencion de tasas web y sucursal
*                                                       En metodo registraLogMultilinea --> private a public,      agrega codigo "SGT"
*                                                       En metodos (ingresoOperacionCreditoMultilineaporFirmar, listaTodosFirmantes)  +LOG
*
* <li> 1.12 (12/01/2005   Carlos Panozo         BEE ) : Multilinea Empresas (Precios, Firmas y MCI) toCertBci 5
*                                                       En metodo ingresoOperacionCreditoMultilineaporFirmar correcion error
*
* <li> 1.13 (17/01/2005   Carlos Panozo         BEE ) : Multilinea Empresas Hito 2
*                                                       En metodo operaCredito cambio en parametros
*                                                       en metodos (avanceMultilinea, renovacionMultilinea) cambio en manejo de opc_s y can_s(ahora son arreglos)
*
* <li> 1.14 (21/01/2005   Hector Carranza       BEE ) : Inclusion de prorroga
*                                                       Metodos nuevos consultaOperacionesParaProrrogar, consultaCancelacionMultilineaProrroga, prorrogarMultilinea
*
* <li> 1.15 (21/01/2005   Carlos Panozo         BEE ) : Multilinea Empresas Hito 2
*
* <li> 1.16 (27/01/2005   Carlos Panozo         BEE ) : Multilinea Empresas Hito 2
*                                                       En metodos (avanceMultilinea, renovacionMultilinea) cambio en obtencion tasa web
*                                                       En metodo prorrogarMultilinea cambio Obtencion de Tasa Especial WEB (Campaña)
*
* <li> 1.17 (14/04/2005   Carlos Panozo         BEE ) : En metodo consultaCartolaMultilinea mejora codigo
*
* <li> 1.18 (02/05/2005   Hector Carranza       BEE ) : nuevo parametro montoabono, Nuevos metodos Consulta CCC Super Ampliada, Nuevos metdos de Prorrogas
*                                                       En metodo activaAvanceMultilinea mejora en visacion de rut avales
*                                                       En metodo (consultaOperacionesParaRenovar, obtieneTotalMontoUtilizadoEnMonedaEspecifica, consultaOperacionesParaProrrogar) cambia ...CreditoAmp por ...CreditoSuperAmp
*                                                       En metodos ingresoOperacionCreditoMultilinea, modificaOperacionCreditoMultilinea, ingresoOperacionCreditoMultilineaporFirmar, prorrogarMultilinea
*                                                       Nuevos metodos consultaOperClienteSuperAmp, consultaOperClienteSuperAmpAll, consultaOperacionesProrrogadas
*                                                       ResultConsultaOperClienteSuperAmp deja de extender se adaptan metodos de set y get.
*
* <li> 1.19 (02/05/2005   Hector Carranza       BEE ) : cambio en tamaño parametro para log de MLO en metodo registraLogMultilinea
*
* <li> 1.20 (27/05/2005   Hector Carranza       BEE ) : Cambio en registraLogMultilinea en variable descripcionSize vuelve de 512 a 255
*                                                       Razon: versiones sybase desarrollo <> certificacion  (version de certificacion ni porduccion no soporta variables char ni varchar mayores de 255)
*
* <li> 1.21 (22/06/2005   Hector Carranza       BEE ) : se agrega a metodo avanceMultilinea 4 parametros (caiNumOpe, iicNumOpe, tasaPropuesta, bandera, ejecutivo) que vienen de operaciones negociadas
*                                                       Metodo operaCredito ya no corre, ahora se ocupa wcorp.serv.creditosglobales.operaCredito atraves de los nuevos metodos ResultLiquidacionDeOperacionDeCreditoOpc operaCredito y Object operaCredito
*                                                       Se agrega metodo consultaOperacionesLiquidadasPorBciCorp
*
* <li> 1.22 (12/07/2005   Hector Carranza       BEE ) : en metodo consultaOperacionesLiquidadasPorBciCorp se realizan las consultas de todas las operaciones de los
*                                                       cuatro productos principales de Avances Multilinea AVC010,AVC721,AVC326,AVC426
*                                                       (el filtro es debido a que existen, mas AVC???), Para Renovacion y Prorroga solo productos del tipo AVC010, AVC326
*                                                       Log innecesario en metodo consultaOperClienteSuperAmpAll borrado.
* <li> 1.23 (03/08/2005   Carlos Panozo         BEE ) : Cambio Moneda comparacion, dado que montos de la linea estan expresados en moneda de CLF
*
* <li> 1.24 (06/07/2006)  Rodrigo Videla    BEE ) : - Se agrega el soporte la las operaciones COM327-132-009-001 en pesos
*                             y a las operaciones COM399-014-001 en UF
*                           - En método consultaOperacionesParaRenovar mejora código, ahora se toman desde un arreglo de string los cuales a su vez de
*                             la tabla renmultilinea.parametros
* <li>1.25  (23/02/2007 Hector Carranza         Bee ) : se agrega multiEnvironment.setIndreq(5,'1') en metodo avanceMultilinea, renovacionMultilinea, prorrogaMultilinea
*                                                       para que IBM valide en forma optima el ejecutivo asignado. (validacion de ejecutivo inexistente o no vigente).
* <li>1.25  (05/04/2007 Hector Carranza         Bee ) : se corrige multiEnvironment.setIndreq(5,'1') por multiEnvironment.setIndreq(5,'2') por normalizacion de stadard parmetros
*                                                       ya que se trata de un avance de curse de multilinea
* <li>1.26  (31/05/2007 Hector Carranza         Bee ) : Se corrige calculo de parametros en la obtencion de la tasa desde sistema precios en metodo de avance, renovacion y prorroga
* <li>2.0   (31/07/2007   Hector Carranza       BEE ) : Se Agrega metodo depositoEnCuenta con el cual se realizara el deposito del avance solicitado considerando el horario en el cual se realiza.
*                                                       Se Agrega metodo obtenerPlantillasProductos que obtiene las plantillas de producto de credito (POC)
*                                                       Se incluyen librerias y referencias en imports.
*                                                       Se hace publico el metodo obtieneConversionMoneda
* <li>2.1   (30/11/2007 Hector Carranza         Bee ) : correccion en calculo de valores segun mneda destino del credito cuando va a renovar y/o prorrogar
*                                                       Se modifica metodo consultaCartolaMultilinea agregando parametro nuevo y
*                                                       se crea metodo     consultaCartolaMultilinea para mantener compatibilidad con versiones anteriores.
*                                                       Se agrega +Log
* <li>2.2   (24/04/2008 Hector Carranza         Bee ) : En metodo de avanceMultilinea se corrige el manejo de la tasa negociada y es ingresada
*                                                       en estructura ROC's
*                                                       En metodo visacionRut debido a que el servicio de riesgo tiene timeout demasiado extenso lo que ha
*                                                       producido encolamiento de instancias de este bean, el timeout es controlado
*                                                       desde este metodo. +LOG
* <li>2.3   (16/05/2008 Hector Carranza         Bee ) : Se agrega metodo ingresoOperacionCreditoPorFirmar() para almacenar operaciones en proceso de firma
* <li>2.4   (20/07/2008 Hector Carranza         Bee ) : Se corrige ingreso de ROC's teniendo en cuenta la consulta de PPC reordenando arreglo de input en metodo avanceMultilinea en metodo avanceMultilinea().
*                                                       Cambio en rutina de obtencion de operaciones a prorrogar metodo consultaOperacionesParaProrrogar().
*
* <li>2.5   (24/04/2009 Hector Carranza         Bee ) : En metodo de avanceMultilinea() y renovacionMultilinea() se han agregado cosultas para obtener lista de seguros desde MC2 (precios), +import correspondientes
*                                                       ademas se agregaron los metodos verificaPertenenciaBanca(), obtieneDetalleSegurosPrecios(), obtieneSegurosDesdePrecios()
* <li>2.6   (25/07/2009 Hector Carranza         Bee ) : Se controla descripcion de error en consulta de avales del cliente. Se corrige mensaje en Log's
*                                                       Se corrige metodos activaAvanceMultilinea, avanceMultilinea, renovacionMultilinea y prorrogarMultilinea en control de consulta de avales y correcion en obtencion de seguros
*                                                       En metodo prorrogarMultilinea se consultan los seguros de la operacion a prorrogar para mantenerlos en operacion actual.
* <li>2.7   (29/10/2009 Hector Carranza         Bee ) : Normativa Seguros BCI - Tema Declaracion Personal de Salud (DPS)
*                                                       Se agregaron datos en objeto de salida en metodos avanceMultilinea y renovacionMultilinea para informar DPS en seguros e informar rut aval
*
* <li>3.0   (28/04/2015 Braulio Rivas, Manuel Escarate, Eduardo Perez (BEE)) : Se agregan nuevos metodos para simulacion, avance, renovacion 
*                                                       y firma en multilinea, cambio general a log4j. 
* 
*<li>3.1    (21/09/2015 Manuel Escárate (BEE)) -  Jimmy Muñoz (ing. Soft. BCI) : Se sobrecarga método avanceMultilinea agregando parametro condición de garantía, se modifica los siguientes métodos,
*                                                iniciarAvance,obtenerCondicionesCredito,generarDashboard.       
*<li>3.2    (01/02/2016 Hector Carranza (Bee S.A.) - Felipe Ojeda (ing.Soft.BCI) ) : En metodos iniciarAvance y generarDashboard se agrego invocacion reconocer tipo linea a evaluar, se agregan nuevos metodos
*                                                       buscarTipoLinea y definirLDC. </li>
*<li>3.3    (03/03/2016 Manuel Escárate (Bee S.A.) - Felipe Ojeda (ing.Soft.BCI) ) : Se modifican los siguientes métodos iniciarAvance,obtenerCondicionesCredito,ingresarOperacionFirma,activarAvanceMultilinea,
*                                                    obtenerCreditosPorFirmar, generarDashboard,iniciarRenovacion, obtenerCondicionesRenovacion,obtieneFactorCAEPorcentaje,obtenerOperacionesARenovar.
*<li>3.4    (10/06/2015 Manuel Escárate (BEE)) -  Felipe Ojeda (ing. Soft. BCI) : Se Agregan a métodos de avance, renovacion y cartola, el identificador de línea utilizada.
*<li>3.5    (12/06/2016 Manuel Escárate (Bee S.A.) - Pablo Paredes (ing.Soft.BCI)) : Se modifican los siguientes métodos renovacionMultilinea, iniciarAvance,ingresarOperacionFirma,activarAvanceMultilinea,obtenerCondicionesRenovacion,avanceMultilinea. </li>                                               
*<li>3.6    (20/07/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI)) : Se modifican los siguientes métodos obtenerCondicionesCredito,avanceMultilinea,renovacionMultilinea,obtenerCondicionesRenovacion para obtener el valor de gasto notarial.
*                                                                              y se agregan los métodos obtieneDisclaimerMandato,obtenerGlosasPorAval,obtenerTextosParaAvales.</li>
* </ul>
* <P>
* <B>Todos los derechos reservados por Banco de Crédito e Inversiones.</B>
* <P>
*
*/

public class MultilineaBean implements SessionBean {

    /**
     * Define Linea de credito por defecto.
     */
    private static final String LINEA_POR_DEFECTO      =  "MLT";

    /**
     * Define respuesta esperada.
     */
    private static final String RESPUESTA_ESPERADA   =  "S";

    /**
     * Define archivo de parametros a utilizar.
     */
    private static final String ARCHIVO_PARAMETROS   =  "multilinea.parametros";
    
    static private DecimalFormat    form             = new DecimalFormat("000000", new DecimalFormatSymbols());
    static private DecimalFormat    form_int         = new DecimalFormat("#0", new DecimalFormatSymbols());
    static private int              iterator         = 0;

    private static final String     JNDI_NAME_SCG    = "wcorp.serv.creditosglobales.ServiciosCreditosGlobales";
    private static final String     JNDI_NAME_CRC    = "wcorp.serv.controlriesgocrediticio.ControlRiesgoCrediticio";
    private static final String     JNDI_NAME_BEX    = "wcorp.serv.bciexpress.BCIExpress";
    private static final String     JNDI_NAME_RSG    = "wcorp.serv.riesgo.ServiciosRiesgo";
    private static final String     JNDI_NAME_RTN    = "wcorp.bprocess.utlhost.RutinasHost";
    private static final String     JNDI_NAME_PRE    = "wcorp.bprocess.precioscontextos.PreciosContextos";
    private static final String     JNDI_NAME_SCB    = "wcorp.bprocess.cliente.Cliente";
    
    private static final int  CURSE_ACT_MTL = 1;
    
    private static final int  SIM_CUR_CLI  = 2;
    
    private static final int SIM_EJE_COM = 3;
    
	/**
	 * Largo del código de moneda
	 */
	private static final int LARGO_CODIGO_MONEDA =  6;
	
	/**
	 * Valor númerico
	 */
	private static final double VALOR_01 =  0.1;
	
	/**
	 * Valor númerico
	 */
	private static final int VALOR_2 =  2;
	
	/**
	 * Valor númerico
	 */
	private static final int VALOR_3 =  3;
	
	/**
	 * Valor númerico
	 */
	private static final int VALOR_4 =  4;
	
	/**
	 * Valor númerico
	 */
	private static final int VALOR_5 =  5;
	
    /**
	 * Valor númerico
	 */
	private static final int VALOR_6 =  6;
	
	/**
	 * Valor númerico
	 */
	private static final int VALOR_8 =  8;
	
	/**
	 * Valor númerico
	 */
	private static final int VALOR_9 =  9;
	
	/**
	 * Valor númerico
	 */
	private static final int VALOR_12 =  12;
	
	/**
	 * Valor númerico
	 */
	private static final int VALOR_83 =  83;
	
		/**
	 * Valor númerico
	 */
	private static final int VALOR_100 =  100;
	
	/**
	 * Valor númerico
	 */
	private static final int VALOR_1000 =  1000;
	
	/**
	 * Valor númerico
	 */
	private static final int VALOR_60 =  60;
	
	/**
	 * Valor númerico
	 */
	private static final int VALOR_24 =  24;
	
	/**
	 * Atributo Logger.
	 */
	private static transient Logger logger  = (Logger)Logger.getLogger(MultilineaBean.class);
    
    private SessionContext          sessionContext   = null;

    /**
     * Variable para instanciar el ejb ServiciosClienteBean.
     */
    private ServiciosCliente servCli;

    static private SimpleDateFormat ddMMyyyy_form        = new SimpleDateFormat("ddMMyyyy");
    static private final SimpleDateFormat timestamp_form = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //usado para sybase

    static private String controlError = "";

    public void ejbActivate() {
    	if (logger.isDebugEnabled()) logger.debug("ejbActivate called");
    }

    public void ejbRemove() {
    	if (logger.isDebugEnabled()) logger.debug("ejbRemove called");
    }

    public void ejbPassivate() {
    	if (logger.isDebugEnabled()) logger.debug("ejbPassivate called");
    }

    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    public void ejbCreate() throws CreateException {
    	if (logger.isDebugEnabled()) logger.debug("ejbCreate called");
    }

    /**
     * Consulta de Operacion de Credito OPC
     *
     * Registro de versiones:<ul>
     * <li>1.0 26/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param caiOperacion <b>CAI NRO OPERACION</b>
     * @param iicOperacion <b>IIC NRO OPERACION</b>
     * @return {@link ResultConsultaOperacionCredito}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.2
     */
    public ResultConsultaOperacionCredito consultaOperacionCredito(MultiEnvironment multiEnvironment, String caiOperacion, int iicOperacion) throws MultilineaException, EJBException {

        InputConsultaOperacionCredito ibean = new InputConsultaOperacionCredito("029",
                                                                                caiOperacion,
                                                                                iicOperacion);

        return (ResultConsultaOperacionCredito) consultaOperacionCredito(multiEnvironment, ibean, new ResultConsultaOperacionCredito());

    }

    /**
     * Instancia de llaves ( Contextualizacion ) CAN
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param idOperacion <b>NRO operacion cai</b>
     * @param numOperacionCan <b>NRO operacion iic</b>
     * @param numVencimiento <b>Correlativo vencto</b>
     * @return {@link ResultContextualizacionIngrCancelacion}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public ResultContextualizacionIngrCancelacion contextualizacionIngrCancelacion(MultiEnvironment multiEnvironment, String idOperacion, int numOperacionCan, int numVencimiento) throws MultilineaException, EJBException {

        InputContextualizacionIngrCancelacion ibean = new InputContextualizacionIngrCancelacion("138",
                                                                                                idOperacion,
                                                                                                numOperacionCan,
                                                                                                numVencimiento);

        return (ResultContextualizacionIngrCancelacion) contextualizacionIngrCancelacion(multiEnvironment, ibean, new ResultContextualizacionIngrCancelacion());

    }

    /**
     * Confirma ingreso de Cancelacion CAN
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param idOperacion <b>NRO Operacion cai</b>
     * @param numOperacionCan <b>NRO Operacion iic</b>
     * @param numVencimiento <b>Correlativo vencto</b>
     * @param tipoCancelacion <b>Tipo cancelacion</b>
     * @param ejecutivo <b>Cancelado por</b>
     * @param idCanConOperacion <b>CAI CANC CON OPER.</b>
     * @param numCanConOperacion <b>CAI CANC CON OPER.</b>
     * @param fechaCanReal <b>Fecha CAN REAL</b>
     * @param tasaInteresCancel <b>TASA INTERES CANC</b>
     * @param tasaComisionCancelacion <b>VALOR TASA</b>
     * @param comision <b>V COMISION</b>
     * @param oficinaCancel <b>OFICINA CANC</b>
     * @param valorRenovado <b>VALOR  RENOV</b>
     * @param totalPagado <b>VALOR  PAGADO</b>
     * @param tipoCargo <b>TIPO CARGO</b>
     * @param idCuentaCargo <b>CAI CTA CARGO</b>
     * @param numCuentaCargo <b>IIC CTA CARGO</b>
     * @param valorCapital <b>CAPITAL</b>
     * @param valorFinal <b>FINAL</b>
     * @return {@link ResultCalculoValoresCancelacion}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public ResultCalculoValoresCancelacion calculoValoresCancelacion(MultiEnvironment multiEnvironment, String idOperacion, int numOperacionCan, int numVencimiento, String tipoCancelacion, String ejecutivo, String idCanConOperacion, int numCanConOperacion, Date fechaCanReal, Double tasaInteresCancel, Double tasaComisionCancelacion, double comision, String oficinaCancel, double valorRenovado, Double totalPagado, String tipoCargo, String idCuentaCargo, int numCuentaCargo, double valorCapital, double valorFinal) throws MultilineaException, EJBException {

        InputCalculoValoresCancelacion ibean = new InputCalculoValoresCancelacion("238",
                                                                                  idOperacion,
                                                                                  numOperacionCan,
                                                                                  numVencimiento,
                                                                                  tipoCancelacion,
                                                                                  ejecutivo,
                                                                                  idCanConOperacion,
                                                                                  numCanConOperacion,
                                                                                  fechaCanReal,
                                                                                  tasaInteresCancel,
                                                                                  tasaComisionCancelacion,
                                                                                  comision,
                                                                                  oficinaCancel,
                                                                                  valorRenovado,
                                                                                  totalPagado,
                                                                                  tipoCargo,
                                                                                  idCuentaCargo,
                                                                                  numCuentaCargo,
                                                                                  valorCapital,
                                                                                  valorFinal);

        return (ResultCalculoValoresCancelacion) calculoValoresCancelacion(multiEnvironment, ibean, new ResultCalculoValoresCancelacion());

    }

    /**
     * Ingreso de Operacion de Credito cal Center
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param tipoOperacion <b>T OPERAC</b>
     * @param caiOperacion <b>CAI NRO OPERACION</b>
     * @param iicOperacion <b>IIC NRO OPERACION</b>
     * @param idCancelacion <b>CAI NRO CANC.</b>
     * @param numCancelacion <b>CAI Nro Canc.</b>
     * @param correlativo <b>NUMERO CANCELACION</b>
     * @param moneda <b>COD MONEDA CRED</b>
     * @param codigoAuxiliar <b>C AUX</b>
     * @param oficinaIngreso <b>OFICINA</b>
     * @param rutDeudor <b>NUM Idc Deudor</b>
     * @param digitoVerificador <b>VRF IDC DEUDOR</b>
     * @param indicadorExtIdc <b>IND IDC DEUDOR</b>
     * @param glosaExtIdc <b>GLS IDC DEUDOR</b>
     * @param numeroDireccion <b>DIRECCIÓN ASOCIADA</b>
     * @param montoCredito <b>MONTO CREDITO</b>
     * @param valorRenovado2 <b>Valor Renovado</b>
     * @param codigoSegDesgrav <b>COD SEG DESGRAV</b>
     * @param primaSegDesgrav <b>V SEG.DESGRAV.</b>
     * @param valorSegCesantia <b>V SEG. CESANTIA</b>
     * @param fechaCurse <b>F CURSE O COLOC.</b>
     * @param valorOtroSeguro <b>VALOR SEGURO</b>
     * @param tasaSprea <b>INTERES O SPRED</b>
     * @param canalContacto <b>CANAL VENTA</b>
     * @param tasaComisionCurse <b>VALOR TASA</b>
     * @param tasaComisionCancelacion2 <b>tasa Com canc</b>
     * @param condicionGar <b>CONDIC GTIAS</b>
     * @param comision2 <b>Valor Comision</b>
     * @param calculoValorFinal <b>CALC VAL FIN</b>
     * @param estructuraVenc <b>VCTOS ESTRUCT ?</b>
     * @param valorGasto <b>GASTOS</b>
     * @param analisisFeriado <b>ANAL. FERIADOS ?</b>
     * @param ejecutivo2 <b>Ejec. Comercial</b>
     * @param insistencia <b>INSISTENCIA</b>
     * @param abono <b>TIPO ABONO</b>
     * @param cargo <b>Tipo cargo</b>
     * @param ctaAbono <b>CAI CTA ABONO</b>
     * @param ctaAbonoTer <b>IIC CTA ABONO</b>
     * @param destinoCredito <b>COD DESTINO CRED</b>
     * @param ctaCargo <b>CAI CTA CARGO</b>
     * @param pinCtaCargo <b>IIC CTA CARGO</b>
     * @param rutAval <b>IdC Deudor</b>
     * @param digitoVerifAval <b>IDC DEUDOR</b>
     * @param indicExtIdc <b>IDC DEUDOR</b>
     * @param glosaCliente <b>IDC DEUDOR</b>
     * @param nroDireccion <b>NRO DIR DEU INDIR</b>
     * @param valorDocumento <b>VALOR DOCUMENTO</b>
     * @param indClasificRiesgo <b>CLASIFIC. RIESGO</b>
     * @param tipoDocum <b>TIPO  DOCUMENTO</b>
     * @param impuestos <b>VALOR IMPUESTOS $</b>
     * @param plazaCobro <b>PLAZA DE COBRO</b>
     * @param codNotaria <b>CODIGO NOTARIA</b>
     * @param gastosNotario <b>GASTOS NOTARIA $</b>
     * @param interesEspecial <b>INTS PERIODOS ESP</b>
     * @param situacionCartera <b>SITUACION CARTERA</b>
     * @param situacionContableLdc <b>SITUACION CONTABLE</b>
     * @param situacionCobranza <b>SITUAC. COBRANZA</b>
     * @param indicadorNP01 <b>Indicador NP01 mes no pago</b>
     * @param indicadorNP02 <b>Indicador NP02 mes no pago</b>
     * @param indicadorNP03 <b>Indicador NP03 mes no pago</b>
     * @param indicadorNP04 <b>Indicador NP04 mes no pago</b>
     * @param vencimientos {@link EstructuraVencimiento}
     * @return {@link ResultSimulacionCredito}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */

    public ResultSimulacionCredito simulacionCredito(MultiEnvironment multiEnvironment, String tipoOperacion, String caiOperacion, int iicOperacion, String idCancelacion, int numCancelacion, int correlativo, String moneda, String codigoAuxiliar, String oficinaIngreso, int rutDeudor, char digitoVerificador, char indicadorExtIdc, String glosaExtIdc, int numeroDireccion, double montoCredito, double valorRenovado2, String codigoSegDesgrav, double primaSegDesgrav, double valorSegCesantia, Date fechaCurse, double valorOtroSeguro, double tasaSprea, char canalContacto, double tasaComisionCurse, double tasaComisionCancelacion2, String condicionGar, double comision2, char calculoValorFinal, char estructuraVenc, double valorGasto, char analisisFeriado, String ejecutivo2, char insistencia, String abono, String cargo, String ctaAbono, int ctaAbonoTer, String destinoCredito, String ctaCargo, int pinCtaCargo, int rutAval, char digitoVerifAval, char indicExtIdc, String glosaCliente, int nroDireccion, double valorDocumento, String indClasificRiesgo, String tipoDocum, double impuestos, String plazaCobro, String codNotaria, double gastosNotario, char interesEspecial, String situacionCartera, String situacionContableLdc, String situacionCobranza, int indicadorNP01, int indicadorNP02, int indicadorNP03, int indicadorNP04, EstructuraVencimiento[] vencimientos) throws MultilineaException, EJBException {

        InputSimulacionCredito ibean = new InputSimulacionCredito("327",
                                                                  tipoOperacion,
                                                                  caiOperacion,
                                                                  iicOperacion,
                                                                  idCancelacion,
                                                                  numCancelacion,
                                                                  correlativo,
                                                                  moneda,
                                                                  codigoAuxiliar,
                                                                  oficinaIngreso,
                                                                  rutDeudor,
                                                                  digitoVerificador,
                                                                  indicadorExtIdc,
                                                                  glosaExtIdc,
                                                                  numeroDireccion,
                                                                  montoCredito,
                                                                  valorRenovado2,
                                                                  codigoSegDesgrav,
                                                                  primaSegDesgrav,
                                                                  valorSegCesantia,
                                                                  fechaCurse,
                                                                  valorOtroSeguro,
                                                                  tasaSprea,
                                                                  canalContacto,
                                                                  tasaComisionCurse,
                                                                  tasaComisionCancelacion2,
                                                                  condicionGar,
                                                                  comision2,
                                                                  calculoValorFinal,
                                                                  estructuraVenc,
                                                                  valorGasto,
                                                                  analisisFeriado,
                                                                  ejecutivo2,
                                                                  insistencia,
                                                                  abono,
                                                                  cargo,
                                                                  ctaAbono,
                                                                  ctaAbonoTer,
                                                                  destinoCredito,
                                                                  ctaCargo,
                                                                  pinCtaCargo,
                                                                  rutAval,
                                                                  digitoVerifAval,
                                                                  indicExtIdc,
                                                                  glosaCliente,
                                                                  nroDireccion,
                                                                  valorDocumento,
                                                                  indClasificRiesgo,
                                                                  tipoDocum,
                                                                  impuestos,
                                                                  plazaCobro,
                                                                  codNotaria,
                                                                  gastosNotario,
                                                                  interesEspecial,
                                                                  situacionCartera,
                                                                  situacionContableLdc,
                                                                  situacionCobranza,
                                                                  indicadorNP01,
                                                                  indicadorNP02,
                                                                  indicadorNP03,
                                                                  indicadorNP04,
                                                                  vencimientos);

        return (ResultSimulacionCredito) simulacionCredito(multiEnvironment, ibean, new ResultSimulacionCredito());

    }

    /**
     * Activacion de OPC al dia
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param caiOperacion <b>CAI NRO OPERACION</b>
     * @param iicOperacion <b>IIC NRO OPERACION</b>
     * @return {@link ResultActivacionDeOpcAlDia}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public ResultActivacionDeOpcAlDia activacionDeOpcAlDia(MultiEnvironment multiEnvironment, String caiOperacion, int iicOperacion) throws MultilineaException, EJBException {

        InputActivacionDeOpcAlDia ibean = new InputActivacionDeOpcAlDia("033",
                                                                        caiOperacion,
                                                                        iicOperacion);

        return (ResultActivacionDeOpcAlDia) activacionDeOpcAlDia(multiEnvironment, ibean, new ResultActivacionDeOpcAlDia());

    }

    /**
     * CCC Consulta Operaci x Cliente Ampliada
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param nombreTitular <b>NOMBRE / R.SOCIAL</b>
     * @param rutDeudor <b>IDC DEUDOR</b>
     * @param digitoVerificador <b>IDC DEUDOR</b>
     * @param indicadorExtIdc <b>IdC Cliente</b>
     * @param glosaExtIdc <b>IdC Cliente</b>
     * @param tipoOperacion <b>TIPO DE OPERACION</b>
     * @param codigoAuxiliar <b>CODIGO AUXILIAR</b>
     * @param moneda <b>MONEDA</b>
     * @param tipoDeudor <b>TIPO DEUDOR</b>
     * @param codEstadoCredito <b>ESTADO</b>
     * @param fechaInicio <b>FECHA INICIO</b>
     * @param fechaTermino <b>FECHA TERMINO</b>
     * @param numOperacion <b>Nro operacion cai</b>
     * @param numOperacionIIC <b>Nro operacion iic</b>
     * @return {@link ResultConsultaOperClienteAmp}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */

    public ResultConsultaOperClienteAmp consultaOperClienteAmp(MultiEnvironment multiEnvironment, String nombreTitular, int rutDeudor, char digitoVerificador, char indicadorExtIdc, String glosaExtIdc, String tipoOperacion, String codigoAuxiliar, String moneda, char tipoDeudor, char codEstadoCredito, Date fechaInicio, Date fechaTermino, String numOperacion, int numOperacionIIC) throws MultilineaException, EJBException {

        InputConsultaOperClienteAmp ibean = new InputConsultaOperClienteAmp("177",
                                                                            nombreTitular,
                                                                            rutDeudor,
                                                                            digitoVerificador,
                                                                            indicadorExtIdc,
                                                                            glosaExtIdc,
                                                                            tipoOperacion,
                                                                            codigoAuxiliar,
                                                                            moneda,
                                                                            tipoDeudor,
                                                                            codEstadoCredito,
                                                                            fechaInicio,
                                                                            fechaTermino,
                                                                            numOperacion,
                                                                            numOperacionIIC);

        return (ResultConsultaOperClienteAmp) consultaOperClienteAmp(multiEnvironment, ibean, new ResultConsultaOperClienteAmp());

    }

    /**
     * Ingreso de Operacion de Credito cal Center
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public Object ingresoRenovacion(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled()) logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled()) logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled()) logger.debug("ingresoRenovacion");

            return servicioscreditosglobales_bean.ingresoRenovacion(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     *  Consulta de Operacion de Credito OPC
     *
     * Registro de versiones:<ul>
     * <li>1.0 26/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.2
     */
    public Object consultaOperacionCredito(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled()) logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled())  logger.debug("consultaOperacionCredito");

            return servicioscreditosglobales_bean.consultaOperacionCredito(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     *  Instancia de llaves ( Contextualizacion ) CAN
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public Object contextualizacionIngrCancelacion(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled()) logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled()) logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled()) logger.debug("contextualizacionIngrCancelacion");

            return servicioscreditosglobales_bean.contextualizacionIngrCancelacion(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     *  Confirma ingreso de Cancelacion CAN
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public Object calculoValoresCancelacion(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled()) logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled()) logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled()) logger.debug("calculoValoresCancelacion");

            return servicioscreditosglobales_bean.calculoValoresCancelacion(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     *  Ingreso de Operacion de Credito cal Center
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public Object simulacionCredito(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled()) logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled()) logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled()) logger.debug("simulacionCredito");

            return servicioscreditosglobales_bean.simulacionCredito(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     *  Activacion de OPC al dia
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public Object activacionDeOpcAlDia(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled()) logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled()) logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled()) logger.debug("activacionDeOpcAlDia");

            return servicioscreditosglobales_bean.activacionDeOpcAlDia(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     *   CCC Consulta Operaci x Cliente Ampliada
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public Object consultaOperClienteAmp(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled()) logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled()) logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled()) logger.debug("consultaOperClienteAmp");

            return servicioscreditosglobales_bean.consultaOperClienteAmp(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Ingreso de DLC (llaves y campos)
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object ingresoDeDlcLlavesYCampos(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled()) logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled()) logger.debug("ingresoDeDlcLlavesYCampos");

            return servicioscreditosglobales_bean.ingresoDeDlcLlavesYCampos(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Ingreso Unitario de EVC
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object ingresoUnitarioDeEvc(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled()) logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled()) logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled())  logger.debug("ingresoUnitarioDeEvc");

            return servicioscreditosglobales_bean.ingresoUnitarioDeEvc(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Ingreso unitario CYA
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object ingresoUnitarioCya(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled()) logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled()) logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled()) logger.debug("ingresoUnitarioCya");

            return servicioscreditosglobales_bean.ingresoUnitarioCya(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Ingreso unitario de RDC
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object ingresoUnitarioDeRdc(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled()) logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled()) logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled()) logger.debug("ingresoUnitarioDeRdc");

            return servicioscreditosglobales_bean.ingresoUnitarioDeRdc(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Ingreso unitario ICG
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object ingresoUnitarioIcg(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled()) logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled()) logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled()) logger.debug("ingresoUnitarioIcg");

            return servicioscreditosglobales_bean.ingresoUnitarioIcg(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Ingreso unitario de VEN
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object ingresoUnitarioDeVen(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled()) logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled()) logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled()) logger.debug("ingresoUnitarioDeVen");

            return servicioscreditosglobales_bean.ingresoUnitarioDeVen(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Liquidacion de Operacion de Credito OPC
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object liquidacionDeOperacionDeCreditoOpc(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

        	if (logger.isDebugEnabled()) logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled())  logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled())  logger.debug("liquidacionDeOperacionDeCreditoOpc");

            return servicioscreditosglobales_bean.liquidacionDeOperacionDeCreditoOpc(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Ingreso de Cancelación CAN
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object ingresoCancelacion(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled())  logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled())  logger.debug("ingresoCancelacion");

            return servicioscreditosglobales_bean.ingresoCancelacion(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Ingreso Unitario de Operacion de Credito OPC
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object ingresoUnitarioDeOperacionDeCreditoOpc(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled())  logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled())  logger.debug("ingresoUnitarioDeOperacionDeCreditoOpc");

            return servicioscreditosglobales_bean.ingresoUnitarioDeOperacionDeCreditoOpc(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * INGRESO ROC
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object ingresoRoc(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled())  logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled())  logger.debug("ingresoRoc");

            return servicioscreditosglobales_bean.ingresoRoc(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * CCC Consulta Operaci x Cliente Ampliada
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param nombreTitular <b>NOMBRE / R.SOCIAL</b>
     * @param rutDeudor <b>IDC DEUDOR</b>
     * @param digitoVerificador <b>IDC DEUDOR</b>
     * @param indicadorExtIdc <b>IdC Cliente</b>
     * @param glosaExtIdc <b>IdC Cliente</b>
     * @param tipoOperacion <b>TIPO DE OPERACION</b>
     * @param codigoAuxiliar <b>CODIGO AUXILIAR</b>
     * @param moneda <b>MONEDA</b>
     * @param tipoDeudor <b>TIPO DEUDOR</b>
     * @param codEstadoCredito <b>ESTADO</b>
     * @param fechaInicio <b>FECHA INICIO</b>
     * @param fechaTermino <b>FECHA TERMINO</b>
     * @return {@link ResultConsultaOperClienteAmp}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public ResultConsultaOperClienteAmp consultaOperClienteAmp(MultiEnvironment multiEnvironment, String nombreTitular, int rutDeudor, char digitoVerificador, char indicadorExtIdc, String glosaExtIdc, String tipoOperacion, String codigoAuxiliar, String moneda, char tipoDeudor, char codEstadoCredito, Date fechaInicio, Date fechaTermino) throws MultilineaException, EJBException {

        Vector vect = new Vector();

        if (logger.isDebugEnabled())  logger.debug("consultaOperClienteAmp()");

        InputConsultaOperClienteAmp ibean = new InputConsultaOperClienteAmp("177",
                                                                            nombreTitular,
                                                                            rutDeudor,
                                                                            digitoVerificador,
                                                                            indicadorExtIdc,
                                                                            glosaExtIdc,
                                                                            tipoOperacion,
                                                                            codigoAuxiliar,
                                                                            moneda,
                                                                            tipoDeudor,
                                                                            codEstadoCredito,
                                                                            fechaInicio,
                                                                            fechaTermino,
                                                                            "",
                                                                            0);

        ResultConsultaOperClienteAmp obean = new ResultConsultaOperClienteAmp();

        if (logger.isDebugEnabled())  logger.debug("antes de consultaOperClienteAmp(ibean, obean)");

        consultaOperClienteAmp(multiEnvironment, ibean, obean);

        OperacionCreditoAmp[] operacionesAmp = obean.getOperacionesAmp();

        if (logger.isDebugEnabled())  logger.debug("operacionesAmp.length [" + operacionesAmp.length + "]");

        for (int i = 0; i < operacionesAmp.length; i++) {

            if (logger.isDebugEnabled())  logger.debug("vect.add(operacionesAmp[" + Integer.toString(i) + "])");

            vect.add(operacionesAmp[i]);
        }

        ResultConsultaOperClienteAmp aux  = new ResultConsultaOperClienteAmp();

        aux.setIndicador(obean.getIndicador());

        while (aux.getIndicador() == 'S') {

            ibean.setNumOperacion(operacionesAmp[operacionesAmp.length - 1].getIdOperacion());
            ibean.setNumOperacionIIC(operacionesAmp[operacionesAmp.length - 1].getNumOperacion());

            if (logger.isDebugEnabled())  logger.debug("antes de consultaOperClienteAmp(ibean, aux)");

            consultaOperClienteAmp(multiEnvironment, ibean, aux);

            operacionesAmp = aux.getOperacionesAmp();

            if (logger.isDebugEnabled())  logger.debug("operacionesAmp.length [" + operacionesAmp.length + "]");

            for (int i = 0; i < operacionesAmp.length; i++) {

                if (logger.isDebugEnabled())  logger.debug("vect.add(operacionesAmp[" + Integer.toString(i) + "])");

                vect.add(operacionesAmp[i]);
            }

            if (logger.isDebugEnabled())  logger.debug("aux.getIndicador() [" + aux.getIndicador() + "]");
        }

        if (logger.isDebugEnabled())  logger.debug("obean.setOperacionesAmp(): vect.size() : " + Integer.toString(vect.size()) + "");

        obean.setOperacionesAmp((OperacionCreditoAmp[]) vect.toArray(new OperacionCreditoAmp[0]));

        return obean;
    }


   /**
    * Opera Credito
    *
    * <p>
    * Observacion: Transformación de llamado de argumentos extendido a método de argumentos minimo. Cambio para llamado a ServiciosCreditos Globales
    *
    * <p>
    *
    * Registro de versiones:<ul>
    * <li>1.0 02/06/2005 Carlos Panozo   (Bee)- versión inicial
    *
    * </ul>
    * <p>
    *
    * @param multiEnvironment
    * @param can_s                                         : arreglo de cancelaciones
    * @param opc_s                                         : arreglo de operaciones
    * @param rdc_s                                         : arreglo de relaciones de clientes
    * @param dlc_s                                         : arreglo de documentos legales
    * @param cya_s                                         : arreglo de cargos y abonos
    * @param evc_s                                         : arreglo de estructuras de vencimientos
    * @param icg_s                                         : arreglo de tasas de interes
    * @param ven_s                                         : arreglo de vencimientos
    * @param roc_s                                         : arreglo de relacion operacion-contexto
    * @param liq_opc                                       : liquidacion
    *
    * @return {@link ResultLiquidacionDeOperacionDeCreditoOpc}
    *
    * @exception MultilineaException
    * @exception EJBException
    *
    * @since 1.21
    */
   public ResultLiquidacionDeOperacionDeCreditoOpc operaCredito(MultiEnvironment multiEnvironment, InputIngresoCancelacion[] can_s, InputIngresoUnitarioDeOperacionDeCreditoOpc[] opc_s, InputIngresoUnitarioDeRdc[] rdc_s, InputIngresoDeDlcLlavesYCampos[] dlc_s, InputIngresoUnitarioCya[] cya_s, InputIngresoUnitarioDeEvc[] evc_s, InputIngresoUnitarioIcg[] icg_s, InputIngresoUnitarioDeVen[] ven_s, InputIngresoRoc[] roc_s, InputLiquidacionDeOperacionDeCreditoOpc liq_opc) throws MultilineaException, EJBException {

       InputOperaCredito ibean = new InputOperaCredito(can_s, opc_s, rdc_s, dlc_s, cya_s, evc_s, icg_s, ven_s, roc_s, liq_opc);
       return (ResultLiquidacionDeOperacionDeCreditoOpc) operaCredito(multiEnvironment, ibean, new ResultLiquidacionDeOperacionDeCreditoOpc());

   }

   /**
    * Opera Credito multiples ocurrencias
    *
    * Registro de versiones:<ul>
    * <li>1.0 02/06/2005 Carlos Panozo   (Bee)- versión inicial
    *
    * </ul>
    * <p>
    *
    * @param multiEnvironment
    * @param ibean
    * @param obean
    *
    * @return {@link Object}
    *
    * @exception MultilineaException
    * @exception EJBException
    *
    * @since 1.21
    */
   public Object operaCredito(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

       try {

           if (logger.isDebugEnabled())  logger.debug("getting InitialContext()");

           InitialContext ic = JNDIConfig.getInitialContext();

           if (logger.isDebugEnabled())  logger.debug("getting ServiciosCreditosGlobalesLocalHome");

           ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

           if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

           ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

           if (logger.isDebugEnabled())  logger.debug("operaCredito");

           return servicioscreditosglobales_bean.operaCredito(multiEnvironment, ibean, obean);

       } catch (GeneralException e) {

    	   if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

           throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

       } catch (Exception e) {

    	   if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

           throw new MultilineaException("UNKNOW", e.toString());
       }
   }

    /**
     * Consulta Cartola de Multilinea
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- versión inicial
     * <li>1.1 26/07/2004 Carlos Panozo   (Bee)- Se Agrega parametro datosLog
     * <li>1.2 28/07/2004 Waldo Iriarte   (Bee)- modifica Log
     * <li>1.3 06/01/2005 Carlos Panozo   (Bee)- control monto disponible, calculo cuando monedas diferentes
     * <li>1.4 14/04/2005 Carlos Panozo   (Bee)- mejora de codigo
     * <li>1.5 02/05/2005 Hector Carranza (Bee)- ...ClienteAmp por ...ClienteSuperAmp
     * <li>1.6 03/08/2005 Carlos Panozo   (Bee)- Cambio Moneda comparacion, dado que los montos de la linea estan expresados en moneda de CLF
     * <li>1.7 30/11/2007 Hector Carranza (Bee)- Se agrega nuevo parametro a metodo para controlar el registro de log en tabla MLO
     * <li>1.8 28/04/2015 Eduardo Pérez   (Bee)- Se agrega nuevo parametro a metodo para controlar el tipo de operación a consultar
     * <li>1.9 12/05/2016 Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI) - se agrega atributo dentro del flujo para identificar tipo de línea.</li>
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param rutDeudor
     * @param digitoVerificador
     * @param datosLog
     * @param grabaLog
     * @return {@link ResultCartolaMultilinea}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public ResultCartolaMultilinea consultaCartolaMultilinea(MultiEnvironment multiEnvironment, int rutDeudor, char digitoVerificador, Hashtable datosLog, boolean grabaLog, String tipoOperacion) throws MultilineaException, EJBException {

        try {
             if (logger.isDebugEnabled())  logger.debug("consultaCartolaMultilinea 1.7");
             boolean flag_exception   = false;
             Date   today             = new Date();
             String fechaHoy          = ddMMyyyy_form.format(today);
             double montoTransformado = 0;

             ResultConsultaValoresCambio obeanCambio = new ResultConsultaValoresCambio();

             ResultCartolaMultilinea     obean       = new ResultCartolaMultilinea();

             InputConsultaOperClienteSuperAmp ibean = new InputConsultaOperClienteSuperAmp();

             ibean.setRutDeudor(rutDeudor);
             ibean.setDigitoVerificador(digitoVerificador);
             ibean.setTipoOperacion(tipoOperacion);
             ibean.setCodEstadoCredito('A');
             ibean.setTipoDeudor('D');

             ResultConsultaOperClienteSuperAmp obean_cccsa = new ResultConsultaOperClienteSuperAmp();

             try {
             obean.setResultConsultaOperClienteSuperAmp((ResultConsultaOperClienteSuperAmp)consultaOperClienteSuperAmpAll(multiEnvironment, ibean, obean_cccsa));
             } catch (Exception e) {
                 //si no hay datos o sale algun error debe mostrar a lo menos los datos de LDC
            	 if (logger.isEnabledFor(Level.ERROR)) logger.error("obean.setResultConsultaOperClienteSuperAmp...Exception [" + Utils.hackle(e.getMessage()) + "]");
                 flag_exception = true;     //indica que mas adelante no haga calculos pues la consulta no tiene data.
             }

             InputConsultaCln icln     = new InputConsultaCln();
             ResultConsultaCln res_cln = new ResultConsultaCln();
             icln.setRutDeudor(rutDeudor);
             icln.setDigitoVerificador(digitoVerificador);
             consultaCln(multiEnvironment, icln, res_cln);

             String sim_mon  =   null;

             obean.setIdOperacionClf(res_cln.getIdOperacion());
             if (logger.isDebugEnabled())  logger.debug("Clf:res_cln.getIdOperacion()                  : "     + res_cln.getIdOperacion());

             obean.setNumOperacionClf(res_cln.getNumOperacion());
             if (logger.isDebugEnabled())  logger.debug("Clf:res_cln.getNumOperacion()                 : "    + res_cln.getNumOperacion());


             //Debiera utilizarse una tabla de cod-descripcion corta, y de no existir, retornar glosa moneda
             if (logger.isDebugEnabled())  logger.debug("Clf:res_cln.getMoneda()                       : "          + res_cln.getMoneda());
             obean.setGlosaMonedaClf(res_cln.getMoneda());
             if (logger.isDebugEnabled())  logger.debug("Clf:res_cln.getCodigoMoneda()                 : "          + res_cln.getCodigoMoneda());
             obean.setCodigoMonedaClf(res_cln.getCodigoMoneda());


             sim_mon = null;

             /******************************************************************************/
             /* Variable que contiene el tipode moneda en que se desea mostrar los valores */
             /******************************************************************************/
             String monedaAConvertir = "0999";

            if (logger.isDebugEnabled())  logger.debug("moneda CLF      : "          + res_cln.getCodigoMoneda());
            if (logger.isDebugEnabled())  logger.debug("moneda Objetivo : "          + monedaAConvertir);

            if (res_cln.getCodigoMoneda().trim().equals(monedaAConvertir)) {
                if (logger.isDebugEnabled())  logger.debug("Son iguales las monedas");
                if (logger.isDebugEnabled())  logger.debug("Clf:res_cln.getMontoDisponible()              : " + res_cln.getMontoDisponible());
                obean.setMontoDisponibleClf(res_cln.getMontoDisponible());

             } else {
                if (logger.isDebugEnabled())  logger.debug("Son distintas");

                 if (res_cln.getCodigoMoneda().trim().equals("0999")) {

                     if (logger.isDebugEnabled())  logger.debug("Original     res_cln.getMontoDisponible()      :" + res_cln.getMontoDisponible());
                     obeanCambio        = obtieneConversionMoneda("UFP", fechaHoy, fechaHoy, "/", res_cln.getMontoDisponible());
                     montoTransformado  = obeanCambio.getTotalCalculado();

                     obean.setMontoDisponibleClf(montoTransformado);

                     if (logger.isDebugEnabled())  logger.debug("Transformada obean.setMontoDisponibleClf()               :" + montoTransformado);

                     obean.setGlosaMonedaClf("UF");
                     if (logger.isDebugEnabled())  logger.debug("obean.setGlosaMonedaClf()                           :" + obean.getGlosaMonedaClf());

                 } else if (res_cln.getCodigoMoneda().trim().equals("0998")){

                    if (logger.isDebugEnabled())  logger.debug("Original     res_cln.getMontoDisponible()      :" + res_cln.getMontoDisponible());
                     obeanCambio        = obtieneConversionMoneda("UFP", fechaHoy, fechaHoy, "*", res_cln.getMontoDisponible());
                     montoTransformado  = obeanCambio.getTotalCalculado();

                     obean.setMontoDisponibleClf(montoTransformado);
                     if (logger.isDebugEnabled())  logger.debug("Transformada obean.setMontoDisponibleClf()               :" + montoTransformado);

                     obean.setGlosaMonedaClf("$");
                     if (logger.isDebugEnabled())  logger.debug("obean.setGlosaMonedaClf()                           :" + obean.getGlosaMonedaClf());

                 } else {

                     if (logger.isDebugEnabled())  logger.debug("Moneda (" + sim_mon + ") distinta de $ y UF");

                    obean.setMontoDisponibleClf(res_cln.getMontoDisponible());
                    if (logger.isDebugEnabled())  logger.debug("Transformada obean.setMontoDisponibleClf()               :" + res_cln.getMontoDisponible());

                    obean.setGlosaMonedaClf(sim_mon);
                    if (logger.isDebugEnabled())  logger.debug("obean.setGlosaMonedaClf()                           :" + obean.getGlosaMonedaClf());
                 }

             }


             Linea[] lineas = res_cln.getLineas();

             obean.setCupoMaximoMlt(0);
             obean.setMontoDisponibleMlt(0);
             obean.setCorrelativoLineaCreditoMlt(0);

             boolean tiene_mlt = false;

             for (int i = 0 ; i < lineas.length ; i++) {

                 if (lineas[i] == null) {
                     break;
                 }

                 // L291203 CPH: obtención de código de linea MLT
                 if (logger.isDebugEnabled()){
	                 logger.debug("lineas[" + i + "].getCodigoTipoInfo()                  : " + lineas[i].getCodigoTipoInfo());
	                 logger.debug("lineas[" + i + "].getCorrelativoLineaCredito()         : " + lineas[i].getCorrelativoLineaCredito());
	                 logger.debug("lineas[" + i + "].getIndVigencia()                     : " + lineas[i].getIndVigencia());
	                 logger.debug("lineas[" + i + "].getCodigoTipoLinea()                 : " + lineas[i].getCodigoTipoLinea());
                 }
                 
                 String tipoLineaOcupada = definirLDC(multiEnvironment, lineas);
                 if (lineas[i].getCodigoTipoLinea().trim().equals(tipoLineaOcupada)) {
                     tiene_mlt = true;

                     // L291203 CPH: obtencion de codigo de moneda
                     sim_mon = lineas[i].getCodMonedaLinea();

                     obean.setCodigoMonedaMlt(sim_mon);

                     if (logger.isDebugEnabled())  {
	                   	 logger.debug("lineas[" + i + "].getCodMonedaLinea()                  : " + lineas[i].getCodMonedaLinea());
	                     logger.debug("lineas[" + i + "].getMoneda()                          : " + lineas[i].getMoneda());
	                     logger.debug("*********************************************************");
	                     logger.debug("res_cln.getCodigoMoneda() : " + sim_mon);
	                     logger.debug("*********************************************************");
                     }
                    if (res_cln.getCodigoMoneda().trim().equals(monedaAConvertir)) {


                        if (logger.isDebugEnabled())  logger.debug("Son iguales las monedas");

                         obean.setCupoMaximoMlt(lineas[i].getCupoMaximo());
                         if (logger.isDebugEnabled())  logger.debug("lineas[" + i + "].getCupoMaximo()                      :" + lineas[i].getCupoMaximo());

                         obean.setMontoDisponibleMlt(lineas[i].getMontoDisponible4());
                         if (logger.isDebugEnabled())  logger.debug("lineas[" + i + "].getMontoDisponible4()                :" + lineas[i].getMontoDisponible4());

                         if (monedaAConvertir.trim().equals("0999")) {
                             obean.setGlosaMonedaMlt("$");
                         } else if (monedaAConvertir.trim().equals("0998")){
                             obean.setGlosaMonedaMlt("UF");
                         } else {
                             obean.setGlosaMonedaMlt(lineas[i].getMoneda());
                         }
                         if (logger.isDebugEnabled())  logger.debug("obean.getGlosaMonedaMlt()                           :" + obean.getGlosaMonedaMlt());
                    }
                    else {

                        if (logger.isDebugEnabled())  logger.debug("Son distintas");

                         if (res_cln.getCodigoMoneda().trim().equals("0999")) {

                             if (logger.isDebugEnabled())  logger.debug("Original     lineas[" + i + "].getCupoMaximo()      :" + lineas[i].getCupoMaximo());
                             obeanCambio        = obtieneConversionMoneda("UFP", fechaHoy, fechaHoy, "/", lineas[i].getCupoMaximo());
                             montoTransformado  = obeanCambio.getTotalCalculado();

                             obean.setCupoMaximoMlt(montoTransformado);
                             if (logger.isDebugEnabled())  logger.debug("Transformada obean.getCupoMaximoMlt()               :" + montoTransformado);

                             if (logger.isDebugEnabled())  logger.debug("Original     lineas[" + i + "].getMontoDisponible4():" + lineas[i].getMontoDisponible4());
                             obeanCambio        = obtieneConversionMoneda("UFP", fechaHoy, fechaHoy, "/", lineas[i].getMontoDisponible4());
                             montoTransformado  = obeanCambio.getTotalCalculado();

                             obean.setMontoDisponibleMlt(montoTransformado);
                             if (logger.isDebugEnabled())  logger.debug("Transformada obean.getMontoDisponibleMlt()          :" + montoTransformado);

                             obean.setGlosaMonedaMlt("UF");
                             if (logger.isDebugEnabled())  logger.debug("obean.getGlosaMonedaMlt()                           :" + obean.getGlosaMonedaMlt());

                         } else if (res_cln.getCodigoMoneda().trim().equals("0998")){

                             if (logger.isDebugEnabled())  logger.debug("Original     lineas[" + i + "].getCupoMaximo()      :" + lineas[i].getCupoMaximo());
                             obeanCambio        = obtieneConversionMoneda("UFP", fechaHoy, fechaHoy, "*", lineas[i].getCupoMaximo());
                             montoTransformado  = obeanCambio.getTotalCalculado();

                             obean.setCupoMaximoMlt(montoTransformado);
                             if (logger.isDebugEnabled())  logger.debug("Transformada obean.getCupoMaximoMlt()               :" + montoTransformado);

                             if (logger.isDebugEnabled())  logger.debug("Original     lineas[" + i + "].getMontoDisponible4():" + lineas[i].getMontoDisponible4());
                             obeanCambio        = obtieneConversionMoneda("UFP", fechaHoy, fechaHoy, "*", lineas[i].getMontoDisponible4());
                             montoTransformado  = obeanCambio.getTotalCalculado();

                             obean.setMontoDisponibleMlt(montoTransformado);
                             if (logger.isDebugEnabled())  logger.debug("Transformada obean.getMontoDisponibleMlt()          :" + montoTransformado);

                             obean.setGlosaMonedaMlt("$");
                             if (logger.isDebugEnabled())  logger.debug("obean.getGlosaMonedaMlt()                           :" + obean.getGlosaMonedaMlt());

                         } else {

                             if (logger.isDebugEnabled())  logger.debug("Moneda (" + sim_mon + ") distinta de $ y UF");

                             obean.setCupoMaximoMlt(lineas[i].getCupoMaximo());
                             if (logger.isDebugEnabled())  logger.debug("lineas[" + i + "].getCupoMaximo()                   :" + lineas[i].getCupoMaximo());

                             obean.setMontoDisponibleMlt(lineas[i].getMontoDisponible4());
                             if (logger.isDebugEnabled())  logger.debug("lineas[" + i + "].getMontoDisponible4()             :" + lineas[i].getMontoDisponible4());

                             obean.setGlosaMonedaMlt(lineas[i].getMoneda());
                             if (logger.isDebugEnabled())  logger.debug("obean.getGlosaMonedaMlt()                           :" + obean.getGlosaMonedaMlt());
                         }
                    }

                     obean.setCorrelativoLineaCreditoMlt(lineas[i].getCorrelativoLineaCredito());
                     if (logger.isDebugEnabled())  logger.debug("lineas[" + i + "].getCorrelativoLineaCredito()         :" + lineas[i].getCorrelativoLineaCredito());

                     break;
                 }
             }

            if (!tiene_mlt) {
                if (logger.isDebugEnabled())  logger.debug("Cliente " + rutDeudor + "-" + digitoVerificador + " No posee Multilinea (MLT)");
                throw new MultilineaException("Especial","Cliente No posee Multilinea (MLT)");
            }

             if (flag_exception){
                 if (logger.isDebugEnabled())  logger.debug("sumar operaciones de obean agrupados por moneda es OMITIDO");
             }else{
             //sumar operaciones de obean agrupados por moneda
             obean.setMontoUtilizadoMLT(obtieneTotalMontoUtilizadoEnMonedaEspecifica(obean, monedaAConvertir));
             }
             return (obean);

         } catch (Exception e) {
        	 if (logger.isEnabledFor(Level.ERROR)) logger.error("ERROR***consultaCartolaMultilinea...Exception [" + e.getMessage() + "]");
             if (grabaLog){
                 if (datosLog!=null) {
                    datosLog.put("tipoOpe", "CCM");
                    datosLog.put("ERR",datosLog.get("ERR")==null? Utils.hackle(e.getMessage()): Utils.hackle(e.getMessage()) + datosLog.get("ERR"));
                    registraLogMultilinea(datosLog);
                 }
                 else {
                	 if (logger.isEnabledFor(Level.ERROR)) logger.error("datosLog es null");
                 }
             }
             throw new MultilineaException("ESPECIAL", e.getMessage());
         }

    }

    /**
     * Consulta Operaciones para Renovar
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- version inicial
     * <li>1.1 26/07/2004 Carlos Panozo   (Bee)- datosLog es agregado en parametros
     * <li>1.2 02/05/2005 Hector Carranza (Bee)- ...ClienteAmp por ...ClienteSuperAmp
     * <li>1.3 06/07/2006 Rodrigo Videla  (Bee)- Se agrega el soporte la las operaciones COM327-132-009-001 en pesos
     *                       y a las operaciones COM399-014-001 en UF
     *                         - Se optimiza el manejo de las operaciones a renovar
     *                           a traves de un arreglo de String que contiene las operaciones a renovar
     * </ul>
     * <p>
     *
     * @param multiEnvironment      :   Variable de ambiente.
     * @param rutDeudor         :   Rut del Deudos.
     * @param digitoVerificador     :   Digito verificador del Rut del Deudor.
     * @param datosLog
     * @return {@link Vector}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public Vector consultaOperacionesParaRenovar(MultiEnvironment multiEnvironment, int rutDeudor, char digitoVerificador, Hashtable datosLog) throws MultilineaException, EJBException {

        try {
            Vector vResult = new Vector();
            String[] creditosARenovar = null;
            InputConsultaOperClienteSuperAmp ibean = new InputConsultaOperClienteSuperAmp();
            ResultConsultaOperClienteSuperAmp  obean;
            OperacionCreditoSuperAmp[] ope;

            ibean.setRutDeudor(rutDeudor);
            if (logger.isDebugEnabled())  logger.debug("rutDeudor=" + rutDeudor);

            ibean.setDigitoVerificador(digitoVerificador);
            if (logger.isDebugEnabled())  logger.debug("digitoVerificador=" + digitoVerificador);

            ibean.setCodEstadoCredito('A');
            ibean.setTipoDeudor('D');

            if (logger.isDebugEnabled())  logger.debug("Antes de Obtener el arregle de String de OperacionesARenovar");
            creditosARenovar = (String[])datosLog.get("operacionesARenovar");
            if (logger.isDebugEnabled())  logger.debug("Despues de Obtener el arregle de String de OperacionesARenovar");

            for(int i = 0; i < creditosARenovar.length; i ++) {
                String moneda = creditosARenovar[i].substring(0, 4);
                String tio = creditosARenovar[i].substring(6, 9);
                String aux = creditosARenovar[i].substring(9, creditosARenovar[i].length());
                if (logger.isDebugEnabled())  logger.debug("Tipo de Operacion a Renovar : " + creditosARenovar[i]);

                ibean.setMoneda(moneda);
                ibean.setTipoOperacion(tio);
                ibean.setCodigoAuxiliar(aux);

                obean = (ResultConsultaOperClienteSuperAmp) consultaOperClienteSuperAmpAll(multiEnvironment, ibean, new ResultConsultaOperClienteSuperAmp());
                ope = obean.getOperacionesSuperAmp();

                for (int ind=0; ind < ope.length; ind++) {
                    if (ope[ind] == null) {
                        if (logger.isDebugEnabled())  logger.debug("break[" + ind + "]");
                    break;
                    } else {
                        vResult.addElement(ope[ind]);
                        if (logger.isDebugEnabled())  logger.debug(aux + "-"+ ind +"[" + ind + "]");
                    }
                }
            }

            return vResult;

        } catch (Exception e) {
        	if (logger.isEnabledFor(Level.ERROR)) logger.error("ERROR***consultaOperacionesParaRenovar...Exception [" + e.getMessage() + "]");
             if (datosLog!=null) {
                datosLog.put("tipoOpe", "COR");
                datosLog.put("ERR",datosLog.get("ERR")==null?e.getMessage():e.getMessage() + datosLog.get("ERR"));
                registraLogMultilinea(datosLog);
             }
             else {
            	 if (logger.isEnabledFor(Level.ERROR)) logger.error("datosLog es null");
             }
             throw new MultilineaException("ESPECIAL", e.getMessage());
         }

    }


    /**
     * Avances de Multilinea
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- version inicial
     * <li>1.1 15/09/2004 Carlos Panozo   (Bee)- mejora logica control error
     * <li>1.2 01/12/2004 Carlos Panozo   (Bee)- mejora de logica +LOG de secuencia
     * <li>1.3 05/12/2004 Carlos Panozo   (Bee)- mejora de logica +LOG de secuencia
     * <li>1.4 06/01/2005 Carlos Panozo   (Bee)- control de monto permitidos, obtencion de tasas web y sucursal
     * <li>1.5 17/01/2005 Carlos Panozo   (Bee)- cambio en manejo de opc_s y can_s(ahora son arreglos)
     * <li>1.6 27/01/2005 Carlos Panozo   (Bee)- cambio en obtencion de tasa web
     * <li>1.7 22/06/2005 Hector Carranza (Bee)- se agregan parametros de entrada  (caiNumOpe numero operacion cai, iicNumOpe numero operacion iic, tasaPropuesta, bandera indicador de accion, ejecutivo)
     *                                           bandera indica logica de accion en el metodo
     *                                           Correcion observacion en variable condicionGar (condicion de garantia) de acuerdo a variable tiene_cual (no tiene ni aval ni fogape)
     * <li>1.8 23/02/2007 Hector Carranza (Bee)- se agrega multiEnvironment.setIndreq(5,'1') para que el host valide en forma optima el ejecutivo asignado. (validacion de ejecutivo inexistente)
     * <li>1.9 05/04/2007 Hector Carranza (bee)- se corrige multiEnvironment.setIndreq(5,'1') por multiEnvironment.setIndreq(5,'2') por normalizacion de stadard parmetros
     * <li>2.0 31/05/2007 Hector Carranza (Bee)- Se corrige calculo de parametros en la obtencion de la tasa desde sistema precios, siendo calculo en 'D'ias.
     * <li>2.1 31/07/2007 Hector Carranza (Bee)- Se inhibe throw new MultilineaException() cuando cliente No posee Multilinea para que igual pueda simular.
     * <li>2.2 24/04/2008 Hector Carranza (Bee)- Se corrige el tratamiento de la tasa negociada, en donde ademas se debe tener el o los registros desde todas
     *                                           las ROC's y anexar una nueva mas con la tasa negociada. +LOG
     * <li>2.3 20/07/2008 Hector Carranza (Bee)- Se corrige codigo de ingreso de ROC's teniendo en cuenta la consulta de PPC reordenando arreglo de input para la simulacion del credito.
     * <li>2.4 24/04/2009 Hector Carranza (Bee)- Se agregado toda la logica para el manejo de los seguros asociados a la multilinea + parametro seguros en mmetodo
     * <li>2.5 29/10/2009 Hector Carranza (Bee)- Se agrega datos en salida de rocampliada para contexto de avales y seguros DPS
     * <li>2.6 21/09/2015 Manuel Escárate (BEE)) -  Jimmy Muñoz (ing. Soft. BCI): Se sobreescribe método y se lleva lógica.</li>
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param tipoOperacion
     * @param moneda
     * @param codigoAuxiliar
     * @param oficinaIngreso
     * @param rutDeudor
     * @param digitoVerificador
     * @param montoCredito
     * @param abono
     * @param cargo
     * @param ctaAbono cuenta
     * @param ctaCargo cuenta
     * @param indicadorNP01 mes no pago
     * @param indicadorNP02 mes no pago
     * @param vencimientos numero vctos
     * @param codBanca Banca
     * @param plan
     * @param datosLog log MLO
     * @param codSegmento Codigo Segmento
     * @param caiNumOpe numero operacion cai
     * @param iicNumOpe numero operacion iic
     * @param tasaPropuesta
     * @param bandera   indicador de accion
     * @param ejecutivo
     * @param lista de seguros
     *
     * @return {@link ResultAvanceMultilinea}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public ResultAvanceMultilinea avanceMultilinea(MultiEnvironment multiEnvironment, String tipoOperacion, String moneda, String codigoAuxiliar, String oficinaIngreso, int rutDeudor, char digitoVerificador, double montoCredito, String abono, String cargo, int ctaAbono, int ctaCargo, int indicadorNP01, int indicadorNP02, EstructuraVencimiento[] vencimientos, String codBanca, char plan, Hashtable datosLog, String codSegmento, String caiNumOpe, int iicNumOpe, double tasaPropuesta, String bandera, String ejecutivo, String[] seguros) throws MultilineaException, EJBException {
    	return avanceMultilinea(multiEnvironment,tipoOperacion,moneda,codigoAuxiliar,oficinaIngreso,rutDeudor,digitoVerificador,montoCredito,abono,cargo,ctaAbono, ctaCargo, indicadorNP01, indicadorNP02,vencimientos, codBanca, plan, datosLog,codSegmento,caiNumOpe,iicNumOpe,tasaPropuesta,bandera,ejecutivo,seguros, null);
    }

    /**
     * Obtiene tasa de multilinea
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- version inicial
     * <li>1.1 28/07/2004 Waldo Iriarte   (Bee)- cambio en parametros consultaTasaMultilinea
     *
     * </ul>
     * <p>
     *
     * @param codBanca
     * @param rut
     * @param plan
     * @param moneda
     * @param plazoMeses
     * @param montoCredito
     * @param descuento
     * @param plazoDias
     * @param totalVencimientos
     * @return {@link ResultConsultaTasaMultilinea}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    private ResultConsultaTasaMultilinea obtieneTasaMultilinea(String codBanca, String rut, char plan, String moneda, int plazoMeses, int montoCredito, String descuento, int plazoDias, int totalVencimientos) throws MultilineaException, EJBException {

        try {

            BCIExpress     bciexpressBean = null;
            InitialContext ic             = JNDIConfig.getInitialContext();
            Object         obj_exp        = ic.lookup(JNDI_NAME_BEX);
            BCIExpressHome home_exp       = (BCIExpressHome) PortableRemoteObject.narrow(obj_exp, BCIExpressHome.class);

            if (logger.isDebugEnabled())  logger.debug("BCIExpressBean creado");

            bciexpressBean = (BCIExpress) PortableRemoteObject.narrow(home_exp.create(), BCIExpress.class);

            if (logger.isDebugEnabled())  logger.debug("obtieneTasaMultilinea");

            return bciexpressBean.consultaTasaMultilinea(codBanca, rut, plan, moneda, plazoMeses, montoCredito, descuento, plazoDias, totalVencimientos);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.getMessage());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.getMessage());

            throw new MultilineaException("UNKNOW", e.getMessage());
        }

    }

    /**
     * Obtiene tasa de multilinea de Precios
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- version inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param codEvento
     * @param codConcepto
     * @param fecProceso
     * @param numCliente
     * @param vrfCliente
     * @param codTipoCanal
     * @param codCanal
     * @param codMoneda
     * @param codTipoOperacion
     * @param codAuxiliar
     * @param numPlazoOperacion
     * @param indPlazoOperacion
     * @param valMontoOperacion
     * @param vctoOperacion
     * @param codClfRentabilidad
     * @param codFactorRiesgo
     * @param indTipoPago
     * @param codigoSegmento
     * @param caiOperacion
     * @param iicOperacion
     * @return {@link ResultConsultaSpr}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    private ResultConsultaSpr obtieneTasaMultilineaPrecios(MultiEnvironment multiEnvironment, String codEvento, String codConcepto, Date fecProceso, int numCliente, char vrfCliente, String codTipoCanal, String codCanal, String codMoneda, String codTipoOperacion, String codAuxiliar, int numPlazoOperacion, char indPlazoOperacion, double valMontoOperacion, double vctoOperacion,  String codClfRentabilidad, String codFactorRiesgo, String indTipoPago, String codigoSegmento, String caiOperacion, int iicOperacion) throws MultilineaException, EJBException {

        try {

            PreciosContextos     precioscontextosBean = null;
            InitialContext ic                         = JNDIConfig.getInitialContext();
            Object         obj_pre                    = ic.lookup(JNDI_NAME_PRE);
            PreciosContextosHome home_pre             = (PreciosContextosHome) PortableRemoteObject.narrow(obj_pre, PreciosContextosHome.class);

            if (logger.isDebugEnabled())  logger.debug("precioscontextosBean creado");

            precioscontextosBean = (PreciosContextos) PortableRemoteObject.narrow(home_pre.create(), PreciosContextos.class);

            if (logger.isDebugEnabled())  logger.debug("obtieneTasaMultilineaPrecios");
            String codTipoConsulta = "CON";
            String extOperacion ="";
            char indCliente = ' ';
            String glsCliente ="";
            Date fecVctoInicial = null;
            double valSaldoOperacion = 0D;
            double valInteOperacion = 0D;
            double valMontoAdicional = 0D;
            String caiOperacionRcc = "";

            return precioscontextosBean.casoDeUsoConsultaSpr(multiEnvironment,
                                                             codEvento,
                                                             codConcepto,
                                                             fecProceso,
                                                             codTipoConsulta,
                                                             caiOperacion,
                                                             iicOperacion,
                                                             extOperacion,
                                                             numCliente,
                                                             vrfCliente,
                                                             indCliente,
                                                             glsCliente,
                                                             codTipoCanal,
                                                             codCanal,
                                                             codMoneda,
                                                             codTipoOperacion,
                                                             codAuxiliar,
                                                             numPlazoOperacion,
                                                             indPlazoOperacion,
                                                             fecVctoInicial,
                                                             valMontoOperacion,
                                                             valSaldoOperacion,
                                                             vctoOperacion,
                                                             valInteOperacion,
                                                             valMontoAdicional,
                                                             codClfRentabilidad,
                                                             codFactorRiesgo,
                                                             indTipoPago,
                                                             caiOperacionRcc,
                                                             codigoSegmento);


        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.getMessage());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.getMessage());

            throw new MultilineaException("UNKNOW", e.getMessage());
        }

    }

    /**
     * Activa Avance de Multilinea
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo    (Bee)- version inicial
     * <li>1.1 26/07/2004 Carlos Panozo    (Bee)- cambio en parametros de entrada (+datosLog)
     * <li>1.2 28/07/2004 Waldo Iriarte    (Bee)- visacion de avales, log en MLO
     * <li>1.3 15/09/2004 Carlos Panozo    (Bee)- mejora en logica control errores
     * <li>1.4 02/05/2005 Hector Carranza  (Bee)- mejora en visacion de rut avales

     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param idOperacion  numero operacion cai
     * @param numOperacion numero operacion iic
     * @param rutEmpresa
     * @param dvEmpresa
     * @param condicionGarantia
     * @param whoVisa   cuantos avales visan
     * @param datosLog
     * @return {@link ResultActivacionDeOpcAlDia}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public ResultActivacionDeOpcAlDia activaAvanceMultilinea(MultiEnvironment multiEnvironment, String idOperacion, int numOperacion, String rutEmpresa, String dvEmpresa, String condicionGarantia, int whoVisa, Hashtable datosLog) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("inicio activaAvanceMultilinea");

            //  whoVisa = -1  no visa  a nadie  //
            //  whoVisa >= 0  visa empresa e indica cuantos avales visar  //

            boolean tiene_avales = condicionGarantia.equals("2  ") ? true : false;
            boolean visa_empresa = whoVisa >= 0 ? true : false;
            boolean visa_avales  = whoVisa >  0 ? true : false;
            if (logger.isDebugEnabled())  logger.debug("visa_empresa [" + (visa_empresa ? "SI" : "NO") + "]");
            if (logger.isDebugEnabled())  logger.debug("visa_avales  [" + (visa_avales  ? "SI(" + whoVisa + ")" : "NO") + "]");

            String respuestaVisacion = null;

            if (visa_empresa){
                if (logger.isDebugEnabled())  logger.debug("visando Empresa [" + rutEmpresa + "-" + dvEmpresa + "]");
                    respuestaVisacion = visacionRut(rutEmpresa, dvEmpresa.charAt(0), multiEnvironment.getCanal(), "EMP");
                    if ((respuestaVisacion == null) || (respuestaVisacion != null && !respuestaVisacion.trim().equals("AP"))){
                        datosLog.put("ERR","Error Visacion (" + respuestaVisacion + ") para " + rutEmpresa + "-" + dvEmpresa.charAt(0));
                        if (logger.isDebugEnabled())  logger.debug("Visacion NOK:" + respuestaVisacion);
                        throw new MultilineaException("ESPECIAL", "ERRVISACION"+respuestaVisacion);
                    }
                if (logger.isDebugEnabled())  logger.debug("visacion Empresa [" + rutEmpresa + "-" + dvEmpresa + "]    OK !!!");
            }

            if (visa_avales){
                if (logger.isDebugEnabled())  logger.debug("visa_avales [true]");
                if (tiene_avales){
                    if (logger.isDebugEnabled())  logger.debug("tiene_avales [true]");
                    controlError = "ERRAVCAVL";
                    //Consulta de avales si es que existe condicion de garantia = aval
                        //luego se deben visar los whovisa primeros ...

                    ResultConsultaAvales obeanAval = new ResultConsultaAvales();

                    InputConsultaAvales abean = new InputConsultaAvales("026",
                                                                        Integer.parseInt(rutEmpresa),   //      int rutDeudor,
                                                                        dvEmpresa.charAt(0),            //      char digitoVerificaCli,
                                                                        ' ',                            //      idCliente,
                                                                        " ",                            //      glosaCliente,
                                                                        " ",                            //      nombreCli,
                                                                        " ",                            //      idOperacion,
                                                                        0,                              //      numOperacion,
                                                                        0,                              //      numCorrelativo,
                                                                        "AVL",                          //      tipoOperacion SOLO AVALES,
                                                                        "AVC");                         //      tipoCredito ASOCIADO A MULTILINEA);

                        consultaAvales(multiEnvironment, abean, obeanAval);

                    if (logger.isDebugEnabled())  logger.debug("despues consultaAvales");

                    Aval[] avales    = obeanAval.getAvales();

                    if (logger.isDebugEnabled())  logger.debug("avales.length ["+ avales.length +"]");

                        int visados = 0;

                    for (int i = 0; i < avales.length; i++) {

                            if (visados >= whoVisa)
                            break;

                        String rutAval = Integer.toString(avales[i].getRutAval());
                        char   dvfAval = avales[i].getDigitoVerificaAval();
                            char   indvige = avales[i].getVigente();
                        if (logger.isDebugEnabled())  logger.debug("Visando aval ["+ rutAval +"] - ["+dvfAval+"]");
                            if (logger.isDebugEnabled())  logger.debug("esta vigente el rut "+ rutAval +" ? ["+ indvige +"]");
                        controlError = "";
                            if (indvige == 'S'){
                        respuestaVisacion = visacionRut(rutAval, dvfAval, multiEnvironment.getCanal(), "EMP");
                        if ((respuestaVisacion == null) || (respuestaVisacion != null && !respuestaVisacion.trim().equals("AP"))){
                                    datosLog.put("ERR","Error Visacion (" + respuestaVisacion + ") para " + rutAval + "-" + dvfAval);
                                if (logger.isDebugEnabled())  logger.debug("Visacion NOK:" + respuestaVisacion);
                                throw new MultilineaException("ESPECIAL", "ERRVISACION"+respuestaVisacion);
                            }

                            visados++;
                    }
                        }
                        if (logger.isDebugEnabled())  logger.debug("cuantos_visa ["+ visados +"]");
                }
                if (logger.isDebugEnabled())  logger.debug("visacion de avales OK");
             }

            controlError = "";
            if (logger.isDebugEnabled())  logger.debug("antes activacionDeOpcAlDia");
            ResultActivacionDeOpcAlDia obean = new ResultActivacionDeOpcAlDia();

            InputActivacionDeOpcAlDia ibean = new InputActivacionDeOpcAlDia("033",
                                                                            idOperacion,
                                                                            numOperacion);
            activacionDeOpcAlDia(multiEnvironment, ibean, obean);

            if (datosLog != null) {
                datosLog.put("tipoOpe", "OK");
                datosLog.put("MSG", obean.getCim_respuesta());
                registraLogMultilinea(datosLog);
            }

            if (logger.isDebugEnabled())  logger.debug("activacionDeOpcAlDia OK (" + idOperacion + numOperacion + ")");

            return obean;

        }catch(Exception e) {
        	if (logger.isEnabledFor(Level.ERROR)) logger.error("ERROR***activaAvanceMultilinea...Exception [" + e.getMessage() + "]");
            if (datosLog!=null) {
                datosLog.put("tipoOpe", "CUR");
                datosLog.put("ERR",datosLog.get("ERR")==null ? (!controlError.equals("") ? controlError + " " : "") + Utils.hackle(e.getMessage()) : (!controlError.equals("") ? controlError + " " : "") + Utils.hackle(e.getMessage()) + datosLog.get("ERR"));
                registraLogMultilinea(datosLog);
            }
            else {
            	if (logger.isEnabledFor(Level.ERROR)) logger.error("datosLog es null");
            }
            throw new MultilineaException("ESPECIAL", (!controlError.equals("") ? controlError + " " : "") + e.getMessage());
        }

    }

    /**
     * Visacion de Rut de Avales
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- version inicial
     * <li>1.1 26/07/2004 Carlos Panozo   (Bee)- cambio en parametros entrada, log de secuencia
     * <li>1.2 15/09/2004 Carlos Panozo   (Bee)- ServiciosRiesgo por RiesgoDelegate
     * <li>1.3 11/11/2004 Carlos Panozo   (Bee)- +LOG secuencia
     * <li>1.4 24/04/2008 Hector Carranza (Bee)- Debido a que el servicio de riesgo tiene timeout demasiado extenso lo que ha
     *                                           producido encolamiento de instancias de este bean, el timeout es controlado
     *                                           desde este metodo. +LOG
     *
     * </ul>
     * <p>
     *
     * @param rutVisar  Rut aval
     * @param dvfVisar  Rut dv aval
     * @param CanalID   Canal
     * @param origen
     * @return {@link String}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    private String visacionRut(String rutVisar, char dvfVisar, String CanalID, String origen) throws MultilineaException, EJBException {

        if (logger.isDebugEnabled())  logger.debug("visacionRut inicio");

        String visacion = null;

        try {
            if (logger.isDebugEnabled())  logger.debug("Instanciando srvRiesgoBean");
            ServiciosRiesgo     srvRiesgoBean  = null;
            InitialContext      ic             = JNDIConfig.getInitialContext();
            Object              obj_riesgo     = ic.lookup(JNDI_NAME_RSG);
            ServiciosRiesgoHome home_riesgo    = (ServiciosRiesgoHome) PortableRemoteObject.narrow(obj_riesgo, ServiciosRiesgoHome.class);

            srvRiesgoBean = (ServiciosRiesgo) PortableRemoteObject.narrow(home_riesgo.create(), ServiciosRiesgo.class);

            if (logger.isDebugEnabled()){  
	            logger.debug("srvRiesgoBean instanciado");
	            logger.debug("visacionRut ---------");
	            logger.debug("    Rut ("+ rutVisar+")");
	            logger.debug("    dv ("+ dvfVisar+")");
	            logger.debug("    canal ("+ CanalID+")");
	            logger.debug("    origen ("+ origen+")");
            }
            long tiempoOk    = 0;
            long iniLlamada  = 0;
            long finLlamada  = 0;
            long tiempoFuera = 0;

            try{
                tiempoOk= Long.parseLong(TablaValores.getValor("multilinea.parametros", "tiempovisacion", "desc"));
            }catch (Exception e){
            	if (logger.isEnabledFor(Level.ERROR)) logger.error("tiempovisacion no encontrado en multilinea.parametros");
            }
            if (logger.isDebugEnabled())  logger.debug("tiempoOk   ("+ tiempoOk   +")");

            if( tiempoOk > 0){
                iniLlamada = (new Date()).getTime();
                if (logger.isDebugEnabled())  logger.debug("iniLlamada ("+ iniLlamada +") RUT[" + rutVisar +"]");
            }

            visacion = (String) srvRiesgoBean.visarRutEmpresario(Long.parseLong(rutVisar.trim()), dvfVisar, CanalID);

            if( tiempoOk > 0){
                finLlamada = (new Date()).getTime();
                if (logger.isDebugEnabled())  logger.debug("finLlamada ("+ finLlamada +") RUT[" + rutVisar +"]");
                tiempoFuera = (finLlamada - iniLlamada)/1000;
                if (logger.isDebugEnabled())  logger.debug("tiempoFuera("+ tiempoFuera +") RUT[" + rutVisar +"]");
                if (tiempoFuera > tiempoOk){
                    if (logger.isDebugEnabled())  logger.debug("Tiempo de visacion fue demasiado RUT[" + rutVisar +"]");
                    throw new MultilineaException("ESPECIAL", "ERRVISACION:CONTROL");
                }
            }
        }catch (Exception e){
        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Error: " + e);
            throw new MultilineaException("ESPECIAL", e.getMessage());
        }
        if (logger.isDebugEnabled())  logger.debug("visacionRut fin retorno [" + visacion + "] RUT[" + rutVisar +"]");

        return visacion;

    }

    /**
     * Consulta Cancelacion de Multilinea
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- version inicial
     * <li>1.1 26/07/2004 Carlos Panozo   (Bee)- agrega datosLog
     * <li>1.2 06/07/2006 Rodrigo Videla  (Bee)- Se agrega secuencia de log para depuracion
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param idOperacion           Numero operacion cai
     * @param numOperacion          Numero operacion iic
     * @param moneda
     * @param oficinaCancel
     * @param datosLog
     * @return {@link ResultCalculoValoresCancelacion}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public ResultCalculoValoresCancelacion consultaCancelacionMultilinea(MultiEnvironment multiEnvironment, String idOperacion, int numOperacion, String moneda, String oficinaCancel, Hashtable datosLog) throws MultilineaException, EJBException {

        try {
            if (logger.isDebugEnabled())  logger.debug("Antes de Contextualizar Cancelación Multilinea 250604");
            InputContextualizacionIngrCancelacion   input_ctxCan = new  InputContextualizacionIngrCancelacion();
            input_ctxCan.setIdOperacion(idOperacion);
            input_ctxCan.setNumOperacionCan(numOperacion);
            ResultContextualizacionIngrCancelacion output_ctxCan = new ResultContextualizacionIngrCancelacion();
            contextualizacionIngrCancelacion(multiEnvironment, input_ctxCan, output_ctxCan);

            if (logger.isDebugEnabled()){  
	            logger.debug("Antes de Calculo valores Cancelación Multilinea");
	            logger.debug("output_ctxCan.getNumVencimiento()="    + output_ctxCan.getNumVencimiento());
	            logger.debug("output_ctxCan.getEjecutivo()="         + output_ctxCan.getEjecutivo());
	            logger.debug("TasaSpread="                           + Double.valueOf(String.valueOf(output_ctxCan.getTasaSpread())));
	            logger.debug("oficinaCancel="                        + oficinaCancel);
	            logger.debug("idOperacion="                          + idOperacion);
	            logger.debug("numOperacion="                         + numOperacion);
            }
            Date   today            = new Date();
            String fechaHoy         = ddMMyyyy_form.format(today);
            Date   fechaVencimiento = output_ctxCan.getFecVencimiento();
            Double tasaCancelacion  = null;

            if (logger.isDebugEnabled())  logger.debug("fechaHoy="         + fechaHoy);
            if (logger.isDebugEnabled())  logger.debug("fechaVencimiento=" + ddMMyyyy_form.format(fechaVencimiento));

            if (new SimpleDateFormat("yyyyMMdd").format(fechaVencimiento).compareTo(new SimpleDateFormat("yyyyMMdd").format(today))>0) {
                tasaCancelacion=new Double(output_ctxCan.getTasaSpread());
            };
            if (logger.isDebugEnabled())  logger.debug("tasaCancelacion=" + tasaCancelacion);

            ResultCalculoValoresCancelacion output_valCan  = new ResultCalculoValoresCancelacion();
            InputCalculoValoresCancelacion input_valCan    = new InputCalculoValoresCancelacion();
            input_valCan.setIdOperacion(idOperacion);
            input_valCan.setNumOperacionCan(numOperacion);
            input_valCan.setNumVencimiento(output_ctxCan.getNumVencimiento());
            input_valCan.setTipoCancelacion("RTT");
            input_valCan.setEjecutivo(output_ctxCan.getEjecutivo());
            input_valCan.setTasaInteresCancel(tasaCancelacion);
            input_valCan.setOficinaCancel(oficinaCancel);
            calculoValoresCancelacion(multiEnvironment, input_valCan, output_valCan);

            if (logger.isDebugEnabled()){  
	            logger.debug("output_valCan.getValorRenovado()="           + output_valCan.getValorRenovado());
	            logger.debug("output_valCan.getFechaCanComputacional()="   + output_valCan.getFechaCanComputacional());
	            logger.debug("output_valCan.getFechaCanReal()="            + output_valCan.getFechaCanReal());
	            logger.debug("output_valCan.getFechaCanContable()="        + output_valCan.getFechaCanContable());
	            logger.debug("output_valCan.getTasaInteresCancel()="       + output_valCan.getTasaInteresCancel());
	            logger.debug("output_valCan.getTasaComisionCancelacion()=" + output_valCan.getTasaComisionCancelacion());
	            logger.debug("output_valCan.getPlantillaComision()="       + output_valCan.getPlantillaComision());
	            logger.debug("output_valCan.getComision()="                + output_valCan.getComision());
	            logger.debug("output_valCan.getTotalPagado()="             + output_valCan.getTotalPagado());
	            logger.debug("output_valCan.getValorDiferencia()="         + output_valCan.getValorDiferencia());
	            logger.debug("output_valCan.getValorCapital()="            + output_valCan.getValorCapital());
	            logger.debug("output_valCan.getValorFinal()="              + output_valCan.getValorFinal());
	            logger.debug("moneda=" + moneda);
            }
            double capital = output_valCan.getValorCapital();
            if (logger.isDebugEnabled())  logger.debug("capital=" + capital);

            if (moneda.trim().equals("0998")) { //U.FOME
                if (logger.isDebugEnabled())  logger.debug("Ante de obtieneConversionMoneda" + moneda);
                ResultConsultaValoresCambio output_cambio = obtieneConversionMoneda("UFP", fechaHoy, fechaHoy, "*", capital);
                capital = output_cambio.getTotalCalculado();
            }

            if (logger.isDebugEnabled())  logger.debug("capital=" + capital);

            //se inyecta capital en pesos en valorRenovado
            //           CTBL en tipoCargo
            input_valCan.setValorRenovado(capital);
            input_valCan.setTipoCargo("CTBL");
            calculoValoresCancelacion(multiEnvironment, input_valCan, output_valCan);


            if (logger.isDebugEnabled())  logger.debug("output_valCan.getTotalPagado()=" + output_valCan.getTotalPagado());

            return output_valCan;
        } catch (Exception e) {
        	if (logger.isEnabledFor(Level.ERROR)) logger.error("ERROR***consultaCancelacionMultilinea...Exception [" + e.getMessage() + "]");
            if (datosLog!=null) {
                datosLog.put("tipoOpe", "CVC");
                datosLog.put("ERR",datosLog.get("ERR")==null? Utils.hackle(e.getMessage()): Utils.hackle(e.getMessage()) + datosLog.get("ERR"));
                registraLogMultilinea(datosLog);
            }
            else {
            	if (logger.isEnabledFor(Level.ERROR)) logger.error("datosLog es null");
            }
            throw new MultilineaException("ESPECIAL",e.getMessage());
        }

    }

    /**
     * Obtiene conversion de Moneda
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- version inicial
     * <li>1.1 28/07/2004 Waldo Iriarte   (Bee)- log de secuencia
     * <li>1.2 06/07/2006 Rodrigo Videla  (Bee)- Se agrega secuencia de log para depuracion
     * <li>1.3 29/06/2007 Hector Carranza (Bee)- se hace publico el metodo
     *
     * </ul>
     * <p>
     *
     * @param valorCambio
     * @param fecha
     * @param fecha2
     * @param comando
     * @param totalIngresado
     * @return {@link ResultConsultaValoresCambio}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public ResultConsultaValoresCambio obtieneConversionMoneda(String valorCambio, String fecha, String fecha2, String comando, double totalIngresado) throws MultilineaException, EJBException {

        try {
            RutinasHost        rtnHostBean    = null;
            InitialContext     ic1            = JNDIConfig.getInitialContext();
            Object             obj_rtn        = ic1.lookup(JNDI_NAME_RTN);
            RutinasHostHome    home_rtn       = (RutinasHostHome) PortableRemoteObject.narrow(obj_rtn, RutinasHostHome.class);

            if (logger.isDebugEnabled())  logger.debug("RutinasHostBean creado");
            rtnHostBean = (RutinasHost) PortableRemoteObject.narrow(home_rtn.create(), RutinasHost.class);
            if (logger.isDebugEnabled())  logger.debug("RutinasHostBean remote creado");

            int valor = 1;

            if (totalIngresado < 0) {
                valor = -1;
            }
            else {
                valor = 1;
            }
            if (logger.isDebugEnabled()){
	            logger.debug("Metodo obtieneConversionMoneda");
	            logger.debug("req_num_param=043");
	            logger.debug("valorCambio="+valorCambio);
	            logger.debug("fecha="+fecha);
	            logger.debug("fecha2="+fecha2);
	            logger.debug("comando="+comando);
	            logger.debug("totalIngresado=" + new Double(totalIngresado * valor).toString());
            }
            
            InputConsultaValoresCambio  ibean = new InputConsultaValoresCambio("043", valorCambio, fecha, fecha2, comando, totalIngresado * valor);
            ResultConsultaValoresCambio obean = new ResultConsultaValoresCambio();

            obean = (ResultConsultaValoresCambio) rtnHostBean.consultaValoresCambio(ibean, obean);

            obean.setTotalCalculado(obean.getTotalCalculado() * valor);

            return obean;

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.getMessage());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.getMessage());

            throw new MultilineaException("UNKNOW", e.getMessage());
        }

    }

    /**
     * Renovacion de Multilinea
     *
     * Registro de versiones:<ul>
     * <li>1.0  05/07/2004 Carlos Panozo   (Bee)- version inicial
     * <li>1.1  26/07/2004 Carlos Panozo   (Bee)- inclusion datosLog, +Log secuencia
     * <li>1.2  24/08/2004 Carlos Panozo   (Bee)- inhibe llamada de ResultConsultaOperacionCredito
     * <li>1.3  15/09/2004 Carlos Panozo   (Bee)- verificacion de si cliente tiene MLT, control errores
     * <li>1.4  30/11/2004 Carlos Panozo   (Bee)- nuevo parametro codSegmento, adaptacion con rutinas de Precios
     * <li>1.5  01/12/2004 Carlos Panozo   (Bee)- descMoraSt no va mas, mejora en obtencion de ROC
     * <li>1.6  05/12/2004 Carlos Panozo   (Bee)- +LOG secuencia, multiEnvironment.setIndreq(2,'1');   //pos(2)=Solo BD Mañana?  --> '0'=No '1'=Si
     * <li>1.7  17/12/2004 Carlos Panozo   (Bee)- control de morosidad +LOG secuencia
     * <li>1.8  06/01/2005 Carlos Panozo   (Bee)- control de monto permitidos, obtencion de tasas web y sucursal
     * <li>1.9  17/01/2005 Carlos Panozo   (Bee)- cambio en manejo de opc_s y can_s(ahora son arreglos)
     * <li>1.10 27/01/2005 Carlos Panozo   (Bee)- cambio en obtencion de tasa web
     * <li>1.11 22/06/2005 Hector Carranza (Bee)- Correcion observacion con respecto a condicionGar (condicion de garantia) de acuerdo a tiene_cual (no tiene ni aval ni fogape) verificando que solo_G
     * <li>1.12 06/07/2006 Rodrigo Videla  (Bee)- Se agrega secuencia de log para depuracion
     * <li>1.13 23/02/2007 Hector Carranza (Bee)- se agrega multiEnvironment.setIndreq(5,'1') para que el host valide en forma optima el ejecutivo asignado. (validacion de ejecutivo inexistente)
     * <li>1.14 05/04/2007 Hector Carranza (bee)- se corrige multiEnvironment.setIndreq(5,'1') por multiEnvironment.setIndreq(5,'2') por normalizacion de stadard parmetros
     * <li>1.15 10/05/2007 Hector Carranza (bee)- se omite el flag de validacion de ejecutivo en renovacion
     * <li>2.0  31/05/2007 Hector Carranza (Bee)- Se corrige calculo de parametros en la obtencion de la tasa desde sistema precios, siendo calculo en 'D'ias.
     * <li>2.1  30/11/2007 Hector Carranza (Bee)- Se corrige calculo de monto para valorRenovado si moneda es UF o pesos.
     * <li>2.2  24/04/2009 Hector Carranza (Bee)- Se agrego toda la logica para el manejo de seguros comerciales + parametro seguros en metodo.
     * <li>2.3  29/10/2009 Hector Carranza (Bee)- Se agrega datos en salida de rut avala en rocampliada y mara de seguro DPS.
     * <li>2.4  12/05/2016 Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI) - se agrega atributo dentro del flujo para identificar tipo de línea.
     * <li>2.5  29/07/2016 Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI) - se agrega lógica para consulta y obtener valor de gasto notarial.</li>
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param idReq numero requerimiento
     * @param idOperacion   numero operacion cai
     * @param numOperacionCan numero operacion iic
     * @param oficinaCancel
     * @param ejecutivo
     * @param valorRenovado
     * @param tipoOperacion
     * @param moneda
     * @param codigoAuxiliar
     * @param oficinaIngreso
     * @param ctaAbonoTer
     * @param pinCtaCargo
     * @param vencimientos
     * @param codBanca
     * @param plan
     * @param rutDeudor
     * @param dvDeudor
     * @param diasEnMora
     * @param topeMora
     * @param whoVisa
     * @param totalPagado
     * @param datosLog
     * @param codSegmento
     * @param lista de seguros
     *
     * @return {@link ResultRenovacionMultilinea}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public ResultRenovacionMultilinea renovacionMultilinea(MultiEnvironment multiEnvironment, String idReq, String idOperacion, int numOperacionCan, String oficinaCancel, String ejecutivo, double valorRenovado, String tipoOperacion, String moneda, String codigoAuxiliar, String oficinaIngreso, int ctaAbonoTer, int pinCtaCargo, EstructuraVencimiento[] vencimientos, String codBanca, char plan, int rutDeudor, char dvDeudor, int diasEnMora, int topeMora, int whoVisa, double totalPagado, Hashtable datosLog, String codSegmento, String[] seguros) throws MultilineaException, EJBException {

        ResultConsultaSpr obeanSprSGS  = null;
        Vector vectorSPR               = new Vector();
        boolean consultaSeguros        = false;
        String codigoSegDesgrav        = "SEDECO";
        double valInteOperacion        = 0.1;
        int rutAvalDPS                 = 0;
        char rutdigAvalDPS             = ' ';
        String canalCredito = TablaValores.getValor(ARCHIVO_PARAMETROS, "canalCreditoWeb", "canales");
        int indicadorNP03 = 0;
		int indicadorNP04 = 0;
        try {
            if (logger.isDebugEnabled())  logger.debug("Inicio Renovacion Multilinea en MultilineaBean");
            if (logger.isDebugEnabled())  logger.debug("requerimiento=" + idReq);
            if (logger.isDebugEnabled())  logger.debug("");
            if ((datosLog != null) && idReq.equals("096")) {
                datosLog.put("tipoOpe", "SIR");
            }
            if ((datosLog != null) && idReq.equals("097")) {
                datosLog.put("tipoOpe", "REN");
            }

            String canal                   = multiEnvironment.getCanal();

            if (logger.isDebugEnabled())  logger.debug("validando array 'vencimientos'");

            if (vencimientos == null || vencimientos[0] == null) {
                throw new Exception("vencimientos indefinidos");
            }

            Date   fechaCurse             = ManejoEvc.fechaHabil(new Date());
            char   analisisFeriado        = ' ';

            double monto = valorRenovado;

            if (logger.isDebugEnabled()){  
	            logger.debug("+++++++++++++++ valorRenovado ---> monto="      + monto);
	            logger.debug("+++++++++++++++ valorRenovado ---> monto(int)=" + (int) monto);
	            logger.debug("codBanca="   + codBanca);
	            logger.debug("RUT="        + String.valueOf(rutDeudor));
	            logger.debug("RUT(sybase)="+ StrUtl.fillCharLeft(String.valueOf(rutDeudor),'0',8));
	            logger.debug("PLAN="       + plan);
	            logger.debug("moneda="     + moneda);
	            logger.debug("MESES="      + ManejoEvc.getPlazoEnMeses(fechaCurse,analisisFeriado,vencimientos,0,0,0,0));
            }
            Date today = new Date();
            String fechaHoy = ddMMyyyy_form.format(today);
            String codMonedaOrigen = (String)datosLog.get("codMonedaOrigen");
            if (logger.isDebugEnabled()){  
	            logger.debug("********************************************");
	            logger.debug("********************************************");
	            logger.debug("monedaOrigen = " + codMonedaOrigen.toString());
	            logger.debug("monedaDestino =" + moneda.toString());
	            logger.debug("monto = "+ monto);
	            logger.debug("whoViza = " + new Integer(whoVisa).toString() );
	            logger.debug("********************************************");
	            logger.debug("********************************************");
            }

            if (moneda.trim().equals("U.FOME") || moneda.trim().equals("0998") ) { //U.FOME glosa desaparece em MANTENCION CODIGO MONEDA CCC
       
                if (logger.isDebugEnabled())  logger.debug("+++++++++++++++ obtieneConversionMoneda si moneda=0998");
                if (logger.isDebugEnabled())  logger.debug("datos pasados a obtieneConversionMoneda(UFP,"+fechaHoy+ ","+fechaHoy+",*,1");
                ResultConsultaValoresCambio output_cambio = obtieneConversionMoneda("UFP", fechaHoy, fechaHoy, "*", 1);
                monto = monto/output_cambio.getTotalCalculado();
                if (logger.isDebugEnabled())  logger.debug("+++++++++++++++ obtieneConversionMoneda ---> monto="      + monto);
               
            }
            if (logger.isDebugEnabled()){  
	            logger.debug("monto="        + monto);
	            logger.debug("monto(int)="   + (int) monto);
	            logger.debug("diasEnMora="   + diasEnMora);
	            logger.debug("topeMora="     + topeMora);
            }
            String descuento="AVC";

            if (diasEnMora>0 && diasEnMora <= topeMora) {
                descuento = "AVC2"; //avance en mora
            }

            if (logger.isDebugEnabled()){  
	            logger.debug("Plazo......");
	            logger.debug("......en meses:" + ManejoEvc.getPlazoEnMeses(fechaCurse,analisisFeriado,vencimientos,0,0,0,0));
	            logger.debug("......en dias :" + ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,0,0));
	            logger.debug("vencimientos[0].getTotalVencimientosGrupo():" + vencimientos[0].getTotalVencimientosGrupo());
            }
            TablaValores tablaCreditos   = new TablaValores();
            Hashtable    htTablaCreditos = tablaCreditos.getTabla("renmultilinea.parametros");
            if (logger.isDebugEnabled())  logger.debug("Buscando tasa especial:[" + moneda + tipoOperacion + codigoAuxiliar + "]");
            Hashtable    datosCreditos   = (Hashtable)htTablaCreditos.get(moneda + tipoOperacion + codigoAuxiliar);
            String       tasaEspecial    = (String)datosCreditos.get("TASAESPECIAL");
            String       cargoTasaMora   = (String)datosCreditos.get("CARGOTASAMORA");
            double       tasaSprea = 0D;
            double       porcCargoTasaMora   = 0D;
            boolean      hayCargoMora = false;
            if (logger.isDebugEnabled())  logger.debug("tasaEspecial [" + tasaEspecial + "]");
            if (tasaEspecial!=null && !tasaEspecial.trim().equals("")) {//si no es nulo, ni blanco
                try {
                    tasaSprea= Double.parseDouble(tasaEspecial);
                        if (logger.isDebugEnabled())  logger.debug("aplica tasaSpread = tasaEspecial [" + tasaSprea + "]");
                } catch (NumberFormatException nfe) {
                	if (logger.isEnabledFor(Level.ERROR)) logger.error("NumberFormatException::tasaEspecial [" + tasaEspecial + "]");
                };
            };
            if (cargoTasaMora!=null && !cargoTasaMora.trim().equals("")) {//si no es nulo, ni blanco
                try {
                    porcCargoTasaMora= Double.parseDouble(cargoTasaMora);
                    if (logger.isDebugEnabled())  logger.debug("aplica cargoTasaMora = [" + cargoTasaMora + "]");
                    if (logger.isDebugEnabled())  logger.debug("aplica porcCargoTasaMora = [" + porcCargoTasaMora + "]");
                    hayCargoMora=true;
                } catch (NumberFormatException nfe) {
                	if (logger.isEnabledFor(Level.ERROR)) logger.error("NumberFormatException::factorTasaMora [" + cargoTasaMora + "]");
                };
            };

            if (logger.isDebugEnabled()){ 
	            logger.debug("rutDeudor [" + rutDeudor + "]");
	            logger.debug("tasaSprea [" + tasaSprea + "]");
	            logger.debug("Fin obtencion Tasa");
            }
            //*********************************************************************************************//
            //*** INI consulta CLN para obtener condicionGar necesario para el ingreso de la simulacion ***//
            //*** INI Desarrollo BEE 03/2004 hcf                                                        ***//
            //*** renovacion                                                                            ***//
            //*********************************************************************************************//
            if (logger.isDebugEnabled())  logger.debug("Antes ResultConsultaCln");

            ResultConsultaCln obeanCln = new ResultConsultaCln();

            int rutD = Integer.parseInt(Long.toString(rutDeudor));
            InputConsultaCln ibeanCln = new InputConsultaCln("025",
                                                              "",                      // nombreDeudor,
                                                              rutDeudor,               // rutDeudor,
                                                              dvDeudor,                // digitoVerificador,
                                                              ' ',                     // indicadorExtIdc,
                                                              "",                      // glosaExtIdc,
                                                              "",                      // idOperacion,
                                                              0,                       // numOperacion,
                                                              0);                      // totLinIngreso);

            consultaCln(multiEnvironment, ibeanCln, obeanCln);

            Linea[] consultaLinea = obeanCln.getLineas();

            boolean tiene_aval = false;
            boolean tiene_foga = false;
            boolean tiene_cual = false;
            boolean tiene_mlt  = false;
            boolean solo_G     = false;

            if (logger.isDebugEnabled())  logger.debug("consultaLinea.length [" + consultaLinea.length + "]");
            String TipLin = null;
            for (int i = 0; i < consultaLinea.length; i++) {
                if (consultaLinea[i] == null) {
                     break;
                 }

                TipLin = consultaLinea[i].getCodigoTipoLinea();
                if (logger.isDebugEnabled()){ 
	                logger.debug("TipLin   ["+ TipLin +"]");
	                logger.debug("consultaLinea[i].getCodigoTipoInfo   ["+ consultaLinea[i].getCodigoTipoInfo() +"]");
	                logger.debug("consultaLinea[i].getTipoOperacion    ["+ consultaLinea[i].getTipoOperacion() +"]");
	                logger.debug("consultaLinea[i].getCodAuxiliarLinea ["+ consultaLinea[i].getCodAuxiliarLinea() +"]");
                }
                String tipoLineaOcupada = definirLDC(multiEnvironment, consultaLinea);
	        if (TipLin.equals(tipoLineaOcupada)) { //verifica si tipo de linea es multilinea (la ultima)
                    solo_G      = consultaLinea[i].getCodigoTipoInfo() == 'G' ? true : false;
                    tiene_mlt   = true;
                    tiene_aval  = (consultaLinea[i].getTipoOperacion()).equals("AVL") ? true : tiene_aval;
                    tiene_foga  = (consultaLinea[i].getTipoOperacion()).equals("952") ? ((consultaLinea[i].getCodAuxiliarLinea()).trim().equals("101") ? true : tiene_foga) : tiene_foga;
                    tiene_cual  = solo_G ? (!(consultaLinea[i].getTipoOperacion()).trim().equals("") ? true : tiene_cual) : tiene_cual;

                }
            }

            if (idReq.equals("097")) {
            if (!tiene_mlt) {
                if (logger.isDebugEnabled())  logger.debug("Cliente " + rutDeudor + "-" + dvDeudor + " No posee Multilinea (MLT)");
                throw new MultilineaException("Especial","Cliente No posee Multilinea (MLT)");
            }
            }
            //  condicionGar -->
            //  ("4  ";  //sin garantia default)
            //  ("8  ";  //otras garantias)
            //  ("2  ";  //aval);
            //  ("10 ";  //fogape);

            String condicionGar   = tiene_aval ? "2  " : (tiene_foga ? "10 " : (tiene_cual ? "8  " : "4  "));


            // destinoCredito -->
            // ("288" : // captrab default)
            // ("301" : // captrab fogape )  <-- confirmado

            String destinoCredito = tiene_foga ? "301" : "288"; //si tiene fogape entonces destinocredito=captrabfogape sino captrab

            if (logger.isDebugEnabled()){  
	            logger.debug("condicionGar   ["+ condicionGar +"]");
	            logger.debug("destinoCredito ["+ destinoCredito +"]");
	            logger.debug("Despues ResultConsultaCln obeanCln");
            }
            //*********************************************************************************************//
            //*** FIN Desarrollo BEE 03/2004 hcf                                                        ***//
            //*** FIN consulta CLN                                                                      ***//
            //*********************************************************************************************//

            //CPH M090304
            ResultContextualizacionIngrCancelacion res_ctx = contextualizacionIngrCancelacion(multiEnvironment, idOperacion, numOperacionCan, 0);
            Date fechaVencimiento = res_ctx.getFecVencimiento();
            Double tasaCan = null;

            if (logger.isDebugEnabled()){  
	            logger.debug("fechaHoy=" + fechaHoy);
	            logger.debug("fechaVencimiento=" + ddMMyyyy_form.format(fechaVencimiento));
            }
            if (new SimpleDateFormat("yyyyMMdd").format(fechaVencimiento).compareTo(new SimpleDateFormat("yyyyMMdd").format(today))>0) {
                tasaCan=new Double(res_ctx.getTasaSpread());
            };
            if (logger.isDebugEnabled()){  
	            logger.debug("tasaCancelacion=" + tasaCan);
	            logger.debug("ejecutivoCan=" + res_ctx.getEjecutivo());
	            logger.debug("ini visacion en renovacion");
            }
            //  whoVisa = -1  no visa  a nadie  //
            //  whoVisa >= 0  visa empresa e indica cuantos avales visar  //

            boolean tiene_avales = condicionGar.equals("2  ") ? true : false;
            boolean visa_empresa = whoVisa >= 0 ? true : false;
            boolean visa_avales  = whoVisa >  0 ? true : false;
            if (logger.isDebugEnabled()){  
	            logger.debug("visa_empresa [" + (visa_empresa ? "SI" : "NO") + "]");
	            logger.debug("visa_avales  [" + (visa_avales  ? "SI(" + whoVisa + ")" : "NO") + "]");
            }
            String respuestaVisacion = null;

            if (visa_empresa){
                respuestaVisacion = visacionRut(String.valueOf(rutDeudor), dvDeudor, canal, "EMP");
                if ((respuestaVisacion == null) || (respuestaVisacion != null && !respuestaVisacion.trim().equals("AP"))){
                    datosLog.put("ERR","Error Visacion (" + respuestaVisacion + ") para " + String.valueOf(rutDeudor) + "-" + dvDeudor);
                        throw new MultilineaException("ESPECIAL", "ERRVISACION" + respuestaVisacion);
                    }
                if (logger.isDebugEnabled())  logger.debug("visacion de empresa OK");
            }

//            if (visa_avales){
//                if (logger.isDebugEnabled())  logger.debug("visa_avales  [true]");
                if (tiene_avales){
                    if (logger.isDebugEnabled())  logger.debug("tiene_avales [true]");
                    //Consulta de avales si es que existe condicion de garantia = aval
                    //luego se deben visar los 'whoVisa' primeros ...
                    controlError = "ERRAVCAVL";
                    ResultConsultaAvales obeanAval = new ResultConsultaAvales();

                    InputConsultaAvales abean = new InputConsultaAvales("026",
                                                                        Integer.parseInt(String.valueOf(rutDeudor)),    //      int rutDeudor,
                                                                        dvDeudor,                                       //      char digitoVerificaCli,
                                                                        ' ',                                            //      idCliente,
                                                                        " ",                                            //      glosaCliente,
                                                                        " ",                                            //      nombreCli,
                                                                        " ",                                            //      idOperacion,
                                                                        0 ,                                             //      numOperacion,
                                                                        0,                                              //      numCorrelativo,
                                                                        "AVL",                                          //      tipoOperacion SOLO AVALES,
                                                                        "AVC");                                         //      tipoCredito ASOCIADO A MULTILINEA);

                    consultaAvales(multiEnvironment, abean, obeanAval);

                    if (logger.isDebugEnabled())  logger.debug("despues consultaAvales");

                    Aval[] avales    = obeanAval.getAvales();

                    if (logger.isDebugEnabled())  logger.debug("avales.length ["+ avales.length +"]");

                    int visados = 0;

                    for (int i = 0; i < avales.length; i++) {

                        if (visados >= whoVisa) //Visa hasta 'whoVisa' avales
                            break;

                        String rutAval = Integer.toString(avales[i].getRutAval());
                        char   dvfAval = avales[i].getDigitoVerificaAval();
                        char   indvige = avales[i].getVigente();
                        controlError = "";
                        if (logger.isDebugEnabled())  logger.debug("esta vigente el rut "+ rutAval +" ?    vigente["+ indvige +"]");
                        if (indvige == 'S'){
                            if (visa_avales){
                                valInteOperacion = valInteOperacion == 0.1 ? avales[i].getRutAval() : valInteOperacion;
                                if (logger.isDebugEnabled()){ 
	                                logger.debug("valInteOperacion["  + valInteOperacion + "]");
	                                logger.debug("visa_avales  [true] [" + rutAval+ "]");
                                }
                                respuestaVisacion = visacionRut(rutAval, dvfAval, canal, "EMP");
                                if ((respuestaVisacion == null) || (respuestaVisacion != null && !respuestaVisacion.trim().equals("AP"))){
                                    datosLog.put("ERR","Error Visacion (" + respuestaVisacion + ") para " + String.valueOf(rutAval) + "-" + dvfAval);
                                        throw new MultilineaException("ESPECIAL", "ERRVISACION" + respuestaVisacion);
                                    }
                                visados++;
                            }
                            if (!visa_avales){
                                valInteOperacion = avales[i].getRutAval();
                                rutAvalDPS       = avales[i].getRutAval();
                                rutdigAvalDPS    = avales[i].getDigitoVerificaAval();
                                if (logger.isDebugEnabled()){  
	                                logger.debug("visa_avales  [false]");
	                                logger.debug("valInteOperacion["  + valInteOperacion + "]");
	                                logger.debug("rutAvalDPS      ["  + rutAvalDPS       + "]");
	                                logger.debug("rutdigAvalDPS   ["  + rutdigAvalDPS    + "]");
                                }
                                break;
                            }
                         }
                    }
                    if (logger.isDebugEnabled())  logger.debug("cuantos viso ["+ visados +"]");
                }
                if (logger.isDebugEnabled())  logger.debug("visacion de avales OK");
//             }else{
//                if (logger.isDebugEnabled())  logger.debug("visa_avales  [false]");
//             }
            controlError = "";
            if (logger.isDebugEnabled())  logger.debug("fin visacion en renovacion");

            if (logger.isDebugEnabled())  logger.debug("antes ResultRenovacionMultilinea obean = new ResultRenovacionMultilinea()...");

            ResultRenovacionMultilinea obean = new ResultRenovacionMultilinea();

            //******************************************//
            //********  INI Consulta SPR ***************//
            //******************************************//
            if (logger.isDebugEnabled())  logger.debug("llenando vencimientos[0].getPeriodoEntreVctoExpresaEn()=[" + vencimientos[0].getPeriodoEntreVctoExpresaEn() + "]");

            ResultConsultaSpr  obeanSpr = obtieneTasaMultilineaPrecios(multiEnvironment,
                                                                       "CUR",
                                                                       "INT",
                                                                       today,//ddMMyyyy_form.parse("02092003"),;
                                                                       rutDeudor,
                                                                       dvDeudor,
                                                                       canal,//"130"
                                                                       "", //subcanal (oficina)
                                                                       moneda,
                                                                       tipoOperacion,
                                                                       codigoAuxiliar,
                                                                       ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,0,0) < 1 ? 1 : ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,0,0),
                                                                       'D',
                                                                       ((moneda.trim().equals("U.FOME") || moneda.trim().equals("0998")) ? monto : valorRenovado),
                                                                       (double) vencimientos[0].getTotalVencimientosGrupo(),
                                                                       "",//codClfRentabilidad
                                                                       "",//codFactorRiesgo
                                                                       "",//indTipoPago
                                                                       codSegmento,
                                                                       idOperacion,
                                                                       numOperacionCan
                                                                       );

            //******************************************//
            //********  FIN Consulta SPR ***************//
            //******************************************//
            if (obeanSpr==null || obeanSpr.getTotOcurrencias()==0) {
                throw new MultilineaException("ESPECIAL","Error en obtencion de tasa: SPR nulo o 0 ocurrencias");
            }

                //********  INI Consulta SEGUROS Renovacion ***************//
                String  segurosVigentes   = ((String) TablaValores.getValor("multilinea.parametros", "RenovacionBancasSeguros", "vigente"));
                consultaSeguros           = (segurosVigentes == null ? " " : segurosVigentes).trim().equals("S");
                if (logger.isDebugEnabled())  logger.debug("segurosVigentes   [" + segurosVigentes + "]  codBanca[" + codBanca + "]  canal[" + canal + "] Renovacion");

                if (consultaSeguros){
                    String  bancapermitidas = TablaValores.getValor("multilinea.parametros", "RenovacionBancasSeguros", "banca");
                    if (logger.isDebugEnabled())  logger.debug("bancas permitidas [" + bancapermitidas + "]  codBanca[" + codBanca + "]  canal[" + canal + "] Renovacion");
                    consultaSeguros   = verificaPertenenciaBanca(codBanca, bancapermitidas);

                    if (consultaSeguros){
                        try{

                            if (logger.isDebugEnabled())  logger.debug("Antes de la consulta SPR para Seguros Multilinea Renovacion");

                            obeanSprSGS = obtieneSegurosDesdePrecios(multiEnvironment,
                                                                    "CUR",                               //codEvento
                                                                    "SGS",                               //codConcepto
                                                                    fechaCurse,
                                                                    "CON",                               //codTipoConsulta siempre CON para SGS
                                                                    "",                                  // idOperacion,                         //caiOperacion
                                                                    0,                                   // numOperacionCan,                     //iicOperacion
                                                                    "",                                  //extOperacion
                                                                    rutDeudor,
                                                                    dvDeudor,
                                                                    ' ',                                 //indCliente
                                                                    "",                                  //glsCliente
                                                                    canal,                               //130 o 230
                                                                    "",                                  //codigo oficina va si solo si canal = 110
                                                                    moneda,                              //0999
                                                                    tipoOperacion,                       //CON
                                                                    codigoAuxiliar,                      //050
                                                                    ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,0,0) < 1 ? 1 : ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,0,0),
                                                                    'D',                                 // expresa en
                                                                    vencimientos[0].getFechaPrimerVcto(),//fecVctoInicial
                                                                    ((moneda.trim().equals("U.FOME") || moneda.trim().equals("0998")) ? monto : valorRenovado),       //montoCredito,
                                                                    0D,                                  //valSaldoOperacion
                                                                    (double) vencimientos[0].getTotalVencimientosGrupo(), //(double) numCuotas,
                                                                    valInteOperacion,                    //valInteOperacion
                                                                    0D,                                  //valMontoAdicional
                                                                    "",                                  //codClfRentabilidad,
                                                                    "",                                  //codFactorRiesgo,
                                                                    "",                                  //indTipoPago,
                                                                    "",                                  //caiOperacionRcc
                                                                    codSegmento                          //codSegmento
                                                                    );

                            if (logger.isDebugEnabled())  logger.debug("Despues de la consulta SPR a Seguros Multilinea Renovacion");

                        } catch(Exception e) {
                        	if (logger.isEnabledFor(Level.ERROR)) logger.error("ERROR***SimulaCursaCreditoException...Exception [" + e.getMessage() + "]");
                        }

                        if (obeanSprSGS==null || obeanSprSGS.getTotOcurrencias()==0) {
                            if (logger.isDebugEnabled())  logger.debug("La consulta SPR no encuentra ocurrencias en Seguros");
                        } else {
                            for(int i = 0; i < obeanSprSGS.getTotOcurrencias(); i++){
                                if (obeanSprSGS.getInstanciaDeConsultaSpr(i) == null){
                                    if (logger.isDebugEnabled())  logger.debug("Saliendo del llenando vector de SPR's SEGUROS");
                                    break;
                                } else {
                                    vectorSPR.add(obeanSprSGS.getInstanciaDeConsultaSpr(i));
                                    if (logger.isDebugEnabled())  logger.debug("llenando vector de SPR's :" + i);
                                }
                            }
                        }
                    }else{
                        if (logger.isDebugEnabled())  logger.debug("No se consultaron seguros porque banca no esta inscrita. Renovacion");
                    }
                }
                //********  FIN Consulta SEGUROS Renovacion ***************//

            //******************************************//
            //********  INI Consulta PPC ***************//
            //******************************************//
            ResultConsultaSpr  obeanPpc = null;
            try {
                obeanPpc = obtieneTasaMultilineaPrecios(multiEnvironment,
                                                                       "CUR",
                                                                       "PPC",
                                                                       today,//ddMMyyyy_form.parse("02092003")
                                                                       rutDeudor,
                                                                       dvDeudor,
                                                                       canal,//"130"
                                                                       "", //subcanal (oficina)
                                                                       moneda,
                                                                       tipoOperacion,
                                                                       codigoAuxiliar,
                                                                       ManejoEvc.getPlazoEnMeses(fechaCurse,analisisFeriado,vencimientos,0,0,0,0),
                                                                       vencimientos[0].getPeriodoEntreVctoExpresaEn(),//'M'
                                                                       ((moneda.trim().equals("U.FOME") || moneda.trim().equals("0998")) ? monto : valorRenovado),
                                                                       (double) vencimientos[0].getTotalVencimientosGrupo(),
                                                                       "",//codClfRentabilidad
                                                                       "",//codFactorRiesgo
                                                                       "",//indTipoPago
                                                                       codSegmento,
                                                                       idOperacion,
                                                                       numOperacionCan);
            } catch (Exception e) {
            	if (logger.isEnabledFor(Level.ERROR)) logger.error("consulta PPC::" + e.getMessage());
            }

            //******************************************//
            //********  FIN Consulta PPC ***************//
            //******************************************//
            if (obeanPpc==null || obeanPpc.getTotOcurrencias()==0) {
                if (logger.isDebugEnabled())  logger.debug("obeanPpc nulo o 0 ocurrencias");
            } else {
                if (logger.isDebugEnabled())  logger.debug("Rango (" + form.format(obeanPpc.getInstanciaDeConsultaSpr(0).getValMonto()) + " - " + form.format(obeanPpc.getInstanciaDeConsultaSpr(0).getValTasaMonto())+")");
                if (valorRenovado > obeanPpc.getInstanciaDeConsultaSpr(0).getValTasaMonto()) {
                    throw new MultilineaException("ESPECIAL","Monto mayor que el permitido (" + form.format(obeanPpc.getInstanciaDeConsultaSpr(0).getValTasaMonto()) + ")");
                }
                if (valorRenovado < obeanPpc.getInstanciaDeConsultaSpr(0).getValMonto()){
                    throw new MultilineaException("ESPECIAL","Monto menor que el permitido (" + form.format(obeanPpc.getInstanciaDeConsultaSpr(0).getValMonto()) +")");
                }
            }
            
            double valorNotario = 0;
			  ResultConsultaSpr  obeanSprCGN = null;

			  if (canal != null && canal.equals(canalCredito)){  

				  if (getLogger().isDebugEnabled()){
					  getLogger().debug("[renovacionMultilinea] Antes de obtieneGastosNotariales");
				  }

				  try{
					  if (getLogger().isDebugEnabled()){
						  getLogger().debug("[renovacionMultilinea] Antes de la consulta SPR a Gastos Notarios");
					  }

					  obeanSprCGN = obtieneTasaMultilineaPrecios(multiEnvironment,
							  "CUR",
							  "CGN",
							  fechaCurse,
							  rutDeudor,
							  dvDeudor,
							  canal,
							  "", 
							  moneda,
							  tipoOperacion,
							  codigoAuxiliar,
							  ManejoEvc.getPlazoEnMeses(fechaCurse,analisisFeriado,vencimientos,0,0,indicadorNP03,indicadorNP04),
							  vencimientos[0].getPeriodoEntreVctoExpresaEn(),//'M',
							  ((moneda.trim().equals("U.FOME") || moneda.trim().equals("0998")) ? monto : valorRenovado),
							  (double) vencimientos[0].getTotalVencimientosGrupo(),
							  "",
							  "",
							  "",
							  codSegmento,
							  "",
							  0);
					  if (getLogger().isDebugEnabled()){ 
						  getLogger().debug("[renovacionMultilinea] Despues de la consulta SPR a Gastos Notariales");
					  }
				  }
				  catch(Exception e) {
					  if (getLogger().isEnabledFor(Level.ERROR)){
						  getLogger().error("[renovacionMultilinea] [BCI_FINEX] Error controlado en la consulta SPR de CGN" + e.getMessage());
					  }
				  }

				  if (obeanSprCGN == null || obeanSprCGN.getTotOcurrencias() == 0) {
					  if (getLogger().isDebugEnabled()){
						  getLogger().debug("[renovacionMultilinea] No existen ocurrencias en la consulta SPR para Gastos Notariales");
					  }
				  } 
				  else {
					  for(int i = 0; i < obeanSprCGN.getTotOcurrencias(); i++){
						  if (obeanSprCGN.getInstanciaDeConsultaSpr(i) == null){
							  if (getLogger().isDebugEnabled()){
								  getLogger().debug("[renovacionMultilinea] Saliendo del llenando vector de SPR's Gastos Notariales");
							  }
							  break;
						  }
						  else{
							  valorNotario = obeanSprCGN.getInstanciaDeConsultaSpr(i).getValMonto();
							  if (getLogger().isDebugEnabled()){ 
								  getLogger().debug("[renovacionMultilinea] llenando vector de SPR's :" + i);
								  getLogger().debug("[renovacionMultilinea] valorNotario: " +  valorNotario);
							  }
							  break;
						  }
					  }
				  }
				  if (getLogger().isDebugEnabled()){
					  getLogger().debug("[avanceMultilinea] Despues de la consulta SPR a Gastos Notarios");
				  }
			  }
            

            //******************************************//
            //********  INI ingresoRenovacion **********//
            //******************************************//
            String origen = "XXX";
            char indseq = 'X';
            if (logger.isDebugEnabled())  logger.debug("========== CAN ============");

            InputIngresoCancelacion [] can_s = new InputIngresoCancelacion[1];
            can_s[0] = new InputIngresoCancelacion();
            can_s[0].setCim_reqnum("038");
            can_s[0].setIdOperacion(idOperacion);
            if (logger.isDebugEnabled())  logger.debug("idOperacion can_s[0]=" + idOperacion);
            can_s[0].setNumOperacionCan(numOperacionCan);
            if (logger.isDebugEnabled())  logger.debug("numOperacionCan=" + numOperacionCan);
            can_s[0].setTipoCancelacion("RTT");
            if (logger.isDebugEnabled())  logger.debug("ejecutivo=" + ejecutivo);
            can_s[0].setEjecutivo(ejecutivo);
            if (logger.isDebugEnabled())  logger.debug("oficinaCancel=" + oficinaCancel);
            can_s[0].setOficinaCancel(oficinaCancel);
            if (logger.isDebugEnabled())  logger.debug("valorRenovado=" + valorRenovado);
            can_s[0].setValorRenovado(valorRenovado);
            can_s[0].setIdCuentaCargo("0000");
            if (logger.isDebugEnabled())  logger.debug("pinCtaCargo=" + pinCtaCargo);
            can_s[0].setNumCuentaCargo(pinCtaCargo);
            //Si cargo es 0 el tipo de cargo debe ir vacio
            if (logger.isDebugEnabled())  logger.debug("totalPagado=" + totalPagado);
            if (totalPagado == 0D) {
                can_s[0].setTipoCargo("");
            }
            else {
                can_s[0].setTipoCargo("CCMA");
            }
            can_s[0].setTasaInteresCancel(tasaCan);


            if (logger.isDebugEnabled())  logger.debug("========== OPC ============");
            //Una sola ocurrecia de OPC
            InputIngresoUnitarioDeOperacionDeCreditoOpc [] opc_s = new InputIngresoUnitarioDeOperacionDeCreditoOpc[1];
            opc_s[0] = new InputIngresoUnitarioDeOperacionDeCreditoOpc();
            opc_s[0].setCim_reqnum("046");
            opc_s[0].setCim_uniqueid(origen);
            opc_s[0].setCim_indseq(indseq);
            if (logger.isDebugEnabled())  logger.debug("tipoOperacion (String)    : '" + tipoOperacion + "'");
            opc_s[0].setTipoOperacion(tipoOperacion);
            if (logger.isDebugEnabled())  logger.debug("moneda        (String)    : '" + moneda + "'");
            opc_s[0].setMoneda(moneda);
            if (logger.isDebugEnabled())  logger.debug("codigoAuxiliar(String)    : '" + codigoAuxiliar + "'");
            opc_s[0].setCodigoAuxiliar(codigoAuxiliar);
            if (logger.isDebugEnabled())  logger.debug("oficinaIngreso(String)    : '" + oficinaIngreso + "'");
            opc_s[0].setOficinaIngreso(oficinaIngreso);
            if (logger.isDebugEnabled())  logger.debug("rutDeudor     (int)       : '" + rutDeudor + "'");
            opc_s[0].setRutDeudor(rutDeudor);
            if (logger.isDebugEnabled())  logger.debug("digitoVerificador(Char)   : '" + dvDeudor + "'");
            opc_s[0].setDigitoVerificador(dvDeudor);
            if (logger.isDebugEnabled())  logger.debug("montoCredito  (double)    : '" + monto + "'");
            opc_s[0].setMontoCredito(monto);
            if (logger.isDebugEnabled())  logger.debug("codigoSegDesgrav(String)   : '" + codigoSegDesgrav + "'");

            if (obeanSprSGS != null && obeanSprSGS.getTotOcurrencias() > 0){
                codigoSegDesgrav = "PRECIO";
                if (logger.isDebugEnabled())  logger.debug("codigoSegDesgrav**REN*: '" + codigoSegDesgrav + "'");
            }

            opc_s[0].setCodigoSegDesgrav(codigoSegDesgrav);
            if (logger.isDebugEnabled())  logger.debug("fechaCurse (Date)          : '" + fechaCurse + "'");
            if (logger.isDebugEnabled())  logger.debug("tasaSprea (double)          : '" + obeanSpr.getInstanciaDeConsultaSpr(0).getValPropuesto() + "'");
            opc_s[0].setTasaSprea(obeanSpr.getInstanciaDeConsultaSpr(0).getValPropuesto());
            if (logger.isDebugEnabled())  logger.debug("condicionGar (String)       : '" + condicionGar + "'");
            opc_s[0].setCondicionGar(condicionGar);
            if (logger.isDebugEnabled())  logger.debug("abono (String)             : '" + "CCMA" + "'");
            opc_s[0].setAbono("CCMA");
            if (logger.isDebugEnabled())  logger.debug("cargo (String)             : '" + "AUT" + "'");
            opc_s[0].setCargo("AUT");
            if (logger.isDebugEnabled())  logger.debug("ctaAbono (String)          : '" + "0000" + "'");
            opc_s[0].setCtaAbono("0000");
            if (logger.isDebugEnabled())  logger.debug("ctaAbonoTer (int)          : '" + ctaAbonoTer + "'");
            opc_s[0].setCtaAbonoTer(ctaAbonoTer);
            if (logger.isDebugEnabled())  logger.debug("destinoCredito (String)    : '" + destinoCredito + "'");
            opc_s[0].setDestinoCredito(destinoCredito);
            if (logger.isDebugEnabled())  logger.debug("vigenciaCargo (char)       : '" + 'S' + "'");
            opc_s[0].setVigenciaCargo('S');
            if (logger.isDebugEnabled())  logger.debug("ctaCargo (String)         : '" + "0000" + "'");
            opc_s[0].setCtaCargo("0000");
            if (logger.isDebugEnabled())  logger.debug("pinCtaCargo (int)          : '" + pinCtaCargo + "'");
            opc_s[0].setPinCtaCargo(pinCtaCargo);

            if (logger.isDebugEnabled())  logger.debug("========== RDCs ============");
            InputIngresoUnitarioDeRdc []                    rdc_s   = null;

            if (logger.isDebugEnabled())  logger.debug("========== DLC ============");
            InputIngresoDeDlcLlavesYCampos []               dlc_s   = new InputIngresoDeDlcLlavesYCampos[1];
            dlc_s[0] = new  InputIngresoDeDlcLlavesYCampos();
            dlc_s[0].setCim_reqnum("013");
            dlc_s[0].setCim_uniqueid(origen);
            dlc_s[0].setCim_indseq(indseq);
            
            if (canal != null && canal.equals(canalCredito)){ 
				  if (getLogger().isDebugEnabled()) {
					  getLogger().debug("[renovacionMultilinea] Se agregan los campos CodNotaria y GastosNotario en la DLC");
				  }
				  dlc_s[0].setCodNotaria("PRECIO");
				  dlc_s[0].setGastosNotario(valorNotario);
			  }

            if (logger.isDebugEnabled())  logger.debug("========== CYA ============");
            InputIngresoUnitarioCya []                      cya_s   = null;

            if (logger.isDebugEnabled())  logger.debug("========== EVC ============");
            InputIngresoUnitarioDeEvc []                    evc_s   = new InputIngresoUnitarioDeEvc[1];
            evc_s[0] = new InputIngresoUnitarioDeEvc();
            evc_s[0].setCim_reqnum("016");
            evc_s[0].setCim_uniqueid(origen);
            evc_s[0].setCim_indseq(indseq);
            if (logger.isDebugEnabled())  logger.debug("docLegalNumero (int)          : '" + 1 + "'");
            evc_s[0].setDocLegalNumero(1);
            if (logger.isDebugEnabled())  logger.debug("totalVencimientosGrupo (int)  : '" + vencimientos[0].getTotalVencimientosGrupo() + "'");
            evc_s[0].setTotalVencimientosGrupo(vencimientos[0].getTotalVencimientosGrupo());
            if (logger.isDebugEnabled())  logger.debug("fechaPrimerVcto (Date)        : '" + vencimientos[0].getFechaPrimerVcto() + "'");
            evc_s[0].setFechaPrimerVcto(vencimientos[0].getFechaPrimerVcto());
            if (logger.isDebugEnabled())  logger.debug("periodoEntreVcto (int)        : '" + vencimientos[0].getPeriodoEntreVcto() + "'");
            evc_s[0].setPeriodoEntreVcto(vencimientos[0].getPeriodoEntreVcto());
            if (logger.isDebugEnabled())  logger.debug("periodoEntreVctoExpresaEn (char): '" + vencimientos[0].getPeriodoEntreVctoExpresaEn() + "'");
            evc_s[0].setPeriodoEntreVctoExpresaEn(vencimientos[0].getPeriodoEntreVctoExpresaEn());

            if (logger.isDebugEnabled())  logger.debug("========== ICG ============");
            InputIngresoUnitarioIcg []                      icg_s   = null;

            if (logger.isDebugEnabled())  logger.debug("========== VEN ============");
            InputIngresoUnitarioDeVen []                    ven_s   = null;

            if (logger.isDebugEnabled())  logger.debug("========== ROC REN ============");

            DetalleConsultaSpr [] arregloSPR            = null;
            InputIngresoRocAmpliada [] roc_sAmpliada    = null;
            Vector vectorSGS                            = new Vector();

            if (consultaSeguros){
                if (logger.isDebugEnabled())  logger.debug("Se consultaron los seguros REN");
                arregloSPR      = new DetalleConsultaSpr[vectorSPR.size()];
                arregloSPR      = (DetalleConsultaSpr[]) vectorSPR.toArray(new DetalleConsultaSpr[0]);
                roc_sAmpliada   = new InputIngresoRocAmpliada[vectorSPR.size()];

                if (seguros != null){
                    for(int i = 0; i < seguros.length; i++){
                        if (logger.isDebugEnabled())  logger.debug("seguros[" + i +"] [" + seguros[i] + "]");
                        vectorSGS.add(seguros[i]);
                    }
                }
            }


            if (logger.isDebugEnabled())  logger.debug("================ INT REN ======");
            int j=0;
            int totOcurrenciasRoc = obeanSpr.getTotOcurrencias();
            if (logger.isDebugEnabled())  logger.debug("totOcurrenciasRoc=" + totOcurrenciasRoc);
            if (obeanPpc!=null) {
                totOcurrenciasRoc = totOcurrenciasRoc + obeanPpc.getTotOcurrencias();
            }

            if (obeanSprSGS != null){
                totOcurrenciasRoc = totOcurrenciasRoc + obeanSprSGS.getTotOcurrencias();
                if (logger.isDebugEnabled())  logger.debug("totOcurrenciasRoc=" + totOcurrenciasRoc + " (SGS)");
            }

            if (obeanSprCGN != null){
            	totOcurrenciasRoc = totOcurrenciasRoc + obeanSprCGN.getTotOcurrencias();
            	if (getLogger().isDebugEnabled()) { 
            		getLogger().debug("[renovacionMultilinea] totOcurrenciasRoc=" + totOcurrenciasRoc + " (CGN)"); 
            	}
            }
            
            if (diasEnMora>0 && hayCargoMora) { //aplicar recargo porcentual
                    totOcurrenciasRoc = totOcurrenciasRoc + 1;
            }
            if (logger.isDebugEnabled())  logger.debug("totOcurrenciasRoc=" + totOcurrenciasRoc);
            InputIngresoRoc []   roc_s   = new InputIngresoRoc[totOcurrenciasRoc];

            if (logger.isDebugEnabled())  logger.debug("================ INT ======");
            if (logger.isDebugEnabled())  logger.debug("0 - " + (obeanSpr.getTotOcurrencias()-1));
            for (int i=0; i<obeanSpr.getTotOcurrencias();i++) {
                roc_s[i] = new InputIngresoRoc();
                roc_s[i].setCodSistema(obeanSpr.getInstanciaDeConsultaSpr(i).getCodSistemaCpt());
                roc_s[i].setCodEvento(obeanSpr.getInstanciaDeConsultaSpr(i).getCodEventoCpt());
                roc_s[i].setCodigoConcepto(obeanSpr.getInstanciaDeConsultaSpr(i).getCodConceptoCpt());
                roc_s[i].setCodModalidad(obeanSpr.getInstanciaDeConsultaSpr(i).getCodSubConcepto2Cpt());
                roc_s[i].setCaiPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getCaiConceptoCpt());
                roc_s[i].setIicPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getIicConceptoCpt());
                roc_s[i].setFechaPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getFecIniVigenciaCpt());
                roc_s[i].setHoraPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getHraIniVigenciaCpt());
                roc_s[i].setIndCobro(obeanSpr.getInstanciaDeConsultaSpr(i).getCodSubConcepto3Cpt());
                roc_s[i].setIndTipoPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getIndTipoPlantillaCpt());
                roc_s[i].setCodigoMoneda(obeanSpr.getInstanciaDeConsultaSpr(i).getCodMonedaCpt());
                roc_s[i].setTasaMontoInformado(obeanSpr.getInstanciaDeConsultaSpr(i).getValTasaMonto());
                roc_s[i].setIndTipTasBas(obeanSpr.getInstanciaDeConsultaSpr(i).getIndTipTasBas());
                roc_s[i].setIndPerBasTas(obeanSpr.getInstanciaDeConsultaSpr(i).getIndPerBasTas());
                roc_s[i].setInsBajPerBas(obeanSpr.getInstanciaDeConsultaSpr(i).getInsBajPerBas());
                roc_s[i].setIndSobPerBas(obeanSpr.getInstanciaDeConsultaSpr(i).getIndSobPerBas());
                roc_s[i].setIndBasTasVar(obeanSpr.getInstanciaDeConsultaSpr(i).getIndBasTasVar());
                roc_s[i].setIndTipFecPerRep(obeanSpr.getInstanciaDeConsultaSpr(i).getIndTipFecPerRep());
                roc_s[i].setNumPerRep(obeanSpr.getInstanciaDeConsultaSpr(i).getNumPerRep());
                roc_s[i].setIndPerRep(obeanSpr.getInstanciaDeConsultaSpr(i).getIndPerRep());
                roc_s[i].setCostoFondo(obeanSpr.getInstanciaDeConsultaSpr(i).getCostoFondoInformado());
                roc_s[i].setFactorDeRiesgo((double) obeanSpr.getInstanciaDeConsultaSpr(i).getFactorRiesgoInformado());
                roc_s[i].setTasaMontoFinal(obeanSpr.getInstanciaDeConsultaSpr(i).getValMonto());
                roc_s[i].setIndVigencia('S');
                if (obeanSpr.getInstanciaDeConsultaSpr(i).getIndTipoPlantillaCpt()=='P' && i==0) {
                    roc_s[0].setTasaMontoFinal(obeanSpr.getInstanciaDeConsultaSpr(0).getValPropuesto());
                };
                if (logger.isDebugEnabled()){  
	                logger.debug("roc_s[" + i + "].setCodigoConcepto()=" + roc_s[i].getCodigoConcepto());
	                logger.debug("roc_s[" + i + "].setCaiPlantilla()=" + roc_s[i].getCaiPlantilla());
	                logger.debug("roc_s[" + i + "].setIicPlantilla()=" + roc_s[i].getIicPlantilla());
	                logger.debug("roc_s[" + i + "].setTasaMontoInformado()=" + roc_s[i].getTasaMontoInformado());
	                logger.debug("roc_s[" + i + "].setTasaMontoFinal()=" + roc_s[i].getTasaMontoFinal());
	                logger.debug("roc_s[" + i + "].setCostoFondo()" + roc_s[i].getCostoFondo());
                }
                j=i;
            };


            if (logger.isDebugEnabled()){  
	            logger.debug("diasEnMora=" + diasEnMora);
	            logger.debug("hayCargoMora=" + hayCargoMora);
            }
            if (diasEnMora>0 && hayCargoMora) { //ingresamos roc especial con descuento y aplicamos cambio a roc[0]
                if (logger.isDebugEnabled())  logger.debug("hayCargoMora");
                j = j+1;
                roc_s[j] = new InputIngresoRoc();
                roc_s[j].setCodSistema(obeanSpr.getInstanciaDeConsultaSpr(0).getCodSistemaCpt());
                roc_s[j].setCodEvento(obeanSpr.getInstanciaDeConsultaSpr(0).getCodEventoCpt());
                roc_s[j].setCodigoConcepto(obeanSpr.getInstanciaDeConsultaSpr(0).getCodConceptoCpt());
                roc_s[j].setCaiPlantilla(res_ctx.getEjecutivo().trim().substring(0,4));
                roc_s[j].setIicPlantilla(res_ctx.getEjecutivo().trim().substring(4));
                roc_s[j].setFechaPlantilla(fechaCurse);
                roc_s[j].setHoraPlantilla("080000");
                roc_s[j].setIndTipoPlantilla('X'); //plantilla de porcentaje de cargos
                roc_s[j].setCodigoMoneda(obeanSpr.getInstanciaDeConsultaSpr(0).getCodMonedaCpt());
                roc_s[j].setTasaMontoInformado(porcCargoTasaMora);
                roc_s[j].setTasaMontoFinal(roc_s[0].getTasaMontoFinal() * (1 + porcCargoTasaMora));
                roc_s[j].setIndVigencia('S');
                roc_s[0].setTasaMontoFinal(roc_s[0].getTasaMontoFinal() * (1 + porcCargoTasaMora));
                //Por consistencia modificamos en opc tasa Spread
                opc_s[0].setTasaSprea(roc_s[0].getTasaMontoFinal() * (1 + porcCargoTasaMora));
                if (logger.isDebugEnabled()){  
	                logger.debug("roc_s[" + j + "].setCodSistema()=" + roc_s[j].getCodSistema());
	                logger.debug("roc_s[" + j + "].setCodEvento()=" + roc_s[j].getCodEvento());
	                logger.debug("roc_s[" + j + "].setCodigoConcepto()=" + roc_s[j].getCodigoConcepto());
	                logger.debug("roc_s[" + j + "].setCaiPlantilla()=" + roc_s[j].getCaiPlantilla());
	                logger.debug("roc_s[" + j + "].setIicPlantilla()=" + roc_s[j].getIicPlantilla());
	                logger.debug("roc_s[" + j + "].setFechaPlantilla()=" + roc_s[j].getFechaPlantilla());
	                logger.debug("roc_s[" + j + "].setHoraPlantilla()=" + roc_s[j].getHoraPlantilla());
	                logger.debug("roc_s[" + j + "].setIndTipoPlantilla()=" + roc_s[j].getIndTipoPlantilla());
	                logger.debug("roc_s[" + j + "].setTasaMontoInformado()=" + roc_s[j].getTasaMontoInformado());
	                logger.debug("roc_s[" + j + "].setTasaMontoFinal()=" + roc_s[j].getTasaMontoFinal());
                }
            }
            if (logger.isDebugEnabled())  logger.debug("================ PPC ======");
            if (obeanPpc==null || obeanPpc.getTotOcurrencias()==0) {
                if (logger.isDebugEnabled())  logger.debug("PPC: SPR nulo o 0 ocurrencias");
            } else {
                for (int i=0; i<(obeanPpc.getTotOcurrencias());i++) {
                    j = j+1;
                    if (logger.isDebugEnabled())  logger.debug("set roc_s[" + j + "] ppc");
                    roc_s[j] = new InputIngresoRoc();
                    roc_s[j].setCodSistema(obeanPpc.getInstanciaDeConsultaSpr(i).getCodSistemaCpt());
                    roc_s[j].setCodEvento(obeanPpc.getInstanciaDeConsultaSpr(i).getCodEventoCpt());
                    roc_s[j].setCodigoConcepto(obeanPpc.getInstanciaDeConsultaSpr(i).getCodConceptoCpt());
                    roc_s[j].setCodModalidad(obeanPpc.getInstanciaDeConsultaSpr(i).getCodSubConcepto2Cpt());
                    roc_s[j].setCaiPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getCaiConceptoCpt());
                    roc_s[j].setIicPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getIicConceptoCpt());
                    roc_s[j].setFechaPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getFecIniVigenciaCpt());
                    roc_s[j].setHoraPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getHraIniVigenciaCpt());
                    roc_s[j].setIndCobro(obeanPpc.getInstanciaDeConsultaSpr(i).getCodSubConcepto3Cpt());
                    roc_s[j].setIndTipoPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getIndTipoPlantillaCpt());
                    roc_s[j].setCodigoMoneda(obeanPpc.getInstanciaDeConsultaSpr(i).getCodMonedaCpt());
                    roc_s[j].setIndVigencia('S');
                    if (logger.isDebugEnabled()){ 
	                    logger.debug("roc_s[" + j + "].setCodSistema()=" + roc_s[i].getCodSistema());
	                    logger.debug("roc_s[" + j + "].setCodEvento()=" + roc_s[i].getCodEvento());
	                    logger.debug("roc_s[" + j + "].setCodigoConcepto()=" + roc_s[i].getCodigoConcepto());
	                    logger.debug("roc_s[" + j + "].setCodModalidad()=" + roc_s[i].getCodModalidad());
	                    logger.debug("roc_s[" + j + "].setCaiPlantilla()=" + roc_s[i].getCaiPlantilla());
	                    logger.debug("roc_s[" + j + "].setIicPlantilla()=" + roc_s[i].getIicPlantilla());
	                    logger.debug("roc_s[" + j + "].setFechaPlantilla()=" + roc_s[i].getFechaPlantilla());
	                    logger.debug("roc_s[" + j + "].setHoraPlantilla()=" + roc_s[i].getHoraPlantilla());
	                    logger.debug("roc_s[" + j + "].setIndCobro()=" + roc_s[i].getIndCobro());
	                    logger.debug("roc_s[" + j + "].setIndTipoPlantilla()=" + roc_s[i].getIndTipoPlantilla());
	                    logger.debug("roc_s[" + j + "].setCodigoMoneda()=" + roc_s[i].getCodigoMoneda());
	                    logger.debug("roc_s[" + j + "].setIndVigencia()=" + roc_s[i].getIndVigencia());
	                  }
                }
            }

                if (logger.isDebugEnabled())  logger.debug("======== SEGUROS RENOVACION ======");
                if (obeanSprSGS == null || obeanSprSGS.getTotOcurrencias() == 0) {
                    if (logger.isDebugEnabled())  logger.debug("SGS: SPR nulo o 0 ocurrencias");
                } else {
                    if (logger.isDebugEnabled())  logger.debug("roc[" + j + "] SGS ...");
                    for(int i = 0; i < obeanSprSGS.getTotOcurrencias(); i++){
                        j = j+1;
                        /** Seteo del objeto roc_s, que será enviado a el ingreso de una OPC */
                        if (logger.isDebugEnabled()){  
	                        logger.debug("****************************************************************************************");
	                        logger.debug("set roc_s[" + j + "] seguros");
	                    }
                        roc_s[j] = new InputIngresoRoc();
                        roc_s[j].setCodSistema(arregloSPR[i].getCodSistemaCpt());                       // String
                        roc_s[j].setCodEvento(arregloSPR[i].getCodEventoCpt());                         // String
                        roc_s[j].setCodigoConcepto(arregloSPR[i].getCodConceptoCpt());                  // String
                        roc_s[j].setCodigoSubConcepto(arregloSPR[i].getCodSubConcepto1Cpt());           // String
                        roc_s[j].setCodModalidad(arregloSPR[i].getCodSubConcepto2Cpt());                // String
                        roc_s[j].setIndCobro(arregloSPR[i].getCodSubConcepto3Cpt());                    // String
                        roc_s[j].setCaiPlantilla(arregloSPR[i].getCaiConceptoCpt());                    // String
                        roc_s[j].setIicPlantilla(arregloSPR[i].getIicConceptoCpt());                    // String
                        roc_s[j].setIndTipoPlantilla(arregloSPR[i].getIndTipoPlantillaCpt());           // char
                        roc_s[j].setCodigoMoneda(arregloSPR[i].getCodMonedaCpt());                      // String
                        roc_s[j].setFechaPlantilla(arregloSPR[i].getFecIniVigenciaCpt());               // Date
                        roc_s[j].setHoraPlantilla(arregloSPR[i].getHraIniVigenciaCpt());                // String

                        if(arregloSPR[i].getIndSeleccionado() == 'S')
                            roc_s[j].setIndVigencia('S');                                               // char
                        else
                            if(arregloSPR[i].getIndSeleccionado() == 'P' && vectorSGS.contains("SIN_SEGUROS"))
                                roc_s[j].setIndVigencia('S');                                           // char
                            else
                                if(vectorSGS.contains(arregloSPR[i].getCodSubConcepto1Cpt() + arregloSPR[i].getCodSubConcepto3Cpt()))
                                    roc_s[j].setIndVigencia('S');                                       // char
                                else
                                    roc_s[j].setIndVigencia('N');                                       // char

                        roc_s[j].setRutCliente(Integer.parseInt(arregloSPR[i].getIicContextoRcc()));    // int
                        roc_s[j].setDigitoVerificador(arregloSPR[i].getCaiContextoRcc().charAt(0));     // char

                        roc_s[j].setTasaMontoFinal(arregloSPR[i].getValMonto());                        // double
                        roc_s[j].setIndTipTasBas(arregloSPR[i].getIndTipTasBas());                      // char
                        roc_s[j].setIndPerBasTas(arregloSPR[i].getIndPerBasTas());                      // char
                        roc_s[j].setInsBajPerBas(arregloSPR[i].getInsBajPerBas());                      // char
                        roc_s[j].setIndSobPerBas(arregloSPR[i].getIndSobPerBas());                      // char
                        roc_s[j].setTasaMontoInformado(0D);
                        roc_s[j].setIndBasTasVar(arregloSPR[i].getIndBasTasVar());                      // String
                        roc_s[j].setIndTipFecPerRep(arregloSPR[i].getIndTipFecPerRep());                // char
                        roc_s[j].setNumPerRep(arregloSPR[i].getNumPerRep());                            // int
                        roc_s[j].setIndPerRep(arregloSPR[i].getIndPerRep());                            // char
                        roc_s[j].setCostoFondo(arregloSPR[i].getCostoFondoInformado());                 // double
                        roc_s[j].setFactorDeRiesgo(arregloSPR[i].getFactorRiesgoInformado());           // double

                        if (logger.isDebugEnabled()){  
	                        logger.debug("roc_s["+ j +"].setCodSistema()             [" + roc_s[j].getCodSistema()               + "]");
	                        logger.debug("roc_s["+ j +"].setCodEvento()              [" + roc_s[j].getCodEvento()                + "]");
	                        logger.debug("roc_s["+ j +"].setCodigoConcepto()         [" + roc_s[j].getCodigoConcepto()           + "]");
	                        logger.debug("roc_s["+ j +"].setCodigoSubConcepto()      [" + roc_s[j].getCodigoSubConcepto()        + "]");
	                        logger.debug("roc_s["+ j +"].setCodModalidad()           [" + roc_s[j].getCodModalidad()             + "]");
	                        logger.debug("roc_s["+ j +"].setIndCobro()               [" + roc_s[j].getIndCobro()                 + "]");
	                        logger.debug("roc_s["+ j +"].setCaiPlantilla()           [" + roc_s[j].getCaiPlantilla()             + "]");
	                        logger.debug("roc_s["+ j +"].setIicPlantilla()           [" + roc_s[j].getIicPlantilla()             + "]");
	                        logger.debug("roc_s["+ j +"].setIndTipoPlantilla()       [" + roc_s[j].getIndTipoPlantilla()         + "]");
	                        logger.debug("roc_s["+ j +"].setCodigoMoneda()           [" + roc_s[j].getCodigoMoneda()             + "]");
	                        logger.debug("roc_s["+ j +"].setFechaPlantilla()         [" + roc_s[j].getFechaPlantilla()           + "]");
	                        logger.debug("roc_s["+ j +"].setHoraPlantilla()          [" + roc_s[j].getHoraPlantilla()            + "]");
	                        logger.debug("roc_s["+ j +"].setIndVigencia()            [" + roc_s[j].getIndVigencia()              + "]");
	                        logger.debug("roc_s["+ j +"].setRutCliente()             [" + roc_s[j].getRutCliente()               + "]");
	                        logger.debug("roc_s["+ j +"].setDigitoVerificador()      [" + roc_s[j].getDigitoVerificador()        + "]");
	                        logger.debug("roc_s["+ j +"].setTasaMontoFinal()         [" + roc_s[j].getTasaMontoFinal()           + "]");
	                        logger.debug("roc_s["+ j +"].setIndTipTasBas()           [" + roc_s[j].getIndTipTasBas()             + "]");
	                        logger.debug("roc_s["+ j +"].setIndPerBasTas()           [" + roc_s[j].getIndPerBasTas()             + "]");
	                        logger.debug("roc_s["+ j +"].setInsBajPerBas()           [" + roc_s[j].getInsBajPerBas()             + "]");
	                        logger.debug("roc_s["+ j +"].setIndSobPerBas()           [" + roc_s[j].getIndSobPerBas()             + "]");
	                        logger.debug("roc_s["+ j +"].setTasaMontoInformado()     [" + roc_s[j].getTasaMontoInformado()       + "]");
	                        logger.debug("roc_s["+ j +"].setIndBasTasVar()           [" + roc_s[j].getIndBasTasVar()             + "]");
	                        logger.debug("roc_s["+ j +"].setIndTipFecPerRep()        [" + roc_s[j].getIndTipFecPerRep()          + "]");
	                        logger.debug("roc_s["+ j +"].setNumPerRep()              [" + roc_s[j].getNumPerRep()                + "]");
	                        logger.debug("roc_s["+ j +"].setIndPerRep()              [" + roc_s[j].getIndPerRep()                + "]");
	                        logger.debug("roc_s["+ j +"].setCostoFondo()             [" + roc_s[j].getCostoFondo()               + "]");
	                        logger.debug("roc_s["+ j +"].setFactorDeRiesgo()         [" + roc_s[j].getFactorDeRiesgo()           + "]");
                        }
                        /** Seteo objeto roc_s, que será enviado a el ingreso de una OPC */
                        roc_sAmpliada[i] = new InputIngresoRocAmpliada();
                        roc_sAmpliada[i].setCaiOperacion(roc_s[j].getCaiOperacion());
                        roc_sAmpliada[i].setIicOperacion(roc_s[j].getIicOperacion());
                        roc_sAmpliada[i].setExtOperacion(roc_s[j].getExtOperacion());
                        roc_sAmpliada[i].setCodSistema(roc_s[j].getCodSistema());
                        roc_sAmpliada[i].setCodEvento(roc_s[j].getCodEvento());
                        roc_sAmpliada[i].setNumero(roc_s[j].getNumero());
                        roc_sAmpliada[i].setCodigoConcepto(roc_s[j].getCodigoConcepto());
                        roc_sAmpliada[i].setCodigoSubConcepto(roc_s[j].getCodigoSubConcepto());
                        roc_sAmpliada[i].setCodModalidad(roc_s[j].getCodModalidad());
                        roc_sAmpliada[i].setIndCobro(roc_s[j].getIndCobro());
                        roc_sAmpliada[i].setCaiPlantilla(roc_s[j].getCaiPlantilla());
                        roc_sAmpliada[i].setIicPlantilla(roc_s[j].getIicPlantilla());
                        roc_sAmpliada[i].setIndTipoPlantilla(roc_s[j].getIndTipoPlantilla());
                        roc_sAmpliada[i].setCodigoMoneda(roc_s[j].getCodigoMoneda());
                        roc_sAmpliada[i].setFechaPlantilla(roc_s[j].getFechaPlantilla());
                        roc_sAmpliada[i].setHoraPlantilla(roc_s[j].getHoraPlantilla());
                        roc_sAmpliada[i].setIndVigencia(roc_s[j].getIndVigencia());
                        roc_sAmpliada[i].setCodAnulacion(roc_s[j].getCodAnulacion());
                        roc_sAmpliada[i].setFechaUno(roc_s[j].getFechaUno());
                        roc_sAmpliada[i].setRutCliente(roc_s[j].getRutCliente());
                        roc_sAmpliada[i].setDigitoVerificador(roc_s[j].getDigitoVerificador());
                        roc_sAmpliada[i].setFechaDos(roc_s[j].getFechaDos());
                        roc_sAmpliada[i].setTasaMontoFinal(roc_s[j].getTasaMontoFinal());
                        roc_sAmpliada[i].setIndTipTasBas(roc_s[j].getIndTipTasBas());
                        roc_sAmpliada[i].setIndPerBasTas(roc_s[j].getIndPerBasTas());
                        roc_sAmpliada[i].setInsBajPerBas(roc_s[j].getInsBajPerBas());
                        roc_sAmpliada[i].setIndSobPerBas(roc_s[j].getIndSobPerBas());
                        roc_sAmpliada[i].setTasaMontoInformado(roc_s[j].getTasaMontoInformado());
                        roc_sAmpliada[i].setIndBasTasVar(roc_s[j].getIndBasTasVar());
                        roc_sAmpliada[i].setIndTipFecPerRep(roc_s[j].getIndTipFecPerRep());
                        roc_sAmpliada[i].setNumPerRep(roc_s[j].getNumPerRep());
                        roc_sAmpliada[i].setIndPerRep(roc_s[j].getIndPerRep());
                        roc_sAmpliada[i].setCostoFondo(roc_s[j].getCostoFondo());
                        roc_sAmpliada[i].setFactorDeRiesgo(roc_s[j].getFactorDeRiesgo());
                        roc_sAmpliada[i].setDescuentoAcumulado(0D); // double
                        roc_sAmpliada[i].setDescuentoAplicado(roc_s[j].getTasaMontoInformado());   //Double
                        roc_sAmpliada[i].setGlosaTipoSeguro((TablaValores.getValor("multilinea.parametros", "seguros" + roc_s[j].getCodigoSubConcepto(), roc_s[j].getCodigoSubConcepto())));
                        roc_sAmpliada[i].setGlosaTipoCobro((TablaValores.getValor("multilinea.parametros", "tipoCobro" + arregloSPR[i].getCodSubConcepto3Cpt(), arregloSPR[i].getCodSubConcepto3Cpt())));
                        roc_sAmpliada[i].setIndSegObligatorio(arregloSPR[i].getIndSeleccionado()); // char
                        roc_sAmpliada[i].setTasaMinima(0D); // double
                        roc_sAmpliada[i].setDescuentoMaximo(0D); // double
                        roc_sAmpliada[i].setValMonto(arregloSPR[i].getValMonto()); // double
                        roc_sAmpliada[i].setTmcInformado(arregloSPR[i].getTmcInformado()); // double

                        //seteamos aval si lo hubiere para informacion DPS
                        roc_sAmpliada[i].setCodFactorRiesgoRcc(arregloSPR[i].getCodFactorRiesgoRcc());
                        roc_sAmpliada[i].setCodClfRentabilidadRcc(arregloSPR[i].getCodClfRentabilidadRcc());

                        if (rutAvalDPS > 0 ) {
                        roc_sAmpliada[i].setNumClienteRcc(rutAvalDPS);
                        roc_sAmpliada[i].setVrfClienteRcc(rutdigAvalDPS);
                        }

                    }
                }
           
                
                if (obeanSprCGN == null || obeanSprCGN.getTotOcurrencias() == 0) {
                	 if (getLogger().isDebugEnabled()){
                		 getLogger().debug("[renovacionMultilinea] No existen ocurrencias en la consulta SPR para Gastos Notariales");
                	 }
                } 
                else {
                	for(int i = 0; i < obeanSprCGN.getTotOcurrencias(); i++){
                		if (obeanSprCGN.getInstanciaDeConsultaSpr(i) == null){
                			if (getLogger().isDebugEnabled()){
                				getLogger().debug("[renovacionMultilinea] Saliendo del llenando vector de SPR's Gastos Notariales");
                			}
                			break;
                		}
                		else{
                			vectorSPR.add(obeanSprCGN.getInstanciaDeConsultaSpr(i));
                			valorNotario = obeanSprCGN.getInstanciaDeConsultaSpr(i).getValMonto();
                			if (getLogger().isDebugEnabled()){ 
                				getLogger().debug("[renovacionMultilinea] llenando vector de SPR's :" + i);
                				getLogger().debug("[renovacionMultilinea] valorNotario: " +  valorNotario);
                			}
                			break;
                		}
                	}
                }


                if (getLogger().isDebugEnabled()) { 
                	getLogger().debug("[renovacionMultilinea] ================ CGN ======"); 
                }
                if (obeanSprCGN==null || obeanSprCGN.getTotOcurrencias()==0) {
                	if (getLogger().isDebugEnabled()) { 
                		getLogger().debug("[renovacionMultilinea] CGN: SPR nulo o 0 ocurrencias."); 
                	}
                } 
                else {

                	for(int i = 0; i < obeanSprCGN.getTotOcurrencias(); i++){
                		if (obeanSprCGN.getInstanciaDeConsultaSpr(i) == null){
                			if (getLogger().isDebugEnabled()) { 
                				getLogger().debug("[renovacionMultilinea] Saliendo del llenando vector de SPR's Gastos Notariales");
                			}
                			break;
                		}
                		else{
                			j = j+1;
                			if (getLogger().isDebugEnabled()) { 
                				getLogger().debug("[renovacionMultilinea] set rocS[" + j + "] ppc"); 
                			}
                			roc_s[j] = new InputIngresoRoc();
                			roc_s[j].setCodSistema(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodSistemaCpt());
                			roc_s[j].setCodEvento(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodEventoCpt());
                			roc_s[j].setCodigoConcepto(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodConceptoCpt());
                			roc_s[j].setCodigoSubConcepto(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodSubConcepto1Cpt());
                			roc_s[j].setCodModalidad(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodSubConcepto2Cpt());
                			roc_s[j].setCaiPlantilla(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCaiConceptoCpt());
                			roc_s[j].setIicPlantilla(obeanSprCGN.getInstanciaDeConsultaSpr(i).getIicConceptoCpt());
                			roc_s[j].setFechaPlantilla(obeanSprCGN.getInstanciaDeConsultaSpr(i).getFecIniVigenciaCpt());
                			roc_s[j].setHoraPlantilla(obeanSprCGN.getInstanciaDeConsultaSpr(i).getHraIniVigenciaCpt());
                			roc_s[j].setIndCobro(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodSubConcepto3Cpt());
                			roc_s[j].setIndTipoPlantilla(obeanSprCGN.getInstanciaDeConsultaSpr(i).getIndTipoPlantillaCpt());
                			roc_s[j].setCodigoMoneda(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodMonedaCpt());
                			roc_s[j].setIndVigencia(obeanSprCGN.getInstanciaDeConsultaSpr(i).getIndSeleccionado());
                			roc_s[j].setTasaMontoFinal(obeanSprCGN.getInstanciaDeConsultaSpr(i).getValMonto()); 

                			if (getLogger().isDebugEnabled()){
                				getLogger().debug("roc_s[" + (j) + "].setCodEvento()=" + roc_s[j].getCodEvento());
                				getLogger().debug("roc_s[" + (j) + "].setCodigoConcepto()=" + roc_s[j].getCodigoConcepto());
                				getLogger().debug("roc_s[" + (j) + "].setCodigoSubConcepto()=" + roc_s[j].getCodigoSubConcepto());
                				getLogger().debug("roc_s[" + (j) + "].setCodModalidad()=" + roc_s[j].getCodModalidad());
                				getLogger().debug("roc_s[" + (j) + "].setCaiPlantilla()=" + roc_s[j].getCaiPlantilla());
                				getLogger().debug("roc_s[" + (j) + "].setIicPlantilla()=" + roc_s[j].getIicPlantilla());
                				getLogger().debug("roc_s[" + (j) + "].setFechaPlantilla()=" + roc_s[j].getFechaPlantilla());
                				getLogger().debug("roc_s[" + (j) + "].setHoraPlantilla()=" + roc_s[j].getHoraPlantilla());
                				getLogger().debug("roc_s[" + (j) + "].setIndCobro()=" + roc_s[j].getIndCobro());
                				getLogger().debug("roc_s[" + (j) + "].setIndTipoPlantilla()=" + roc_s[j].getIndTipoPlantilla());
                				getLogger().debug("roc_s[" + (j) + "].setCodigoMoneda()=" + roc_s[j].getCodigoMoneda());
                				getLogger().debug("roc_s[" + (j) + "].setIndVigencia()=" + roc_s[j].getIndVigencia());
                				getLogger().debug("roc_s[" + (j) + "].setTasaMontoFinal()=" + roc_s[j].getTasaMontoFinal());
                			}
                			break;
                		}
                	}

                }    

            if (logger.isDebugEnabled())  logger.debug("========== LIQ REN ============");
            String    caiOperacion_LIQ = null;
            int       iicOperacion_LIQ;
            InputLiquidacionDeOperacionDeCreditoOpc liq_opc = new InputLiquidacionDeOperacionDeCreditoOpc();
            liq_opc.setCim_reqnum("032");
            liq_opc.setCim_uniqueid(origen);
            liq_opc.setCim_indseq(indseq);
            if (logger.isDebugEnabled())  logger.debug("========== liquidacionCredito ============");
            ResultLiquidacionDeOperacionDeCreditoOpc resLiqOpc = new ResultLiquidacionDeOperacionDeCreditoOpc();
            if (logger.isDebugEnabled())  logger.debug("========== antes multi ============");
            if (idReq.equals("096")){
                multiEnvironment.setIndreq(0,'1');//pos(0)=RollBack? --> '0'=No '1'=Si
                multiEnvironment.setIndreq(1,'0');//pos(1)=Con ACA?  --> '0'=No '1'=Si
                if (logger.isDebugEnabled())  logger.debug("es 096");
            }
            if (idReq.equals("097")){
                multiEnvironment.setIndreq(0,'0');//pos(0)=RollBack? --> '0'=No '1'=Si
                multiEnvironment.setIndreq(1,'1');//pos(1)=Con ACA?  --> '0'=No '1'=Si
                if (logger.isDebugEnabled())  logger.debug("es 097");
            }
            multiEnvironment.setIndreq(2,'1');//pos(2)=Solo BD Mañana?  --> '0'=No '1'=Si
//            multiEnvironment.setIndreq(5,'2');  // renovacionMultilinea   0=normal      1=valide ejecutivo inexistente

            if (logger.isDebugEnabled())  logger.debug("========== antes operaCredito ============");
            resLiqOpc =  operaCredito(multiEnvironment,
                                       can_s,
                                       opc_s,
                                       rdc_s,
                                       dlc_s,
                                       cya_s,
                                       evc_s,
                                       icg_s,
                                       ven_s,
                                       roc_s,
                                       liq_opc);

            //******************************************//
            //********  FIN renovacionCredito **********//
            //******************************************//
            if (logger.isDebugEnabled())  logger.debug("========== despues operaCredito REN ============");
            obean.setResultLiqOpc(resLiqOpc);
            if (logger.isDebugEnabled())  logger.debug("Set Roc Ampliada...renovacion");
            obean.setArregloROC(roc_sAmpliada);

            if (logger.isDebugEnabled())  logger.debug("llenando 'tasa web' [" + obeanSpr.getInstanciaDeConsultaSpr(0).getValPropuesto() + "]");
            //si la tasa es anualizada (A o X) se 'muestra' la tasa dividida por 12 (CFRANCO)
            if (logger.isDebugEnabled())  logger.debug("valor de 'IndPerBasTas' [" + obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas() + "]");
            if (obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas()=='A' || obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas()=='X') {
                obean.setTasa(opc_s[0].getTasaSprea()/12);
                if (logger.isDebugEnabled())  logger.debug("llenando 'tasa' [" + opc_s[0].getTasaSprea()/12 + "]");
            } else {
                obean.setTasa(opc_s[0].getTasaSprea());
            }
            if (logger.isDebugEnabled())  logger.debug("llenando 'tasa orig' ...(" + obeanSpr.getInstanciaDeConsultaSpr(0).getValMonto() + ")");
            if (obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas()=='A' || obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas()=='X') {
                if (logger.isDebugEnabled())  logger.debug("llenando 'tasa orig' ...(" + obeanSpr.getInstanciaDeConsultaSpr(0).getValMonto()/12 + ")");
                obean.setTasaOriginal(obeanSpr.getInstanciaDeConsultaSpr(0).getValMonto()/12);
            } else {
                obean.setTasaOriginal(obeanSpr.getInstanciaDeConsultaSpr(0).getValMonto());
            }
            if (logger.isDebugEnabled())  logger.debug("llenando 'fechaCurse' [" + fechaCurse + "]");
            obean.setFechaCurse(fechaCurse);
            if (logger.isDebugEnabled())  logger.debug("llenando 'valorNotario' [" + valorNotario + "]");
            obean.setValorGastoNotarial(valorNotario);
            if ((datosLog != null) && idReq.equals("097")) {
                //ResultConsultaOperacionCredito obean_opc = consultaOperacionCredito(multiEnvironment, obean.getCaiOperacion(), obean.getIicOperacion());
                datosLog.put("tipoOpe", "ROK");
                if (logger.isDebugEnabled())  logger.debug("res_ctx.getEjecutivo()=[" + res_ctx.getEjecutivo() + "]");
                if (logger.isDebugEnabled())  logger.debug("datosLog.get(CAI)=[" + datosLog.get("CAI") + "]");
                if (logger.isDebugEnabled())  logger.debug("datosLog.get(IIC)=[" + datosLog.get("IIC") + "]");
                datosLog.put("CAI", resLiqOpc.getCaiOperacion());
                datosLog.put("IIC", String.valueOf(resLiqOpc.getIicOperacion()));
                datosLog.put("MSG", resLiqOpc.getCim_respuesta());
                if (logger.isDebugEnabled())  logger.debug("datosLog.get(CAI)=[" + datosLog.get("CAI") + "]");
                if (logger.isDebugEnabled())  logger.debug("datosLog.get(IIC)=[" + datosLog.get("IIC") + "]");
                try{
                	registraLogMultilinea(datosLog);
                }
                catch(Exception e){
                	if (logger.isDebugEnabled())  logger.debug("ERROR registraLogMultilinea" + datosLog.toString());
                }
                if (logger.isDebugEnabled())  logger.debug("FIN 097***public ResultRenovacionMultilinea ingresoRenovacionMultilinea...");
            } else {
                if (logger.isDebugEnabled())  logger.debug("FIN 096***public ResultRenovacionMultilinea ingresoRenovacionMultilinea...");
            }
            if (logger.isDebugEnabled())  logger.debug("************* Fin renovacionMultilinea en MultilineaBean *************");
            return obean;
        }catch(Exception e) {
        	if (logger.isEnabledFor(Level.ERROR)) logger.error("ERROR***public ResultRenovacionMultilinea ingresoRenovacionMultilinea...Exception [" + (!controlError.equals("") ? controlError + " " : "") + e.getMessage() + "]");
            if (datosLog!=null) {
                datosLog.put("ERR",datosLog.get("ERR")==null? Utils.hackle(e.getMessage()): Utils.hackle(e.getMessage()) + datosLog.get("ERR"));
                registraLogMultilinea(datosLog);
            }
            else {
            	if (logger.isEnabledFor(Level.ERROR)) logger.error("datosLog es null");
            }
            throw new MultilineaException("ESPECIAL", (!controlError.equals("") ? controlError + " " : "") + e.getMessage());
        }

    }

    /**
     * Registra en tabla MLO Log Multilinea
     *
     * Registro de versiones:<ul>
     * <li>1.0 26/07/2004 Carlos Panozo    (Bee)- version inicial
     * <li>1.1 06/01/2005 Carlos Panozo    (Bee)- registraLogMultilinea --> private a public, agrega codigo "SGT"
     * <li>1.2 02/05/2005 Hector Carranza  (Bee)- Agrega codigo "ABK" en LOG MLO, aumenta tamaño msgLog de 255 a 512
     *
     * </ul>
     * <p>
     *
     * @param datosLog
     * @return {@link ResultActualizacionTablaLOG}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.2
     */
    public ResultActualizacionTablaLOG registraLogMultilinea(Hashtable datosLog) throws MultilineaException, EJBException {

        try {

            BCIExpress     bciexpressBean = null;
            InitialContext ic             = JNDIConfig.getInitialContext();
            Object         obj_exp        = ic.lookup(JNDI_NAME_BEX);
            BCIExpressHome home_exp       = (BCIExpressHome) PortableRemoteObject.narrow(obj_exp, BCIExpressHome.class);

            int descripcionSize           = 255;

            if (logger.isDebugEnabled())  logger.debug("registraLogMultilinea creado");

            StringBuffer descripcion = new StringBuffer();

            if (datosLog.containsKey("CAI"))
                descripcion.append("CAI="+ (String) datosLog.get("CAI"));
            descripcion.append("&");
            if (datosLog.containsKey("IIC"))
                descripcion.append("IIC="+ (String) datosLog.get("IIC"));
            descripcion.append("&");
            if (datosLog.containsKey("OFI"))
                descripcion.append("OFI="+ (String) datosLog.get("OFI"));
            descripcion.append("&");
            if (datosLog.containsKey("MTO"))
                descripcion.append("MTO="+ (String) datosLog.get("MTO"));
            descripcion.append("&");
            if (datosLog.containsKey("MON"))
                descripcion.append("MON="+ (String) datosLog.get("MON"));
            descripcion.append("&");
            if (datosLog.containsKey("PVT"))
                descripcion.append("PVT="+ (String) datosLog.get("PVT"));
            descripcion.append("&");
            if (datosLog.containsKey("PLZ"))
                descripcion.append("PLZ="+ (String) datosLog.get("PLZ"));
            descripcion.append("&");
            if (datosLog.containsKey("CTC"))
                descripcion.append("CTC="+ (String) datosLog.get("CTC"));
            descripcion.append("&");
            if (datosLog.containsKey("ABO"))
                descripcion.append("ABO="+ (String) datosLog.get("ABO"));
            descripcion.append("&");
            if (datosLog.containsKey("CAR"))
                descripcion.append("CAR="+ (String) datosLog.get("CAR"));
            descripcion.append("&");
            if (datosLog.containsKey("CTA"))
                descripcion.append("CTA="+ (String) datosLog.get("CTA"));
            descripcion.append("&");
            if (datosLog.containsKey("TIO"))
                descripcion.append("TIO="+ (String) datosLog.get("TIO"));
            descripcion.append("&");
            if (datosLog.containsKey("AUX"))
                descripcion.append("AUX="+ (String) datosLog.get("AUX"));
            descripcion.append("&");
            if (datosLog.containsKey("CNL"))
                descripcion.append("CNL="+ (String) datosLog.get("CNL"));
            descripcion.append("&");
            if (datosLog.containsKey("CBC"))
                descripcion.append("CBC="+ (String) datosLog.get("CBC"));
            descripcion.append("&");
            if (datosLog.containsKey("PLN"))
                descripcion.append("PLN="+ (String) datosLog.get("PLN"));
            descripcion.append("&");
            if (datosLog.containsKey("OPE"))
                descripcion.append("OPE="+ (String) datosLog.get("OPE"));
            descripcion.append("&");
            if (datosLog.containsKey("EJE"))
                descripcion.append("EJE="+ (String) datosLog.get("EJE"));
            descripcion.append("&");
            if (datosLog.containsKey("SGT"))
                descripcion.append("SGT="+ (String) datosLog.get("SGT"));
            descripcion.append("&");
            if (datosLog.containsKey("ABK"))
                descripcion.append("ABK="+ (String) datosLog.get("ABK"));
            descripcion.append("&");
            if (datosLog.containsKey("MSG"))
                descripcion.append("MSG="+ (String) datosLog.get("MSG"));
            if (datosLog.containsKey("ERR"))
                descripcion.append("ERR="+ (String) datosLog.get("ERR"));

            bciexpressBean = (BCIExpress) PortableRemoteObject.narrow(home_exp.create(), BCIExpress.class);

            if (logger.isDebugEnabled())  logger.debug("registraLogMultilinea");
            String msgLog = descripcion.toString();
            if (logger.isDebugEnabled())  logger.debug("msgLog=[" + msgLog + "]");
            if (msgLog.length()>descripcionSize)
                msgLog = msgLog.substring(0,(descripcionSize-1)); //tamaño máximo campo Sybase
            if (logger.isDebugEnabled())  logger.debug("msgLog=[" + msgLog + "]");

            return bciexpressBean.actualizacionTablaLOG((String)datosLog.get("idConvenio"),
                                                        (String)timestamp_form.format(new Date()),
                                                        (String)datosLog.get("rutEmpresa"),
                                                        ((String)datosLog.get("digitoVerifEmp")).charAt(0),
                                                        (String)datosLog.get("rutUsuario"),
                                                        ((String)datosLog.get("digitoVerifUsu")).charAt(0),
                                                        (String)datosLog.get("tipoOpe"),
                                                        msgLog);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.getMessage());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.getMessage());

            throw new MultilineaException("UNKNOW", e.getMessage());
        }

    }

    /**
     * Obtiene Total Monto Utilizado En Moneda Especifica
     *
     * Registro de versiones:<ul>
     * <li>1.0 28/07/2004 Waldo Iriarte     (Bee)- version inicial
     * <li>1.1 11/11/2004 Carlos Panozo     (Bee)- +LOG secuencia, mejora logica
     * <li>1.2 02/05/2005 Hector Carranza   (Bee)- ...ClienteAmp por ...ClienteSuperAmp
     *
     * </ul>
     * <p>
     *
     * @param obean
     * @param monedaADejar codigo moneda
     * @return double
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.3
     */
    private double obtieneTotalMontoUtilizadoEnMonedaEspecifica(ResultCartolaMultilinea obean, String monedaADejar) throws MultilineaException, EJBException {

        double saldoCreditoTrfmr = 0;
        double montoTransformado = 0;
        double montoUtilizado    = 0;
        String tipoCambio        = "UFP";
        String conversor         = "";
        Date   today             = new Date();
        String fechaHoy          = ddMMyyyy_form.format(today);

        if (logger.isDebugEnabled()){  
	        logger.debug("********************************************************************");
	        logger.debug("*           obtieneTotalMontoUtilizadoEnMonedaEspecifica           *");
	        logger.debug("********************************************************************");
	        logger.debug("La moneda en la que se desea dejar es : '" + monedaADejar + "'");
	        logger.debug("********************************************************************");
        }
        ResultConsultaValoresCambio obeanCambio = new ResultConsultaValoresCambio();

        OperacionCreditoSuperAmp[] operacionesAmp = obean.getResultConsultaOperClienteSuperAmp().getOperacionesSuperAmp();

        if (operacionesAmp != null && operacionesAmp[0] != null) {

            for (int i = 0; i < operacionesAmp.length; i++) {

                if (logger.isDebugEnabled())  logger.debug("operacionesAmp[" + Integer.toString(i) + "]");

                if (!operacionesAmp[i].getCodMonedaCred().trim().equals(monedaADejar.trim())) {

                    if (logger.isDebugEnabled()){  
	                    logger.debug("La moneda a transformar es            : '" + String.valueOf(operacionesAmp[i].getCodMonedaCred()) + "'");
	                    logger.debug("El monto a transformar es             : '" + String.valueOf(operacionesAmp[i].getSaldoCredito()) + "'");
                    }
                    saldoCreditoTrfmr = saldoCreditoTrfmr + operacionesAmp[i].getSaldoCredito();

                    if (operacionesAmp[i].getCodMonedaCred().trim().equals("0999")) {

                        conversor = "/";
                    }
                    else {

                        conversor = "*";
                    }
                }
                else {

                    if (logger.isDebugEnabled())  logger.debug("El monto de la moneda a dejar es      : '" + String.valueOf(operacionesAmp[i].getSaldoCredito()) + "'");

                    montoUtilizado = montoUtilizado + operacionesAmp[i].getSaldoCredito();
                }

            }

            if (logger.isDebugEnabled()){  
	            logger.debug("********************************************************************");
	            logger.debug("El Total a transformar es             : '" + String.valueOf(saldoCreditoTrfmr) + "'");
	            logger.debug("El conversor es                       : '" + conversor + "'");
            }
            if (!conversor.equals("")) {
                if (logger.isDebugEnabled())  logger.debug("Hay conversor ");
                obeanCambio       = obtieneConversionMoneda(tipoCambio, fechaHoy, fechaHoy, conversor, saldoCreditoTrfmr);

                montoTransformado = obeanCambio.getTotalCalculado();

                obean.setValorUFUtilizadoMLT(obeanCambio.getEquivalencia());
                if (logger.isDebugEnabled()){ 
	                logger.debug("El Total transformado es              : '" + String.valueOf(montoTransformado) + "'");
	                logger.debug("El Valor UF Utilizado es              : '" + String.valueOf(obeanCambio.getEquivalencia()) + "'");
                }
                montoUtilizado    = montoUtilizado + montoTransformado;
            }
            if (logger.isDebugEnabled())  logger.debug("El Total sumado es                    : '" + String.valueOf(montoUtilizado) + "'");
        }

        if (logger.isDebugEnabled())  logger.debug("El Total Retornado Es                 : '" + String.valueOf(montoUtilizado) + "'");

        return montoUtilizado;

    }

    /**
     * Consulta CLN ( CLC Mejorada )
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- version inicial
     * <li>1.1 01/09/2004 Carlos Panozo   (Bee)- version inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param nombreDeudor <b>NOMBRE DEUDOR</b>
     * @param rutDeudor <b>IDC DEUDOR</b>
     * @param digitoVerificador <b>IDC DEUDOR</b>
     * @param indicadorExtIdc <b>IDC DEUDOR</b>
     * @param glosaExtIdc <b>IDC DEUDOR</b>
     * @param idOperacion <b>NUMERO</b>
     * @param numOperacion <b>NUMERO</b>
     * @param totLinIngreso <b>TOT. LIN INGRESO</b>
     * @return {@link ResultConsultaCln}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public ResultConsultaCln consultaCln(MultiEnvironment multiEnvironment, String nombreDeudor, int rutDeudor, char digitoVerificador, char indicadorExtIdc, String glosaExtIdc, String idOperacion, int numOperacion, int totLinIngreso) throws MultilineaException, EJBException {

        InputConsultaCln ibean = new InputConsultaCln("025",
                                                      nombreDeudor,
                                                      rutDeudor,
                                                      digitoVerificador,
                                                      indicadorExtIdc,
                                                      glosaExtIdc,
                                                      idOperacion,
                                                      numOperacion,
                                                      totLinIngreso);

        return (ResultConsultaCln) consultaCln(multiEnvironment, ibean, new ResultConsultaCln());

    }

    /**
     * Consulta de Avales
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- version inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param rutDeudor <b>IDC DEUDOR</b>
     * @param digitoVerificaCli <b>IDC DEUDOR</b>
     * @param idCliente <b>IDC DEUDOR</b>
     * @param glosaCliente <b>IDC DEUDOR</b>
     * @param nombreCli <b>NOMBRE DEUDOR</b>
     * @param idOperacion <b>NUMERO LINEA</b>
     * @param numOperacion <b>NUMERO LINEA</b>
     * @param numCorrelativo <b>NUMERO LINEA</b>
     * @param tipoOperacion <b>TIPO OPERACION</b>
     * @param tipoCredito <b>TIPO CREDITO</b>
     * @return {@link ResultConsultaAvales}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public ResultConsultaAvales consultaAvales(MultiEnvironment multiEnvironment, int rutDeudor, char digitoVerificaCli, char idCliente, String glosaCliente, String nombreCli, String idOperacion, int numOperacion, int numCorrelativo, String tipoOperacion, String tipoCredito) throws MultilineaException, EJBException {

        InputConsultaAvales ibean = new InputConsultaAvales("026",
                                                            rutDeudor,
                                                            digitoVerificaCli,
                                                            idCliente,
                                                            glosaCliente,
                                                            nombreCli,
                                                            idOperacion,
                                                            numOperacion,
                                                            numCorrelativo,
                                                            tipoOperacion,
                                                            tipoCredito);

        return (ResultConsultaAvales) consultaAvales(multiEnvironment, ibean, new ResultConsultaAvales());

    }

    /**
     * Consulta CLN ( CLC Mejorada )
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- version inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public Object consultaCln(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled())  logger.debug("getting ControlRiesgoCrediticioLocalHome");

            ControlRiesgoCrediticioLocalHome controlriesgocrediticio_home = (ControlRiesgoCrediticioLocalHome) ic.lookup(JNDI_NAME_CRC);

            if (logger.isDebugEnabled())  logger.debug("ControlRiesgoCrediticioLocalHome creado");

            ControlRiesgoCrediticioLocal controlriesgocrediticio_bean = controlriesgocrediticio_home.create();

            if (logger.isDebugEnabled())  logger.debug("consultaCln");

            return controlriesgocrediticio_bean.consultaCln(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.getMessage());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.getMessage());

            throw new MultilineaException("UNKNOW", e.getMessage());
        }
    }

    /**
     * Consulta de Avales
     *
     * Registro de versiones:<ul>
     * <li>1.0 05/07/2004 Carlos Panozo   (Bee)- version inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.0
     */
    public Object consultaAvales(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled())  logger.debug("getting ControlRiesgoCrediticioLocalHome");

            ControlRiesgoCrediticioLocalHome controlriesgocrediticio_home = (ControlRiesgoCrediticioLocalHome) ic.lookup(JNDI_NAME_CRC);

            if (logger.isDebugEnabled())  logger.debug("ControlRiesgoCrediticioLocalHome creado");

            ControlRiesgoCrediticioLocal controlriesgocrediticio_bean = controlriesgocrediticio_home.create();

            if (logger.isDebugEnabled())  logger.debug("consultaAvales");

            return controlriesgocrediticio_bean.consultaAvales(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

            if (logger.isDebugEnabled())  logger.debug("MultilineaException " + e.getMessage());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.getMessage());

            throw new MultilineaException("UNKNOW", e.getMessage());
        }
    }


    /**
     * Ingreso Operacion Credito Multilinea para el proceso de firma
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial

     *
     * </ul>
     * <p>
     *
     * @param numero <b>num convenio</b>
     * @param rutEmpresa <b>RutEmpresa</b>
     * @param digitoVerifEmp <b>digitoVerifEmp</b>
     * @param tipoOperacion <b>tipoOperacion</b>
     * @param auxiliarOpe <b>codigoAuxiliar</b>
     * @param glosaTipoCredito <b>glosaTipoCredito</b>
     * @param tipoAbono <b>cod abono</b>
     * @param tipoCargoAbono <b>cod cargo</b>
     * @param oficinaIngreso <b>oficinaIngreso</b>
     * @param ctaAbono <b>cta abono</b>
     * @param ctaCargo <b>cta cargo</b>
     * @param indicador <b>indicadorNP01 mes no pago</b>
     * @param indicadorAplic <b>indicadorNP01 mes no pago</b>
     * @param montoCredito <b>montoCredito</b>
     * @param codigoMoneda <b>cod moneda</b>
     * @param totalVencimientos <b>num total vctos</b>
     * @param fechaPrimerVenc <b>fecha primer vcto</b>
     * @param fechaInicio <b>fecha ini curse</b>
     * @param fechaFin <b>fecha fin curse</b>
     * @param estadoSolicit <b>estado solicitud</b>
     * @param procesoNegocio <b>procesoNegocio</b>
     * @param numOperacion <b>cai ope</b>
     * @param auxiliarCredito <b>iic ope</b>
     * @param numOperacionCan <b>cai ope ren</b>
     * @param codAuxiliarCredito <b>iic ope ren</b>
     * @param codigoMoneda2 <b>cod mon orig ren</b>
     * @param monedaLinea <b>glosa mon ori ren</b>
     * @param fecExpiracion <b>fec prim vcto ren</b>
     * @param montoAbonado <b>monto abono capital prorroga</b>
     * @return {@link ResultIngresoOperacionCreditoMultilinea}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public ResultIngresoOperacionCreditoMultilinea ingresoOperacionCreditoMultilinea(String numero, String rutEmpresa, char digitoVerifEmp, String tipoOperacion, String auxiliarOpe, String glosaTipoCredito, String tipoAbono, String tipoCargoAbono, String oficinaIngreso, String ctaAbono, String ctaCargo, String indicador, String indicadorAplic, String montoCredito, String codigoMoneda, String totalVencimientos, String fechaPrimerVenc, String fechaInicio, String fechaFin, String estadoSolicit, String procesoNegocio, String numOperacion, String auxiliarCredito, String numOperacionCan, String codAuxiliarCredito, String codigoMoneda2, String monedaLinea, String fecExpiracion, String montoAbonado) throws MultilineaException, EJBException {

        InputIngresoOperacionCreditoMultilinea ibean = new InputIngresoOperacionCreditoMultilinea(numero,
                                                                                                  rutEmpresa,
                                                                                                  digitoVerifEmp,
                                                                                                  tipoOperacion,
                                                                                                  auxiliarOpe,
                                                                                                  glosaTipoCredito,
                                                                                                  tipoAbono,
                                                                                                  tipoCargoAbono,
                                                                                                  oficinaIngreso,
                                                                                                  ctaAbono,
                                                                                                  ctaCargo,
                                                                                                  indicador,
                                                                                                  indicadorAplic,
                                                                                                  montoCredito,
                                                                                                  codigoMoneda,
                                                                                                  totalVencimientos,
                                                                                                  fechaPrimerVenc,
                                                                                                  fechaInicio,
                                                                                                  fechaFin,
                                                                                                  estadoSolicit,
                                                                                                  procesoNegocio,
                                                                                                  numOperacion,
                                                                                                  auxiliarCredito,
                                                                                                  numOperacionCan,
                                                                                                  codAuxiliarCredito,
                                                                                                  codigoMoneda2,
                                                                                                  monedaLinea,
                                                                                                  fecExpiracion,
                                                                                                  montoAbonado );

        return (ResultIngresoOperacionCreditoMultilinea) ingresoOperacionCreditoMultilinea(ibean, new ResultIngresoOperacionCreditoMultilinea());

    }

    /**
     * Consulta Operacion Credito Multilinea en proceso de firma
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param identificacion <b>identificacion</b>
     * @param numeroCopia <b>numeroCopia</b>
     * @param rutEmpresa2 <b>rutEmpresa2</b>
     * @param digitoVerificador <b>digitoVerificador</b>
     * @return {@link ResultConsultaOperacionCreditoMultilinea}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public ResultConsultaOperacionCreditoMultilinea consultaOperacionCreditoMultilinea(String identificacion, String numeroCopia, String rutEmpresa2, char digitoVerificador) throws MultilineaException, EJBException {

        InputConsultaOperacionCreditoMultilinea ibean = new InputConsultaOperacionCreditoMultilinea(identificacion,
                                                                                                    numeroCopia,
                                                                                                    rutEmpresa2,
                                                                                                    digitoVerificador);

        return (ResultConsultaOperacionCreditoMultilinea) consultaOperacionCreditoMultilinea(ibean, new ResultConsultaOperacionCreditoMultilinea());

    }

    /**
     * Modifica Operacion Credito Multilinea en proceso de firma
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial

     *
     * </ul>
     * <p>
     *
     * @param identificador <b>identificador</b>
     * @param numero <b>num convenio</b>
     * @param rutEmpresa <b>RutEmpresa</b>
     * @param digitoVerifEmp <b>digitoVerifEmp</b>
     * @param tipoOperacion <b>tipoOperacion</b>
     * @param auxiliarOpe <b>codigoAuxiliar</b>
     * @param glosaTipoCredito <b>glosaTipoCredito</b>
     * @param tipoAbono <b>cod abono</b>
     * @param tipoCargoAbono <b>cod cargo</b>
     * @param oficinaIngreso <b>oficinaIngreso</b>
     * @param ctaAbono <b>cta abono</b>
     * @param ctaCargo <b>cta cargo</b>
     * @param indicador <b>indicadorNP01 mes no pago</b>
     * @param indicadorAplic <b>indicadorNP02 mes no pago</b>
     * @param montoCredito <b>montoCredito</b>
     * @param codigoMoneda <b>cod moneda</b>
     * @param totalVencimientos <b>num total vctos</b>
     * @param fechaPrimerVenc <b>fecha primer vcto</b>
     * @param fechaInicio <b>fecha ini curse</b>
     * @param fechaFin <b>fecha fin curse</b>
     * @param estadoSolicit <b>estado solicitud</b>
     * @param procesoNegocio <b>procesoNegocio</b>
     * @param numOperacion <b>cai ope</b>
     * @param auxiliarCredito <b>iic ope</b>
     * @param numOperacionCan <b>cai ope ren</b>
     * @param codAuxiliarCredito <b>iic ope ren</b>
     * @param codigoMoneda2 <b>cod mon orig ren</b>
     * @param monedaLinea <b>glosa mon ori ren</b>
     * @param fecExpiracion <b>fec prim vcto ren</b>
     * @param montoAbonado <b>monto abono capital prorroga</b>
     * @return {@link ResultModificaOperacionCreditoMultilinea}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public ResultModificaOperacionCreditoMultilinea modificaOperacionCreditoMultilinea(String identificador, String numero, String rutEmpresa, char digitoVerifEmp, String tipoOperacion, String auxiliarOpe, String glosaTipoCredito, String tipoAbono, String tipoCargoAbono, String oficinaIngreso, String ctaAbono, String ctaCargo, String indicador, String indicadorAplic, String montoCredito, String codigoMoneda, String totalVencimientos, String fechaPrimerVenc, String fechaInicio, String fechaFin, String estadoSolicit, String procesoNegocio, String numOperacion, String auxiliarCredito, String numOperacionCan, String codAuxiliarCredito, String codigoMoneda2, String monedaLinea, String fecExpiracion, String montoAbonado) throws MultilineaException, EJBException {

        InputModificaOperacionCreditoMultilinea ibean = new InputModificaOperacionCreditoMultilinea(identificador,
                                                                                                    numero,
                                                                                                    rutEmpresa,
                                                                                                    digitoVerifEmp,
                                                                                                    tipoOperacion,
                                                                                                    auxiliarOpe,
                                                                                                    glosaTipoCredito,
                                                                                                    tipoAbono,
                                                                                                    tipoCargoAbono,
                                                                                                    oficinaIngreso,
                                                                                                    ctaAbono,
                                                                                                    ctaCargo,
                                                                                                    indicador,
                                                                                                    indicadorAplic,
                                                                                                    montoCredito,
                                                                                                    codigoMoneda,
                                                                                                    totalVencimientos,
                                                                                                    fechaPrimerVenc,
                                                                                                    fechaInicio,
                                                                                                    fechaFin,
                                                                                                    estadoSolicit,
                                                                                                    procesoNegocio,
                                                                                                    numOperacion,
                                                                                                    auxiliarCredito,
                                                                                                    numOperacionCan,
                                                                                                    codAuxiliarCredito,
                                                                                                    codigoMoneda2,
                                                                                                    monedaLinea,
                                                                                                    fecExpiracion,
                                                                                                    montoAbonado);

        return (ResultModificaOperacionCreditoMultilinea) modificaOperacionCreditoMultilinea(ibean, new ResultModificaOperacionCreditoMultilinea());

    }

    /**
     * Elimina Operacion Credito Multilinea para el proceso de firma
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param identificacion <b>identificacion</b>
     * @param numeroCopia <b>numeroCopia</b>
     * @param rutEmpresa2 <b>rutEmpresa2</b>
     * @param digitoVerificador <b>digitoVerificador</b>
     * @return {@link ResultEliminaOperacionCreditoMultilinea}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */

    public ResultEliminaOperacionCreditoMultilinea eliminaOperacionCreditoMultilinea(String identificacion, String numeroCopia, String rutEmpresa2, char digitoVerificador) throws MultilineaException, EJBException {

        InputEliminaOperacionCreditoMultilinea ibean = new InputEliminaOperacionCreditoMultilinea(identificacion,
                                                                                                  numeroCopia,
                                                                                                  rutEmpresa2,
                                                                                                  digitoVerificador);

        return (ResultEliminaOperacionCreditoMultilinea) eliminaOperacionCreditoMultilinea(ibean, new ResultEliminaOperacionCreditoMultilinea());

    }

    /**
     * Cambia Estado Operacion Credito Multilinea en proceso de firma
     *
     * Registro de versiones:<ul>
     * <li>1.0 01/12/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param identificacion <b>identificacion</b>
     * @param numeroCopia <b>numeroCopia</b>
     * @param rutEmpresa2 <b>rutEmpresa2</b>
     * @param digitoVerificador <b>digitoVerificador</b>
     * @param estadoSolicit <b>estado solicitud</b>
     * @return {@link ResultCambiaEstadoOperacionCreditoMultilinea}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.8
     */
    public ResultCambiaEstadoOperacionCreditoMultilinea cambiaEstadoOperacionCreditoMultilinea(String identificacion, String numeroCopia, String rutEmpresa2, char digitoVerificador, String estadoSolicit) throws MultilineaException, EJBException {

        InputCambiaEstadoOperacionCreditoMultilinea ibean = new InputCambiaEstadoOperacionCreditoMultilinea(identificacion,
                                                                                                            numeroCopia,
                                                                                                            rutEmpresa2,
                                                                                                            digitoVerificador,
                                                                                                            estadoSolicit);

        return (ResultCambiaEstadoOperacionCreditoMultilinea) cambiaEstadoOperacionCreditoMultilinea(ibean, new ResultCambiaEstadoOperacionCreditoMultilinea());

    }

    /**
     * Ingreso Operacion Credito Multilinea para el proceso de firma
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object ingresoOperacionCreditoMultilinea(Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled())  logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled())  logger.debug("ingresoOperacionCreditoMultilinea");

            return servicioscreditosglobales_bean.ingresoOperacionCreditoMultilinea(ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Consulta Operacion Credito Multilinea para el proceso de firma
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object consultaOperacionCreditoMultilinea(Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled())  logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled())  logger.debug("consultaOperacionCreditoMultilinea");

            return servicioscreditosglobales_bean.consultaOperacionCreditoMultilinea(ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Modifica Operacion Credito Multilinea para el proceso de firma
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object modificaOperacionCreditoMultilinea(Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled())  logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled())  logger.debug("modificaOperacionCreditoMultilinea");

            return servicioscreditosglobales_bean.modificaOperacionCreditoMultilinea(ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Elimina Operacion Credito Multilinea para el proceso de firma
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.7
     */
    public Object eliminaOperacionCreditoMultilinea(Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled())  logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled())  logger.debug("eliminaOperacionCreditoMultilinea");

            return servicioscreditosglobales_bean.eliminaOperacionCreditoMultilinea(ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Cambia Estado Credito Multilinea para el proceso de firma
     *
     * Registro de versiones:<ul>
     * <li>1.0 01/12/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.8
     */
    public Object cambiaEstadoOperacionCreditoMultilinea(Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled())  logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled())  logger.debug("cambiaEstadoOperacionCreditoMultilinea");

            return servicioscreditosglobales_bean.cambiaEstadoOperacionCreditoMultilinea(ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Ingreso Operacion Credito Multilinea por Firmar
     *
     * <p>
     * Observacion: Se considero actualizar la primera firma, e.d., si solo si, solo uno firma
     *
     * <p>
     *
     * Registro de versiones:<ul>
     * <li>1.0 01/12/2004 Carlos Panozo   (Bee)- versión inicial
     * <li>1.1 05/12/2004 Carlos Panozo   (Bee)- mejora logica, control de primera firma, LOG en MLO
     * <li>1.2 06/01/2005 Carlos Panozo   (Bee)- mejora logica, +LOG, LOG en MLO
     * <li>1.3 12/01/2005 Carlos Panozo   (Bee)- correccion error de igualdad (== por equals)
     * <li>1.3 22/06/2005 Hector Carranza (Bee)- inclusion de operaciones negociadas (AVC,AVN,REN,RNN,PRO,PRN)
     *                                           Avances neg, renovacion neg, prorrogas neg, +LOG en MLO
     *
     * </ul>
     * <p>
     *
     * @param numeroAutorizacion <b>numero autorizacion</b>
     * @param autoTransac <b>Autoriza transacc</b>
     * @param fecExpiracion <b>fecha expiracion</b>
     * @param rutUsuario <b>Rut Usuario</b>
     * @param dvUsuario <b>digito Ver Usuario</b>
     * @param numero <b>num convenio</b>
     * @param rutEmpresa <b>RutEmpresa</b>
     * @param digitoVerifEmp <b>digitoVerifEmp</b>
     * @param tipoOperacion <b>tipoOperacion</b>
     * @param auxiliarOpe <b>codigoAuxiliar</b>
     * @param glosaTipoCredito <b>glosaTipoCredito</b>
     * @param tipoAbono <b>cod abono</b>
     * @param tipoCargoAbono <b>cod cargo</b>
     * @param oficinaIngreso <b>oficinaIngreso</b>
     * @param ctaAbono <b>cta abono</b>
     * @param ctaCargo <b>cta cargo</b>
     * @param indicador <b>indicadorNP01 mes no pago</b>
     * @param indicadorAplic <b>indicadorNP02 mes no pago</b>
     * @param montoCredito <b>montoCredito</b>
     * @param codigoMoneda <b>cod moneda</b>
     * @param totalVencimientos <b>num total vctos</b>
     * @param fechaPrimerVenc <b>fecha primer vcto</b>
     * @param fechaInicio <b>fecha ini curse</b>
     * @param fechaFin <b>fecha fin curse</b>
     * @param estadoSolicit <b>estado solicitud</b>
     * @param procesoNegocio <b>procesoNegocio</b>
     * @param numOperacion <b>cai ope</b>
     * @param auxiliarCredito <b>iic ope</b>
     * @param numOperacionCan <b>cai ren</b>
     * @param codAuxiliarCredito <b>iic ren</b>
     * @param codigoMoneda2     <b>cod moneda origen ren</b>
     * @param monedaLinea <b>glosa moneda origen ren</b>
     * @param fecExpiracion  <b>fecha vcto ren</b>
     * @param datosLog
     * @param montoAbonado <b>monto abono capital prorroga</b>
     * @return {@link ResultAutorizaPagoCuentaCorriente}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.8
     */
    public ResultAutorizaPagoCuentaCorriente ingresoOperacionCreditoMultilineaporFirmar(String numeroAutorizacion, String autoTransac, String fecExpiracion, String rutUsuario, String dvUsuario, String idConvenio, String rutEmpresa, char digitoVerifEmp, String tipoOperacion, String auxiliarOpe, String glosaTipoCredito, String tipoAbono, String tipoCargoAbono, String oficinaIngreso, String ctaAbono, String ctaCargo, String indicador, String indicadorAplic, String montoCredito, String codigoMoneda, String totalVencimientos, String fechaPrimerVenc, String fechaInicio, String fechaFin, String estadoSolicit, String procesoNegocio, String numOperacion, String auxiliarCredito, String numOperacionCan, String codAuxiliarCredito, String codigoMoneda2, String monedaLinea, String fecExpiracion2, Hashtable datosLog, String montoAbonado) throws MultilineaException, EJBException {



        ResultAutorizaPagoCuentaCorriente res_aut  = null;

        try {

            if (logger.isDebugEnabled())  logger.debug("INI ingresoOperacionCreditoMultilineaporFirmar" );

            boolean continua     = false;
            boolean primera      = false;
            String msgError      = "";
            String autorizacion  = "";
            String numope        = numeroAutorizacion.trim();

            if (logger.isDebugEnabled()){ 
	            logger.debug("Almacenando primera firma" );
	            logger.debug("numOperacion=[" + numOperacion +"]");
	            logger.debug("auxiliarCredito=[" + auxiliarCredito + "]");
	            logger.debug("numOperacionCan=[" + numOperacionCan +"]");
	            logger.debug("codAuxiliarCredito=[" + codAuxiliarCredito + "]");
	            logger.debug("numope            =[" + numope + "]");
            }
            String cai  = "";
            String iic  = "";

            if (logger.isDebugEnabled())  logger.debug("procesoNegocio=[" + procesoNegocio + "]");
            if (procesoNegocio.trim().equals("REN") || procesoNegocio.trim().equals("PRO")) {
                cai = numOperacionCan;
                iic = codAuxiliarCredito;
            } else {
                cai = numOperacion;
                iic = auxiliarCredito;
            }

            if (numope.equals("")){ //si ya existe no la creamos...

                    try {

                        if (!existOperacionenFirmas(idConvenio, rutEmpresa, digitoVerifEmp, cai, iic)){

                            if (logger.isDebugEnabled())  logger.debug("Antes de ingresoOperacionCreditoMultilinea" );
                            ResultIngresoOperacionCreditoMultilinea res_ing = null;
                            res_ing = (ResultIngresoOperacionCreditoMultilinea) ingresoOperacionCreditoMultilinea( idConvenio,
                                                                                                                   rutEmpresa,
                                                                                                                   digitoVerifEmp,
                                                                                                                   tipoOperacion,
                                                                                                                   auxiliarOpe,
                                                                                                                   glosaTipoCredito,
                                                                                                                   tipoAbono,
                                                                                                                   tipoCargoAbono,
                                                                                                                   oficinaIngreso,
                                                                                                                   ctaAbono,
                                                                                                                   ctaCargo,
                                                                                                                   indicador,
                                                                                                                   indicadorAplic,
                                                                                                                   montoCredito,
                                                                                                                   codigoMoneda,
                                                                                                                   totalVencimientos,
                                                                                                                   fechaPrimerVenc,
                                                                                                                   fechaInicio,
                                                                                                                   fechaFin,
                                                                                                                   "PEN",             //pendiente
                                                                                                                   procesoNegocio,
                                                                                                                   numOperacion,
                                                                                                                   auxiliarCredito,
                                                                                                                   numOperacionCan,
                                                                                                                   codAuxiliarCredito,
                                                                                                                   codigoMoneda2,
                                                                                                                   monedaLinea,
                                                                                                                   fecExpiracion2,
                                                                                                                   montoAbonado );


                            numope        = res_ing.getIdentificador();
                            continua      = true;
                            primera       = true;
                            if (logger.isDebugEnabled())  logger.debug("Despues de ingresoOperacionCreditoMultilinea" );

                         }else{

                            if (logger.isDebugEnabled())  logger.debug("Operacion ya fue Ingresada para Firmar.");
                            msgError  += "Operacion ya fue Ingresada para Firmar.";
                            if (datosLog!=null) {
                               datosLog.put("ERR",msgError);
                            }
                            throw new MultilineaException("ESPECIAL", msgError);
                         }

                    } catch (Exception e) {

                    	if (logger.isEnabledFor(Level.ERROR)) logger.error(e.getClass() + " : " + e.getMessage());
                        msgError  += "No se pudo realizar operacion para registrar firma.";
                        if (datosLog!=null) {
                           datosLog.put("ERR",datosLog.get("ERR")==null? Utils.hackle(e.getMessage()): Utils.hackle(e.getMessage()) + datosLog.get("ERR"));
                        }

                        throw new MultilineaException("ESPECIAL", msgError);
                    }
            }else{
                //operacion ya fue ingresada anteriormente y ahora esta firmando supuestamente otro usuario
                if (logger.isDebugEnabled())  logger.debug("firmando numope    :["+numope+"]" );
                continua = true;
            }

            if (continua){
                    /** validamos si el usuario cumple **/

                    try {

                        res_aut = (ResultAutorizaPagoCuentaCorriente) autorizaFirmaUsuario( rutUsuario,
                                                                                            dvUsuario.charAt(0),
                                                                                            idConvenio,
                                                                                            rutEmpresa,
                                                                                            digitoVerifEmp,
                                                                                            numope,
                                                                                            autoTransac.charAt(0),
                                                                                            fecExpiracion             );

                        if (logger.isDebugEnabled())  logger.debug("Despues de autorizaFirmaUsuario" );

                    } catch (Exception e) {

                    	if (logger.isEnabledFor(Level.ERROR)) logger.error(e.getClass() + " : " + e.getMessage());
                        msgError  += "No se Puede Validar Firma.";
                        if (datosLog!=null) {
                           datosLog.put("ERR",datosLog.get("ERR")==null? Utils.hackle(e.getMessage()): Utils.hackle(e.getMessage()) + datosLog.get("ERR"));
                        }

                        throw new MultilineaException("ESPECIAL", msgError);
                    }

                    /*
                     *  Ya tenemos la autorizacion.
                     */
                    AutorizaPagoCtaCte[] autPagoCtaCte         = res_aut.getAutorizaPagoCtaCte();

                    if (logger.isDebugEnabled())  logger.debug("autPagoCtaCte.length = " + autPagoCtaCte.length);

                    for (int i = 0; i < autPagoCtaCte.length; i++) {

                        if (autPagoCtaCte[i] == null) {
                            break;
                        }

                        if (logger.isDebugEnabled())  logger.debug("autPagoCtaCte[i].getCodigo() = " + autPagoCtaCte[i].getCodigo());

                        autorizacion = autPagoCtaCte[i].getCodigo();
                    }

                    if (logger.isDebugEnabled())  logger.debug("autorizacion = " + autorizacion);
                    continua      = false;

                    if (autorizacion.equals("0")) {
                        msgError  +=  "Pendiente por Falta de Firmas.";
                        continua   = true;
                        if (datosLog!=null) {
                           datosLog.put("tipoOpe", "RSF");
                           datosLog.put("MSG",msgError + "(" + numope + ")");
                           registraLogMultilinea(datosLog);
                        }
                        else {
                           if (logger.isDebugEnabled())  logger.debug("datosLog es null");
                        }
                    } else if (autorizacion.equals("100")) {
                        msgError  +=  "El Usuario no es Apoderado.";
                        if (datosLog!=null) {
                           datosLog.put("tipoOpe", "RSF");
                           datosLog.put("ERR",msgError + "(" + numope + ")");
                           registraLogMultilinea(datosLog);
                        }
                        else {
                           if (logger.isDebugEnabled())  logger.debug("datosLog es null");
                        }
                    } else if (autorizacion.equals("101")) {
                        msgError  +=  "Ya Firmo el Apoderado.";
                        if (datosLog!=null) {
                           datosLog.put("tipoOpe", "RSF");
                           datosLog.put("ERR",msgError + "(" + numope + ")");
                           registraLogMultilinea(datosLog);
                        }
                        else {
                           if (logger.isDebugEnabled())  logger.debug("datosLog es null");
                        }
                    } else if (autorizacion.equals("102")) {
                        msgError  +=  "Problemas de Conexion.";
                        if (datosLog!=null) {
                           datosLog.put("tipoOpe", "RSF");
                           datosLog.put("ERR",msgError + "(" + numope + ")");
                           registraLogMultilinea(datosLog);
                        }
                        else {
                           if (logger.isDebugEnabled())  logger.debug("datosLog es null");
                        }
                    } else if (autorizacion.equals("1")) {
                        continua   = true;
                        msgError   +=  "OK";
                        if (datosLog!=null) {
                           datosLog.put("tipoOpe", "RSF");
                           datosLog.put("MSG",msgError + "(" + numope + ")");
                           registraLogMultilinea(datosLog);
                        }
                        else {
                           if (logger.isDebugEnabled())  logger.debug("datosLog es null");
                        }

                    } else {
                        msgError   +=  "Problema desconocido al Autorizar Firma.";
                    }

            }

            if (continua){
                /*** el registro va ser ingresado, con estado 'PEN'diente...                                                       ***/
                if (autorizacion.equals("0") ) {
                    try {
                        /*** actualizamos registro de operaciones con estado 'OKE'***/
                        if (logger.isDebugEnabled())  logger.debug("Antes de modificaOperacionCreditoMultilinea" );
                        ResultModificaOperacionCreditoMultilinea res_mod = null;
                        res_mod = (ResultModificaOperacionCreditoMultilinea) modificaOperacionCreditoMultilinea( numope,
                                                                                                                 idConvenio,
                                                                                                                 rutEmpresa,
                                                                                                                 digitoVerifEmp,
                                                                                                                 tipoOperacion,
                                                                                                                 auxiliarOpe,
                                                                                                                 glosaTipoCredito,
                                                                                                                 tipoAbono,
                                                                                                                 tipoCargoAbono,
                                                                                                                 oficinaIngreso,
                                                                                                                 ctaAbono,
                                                                                                                 ctaCargo,
                                                                                                                 indicador,
                                                                                                                 indicadorAplic,
                                                                                                                 montoCredito,
                                                                                                                 codigoMoneda,
                                                                                                                 totalVencimientos,
                                                                                                                 fechaPrimerVenc,
                                                                                                                 fechaInicio,
                                                                                                                 fechaFin,
                                                                                                                 "PEN",
                                                                                                                 procesoNegocio,
                                                                                                                 numOperacion,
                                                                                                                 auxiliarCredito,
                                                                                                                 numOperacionCan,
                                                                                                                 codAuxiliarCredito,
                                                                                                                 codigoMoneda2,
                                                                                                                 monedaLinea,
                                                                                                                 fecExpiracion2,
                                                                                                                 montoAbonado);

                        if (logger.isDebugEnabled())  logger.debug("Despues de modificaOperacionCreditoMultilinea" );

                    } catch (Exception e) {

                    	if (logger.isEnabledFor(Level.ERROR)) logger.error(e.getClass() + " : " + e.getMessage());
                        msgError   +=  "No se pudo realizar operacion para registrar firma.";

                        if (datosLog!=null) {
                            datosLog.put("ERR",msgError + "(" + numope + ")");
                        }
                        throw new MultilineaException("ESPECIAL", msgError);
                    }
                }else if (autorizacion.equals("1") ) {
                    if (primera){   //*** si firma es valida y ademas cumple las firmas solicitadas (sola una) actualizamos registro de Operacion Credito Multilinea a OKE***/
                        if (logger.isDebugEnabled())  logger.debug("Firmo el primero y quedo Ok.");
                        ResultCambiaEstadoOperacionCreditoMultilinea res_cam = null;
                        res_cam = (ResultCambiaEstadoOperacionCreditoMultilinea) cambiaEstadoOperacionCreditoMultilinea( numope,
                                                                                                                         idConvenio,
                                                                                                                         rutEmpresa,
                                                                                                                         digitoVerifEmp,
                                                                                                                         "OKE");
                        if (logger.isDebugEnabled())  logger.debug("En operaciones por firmar quedo OKE.");
                    }

                }
            }else{
                if (primera){
                    ResultCambiaEstadoOperacionCreditoMultilinea res_cam = null;
                    res_cam = (ResultCambiaEstadoOperacionCreditoMultilinea) cambiaEstadoOperacionCreditoMultilinea( numope,
                                                                                                                     idConvenio,
                                                                                                                     rutEmpresa,
                                                                                                                     digitoVerifEmp,
                                                                                                                     "ERR");
                }
                if (datosLog!=null) {
                   datosLog.put("ERR","Primera firma y no pudo firmar" + "(" + numope + ")");
                }

                throw new MultilineaException("ESPECIAL", msgError);
            }


            return  res_aut;

        } catch(Exception e) {
        	if (logger.isEnabledFor(Level.ERROR)) logger.error("ERROR*** ingresoOperacionCreditoMultilineaporFirmar...Exception [" + e.getMessage() + "]");

            if (datosLog!=null) {
                if (procesoNegocio.equals("AVC"))
                    datosLog.put("tipoOpe", "FAV");
                else if (procesoNegocio.equals("AVN"))
                    datosLog.put("tipoOpe", "FAN");//firma avance negociado
                else if (procesoNegocio.equals("REN"))
                    datosLog.put("tipoOpe", "FRE");
                else if (procesoNegocio.equals("RNN"))
                    datosLog.put("tipoOpe", "FRN");//firma renovacion negociada
                else if (procesoNegocio.equals("PRO"))
                    datosLog.put("tipoOpe", "FPR");
                else if (procesoNegocio.equals("PRN"))
                    datosLog.put("tipoOpe", "FPN");//firma prorroga negociada

                datosLog.put("ERR",datosLog.get("ERR")==null? Utils.hackle(e.getMessage()): Utils.hackle(e.getMessage()) + datosLog.get("ERR"));
                registraLogMultilinea(datosLog);
            }
            else {
            	if (logger.isEnabledFor(Level.ERROR)) logger.error("datosLog es null");
            }

            throw new MultilineaException("ESPECIAL", e.getMessage());
        }


    }

    /**
     * Autoriza Firma Usuario
     *
     * Registro de versiones:<ul>
     * <li>1.0 01/12/2004 Carlos Panozo   (Bee)- versión inicial

     *
     * </ul>
     * <p>
     *
     * @param rutUsu <b>RUT Usuario</b>
     * @param digitoVerifUsu <b>RUT dg Empresa</b>
     * @param numConvenio <b>numero convenio</b>
     * @param rutEmp <b>RUT Empresa</b>
     * @param digitoVerifEmp <b>RUT dg Empresa</b>
     * @param numeroAutorizacion <b>Numero autorizacion</b>
     * @param autoTransac <b>Autor transaccion</b>
     * @param fecExpiracion <b>Fecha expiracion</b>
     * @return {@link ResultAutorizaPagoCuentaCorriente}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.8
     */
    private ResultAutorizaPagoCuentaCorriente autorizaFirmaUsuario(String rutUsu, char digitoVerifUsu, String numConvenio, String rutEmp, char digitoVerifEmp, String numeroAutorizacion, char autoTransac, String fecExpiracion) throws MultilineaException, EJBException {

        try {

            BCIExpress     bciexpressBean = null;
            InitialContext ic             = JNDIConfig.getInitialContext();
            Object         obj_exp        = ic.lookup(JNDI_NAME_BEX);
            BCIExpressHome home_exp       = (BCIExpressHome) PortableRemoteObject.narrow(obj_exp, BCIExpressHome.class);

            if (logger.isDebugEnabled())  logger.debug("BCIExpressBean creado");

            bciexpressBean = (BCIExpress) PortableRemoteObject.narrow(home_exp.create(), BCIExpress.class);

            if (logger.isDebugEnabled())  logger.debug("antes de autorizaPagoCuentaCorriente");

            ResultAutorizaPagoCuentaCorriente res_aut = bciexpressBean.autorizaPagoCuentaCorriente(rutUsu,
                                                                                                  digitoVerifUsu,
                                                                                                  numConvenio,
                                                                                                  rutEmp,
                                                                                                  digitoVerifEmp,
                                                                                                  numeroAutorizacion,
                                                                                                  autoTransac,
                                                                                                  fecExpiracion);


            return res_aut;

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.getMessage());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.getMessage());

            throw new MultilineaException("UNKNOW", e.getMessage());
        }

    }

    /**
     * Lista todos los usuarios que han firmado
     *
     * Registro de versiones:<ul>
     * <li>1.0 01/12/2004 Carlos Panozo   (Bee)- versión inicial
     * <li>1.1 06/01/2005 Carlos Panozo   (Bee)- LOG en MLO
     *
     * </ul>
     * <p>
     *
     * @param rutEmp <b>RUT Empresa</b>
     * @param numConvenio <b>Numero convenio</b>
     * @param numeroAutorizacion <b>Numero autorizacion</b>
     * @return {@link ResultConsultarApoderadosquehanFirmado}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.8
     */
    public ResultConsultarApoderadosquehanFirmado listaTodosFirmantes(String rutEmp, String numConvenio, String numeroAutorizacion, Hashtable datosLog) throws MultilineaException, EJBException {

        try {

            BCIExpress     bciexpressBean = null;
            InitialContext ic             = JNDIConfig.getInitialContext();
            Object         obj_exp        = ic.lookup(JNDI_NAME_BEX);
            BCIExpressHome home_exp       = (BCIExpressHome) PortableRemoteObject.narrow(obj_exp, BCIExpressHome.class);

            if (logger.isDebugEnabled())  logger.debug("BCIExpressBean creado");

            bciexpressBean = (BCIExpress) PortableRemoteObject.narrow(home_exp.create(), BCIExpress.class);

            if (logger.isDebugEnabled())  logger.debug("obtieneTasaMultilinea");


            if (logger.isDebugEnabled())  logger.debug("Consultando apoderados que han firmado");

            ResultConsultarApoderadosquehanFirmado res_aut = bciexpressBean.consultarApoderadosquehanFirmado(rutEmp,
                                                                                                             numConvenio,
                                                                                                             numeroAutorizacion);


            return res_aut;

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.getMessage());
            if (datosLog!=null) {
               datosLog.put("tipoOpe", "LFR");
               datosLog.put("ERR",datosLog.get("ERR")==null? Utils.hackle(e.getMessage()): Utils.hackle(e.getMessage()) + datosLog.get("ERR"));
               registraLogMultilinea(datosLog);
            }
            else {
            	if (logger.isEnabledFor(Level.ERROR)) logger.error("datosLog es null");
            }

            throw new MultilineaException("UNKNOW", e.getMessage());
        }

    }

    /**
     * Existen Operaciones en Proceso de Firmar
     *
     * <p>
     * Observacion: busca si existen operaciones de credito en proceso de firma
     *
     * <p>
     *
     * Registro de versiones:<ul>
     * <li>1.0 01/12/2004 Carlos Panozo   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param numConvenio,
     * @param rutEmp,
     * @param digitoVerifEmp,
     * @param cai Numero Operacion,
     * @param iic Numero Operacion
     * @return boolean
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.8
     */
    private boolean existOperacionenFirmas(String numConvenio, String rutEmp, char digitoVerifEmp, String cai, String iic) throws MultilineaException, EJBException {

        try {

            boolean retorno = false;
            String cai1     = cai.trim();
            String iic1     = iic.trim();

            if (logger.isDebugEnabled())  logger.debug("Antes de consultaOperacionCreditoMultilinea");
            InputConsultaOperacionCreditoMultilinea ibean = new InputConsultaOperacionCreditoMultilinea(" ",
                                                                                                        numConvenio,
                                                                                                        rutEmp,
                                                                                                        digitoVerifEmp);

            ResultConsultaOperacionCreditoMultilinea res_con=  (ResultConsultaOperacionCreditoMultilinea) consultaOperacionCreditoMultilinea(ibean, new ResultConsultaOperacionCreditoMultilinea());

            if (logger.isDebugEnabled()){ 
	            logger.debug("Despues de consultaOperacionCreditoMultilinea 1");
	            logger.debug("Existe cai1 =[" + cai1 + "]");
	            logger.debug("Existe iic1 =[" + iic1 + "]");
            }
            if (res_con != null){

                ConsultaOperacionCreditoMultilinea[] res_res = res_con.getConsultaOperCredMultilinea();

                 for (int i = 0; i < res_res.length; i++) {

                    if (res_res[i] == null) {
                           break;
                    }
                    String cai2     = res_res[i].getNumOperacion().trim().equals("")?res_res[i].getNumOperacionCan().trim():res_res[i].getNumOperacion().trim();
                    String iic2     = res_res[i].getAuxiliarCredito().trim().equals("")?res_res[i].getCodAuxiliarCredito().trim():res_res[i].getAuxiliarCredito().trim();

                    if (cai1.equals(cai2) && iic1.equals(iic2)){
                        if (logger.isDebugEnabled()){  
	                        logger.debug("Existe cai2 =[" + cai2 + "]");
	                        logger.debug("Existe iic2 =[" + iic2 + "]");
                        }
                        retorno = true;
                        break;
                    }
                }
            }


            return retorno;

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.getMessage());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.getMessage());

            throw new MultilineaException("UNKNOW", e.getMessage());
        }

    }


    /**
     * Consulta operaciones de multilinea y filtra solo aquellas que son prorrogables
     *
     * <p>
     * Observacion: el filtro esta dado por aquellas que son del tipo AVC010 y AVC326
     * <p>
     *
     * Registro de versiones:<ul>
     * <li>1.0 21/01/2005 Hector Carranza (Bee)- versión inicial
     * <li>1.1 02/05/2005 Hector Carranza (Bee)- ...ClienteAmp por ...ClienteSuperAmp
     * <li>1.2 20/07/2008 Hector Carranza (Bee)- Se mejora mediante cambio en rutina  de obtencion
     *                                           y consulta de operaciones prorrogables a traves de un arreglo de String.
     *                                           Se incorpora +Log.
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param rutDeudor
     * @param digitoVerificador
     * @param datosLog
     * @return Vector con todas aquellas operaciones que cumplan
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.14
     */
     public Vector consultaOperacionesParaProrrogar(MultiEnvironment multiEnvironment, int rutDeudor, char digitoVerificador, Hashtable datosLog) throws MultilineaException, EJBException {

        try {
            if (logger.isDebugEnabled())  logger.debug("**** INI consultaOperacionesParaProrrogar");
        Vector vResult = new Vector();
            String[] creditosARenovar = null;
        InputConsultaOperClienteSuperAmp ibean = new InputConsultaOperClienteSuperAmp();
            ResultConsultaOperClienteSuperAmp  obean;
            OperacionCreditoSuperAmp[] ope;

        ibean.setRutDeudor(rutDeudor);
            if (logger.isDebugEnabled())  logger.debug("rutDeudor          =[" + rutDeudor + "]");

            ibean.setDigitoVerificador(digitoVerificador);
            if (logger.isDebugEnabled())  logger.debug("digitoVerificador  =[" + digitoVerificador + "]");

            ibean.setCodEstadoCredito('A');
            if (logger.isDebugEnabled())  logger.debug("codEstadoCredito   =[A]");

            ibean.setTipoDeudor('D');
            if (logger.isDebugEnabled())  logger.debug("tipoDeudor         =[D]");

            if (logger.isDebugEnabled())  logger.debug("Antes de Obtener el arreglo de String de consultaOperacionesParaProrrogar");
            creditosARenovar = (String[])datosLog.get("operacionesAProrrogar");
            if (logger.isDebugEnabled())  logger.debug("Despues de Obtener el arreglo de String de consultaOperacionesParaProrrogar");

            for(int i = 0; i < creditosARenovar.length; i ++) {
                String moneda = creditosARenovar[i].substring(0, 4);
                String tio = creditosARenovar[i].substring(6, 9);
                String aux = creditosARenovar[i].substring(9, creditosARenovar[i].length());
                if (logger.isDebugEnabled())  logger.debug("Tipo de Operacion a Prorrogar moneda=[" + moneda + "]  tio=[" + tio + "]  aux=[" + aux + "]");

                ibean.setMoneda(moneda);
                ibean.setTipoOperacion(tio);
                ibean.setCodigoAuxiliar(aux);

                obean = (ResultConsultaOperClienteSuperAmp) consultaOperClienteSuperAmpAll(multiEnvironment, ibean, new ResultConsultaOperClienteSuperAmp());
                ope = obean.getOperacionesSuperAmp();

                for (int ind=0; ind < ope.length; ind++) {
                    if (ope[ind] == null) {
                        if (logger.isDebugEnabled())  logger.debug("break[" + ind + "]");
                        break;
                    } else {
                        vResult.addElement(ope[ind]);
                        if (logger.isDebugEnabled())  logger.debug(aux + "-"+ ind +"[" + ind + "]");
                    }
                }
            }
            if (logger.isDebugEnabled())  logger.debug("**** FIN consultaOperacionesParaProrrogar");
            return vResult;

        } catch (Exception e) {
        	if (logger.isEnabledFor(Level.ERROR)) logger.error("ERROR*** consultaOperacionesParaProrrogar... Exception [" + e.getMessage() + "]");
             if (datosLog!=null) {
                datosLog.put("tipoOpe", "COP");  // COP = Consulta Operacione para Prorrogar
                datosLog.put("ERR",datosLog.get("ERR")==null ? e.getMessage() : e.getMessage() + datosLog.get("ERR"));
                registraLogMultilinea(datosLog);
             }
             else {
            	 if (logger.isEnabledFor(Level.ERROR)) logger.error("datosLog es null");
             }
             throw new MultilineaException("ESPECIAL", e.getMessage());
         }

    }


    /**
     * Consulta Valores de Cancelacion Multilinea Prorroga
     *
     * Registro de versiones:<ul>
     * <li>1.0 21/01/2005 Hector Carranza (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param idOperacion
     * @param numOperacion
     * @param moneda
     * @param oficinaCancel
     * @param datosLog
     * @return {@link ResultCalculoValoresCancelacion}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.14
     */
    public ResultCalculoValoresCancelacion consultaCancelacionMultilineaProrroga(MultiEnvironment multiEnvironment, String idOperacion, int numOperacion, String moneda, String oficinaCancel, Hashtable datosLog) throws MultilineaException, EJBException {

        try {
            if (logger.isDebugEnabled())  logger.debug("///***---------------------------------------------------***///");
            if (logger.isDebugEnabled())  logger.debug("Antes de Contextualizar Cancelación Prorroga Multilinea. ");
            InputContextualizacionIngrCancelacion   input_ctxCan = new  InputContextualizacionIngrCancelacion();
            input_ctxCan.setIdOperacion(idOperacion);
            input_ctxCan.setNumOperacionCan(numOperacion);
            ResultContextualizacionIngrCancelacion output_ctxCan = new ResultContextualizacionIngrCancelacion();
            contextualizacionIngrCancelacion(multiEnvironment, input_ctxCan, output_ctxCan);

            if (logger.isDebugEnabled()){  
	            logger.debug("Antes de Calculo valores Cancelación Prorroga Multilinea");
	            logger.debug("output_ctxCan.getNumVencimiento()="    + output_ctxCan.getNumVencimiento());
	            logger.debug("output_ctxCan.getEjecutivo()="         + output_ctxCan.getEjecutivo());
	            logger.debug("TasaSpread="                           + Double.valueOf(String.valueOf(output_ctxCan.getTasaSpread())));
	            logger.debug("oficinaCancel="                        + oficinaCancel);
	            logger.debug("idOperacion="                          + idOperacion);
	            logger.debug("numOperacion="                         + numOperacion);
            }
            Date   today            = new Date();
            String fechaHoy         = ddMMyyyy_form.format(today);
            Date   fechaVencimiento = output_ctxCan.getFecVencimiento();
            Double tasaCancelacion  = null;

            if (logger.isDebugEnabled()){ 
	            logger.debug("fechaHoy="         + fechaHoy);
	            logger.debug("fechaVencimiento=" + ddMMyyyy_form.format(fechaVencimiento));
            }
            if (new SimpleDateFormat("yyyyMMdd").format(fechaVencimiento).compareTo(new SimpleDateFormat("yyyyMMdd").format(today))>0) {
                tasaCancelacion=new Double(output_ctxCan.getTasaSpread());
            };
            if (logger.isDebugEnabled())  logger.debug("tasaCancelacion=" + tasaCancelacion);

            ResultCalculoValoresCancelacion output_valCan  = new ResultCalculoValoresCancelacion();
            InputCalculoValoresCancelacion input_valCan    = new InputCalculoValoresCancelacion();
            input_valCan.setIdOperacion(idOperacion);
            input_valCan.setNumOperacionCan(numOperacion);
            input_valCan.setNumVencimiento(output_ctxCan.getNumVencimiento());
            input_valCan.setTipoCancelacion("PTT");                   //    PTT --> codigo prorroga
            input_valCan.setEjecutivo(output_ctxCan.getEjecutivo());
            input_valCan.setTasaInteresCancel(tasaCancelacion);
            input_valCan.setOficinaCancel(oficinaCancel);
            calculoValoresCancelacion(multiEnvironment, input_valCan, output_valCan);

            if (logger.isDebugEnabled()){  
	            logger.debug("output_valCan.getValorRenovado()="           + output_valCan.getValorRenovado());
	            logger.debug("output_valCan.getFechaCanComputacional()="   + output_valCan.getFechaCanComputacional());
	            logger.debug("output_valCan.getFechaCanReal()="            + output_valCan.getFechaCanReal());
	            logger.debug("output_valCan.getFechaCanContable()="        + output_valCan.getFechaCanContable());
	            logger.debug("output_valCan.getTasaInteresCancel()="       + output_valCan.getTasaInteresCancel());
	            logger.debug("output_valCan.getTasaComisionCancelacion()=" + output_valCan.getTasaComisionCancelacion());
	            logger.debug("output_valCan.getPlantillaComision()="       + output_valCan.getPlantillaComision());
	            logger.debug("output_valCan.getComision()="                + output_valCan.getComision());
	            logger.debug("output_valCan.getTotalPagado()="             + output_valCan.getTotalPagado());
	            logger.debug("output_valCan.getValorDiferencia()="         + output_valCan.getValorDiferencia());
	            logger.debug("output_valCan.getValorCapital()="            + output_valCan.getValorCapital());
	            logger.debug("output_valCan.getValorFinal()="              + output_valCan.getValorFinal());
	            logger.debug("moneda=" + moneda);
            }
            double capital = output_valCan.getValorCapital();
            if (logger.isDebugEnabled())  logger.debug("capital=" + capital);

            if (moneda.trim().equals("0998")) { //U.FOME
                ResultConsultaValoresCambio output_cambio = obtieneConversionMoneda("UFP", fechaHoy, fechaHoy, "*", capital);
                capital = output_cambio.getTotalCalculado();
            }

            if (logger.isDebugEnabled())  logger.debug("capital=" + capital);

            //se inyecta capital en pesos en valorRenovado
            //           CTBL en tipoCargo
            input_valCan.setValorRenovado(capital);
            input_valCan.setTipoCargo("CTBL");
            calculoValoresCancelacion(multiEnvironment, input_valCan, output_valCan);

            if (logger.isDebugEnabled())  logger.debug("output_valCan.getTotalPagado()=" + output_valCan.getTotalPagado());

            return output_valCan;
        } catch (Exception e) {
        	if (logger.isEnabledFor(Level.ERROR)) logger.error("ERROR*** consultaCancelacionMultilineaProrroga... Exception [" + e.getMessage() + "]");
            if (datosLog!=null) {
                datosLog.put("tipoOpe", "CVP");
                datosLog.put("ERR",datosLog.get("ERR")==null? Utils.hackle(e.getMessage()): Utils.hackle(e.getMessage()) + datosLog.get("ERR"));
                registraLogMultilinea(datosLog);
            }
            else {
            	if (logger.isEnabledFor(Level.ERROR)) logger.error("datosLog es null");
            }
            throw new MultilineaException("ESPECIAL",e.getMessage());
        }

    }

    /**
     * Realiza la Prorroga de la multilinea que esta operando
     *
     * <p>
     * Observacion: el parametro montoAbonado que se agrego debe  ser restado al capital en simulacion
     *              que en este caso esta dado por (valorRenovado - montoAbonado)
     *
     * <p>
     *
     * Registro de versiones:<ul>
     * <li>1.0 21/01/2005 Hector Carranza (Bee)- versión inicial
     * <li>1.1 27/01/2005 Hector Carranza (Bee)- cambio Obtencion de Tasa Especial WEB (Campaña)
     * <li>1.2 02/05/2005 Hector Carranza (Bee)- nuevo parametro montoAbonado, este es de cuidado pues rebaja a valorRenovado
     * <li>1.3 22/06/2005 Hector Carranza (Bee)- Correcion observacion con respecto a condicionGar (condicion de garantia) de acuerdo a tiene_cual (no tiene ni aval ni fogape) verificando que solo_G
     * <li>1.4 23/02/2007 Hector Carranza (Bee)- se agrega multiEnvironment.setIndreq(5,'1') para que el host valide en forma optima el ejecutivo asignado. (validacion de ejecutivo inexistente)
     * <li>1.5 05/04/2007 Hector Carranza (bee)- se corrige multiEnvironment.setIndreq(5,'1') por multiEnvironment.setIndreq(5,'2') por normalizacion de stadard parmetros
     * <li>1.6 10/05/2007 Hector Carranza (bee)- se omite el flag de validacion de ejecutivo en prorroga
     * <li>2.0 31/05/2007 Hector Carranza (Bee)- Se corrige calculo de parametros en la obtencion de la tasa desde sistema precios, siendo calculo en 'D'ias.
     * <li>2.1 30/11/2007 Hector Carranza (Bee)- correccion en calculo de valores para la prorroga teniendo en cuenta la moneda en la cual se va a prorrogar.
     * <li>2.2 29/10/2009 Hector Carranza (Bee)- se agrega rut de aval DPS en validacion de avales.
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param idReq
     * @param idOperacion
     * @param numOperacionCan
     * @param oficinaCancel
     * @param ejecutivo
     * @param valorRenovado
     * @param tipoOperacion
     * @param moneda
     * @param codigoAuxiliar
     * @param oficinaIngreso
     * @param ctaAbonoTer
     * @param pinCtaCargo
     * @param vencimientos
     * @param codBanca
     * @param plan
     * @param rutDeudor
     * @param dvDeudor
     * @param diasEnMora
     * @param topeMora
     * @param whoVisa
     * @param totalPagado
     * @param datosLog
     * @param montoAbonado
     * @return {@link ResultRenovacionMultilinea}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.14
     */
    public ResultRenovacionMultilinea prorrogarMultilinea(MultiEnvironment multiEnvironment, String idReq, String idOperacion, int numOperacionCan, String oficinaCancel, String ejecutivo, double valorRenovado, String tipoOperacion, String moneda, String codigoAuxiliar, String oficinaIngreso, int ctaAbonoTer, int pinCtaCargo, EstructuraVencimiento[] vencimientos, String codBanca, char plan, int rutDeudor, char dvDeudor, int diasEnMora, int topeMora, int whoVisa, double totalPagado, Hashtable datosLog, String codSegmento, double montoAbonado) throws MultilineaException, EJBException {

        ResultConsultaSpr obeanSprSGS  = null;
        Vector vectorSPR               = new Vector();
        boolean consultaSeguros        = false;
        String codigoSegDesgrav        = "SEDECO";
        double valInteOperacion        = 0.1;
        String[] seguros               = null;
        String codigoSegDesgravAux     = "";
        int rutAvalDPS                 = 0;
        char rutdigAvalDPS             = ' ';

        try {
            if (logger.isDebugEnabled())  logger.debug("Inicio prorrogarMultilinea 20090725 ************************");
            if (logger.isDebugEnabled())  logger.debug("requerimiento=" + idReq);
            if ((datosLog != null) && idReq.equals("096")) {
                datosLog.put("tipoOpe", "SIP");
            }
            if ((datosLog != null) && idReq.equals("097")) {
                datosLog.put("tipoOpe", "PRO");
            }

            String canal                   = multiEnvironment.getCanal();

            if (logger.isDebugEnabled())  logger.debug("validando array 'vencimientos'");

            if (vencimientos == null || vencimientos[0] == null) {
                throw new Exception("vencimientos indefinidos");
            }
            if (logger.isDebugEnabled())  logger.debug("calcula fechaCurse'");
            Date   fechaCurse             = ManejoEvc.fechaHabil(new Date());
            char   analisisFeriado        = ' ';
            if (logger.isDebugEnabled())  logger.debug("listo calcula fechaCurse ->'" + fechaCurse);
            double monto = valorRenovado;

            if (logger.isDebugEnabled()){  
	            logger.debug("montoAbonado="+ montoAbonado);
	            logger.debug("monto="      + monto);
	            logger.debug("monto(int)=" + (int) monto);
	            logger.debug("codBanca="   + codBanca);
	            logger.debug("RUT="        + String.valueOf(rutDeudor));
	            logger.debug("RUT(sybase)="+ StrUtl.fillCharLeft(String.valueOf(rutDeudor),'0',8));
	            logger.debug("PLAN="       + plan);
	            logger.debug("moneda="     + moneda);
	            logger.debug("MESES="      + ManejoEvc.getPlazoEnMeses(fechaCurse,analisisFeriado,vencimientos,0,0,0,0));
            }
            Date today = new Date();
            String fechaHoy = ddMMyyyy_form.format(today);

            if (moneda.trim().equals("U.FOME") || moneda.trim().equals("0998")) { //U.FOME glosa desaparece em MANTENCION CODIGO MONEDA CCC
                if (logger.isDebugEnabled())  logger.debug("Prorroga obtieneConversionMoneda(UFP,"+fechaHoy+ ","+fechaHoy+",*,1");
                ResultConsultaValoresCambio output_cambio = obtieneConversionMoneda("UFP", fechaHoy, fechaHoy, "*", 1);
                if (logger.isDebugEnabled())  logger.debug("valor UF                     =" + output_cambio.getTotalCalculado());
                monto = monto - montoAbonado;
                if (logger.isDebugEnabled())  logger.debug("monto - montoAbonado         =" + monto);
                monto = monto/output_cambio.getTotalCalculado();
                if (logger.isDebugEnabled())  logger.debug("monto en UF                  =" + monto);
            }
            if (logger.isDebugEnabled())  logger.debug("valorRenovado                =" + valorRenovado);
            valorRenovado = valorRenovado - montoAbonado;
            if (logger.isDebugEnabled()){  
	            logger.debug("valorRenovado - montoAbonado =" + valorRenovado);
	            logger.debug("monto="        + monto);
	            logger.debug("monto(int)="   + (int) monto);
	            logger.debug("diasEnMora="   + diasEnMora);
	            logger.debug("topeMora="     + topeMora);
            }
            String descuento="AVC";

            if (diasEnMora>0 && diasEnMora <= topeMora) {
                descuento = "AVC2"; //avance en mora
            }

            if (logger.isDebugEnabled()){  
	            logger.debug("Plazo......");
	            logger.debug("......en meses:" + ManejoEvc.getPlazoEnMeses(fechaCurse,analisisFeriado,vencimientos,0,0,0,0));
	            logger.debug("......en dias :" + ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,0,0));
	            logger.debug("vencimientos[0].getTotalVencimientosGrupo():" + vencimientos[0].getTotalVencimientosGrupo());
            }
            //[CPH] INI: Obtencio'n de Tasa Especial WEB (Campan'a)
            TablaValores tablaCreditos   = new TablaValores();
            Hashtable    htTablaCreditos = tablaCreditos.getTabla("promultilinea.parametros");
            if (logger.isDebugEnabled())  logger.debug("Buscando tasa especial:[" + moneda + tipoOperacion + codigoAuxiliar + "]");
            Hashtable    datosCreditos   = (Hashtable)htTablaCreditos.get(moneda + tipoOperacion + codigoAuxiliar);
            String       tasaEspecial    = (String)datosCreditos.get("TASAESPECIAL");
            String       cargoTasaMora   = (String)datosCreditos.get("CARGOTASAMORA");
            double       tasaSprea = 0D;
            double       porcCargoTasaMora   = 0D;
            boolean      hayCargoMora = false;
            if (logger.isDebugEnabled())  logger.debug("tasaEspecial [" + tasaEspecial + "]");
            if (tasaEspecial!=null && !tasaEspecial.trim().equals("")) {//si no es nulo, ni blanco
                try {
                    tasaSprea= Double.parseDouble(tasaEspecial);
                    if (logger.isDebugEnabled())  logger.debug("aplica tasaSpread = tasaEspecial [" + tasaSprea + "]");
                } catch (NumberFormatException nfe) {
                	if (logger.isEnabledFor(Level.ERROR)) logger.error("NumberFormatException::tasaEspecial [" + tasaEspecial + "]");
                };
            };
            if (cargoTasaMora!=null && !cargoTasaMora.trim().equals("")) {//si no es nulo, ni blanco
                try {
                    porcCargoTasaMora= Double.parseDouble(cargoTasaMora);
                    if (logger.isDebugEnabled()){ 
	                    logger.debug("aplica cargoTasaMora = [" + cargoTasaMora + "]");
	                    logger.debug("aplica porcCargoTasaMora = [" + porcCargoTasaMora + "]");
                    }
                    hayCargoMora=true;
                } catch (NumberFormatException nfe) {
                	if (logger.isEnabledFor(Level.ERROR)) logger.error("NumberFormatException::factorTasaMora [" + cargoTasaMora + "]");
                };
            };
            //[CPH] FIN Obtencio'n de Tasa Especial WEB (Campan'a)

            if (logger.isDebugEnabled()){  
	            logger.debug("rutDeudor [" + rutDeudor + "]");
	            logger.debug("tasaSprea [" + tasaSprea + "]");
	            logger.debug("Fin obtencion Tasa");
	            logger.debug("Prorroga - INI Consultando Operacion [" + idOperacion + numOperacionCan + "]");
            }
            try{

                ResultConsultaOperacionCredito res_con_ope = consultaOperacionCredito(multiEnvironment, idOperacion, numOperacionCan);
                codigoSegDesgravAux = res_con_ope.getCodigoSegDesgrav().trim();
                if (logger.isDebugEnabled())  logger.debug("Prorroga - codigoSegDesgravAux       [" + codigoSegDesgravAux + "]");
                if (codigoSegDesgravAux.equals("PRECIO")){
                    if (logger.isDebugEnabled())  logger.debug("Prorroga - Antes de obtieneDetalleSegurosPrecios ...");
                    ResultConsultaCgr  obeanCGRSGS = obtieneDetalleSegurosPrecios(multiEnvironment,
                                                                idOperacion,          //("caiOperacion"),
                                                                numOperacionCan + "", //("iicOperacion"),
                                                                "",                   //("extOperacion"),
                                                                "CRE",                //("macroProducto"),
                                                                "CUR",                //("codigoEvento"),
                                                                0,                    //Integer.parseInt(("rutCliente").trim()),
                                                                ' ',                  //("digitoVerificador").charAt(0),
                                                                ' ',                  //("idcCliente").charAt(0),
                                                                "",                   //("glsCliente"),
                                                                "",                   //("glsNomCliente"),
                                                                "SGS",                //("codigoConcepto"),
                                                                'E',                  //("tipoConsulta").charAt(0),
                                                                ' ',                  //("siguiente").charAt(0),
                                                                "",                   //("caiOperacionNext"),
                                                                "",                   //("iicOperacionNext"),
                                                                "",                   //("extOperacionNext"),
                                                                "",                   //("codigoSistemaNext"),
                                                                "",                   //("codigoEventoNext"),
                                                                0);                   //Integer.parseInt(("numero").trim()));

                    if (logger.isDebugEnabled())  logger.debug("Prorroga - Despues de obtieneDetalleSegurosPrecios");

                    if (obeanCGRSGS != null){
                        if (logger.isDebugEnabled())  logger.debug("Prorroga - se obtiene Detalle Seguros Precios");
                        if (obeanCGRSGS.getTotOcurrencias() > 0){
                            seguros = new String[obeanCGRSGS.getTotOcurrencias()];
                            for(int k = 0; k < obeanCGRSGS.getTotOcurrencias(); k++){
                                if (obeanCGRSGS.getInstanciaDeConsultaCgr()[k].getIndVigenciaInst() == 'S'){
                                    seguros[k] = obeanCGRSGS.getInstanciaDeConsultaCgr()[k].getCodSubConceptoInst() + obeanCGRSGS.getInstanciaDeConsultaCgr()[k].getIndCobro();
                                    if (logger.isDebugEnabled())  logger.debug("Prorroga - seguros CGR   =[" + seguros[k] + "]");
                                }else{
                                    seguros[k] = "";
                                }
                            }
                        }else{
                            if (logger.isDebugEnabled())  logger.debug("Prorroga - obeanCGRSGS.getTotOcurrencias() < 0");
                        }
                    }else{
                        if (logger.isDebugEnabled())  logger.debug("Prorroga - obtieneDetalleSegurosPrecios es nulo");
                    }

                }

            } catch (Exception e) {
            	if (logger.isEnabledFor(Level.ERROR)) logger.error("Prorroga - consultaOperacionCredito  ::Exception [" + e.getMessage() + "]");
                throw new MultilineaException("ESPECIAL", "Error en obtencion de Operacion a Prorrogar");
            }
            if (logger.isDebugEnabled())  logger.debug("Prorroga - FIN Consultando Operacion [" + idOperacion + numOperacionCan + "] para Prorroga");


            //*********************************************************************************************//
            //*** INI consulta CLN para obtener condicionGar necesario para el ingreso de la simulacion ***//
            //*** INI Desarrollo BEE 03/2004 hcf                                                        ***//
            //***                                                                                       ***//
            //*********************************************************************************************//
            if (logger.isDebugEnabled())  logger.debug("Antes ResultConsultaCln");

            ResultConsultaCln obeanCln = new ResultConsultaCln();

            int rutD = Integer.parseInt(Long.toString(rutDeudor));
            InputConsultaCln ibeanCln = new InputConsultaCln("025",
                                                              "",                      // nombreDeudor,
                                                              rutDeudor,               // rutDeudor,
                                                              dvDeudor,                // digitoVerificador,
                                                              ' ',                     // indicadorExtIdc,
                                                              "",                      // glosaExtIdc,
                                                              "",                      // idOperacion,
                                                              0,                       // numOperacion,
                                                              0);                      // totLinIngreso);

            consultaCln(multiEnvironment, ibeanCln, obeanCln);

            Linea[] consultaLinea = obeanCln.getLineas();

            boolean tiene_aval = false;
            boolean tiene_foga = false;
            boolean tiene_cual = false;
            boolean tiene_mlt  = false;
            boolean solo_G     = false;

            if (logger.isDebugEnabled())  logger.debug("consultaLinea.length [" + consultaLinea.length + "]");
            String TipLin = null;
            for (int i = 0; i < consultaLinea.length; i++) {
                if (consultaLinea[i] == null) {
                     break;
                 }

                TipLin = consultaLinea[i].getCodigoTipoLinea();
                if (logger.isDebugEnabled()){  
	                logger.debug("TipLin   ["+ TipLin +"]");
	                logger.debug("consultaLinea[i].getCodigoTipoInfo   ["+ consultaLinea[i].getCodigoTipoInfo() +"]");
	                logger.debug("consultaLinea[i].getTipoOperacion    ["+ consultaLinea[i].getTipoOperacion() +"]");
	                logger.debug("consultaLinea[i].getCodAuxiliarLinea ["+ consultaLinea[i].getCodAuxiliarLinea() +"]");
                }
                if (TipLin.equals("MLT")) { //verifica si tipo de linea es multilinea (la ultima)
                    solo_G      = consultaLinea[i].getCodigoTipoInfo() == 'G' ? true : false;
                    tiene_mlt   = true;
                    tiene_aval  = (consultaLinea[i].getTipoOperacion()).equals("AVL") ? true : tiene_aval;
                    tiene_foga  = (consultaLinea[i].getTipoOperacion()).equals("952") ? ((consultaLinea[i].getCodAuxiliarLinea()).trim().equals("101") ? true : tiene_foga) : tiene_foga;
                    tiene_cual  = solo_G ? (!(consultaLinea[i].getTipoOperacion()).trim().equals("") ? true : tiene_cual) : tiene_cual;

                }
            }

            if (idReq.equals("097")) {
                if (!tiene_mlt) {
                    if (logger.isDebugEnabled())  logger.debug("Cliente " + rutDeudor + "-" + dvDeudor + " No posee Multilinea (MLT)");
                    throw new MultilineaException("Especial","Cliente No posee Multilinea (MLT)");
                }
            }
            //  condicionGar -->
            //  ("4  ";  //sin garantia default)
            //  ("8  ";  //otras garantias)
            //  ("2  ";  //aval);
            //  ("10 ";  //fogape);

            String condicionGar   = tiene_aval ? "2  " : (tiene_foga ? "10 " : (tiene_cual ? "8  " : "4  "));


            // destinoCredito -->
            // ("288" : // captrab default)
            // ("301" : // captrab fogape )  <-- confirmado

            String destinoCredito = tiene_foga ? "301" : "288"; //si tiene fogape entonces destinocredito=captrabfogape sino captrab

            if (logger.isDebugEnabled()){  
	            logger.debug("condicionGar   ["+ condicionGar +"]");
	            logger.debug("destinoCredito ["+ destinoCredito +"]");
	            logger.debug("Despues ResultConsultaCln obeanCln");
            }
            //*********************************************************************************************//
            //*** FIN Desarrollo BEE 03/2004 hcf                                                        ***//
            //*** FIN consulta CLN                                                                      ***//
            //*********************************************************************************************//

            //CPH M090304
            ResultContextualizacionIngrCancelacion res_ctx = contextualizacionIngrCancelacion(multiEnvironment, idOperacion, numOperacionCan, 0);
            Date fechaVencimiento = res_ctx.getFecVencimiento();
            Double tasaCan = null;

            if (logger.isDebugEnabled()){  
	            logger.debug("fechaHoy=" + fechaHoy);
	            logger.debug("fechaVencimiento=" + ddMMyyyy_form.format(fechaVencimiento));
            }
            if (new SimpleDateFormat("yyyyMMdd").format(fechaVencimiento).compareTo(new SimpleDateFormat("yyyyMMdd").format(today))>0) {
                tasaCan=new Double(res_ctx.getTasaSpread());
            };
            if (logger.isDebugEnabled()){  
	            logger.debug("tasaCancelacion=" + tasaCan);
	            logger.debug("ejecutivoCan=" + res_ctx.getEjecutivo());
	            logger.debug("ini visacion en prorroga");
	        }
            //  whoVisa = -1  no visa  a nadie  //
            //  whoVisa >= 0  visa empresa e indica cuantos avales visar  //

            boolean tiene_avales = condicionGar.equals("2  ") ? true : false;
            boolean visa_empresa = whoVisa >= 0 ? true : false;
            boolean visa_avales  = whoVisa >  0 ? true : false;
            if (logger.isDebugEnabled()){ 
	            logger.debug("visa_empresa [" + (visa_empresa ? "SI" : "NO") + "]");
	            logger.debug("visa_avales  [" + (visa_avales  ? "SI(" + whoVisa + ")" : "NO") + "]");
            }
            String respuestaVisacion = null;

            if (visa_empresa){
                respuestaVisacion = visacionRut(String.valueOf(rutDeudor), dvDeudor, canal, "EMP");
                if ((respuestaVisacion == null) || (respuestaVisacion != null && !respuestaVisacion.trim().equals("AP"))){
                    datosLog.put("ERR","Error Visacion (" + respuestaVisacion + ") para " + String.valueOf(rutDeudor) + "-" + dvDeudor);
                        throw new MultilineaException("ESPECIAL", "ERRVISACION" + respuestaVisacion);
                    }
                if (logger.isDebugEnabled())  logger.debug("visacion de empresa OK");
            }

//            if (visa_avales){
//                if (logger.isDebugEnabled())  logger.debug("visa_avales [true]");
                if (tiene_avales){
                    if (logger.isDebugEnabled())  logger.debug("tiene_avales [true]");
                    //Consulta de avales si es que existe condicion de garantia = aval
                    //luego se deben visar los 'whoVisa' primeros ...
                    controlError = "ERRAVCAVL";
                    ResultConsultaAvales obeanAval = new ResultConsultaAvales();

                    InputConsultaAvales abean = new InputConsultaAvales("026",
                                                                        Integer.parseInt(String.valueOf(rutDeudor)),    //      int rutDeudor,
                                                                        dvDeudor,                                       //      char digitoVerificaCli,
                                                                        ' ',                                            //      idCliente,
                                                                        " ",                                            //      glosaCliente,
                                                                        " ",                                            //      nombreCli,
                                                                        " ",                                            //      idOperacion,
                                                                        0 ,                                             //      numOperacion,
                                                                        0,                                              //      numCorrelativo,
                                                                        "AVL",                                          //      tipoOperacion SOLO AVALES,
                                                                        "AVC");                                         //      tipoCredito ASOCIADO A MULTILINEA);

                        consultaAvales(multiEnvironment, abean, obeanAval);

                    if (logger.isDebugEnabled())  logger.debug("despues consultaAvales");

                    Aval[] avales    = obeanAval.getAvales();

                    if (logger.isDebugEnabled())  logger.debug("avales.length ["+ avales.length +"]");

                    int visados = 0;

                    for (int i = 0; i < avales.length; i++) {

                        if (visados >= whoVisa) //Visa hasta 'whoVisa' avales
                            break;

                        String rutAval = Integer.toString(avales[i].getRutAval());
                        char   dvfAval = avales[i].getDigitoVerificaAval();
                        char   indvige = avales[i].getVigente();
                        controlError = "";

                        if (logger.isDebugEnabled())  logger.debug("esta vigente el rut "+ rutAval +" ?    vigente["+ indvige +"]");
                        if (indvige == 'S'){
                            if (visa_avales){
                                valInteOperacion = valInteOperacion == 0.1 ? avales[i].getRutAval() : valInteOperacion;
                                if (logger.isDebugEnabled()){  
	                                logger.debug("valInteOperacion["  + valInteOperacion + "]");
	                                logger.debug("visa_avales     [true] [" + rutAval+ "]");
	                             }
                            respuestaVisacion = visacionRut(rutAval, dvfAval, canal, "EMP");
                            if ((respuestaVisacion == null) || (respuestaVisacion != null && !respuestaVisacion.trim().equals("AP"))){
                                datosLog.put("ERR","Error Visacion (" + respuestaVisacion + ") para " + String.valueOf(rutAval) + "-" + dvfAval);
                                    throw new MultilineaException("ESPECIAL", "ERRVISACION" + respuestaVisacion);
                                }
                            visados++;
                        }
                            if (!visa_avales){
                                valInteOperacion = avales[i].getRutAval();
                                rutAvalDPS       = avales[i].getRutAval();
                                rutdigAvalDPS    = avales[i].getDigitoVerificaAval();
                                if (logger.isDebugEnabled()){  
	                                logger.debug("visa_avales     [false]");
	                                logger.debug("valInteOperacion["  + valInteOperacion + "]");
	                                logger.debug("rutAvalDPS      ["  + rutAvalDPS       + "]");
	                                logger.debug("rutdigAvalDPS   ["  + rutdigAvalDPS    + "]");
                                }
                                break;
                            }
                         }
                    }
                    if (logger.isDebugEnabled())  logger.debug("cuantos viso ["+ visados +"]");
                }
                if (logger.isDebugEnabled())  logger.debug("visacion de avales OK");
//             }
            controlError = "";
            if (logger.isDebugEnabled())  logger.debug("fin visacion en prorroga");

            if (logger.isDebugEnabled())  logger.debug("antes ResultRenovacionMultilinea obean = new ResultRenovacionMultilinea()...");

            ResultRenovacionMultilinea obean = new ResultRenovacionMultilinea();

            //******************************************//
            //********  INI Consulta SPR ***************//
            //******************************************//
            if (logger.isDebugEnabled())  logger.debug("llenando vencimientos[0].getPeriodoEntreVctoExpresaEn()=[" + vencimientos[0].getPeriodoEntreVctoExpresaEn() + "]");

            ResultConsultaSpr  obeanSpr = obtieneTasaMultilineaPrecios(multiEnvironment,
                                                                       "CUR",
                                                                       "INT",
                                                                       today,//ddMMyyyy_form.parse("02092003"),;
                                                                       rutDeudor,
                                                                       dvDeudor,
                                                                       canal,//"130"
                                                                       "", //subcanal (oficina)
                                                                       moneda,
                                                                       tipoOperacion,
                                                                       codigoAuxiliar,
                                                                       ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,0,0) < 1 ? 1 : ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,0,0),
                                                                       'D',
                                                                       ((moneda.trim().equals("U.FOME") || moneda.trim().equals("0998")) ? monto : valorRenovado),
                                                                       (double) vencimientos[0].getTotalVencimientosGrupo(),
                                                                       "",//codClfRentabilidad
                                                                       "",//codFactorRiesgo
                                                                       "",//indTipoPago
                                                                       codSegmento,
                                                                       idOperacion,
                                                                       numOperacionCan
                                                                       );

            //******************************************//
            //********  FIN Consulta SPR ***************//
            //******************************************//
            if (obeanSpr==null || obeanSpr.getTotOcurrencias()==0) {
                throw new MultilineaException("ESPECIAL","Error en obtencion de tasa: SPR nulo o 0 ocurrencias");
            }

                //********  INI Consulta SEGUROS Prorroga ***************//
                if (codigoSegDesgravAux.equals("PRECIO")){
                    String  segurosVigentes   = ((String) TablaValores.getValor("multilinea.parametros", "ProrrogasBancasSeguros", "vigente"));
                    consultaSeguros           = (segurosVigentes == null ? " " : segurosVigentes).trim().equals("S");
                    if (logger.isDebugEnabled())  logger.debug("segurosVigentes   [" + segurosVigentes + "]  codBanca[" + codBanca + "]  canal[" + canal + "] Prorroga");

                    if (consultaSeguros){
                        String  bancapermitidas = TablaValores.getValor("multilinea.parametros", "ProrrogasBancasSeguros", "banca");
                        if (logger.isDebugEnabled())  logger.debug("bancas permitidas [" + bancapermitidas + "]  codBanca[" + codBanca + "]  canal[" + canal + "] Prorroga");
                        consultaSeguros   = verificaPertenenciaBanca(codBanca, bancapermitidas);

                        if (consultaSeguros){
                            try{

                                if (logger.isDebugEnabled())  logger.debug("Antes de la consulta SPR para Seguros Multilinea Prorroga");

                                obeanSprSGS = obtieneSegurosDesdePrecios(multiEnvironment,
                                                                        "CUR",                               //codEvento
                                                                        "SGS",                               //codConcepto
                                                                        fechaCurse,
                                                                        "CON",                               //codTipoConsulta siempre CON para SGS
                                                                        "",                                  // idOperacion,                         //caiOperacion
                                                                        0,                                   // numOperacionCan,                     //iicOperacion
                                                                        "",                                  //extOperacion
                                                                        rutDeudor,
                                                                        dvDeudor,
                                                                        ' ',                                 //indCliente
                                                                        "",                                  //glsCliente
                                                                        canal,                               //130 o 230
                                                                        "",                                  //codigo oficina va si solo si canal = 110
                                                                        moneda,                              //0999
                                                                        tipoOperacion,                       //CON
                                                                        codigoAuxiliar,                      //050
                                                                        ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,0,0) < 1 ? 1 : ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,0,0),
                                                                        'D',                                 // expresa en
                                                                        vencimientos[0].getFechaPrimerVcto(),//fecVctoInicial
                                                                        ((moneda.trim().equals("U.FOME") || moneda.trim().equals("0998")) ? monto : valorRenovado),       //montoCredito,
                                                                        0D,                                  //valSaldoOperacion
                                                                        (double) vencimientos[0].getTotalVencimientosGrupo(), //(double) numCuotas,
                                                                        valInteOperacion,                    //valInteOperacion
                                                                        0D,                                  //valMontoAdicional
                                                                        "",                                  //codClfRentabilidad,
                                                                        "",                                  //codFactorRiesgo,
                                                                        "",                                  //indTipoPago,
                                                                        "",                                  //caiOperacionRcc
                                                                        codSegmento                          //codSegmento
                                                                        );

                                if (logger.isDebugEnabled())  logger.debug("Despues de la consulta SPR a Seguros Multilinea Prorroga");

                            } catch(Exception e) {
                                if (logger.isDebugEnabled())  logger.debug("ERROR***SimulaCursaCreditoException...Exception [" + e.getMessage() + "]");
                            }

                            if (obeanSprSGS==null || obeanSprSGS.getTotOcurrencias()==0) {
                                if (logger.isDebugEnabled())  logger.debug("La consulta SPR no encuentra ocurrencias en Seguros");
                            } else {
                                for(int i = 0; i < obeanSprSGS.getTotOcurrencias(); i++){
                                    if (obeanSprSGS.getInstanciaDeConsultaSpr(i) == null){
                                        if (logger.isDebugEnabled())  logger.debug("Saliendo del llenando vector de SPR's SEGUROS");
                                        break;
                                    } else {
                                        vectorSPR.add(obeanSprSGS.getInstanciaDeConsultaSpr(i));
                                        if (logger.isDebugEnabled())  logger.debug("llenando vector de SPR's :" + i);
                                    }
                                }
                            }
                        }else{
                            if (logger.isDebugEnabled())  logger.debug("No se consultaron seguros porque banca no esta inscrita. Prorroga");
                        }
                    }
                }
                //********  FIN Consulta SEGUROS Prorroga ***************//


            //******************************************//
            //********  INI Consulta PPC ***************//
            //******************************************//
            ResultConsultaSpr  obeanPpc = null;
            try {
                obeanPpc = obtieneTasaMultilineaPrecios(multiEnvironment,
                                                       "CUR",
                                                       "PPC",
                                                       today,//ddMMyyyy_form.parse("02092003")
                                                       rutDeudor,
                                                       dvDeudor,
                                                       canal,//"130"
                                                       "", //subcanal (oficina)
                                                       moneda,
                                                       tipoOperacion,
                                                       codigoAuxiliar,
                                                       ManejoEvc.getPlazoEnMeses(fechaCurse,analisisFeriado,vencimientos,0,0,0,0),
                                                       vencimientos[0].getPeriodoEntreVctoExpresaEn(),//'M'
                                                       ((moneda.trim().equals("U.FOME") || moneda.trim().equals("0998")) ? monto : valorRenovado),
                                                       (double) vencimientos[0].getTotalVencimientosGrupo(),
                                                       "",//codClfRentabilidad
                                                       "",//codFactorRiesgo
                                                       "",//indTipoPago
                                                       codSegmento,
                                                       idOperacion,
                                                       numOperacionCan);
            } catch (Exception e) {
            	if (logger.isEnabledFor(Level.ERROR)) logger.error("consulta PPC::" + e.getMessage());
            }

            //******************************************//
            //********  FIN Consulta PPC ***************//
            //******************************************//
            if (obeanPpc==null || obeanPpc.getTotOcurrencias()==0) {
                if (logger.isDebugEnabled())  logger.debug("obeanPpc nulo o 0 ocurrencias");
            } else {
                if (logger.isDebugEnabled())  logger.debug("Rango (" + form.format(obeanPpc.getInstanciaDeConsultaSpr(0).getValMonto()) + " - " + form.format(obeanPpc.getInstanciaDeConsultaSpr(0).getValTasaMonto())+")");
                if ((((moneda.trim().equals("U.FOME") || moneda.trim().equals("0998")) ? monto : valorRenovado)) > obeanPpc.getInstanciaDeConsultaSpr(0).getValTasaMonto()) {
                    throw new MultilineaException("ESPECIAL","Monto mayor que el permitido (" + form.format(obeanPpc.getInstanciaDeConsultaSpr(0).getValTasaMonto()) + ")");
                }
                if ((((moneda.trim().equals("U.FOME") || moneda.trim().equals("0998")) ? monto : valorRenovado)) < obeanPpc.getInstanciaDeConsultaSpr(0).getValMonto()){
                    throw new MultilineaException("ESPECIAL","Monto menor que el permitido (" + form.format(obeanPpc.getInstanciaDeConsultaSpr(0).getValMonto()) +")");
                }
            }

            //******************************************//
            //********  INI ingresoProrroga **********//
            //******************************************//
            String origen = "XXX";
            char indseq = 'X';
            if (logger.isDebugEnabled())  logger.debug("========== CAN ============");

            InputIngresoCancelacion [] can_s = new InputIngresoCancelacion[1];
            can_s[0] = new InputIngresoCancelacion();
            can_s[0].setCim_reqnum("038");
            can_s[0].setIdOperacion(idOperacion);
            if (logger.isDebugEnabled())  logger.debug("idOperacion can_s[0]=" + idOperacion);
            can_s[0].setNumOperacionCan(numOperacionCan);
            if (logger.isDebugEnabled())  logger.debug("numOperacionCan=" + numOperacionCan);
            can_s[0].setTipoCancelacion("PTT");                 //      <--- dato
            if (logger.isDebugEnabled())  logger.debug("ejecutivo=" + ejecutivo);
            can_s[0].setEjecutivo(ejecutivo);
            if (logger.isDebugEnabled())  logger.debug("oficinaCancel=" + oficinaCancel);
            can_s[0].setOficinaCancel(oficinaCancel);
            if (logger.isDebugEnabled())  logger.debug("valorRenovado=" + valorRenovado);
            can_s[0].setValorRenovado(valorRenovado);
            can_s[0].setIdCuentaCargo("0000");
            if (logger.isDebugEnabled())  logger.debug("pinCtaCargo=" + pinCtaCargo);
            can_s[0].setNumCuentaCargo(pinCtaCargo);
            //Si cargo es 0 el tipo de cargo debe ir vacio
            if (logger.isDebugEnabled())  logger.debug("totalPagado=" + totalPagado);
            if (totalPagado == 0D) {
                can_s[0].setTipoCargo("");
            }
            else {
                can_s[0].setTipoCargo("CCMA");
            }
            can_s[0].setTasaInteresCancel(tasaCan);
            if (logger.isDebugEnabled())  logger.debug("========== OPC ============");
            //Una sola ocurrecia de OPC
            InputIngresoUnitarioDeOperacionDeCreditoOpc [] opc_s = new InputIngresoUnitarioDeOperacionDeCreditoOpc[1];
            opc_s[0] = new InputIngresoUnitarioDeOperacionDeCreditoOpc();
            opc_s[0].setCim_reqnum("046");
            opc_s[0].setCim_uniqueid(origen);
            opc_s[0].setCim_indseq(indseq);
            if (logger.isDebugEnabled())  logger.debug("tipoOperacion (String)    : '" + tipoOperacion + "'");
            opc_s[0].setTipoOperacion(tipoOperacion);
            if (logger.isDebugEnabled())  logger.debug("moneda        (String)    : '" + moneda + "'");
            opc_s[0].setMoneda(moneda);
            if (logger.isDebugEnabled())  logger.debug("codigoAuxiliar(String)    : '" + codigoAuxiliar + "'");
            opc_s[0].setCodigoAuxiliar(codigoAuxiliar);
            if (logger.isDebugEnabled())  logger.debug("oficinaIngreso(String)    : '" + oficinaIngreso + "'");
            opc_s[0].setOficinaIngreso(oficinaIngreso);
            if (logger.isDebugEnabled())  logger.debug("rutDeudor     (int)       : '" + rutDeudor + "'");
            opc_s[0].setRutDeudor(rutDeudor);
            if (logger.isDebugEnabled())  logger.debug("digitoVerificador(Char)   : '" + dvDeudor + "'");
            opc_s[0].setDigitoVerificador(dvDeudor);
            if (logger.isDebugEnabled())  logger.debug("montoCredito  (double)    : '" + monto + "'");
            opc_s[0].setMontoCredito(monto);
            if (logger.isDebugEnabled())  logger.debug("codigoSegDesgrav(String)  : '" + codigoSegDesgrav + "'");

            if (obeanSprSGS != null && obeanSprSGS.getTotOcurrencias() > 0){
                codigoSegDesgrav = "PRECIO";
                if (logger.isDebugEnabled())  logger.debug("codigoSegDesgrav**PRO*: '" + codigoSegDesgrav + "'");
            }
            opc_s[0].setCodigoSegDesgrav(codigoSegDesgrav);

            if (logger.isDebugEnabled())  logger.debug("fechaCurse (Date)          : '" + fechaCurse + "'");
            if (logger.isDebugEnabled())  logger.debug("tasaSprea (double)          : '" + obeanSpr.getInstanciaDeConsultaSpr(0).getValPropuesto() + "'");
            opc_s[0].setTasaSprea(obeanSpr.getInstanciaDeConsultaSpr(0).getValPropuesto());
            if (logger.isDebugEnabled())  logger.debug("condicionGar (String)       : '" + condicionGar + "'");
            opc_s[0].setCondicionGar(condicionGar);
            if (logger.isDebugEnabled())  logger.debug("abono (String)             : '" + "CCMA" + "'");
            opc_s[0].setAbono("CCMA");
            if (logger.isDebugEnabled())  logger.debug("cargo (String)             : '" + "AUT" + "'");
            opc_s[0].setCargo("AUT");
            if (logger.isDebugEnabled())  logger.debug("ctaAbono (String)          : '" + "0000" + "'");
            opc_s[0].setCtaAbono("0000");
            if (logger.isDebugEnabled())  logger.debug("ctaAbonoTer (int)          : '" + ctaAbonoTer + "'");
            opc_s[0].setCtaAbonoTer(ctaAbonoTer);
            if (logger.isDebugEnabled())  logger.debug("destinoCredito (String)    : '" + destinoCredito + "'");
            opc_s[0].setDestinoCredito(destinoCredito);
            if (logger.isDebugEnabled())  logger.debug("vigenciaCargo (char)       : '" + 'S' + "'");
            opc_s[0].setVigenciaCargo('S');
            if (logger.isDebugEnabled())  logger.debug("ctaCargo (String)         : '" + "0000" + "'");
            opc_s[0].setCtaCargo("0000");
            if (logger.isDebugEnabled())  logger.debug("pinCtaCargo (int)          : '" + pinCtaCargo + "'");
            opc_s[0].setPinCtaCargo(pinCtaCargo);

            if (logger.isDebugEnabled())  logger.debug("========== RDCs ============");
            InputIngresoUnitarioDeRdc []                    rdc_s   = null;

            if (logger.isDebugEnabled())  logger.debug("========== DLC ============");
            InputIngresoDeDlcLlavesYCampos []               dlc_s   = new InputIngresoDeDlcLlavesYCampos[1];
            dlc_s[0] = new  InputIngresoDeDlcLlavesYCampos();
            dlc_s[0].setCim_reqnum("013");
            dlc_s[0].setCim_uniqueid(origen);
            dlc_s[0].setCim_indseq(indseq);

            if (logger.isDebugEnabled())  logger.debug("========== CYA ============");
            InputIngresoUnitarioCya []                      cya_s   = null;

            if (logger.isDebugEnabled())  logger.debug("========== EVC ============");
            InputIngresoUnitarioDeEvc []                    evc_s   = new InputIngresoUnitarioDeEvc[1];
            evc_s[0] = new InputIngresoUnitarioDeEvc();
            evc_s[0].setCim_reqnum("016");
            evc_s[0].setCim_uniqueid(origen);
            evc_s[0].setCim_indseq(indseq);
            if (logger.isDebugEnabled())  logger.debug("docLegalNumero (int)          : '" + 1 + "'");
            evc_s[0].setDocLegalNumero(1);
            if (logger.isDebugEnabled())  logger.debug("totalVencimientosGrupo (int)  : '" + vencimientos[0].getTotalVencimientosGrupo() + "'");
            evc_s[0].setTotalVencimientosGrupo(vencimientos[0].getTotalVencimientosGrupo());
            if (logger.isDebugEnabled())  logger.debug("fechaPrimerVcto (Date)        : '" + vencimientos[0].getFechaPrimerVcto() + "'");
            evc_s[0].setFechaPrimerVcto(vencimientos[0].getFechaPrimerVcto());
            //if (logger.isDebugEnabled())  logger.debug("fechaPrimerVcto (Date)BEE06   : '" + "29092003" + "'");
            //evc_s[0].setFechaPrimerVcto(ddMMyyyy_form.parse("29092003"));
            if (logger.isDebugEnabled())  logger.debug("periodoEntreVcto (int)        : '" + vencimientos[0].getPeriodoEntreVcto() + "'");
            evc_s[0].setPeriodoEntreVcto(vencimientos[0].getPeriodoEntreVcto());
            if (logger.isDebugEnabled())  logger.debug("periodoEntreVctoExpresaEn (char): '" + vencimientos[0].getPeriodoEntreVctoExpresaEn() + "'");
            evc_s[0].setPeriodoEntreVctoExpresaEn(vencimientos[0].getPeriodoEntreVctoExpresaEn());

            if (logger.isDebugEnabled())  logger.debug("========== ICG ============");
            InputIngresoUnitarioIcg []                      icg_s   = null;

            if (logger.isDebugEnabled())  logger.debug("========== VEN ============");
            InputIngresoUnitarioDeVen []                    ven_s   = null;

            if (logger.isDebugEnabled())  logger.debug("========== ROC PRO ============");

            DetalleConsultaSpr [] arregloSPR            = null;
            InputIngresoRocAmpliada [] roc_sAmpliada    = null;
            Vector vectorSGS                            = new Vector();

            if (consultaSeguros){
                if (logger.isDebugEnabled())  logger.debug("Se consultaron los seguros PRO");
                arregloSPR      = new DetalleConsultaSpr[vectorSPR.size()];
                arregloSPR      = (DetalleConsultaSpr[]) vectorSPR.toArray(new DetalleConsultaSpr[0]);
                roc_sAmpliada   = new InputIngresoRocAmpliada[vectorSPR.size()];

                if (seguros != null){
                    for(int i = 0; i < seguros.length; i++){
                        if (logger.isDebugEnabled())  logger.debug("seguros[" + i +"] [" + seguros[i] + "]");
                        vectorSGS.add(seguros[i]);
                    }
                }
            }

            if (logger.isDebugEnabled())  logger.debug("================ INT PRO ======");
            int j=0;
            int totOcurrenciasRoc = obeanSpr.getTotOcurrencias();
            if (logger.isDebugEnabled())  logger.debug("totOcurrenciasRoc=" + totOcurrenciasRoc);
            if (obeanPpc!=null) {
                totOcurrenciasRoc = totOcurrenciasRoc + obeanPpc.getTotOcurrencias();
            }

            if (obeanSprSGS != null){
                totOcurrenciasRoc = totOcurrenciasRoc + obeanSprSGS.getTotOcurrencias();
                if (logger.isDebugEnabled())  logger.debug("totOcurrenciasRoc=" + totOcurrenciasRoc + " (SGS)");
            }

            if (diasEnMora>0 && hayCargoMora) { //aplicar recargo porcentual
                    totOcurrenciasRoc = totOcurrenciasRoc + 1;
            }

            if (logger.isDebugEnabled())  logger.debug("totOcurrenciasRoc=" + totOcurrenciasRoc);

            InputIngresoRoc []   roc_s   = new InputIngresoRoc[totOcurrenciasRoc];

            if (logger.isDebugEnabled())  logger.debug("================ INT ======");

            if (logger.isDebugEnabled())  logger.debug("0 - " + (obeanSpr.getTotOcurrencias()-1));

            for (int i=0; i<obeanSpr.getTotOcurrencias();i++) {
                roc_s[i] = new InputIngresoRoc();
                roc_s[i].setCodSistema(obeanSpr.getInstanciaDeConsultaSpr(i).getCodSistemaCpt());
                roc_s[i].setCodEvento(obeanSpr.getInstanciaDeConsultaSpr(i).getCodEventoCpt());
                roc_s[i].setCodigoConcepto(obeanSpr.getInstanciaDeConsultaSpr(i).getCodConceptoCpt());
                roc_s[i].setCodModalidad(obeanSpr.getInstanciaDeConsultaSpr(i).getCodSubConcepto2Cpt());
                roc_s[i].setCaiPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getCaiConceptoCpt());
                roc_s[i].setIicPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getIicConceptoCpt());
                roc_s[i].setFechaPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getFecIniVigenciaCpt());
                roc_s[i].setHoraPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getHraIniVigenciaCpt());
                roc_s[i].setIndCobro(obeanSpr.getInstanciaDeConsultaSpr(i).getCodSubConcepto3Cpt());
                roc_s[i].setIndTipoPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getIndTipoPlantillaCpt());
                roc_s[i].setCodigoMoneda(obeanSpr.getInstanciaDeConsultaSpr(i).getCodMonedaCpt());
                roc_s[i].setTasaMontoInformado(obeanSpr.getInstanciaDeConsultaSpr(i).getValTasaMonto());
                roc_s[i].setIndTipTasBas(obeanSpr.getInstanciaDeConsultaSpr(i).getIndTipTasBas());
                roc_s[i].setIndPerBasTas(obeanSpr.getInstanciaDeConsultaSpr(i).getIndPerBasTas());
                roc_s[i].setInsBajPerBas(obeanSpr.getInstanciaDeConsultaSpr(i).getInsBajPerBas());
                roc_s[i].setIndSobPerBas(obeanSpr.getInstanciaDeConsultaSpr(i).getIndSobPerBas());
                roc_s[i].setIndBasTasVar(obeanSpr.getInstanciaDeConsultaSpr(i).getIndBasTasVar());
                roc_s[i].setIndTipFecPerRep(obeanSpr.getInstanciaDeConsultaSpr(i).getIndTipFecPerRep());
                roc_s[i].setNumPerRep(obeanSpr.getInstanciaDeConsultaSpr(i).getNumPerRep());
                roc_s[i].setIndPerRep(obeanSpr.getInstanciaDeConsultaSpr(i).getIndPerRep());
                roc_s[i].setCostoFondo(obeanSpr.getInstanciaDeConsultaSpr(i).getCostoFondoInformado());
                roc_s[i].setFactorDeRiesgo((double) obeanSpr.getInstanciaDeConsultaSpr(i).getFactorRiesgoInformado());
                roc_s[i].setTasaMontoFinal(obeanSpr.getInstanciaDeConsultaSpr(i).getValMonto());
                roc_s[i].setIndVigencia('S');
                if (obeanSpr.getInstanciaDeConsultaSpr(i).getIndTipoPlantillaCpt()=='P' && i==0) {
                    roc_s[0].setTasaMontoFinal(obeanSpr.getInstanciaDeConsultaSpr(0).getValPropuesto());
                };
                if (logger.isDebugEnabled()){  
	                logger.debug("roc_s[" + i + "].setCodigoConcepto()="     + roc_s[i].getCodigoConcepto());
	                logger.debug("roc_s[" + i + "].setCaiPlantilla()="       + roc_s[i].getCaiPlantilla());
	                logger.debug("roc_s[" + i + "].setIicPlantilla()="       + roc_s[i].getIicPlantilla());
	                logger.debug("roc_s[" + i + "].setTasaMontoInformado()=" + roc_s[i].getTasaMontoInformado());
	                logger.debug("roc_s[" + i + "].setTasaMontoFinal()="     + roc_s[i].getTasaMontoFinal());
                }
                j=i;
            };

            if (logger.isDebugEnabled()){ 
	            logger.debug("diasEnMora=" + diasEnMora);
	            logger.debug("hayCargoMora=" + hayCargoMora);
            }
            if (diasEnMora>0 && hayCargoMora) { //ingresamos roc especial con descuento y aplicamos cambio a roc[0]
                if (logger.isDebugEnabled())  logger.debug("hayCargoMora");
                j = j+1;
                roc_s[j] = new InputIngresoRoc();
                roc_s[j].setCodSistema(obeanSpr.getInstanciaDeConsultaSpr(0).getCodSistemaCpt());
                roc_s[j].setCodEvento(obeanSpr.getInstanciaDeConsultaSpr(0).getCodEventoCpt());
                roc_s[j].setCodigoConcepto(obeanSpr.getInstanciaDeConsultaSpr(0).getCodConceptoCpt());
                roc_s[j].setCaiPlantilla(res_ctx.getEjecutivo().trim().substring(0,4));
                roc_s[j].setIicPlantilla(res_ctx.getEjecutivo().trim().substring(4));
                roc_s[j].setFechaPlantilla(fechaCurse);
                roc_s[j].setHoraPlantilla("080000");
                roc_s[j].setIndTipoPlantilla('X'); //plantilla de porcentaje de cargos
                roc_s[j].setCodigoMoneda(obeanSpr.getInstanciaDeConsultaSpr(0).getCodMonedaCpt());
                roc_s[j].setTasaMontoInformado(porcCargoTasaMora);
                roc_s[j].setTasaMontoFinal(roc_s[0].getTasaMontoFinal() * (1 + porcCargoTasaMora));
                roc_s[j].setIndVigencia('S');
                roc_s[0].setTasaMontoFinal(roc_s[0].getTasaMontoFinal() * (1 + porcCargoTasaMora));
                //Por consistencia modificamos en opc tasa Spread
                opc_s[0].setTasaSprea(roc_s[0].getTasaMontoFinal() * (1 + porcCargoTasaMora));
                if (logger.isDebugEnabled()){ 
	                logger.debug("roc_s[" + j + "].setCodSistema()=" + roc_s[j].getCodSistema());
	                logger.debug("roc_s[" + j + "].setCodEvento()=" + roc_s[j].getCodEvento());
	                logger.debug("roc_s[" + j + "].setCodigoConcepto()=" + roc_s[j].getCodigoConcepto());
	                logger.debug("roc_s[" + j + "].setCaiPlantilla()=" + roc_s[j].getCaiPlantilla());
	                logger.debug("roc_s[" + j + "].setIicPlantilla()=" + roc_s[j].getIicPlantilla());
	                logger.debug("roc_s[" + j + "].setFechaPlantilla()=" + roc_s[j].getFechaPlantilla());
	                logger.debug("roc_s[" + j + "].setHoraPlantilla()=" + roc_s[j].getHoraPlantilla());
	                logger.debug("roc_s[" + j + "].setIndTipoPlantilla()=" + roc_s[j].getIndTipoPlantilla());
	                logger.debug("roc_s[" + j + "].setTasaMontoInformado()=" + roc_s[j].getTasaMontoInformado());
	                logger.debug("roc_s[" + j + "].setTasaMontoFinal()=" + roc_s[j].getTasaMontoFinal());
                }
            }

            if (logger.isDebugEnabled())  logger.debug("================ PPC ======");
            if (obeanPpc==null || obeanPpc.getTotOcurrencias()==0) {
                if (logger.isDebugEnabled())  logger.debug("PPC: SPR nulo o 0 ocurrencias");
            } else {
                for (int i=0; i<(obeanPpc.getTotOcurrencias());i++) {
                    j = j+1;
                    if (logger.isDebugEnabled())  logger.debug("set roc_s[" + j + "] ppc");
                    roc_s[j] = new InputIngresoRoc();
                    roc_s[j].setCodSistema(obeanPpc.getInstanciaDeConsultaSpr(i).getCodSistemaCpt());
                    roc_s[j].setCodEvento(obeanPpc.getInstanciaDeConsultaSpr(i).getCodEventoCpt());
                    roc_s[j].setCodigoConcepto(obeanPpc.getInstanciaDeConsultaSpr(i).getCodConceptoCpt());
                    roc_s[j].setCodModalidad(obeanPpc.getInstanciaDeConsultaSpr(i).getCodSubConcepto2Cpt());
                    roc_s[j].setCaiPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getCaiConceptoCpt());
                    roc_s[j].setIicPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getIicConceptoCpt());
                    roc_s[j].setFechaPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getFecIniVigenciaCpt());
                    roc_s[j].setHoraPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getHraIniVigenciaCpt());
                    roc_s[j].setIndCobro(obeanPpc.getInstanciaDeConsultaSpr(i).getCodSubConcepto3Cpt());
                    roc_s[j].setIndTipoPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getIndTipoPlantillaCpt());
                    roc_s[j].setCodigoMoneda(obeanPpc.getInstanciaDeConsultaSpr(i).getCodMonedaCpt());
                    roc_s[j].setIndVigencia('S');
                    if (logger.isDebugEnabled()){ 
	                    logger.debug("roc_s[" + j + "].setCodSistema()=" + roc_s[i].getCodSistema());
	                    logger.debug("roc_s[" + j + "].setCodEvento()=" + roc_s[i].getCodEvento());
	                    logger.debug("roc_s[" + j + "].setCodigoConcepto()=" + roc_s[i].getCodigoConcepto());
	                    logger.debug("roc_s[" + j + "].setCodModalidad()=" + roc_s[i].getCodModalidad());
	                    logger.debug("roc_s[" + j + "].setCaiPlantilla()=" + roc_s[i].getCaiPlantilla());
	                    logger.debug("roc_s[" + j + "].setIicPlantilla()=" + roc_s[i].getIicPlantilla());
	                    logger.debug("roc_s[" + j + "].setFechaPlantilla()=" + roc_s[i].getFechaPlantilla());
	                    logger.debug("roc_s[" + j + "].setHoraPlantilla()=" + roc_s[i].getHoraPlantilla());
	                    logger.debug("roc_s[" + j + "].setIndCobro()=" + roc_s[i].getIndCobro());
	                    logger.debug("roc_s[" + j + "].setIndTipoPlantilla()=" + roc_s[i].getIndTipoPlantilla());
	                    logger.debug("roc_s[" + j + "].setCodigoMoneda()=" + roc_s[i].getCodigoMoneda());
	                    logger.debug("roc_s[" + j + "].setIndVigencia()=" + roc_s[i].getIndVigencia());
                    }
                }
            }

                if (logger.isDebugEnabled())  logger.debug("======== SEGUROS PRORROGA ======");
                if (obeanSprSGS == null || obeanSprSGS.getTotOcurrencias() == 0) {
                    if (logger.isDebugEnabled())  logger.debug("SGS: SPR nulo o 0 ocurrencias");
                } else {
                    if (logger.isDebugEnabled())  logger.debug("roc[" + j + "] SGS ...");
                    for(int i = 0; i < obeanSprSGS.getTotOcurrencias(); i++){
                        j = j+1;
                        /** Seteo del objeto roc_s, que será enviado a el ingreso de una OPC */
                        if (logger.isDebugEnabled())  logger.debug("set roc_s[" + j + "] seguros");
                        roc_s[j] = new InputIngresoRoc();
                        roc_s[j].setCodSistema(arregloSPR[i].getCodSistemaCpt());                       // String
                        roc_s[j].setCodEvento(arregloSPR[i].getCodEventoCpt());                         // String
                        roc_s[j].setCodigoConcepto(arregloSPR[i].getCodConceptoCpt());                  // String
                        roc_s[j].setCodigoSubConcepto(arregloSPR[i].getCodSubConcepto1Cpt());           // String
                        roc_s[j].setCodModalidad(arregloSPR[i].getCodSubConcepto2Cpt());                // String
                        roc_s[j].setIndCobro(arregloSPR[i].getCodSubConcepto3Cpt());                    // String
                        roc_s[j].setCaiPlantilla(arregloSPR[i].getCaiConceptoCpt());                    // String
                        roc_s[j].setIicPlantilla(arregloSPR[i].getIicConceptoCpt());                    // String
                        roc_s[j].setIndTipoPlantilla(arregloSPR[i].getIndTipoPlantillaCpt());           // char
                        roc_s[j].setCodigoMoneda(arregloSPR[i].getCodMonedaCpt());                      // String
                        roc_s[j].setFechaPlantilla(arregloSPR[i].getFecIniVigenciaCpt());               // Date
                        roc_s[j].setHoraPlantilla(arregloSPR[i].getHraIniVigenciaCpt());                // String

                        if(arregloSPR[i].getIndSeleccionado() == 'S')
                            roc_s[j].setIndVigencia('S');                                               // char
                        else
                            if(arregloSPR[i].getIndSeleccionado() == 'P' && vectorSGS.contains("SIN_SEGUROS"))
                                roc_s[j].setIndVigencia('S');                                           // char
                            else
                                if(vectorSGS.contains(arregloSPR[i].getCodSubConcepto1Cpt() + arregloSPR[i].getCodSubConcepto3Cpt()))
                                    roc_s[j].setIndVigencia('S');                                       // char
                                else
                                    roc_s[j].setIndVigencia('N');                                       // char

                        roc_s[j].setRutCliente(Integer.parseInt(arregloSPR[i].getIicContextoRcc()));    // int
                        roc_s[j].setDigitoVerificador(arregloSPR[i].getCaiContextoRcc().charAt(0));     // char

                        roc_s[j].setTasaMontoFinal(arregloSPR[i].getValMonto());                        // double
                        roc_s[j].setIndTipTasBas(arregloSPR[i].getIndTipTasBas());                      // char
                        roc_s[j].setIndPerBasTas(arregloSPR[i].getIndPerBasTas());                      // char
                        roc_s[j].setInsBajPerBas(arregloSPR[i].getInsBajPerBas());                      // char
                        roc_s[j].setIndSobPerBas(arregloSPR[i].getIndSobPerBas());                      // char
                        roc_s[j].setTasaMontoInformado(0D);
                        roc_s[j].setIndBasTasVar(arregloSPR[i].getIndBasTasVar());                      // String
                        roc_s[j].setIndTipFecPerRep(arregloSPR[i].getIndTipFecPerRep());                // char
                        roc_s[j].setNumPerRep(arregloSPR[i].getNumPerRep());                            // int
                        roc_s[j].setIndPerRep(arregloSPR[i].getIndPerRep());                            // char
                        roc_s[j].setCostoFondo(arregloSPR[i].getCostoFondoInformado());                 // double
                        roc_s[j].setFactorDeRiesgo(arregloSPR[i].getFactorRiesgoInformado());           // double

                        if (logger.isDebugEnabled()){  
	                        logger.debug("roc_s["+ j +"].setCodSistema()             [" + roc_s[j].getCodSistema()               + "]");
	                        logger.debug("roc_s["+ j +"].setCodEvento()              [" + roc_s[j].getCodEvento()                + "]");
	                        logger.debug("roc_s["+ j +"].setCodigoConcepto()         [" + roc_s[j].getCodigoConcepto()           + "]");
	                        logger.debug("roc_s["+ j +"].setCodigoSubConcepto()      [" + roc_s[j].getCodigoSubConcepto()        + "]");
	                        logger.debug("roc_s["+ j +"].setCodModalidad()           [" + roc_s[j].getCodModalidad()             + "]");
	                        logger.debug("roc_s["+ j +"].setIndCobro()               [" + roc_s[j].getIndCobro()                 + "]");
	                        logger.debug("roc_s["+ j +"].setCaiPlantilla()           [" + roc_s[j].getCaiPlantilla()             + "]");
	                        logger.debug("roc_s["+ j +"].setIicPlantilla()           [" + roc_s[j].getIicPlantilla()             + "]");
	                        logger.debug("roc_s["+ j +"].setIndTipoPlantilla()       [" + roc_s[j].getIndTipoPlantilla()         + "]");
	                        logger.debug("roc_s["+ j +"].setCodigoMoneda()           [" + roc_s[j].getCodigoMoneda()             + "]");
	                        logger.debug("roc_s["+ j +"].setFechaPlantilla()         [" + roc_s[j].getFechaPlantilla()           + "]");
	                        logger.debug("roc_s["+ j +"].setHoraPlantilla()          [" + roc_s[j].getHoraPlantilla()            + "]");
	                        logger.debug("roc_s["+ j +"].setIndVigencia()            [" + roc_s[j].getIndVigencia()              + "]");
	                        logger.debug("roc_s["+ j +"].setRutCliente()             [" + roc_s[j].getRutCliente()               + "]");
	                        logger.debug("roc_s["+ j +"].setDigitoVerificador()      [" + roc_s[j].getDigitoVerificador()        + "]");
	                        logger.debug("roc_s["+ j +"].setTasaMontoFinal()         [" + roc_s[j].getTasaMontoFinal()           + "]");
	                        logger.debug("roc_s["+ j +"].setIndTipTasBas()           [" + roc_s[j].getIndTipTasBas()             + "]");
	                        logger.debug("roc_s["+ j +"].setIndPerBasTas()           [" + roc_s[j].getIndPerBasTas()             + "]");
	                        logger.debug("roc_s["+ j +"].setInsBajPerBas()           [" + roc_s[j].getInsBajPerBas()             + "]");
	                        logger.debug("roc_s["+ j +"].setIndSobPerBas()           [" + roc_s[j].getIndSobPerBas()             + "]");
	                        logger.debug("roc_s["+ j +"].setTasaMontoInformado()     [" + roc_s[j].getTasaMontoInformado()       + "]");
	                        logger.debug("roc_s["+ j +"].setIndBasTasVar()           [" + roc_s[j].getIndBasTasVar()             + "]");
	                        logger.debug("roc_s["+ j +"].setIndTipFecPerRep()        [" + roc_s[j].getIndTipFecPerRep()          + "]");
	                        logger.debug("roc_s["+ j +"].setNumPerRep()              [" + roc_s[j].getNumPerRep()                + "]");
	                        logger.debug("roc_s["+ j +"].setIndPerRep()              [" + roc_s[j].getIndPerRep()                + "]");
	                        logger.debug("roc_s["+ j +"].setCostoFondo()             [" + roc_s[j].getCostoFondo()               + "]");
	                        logger.debug("roc_s["+ j +"].setFactorDeRiesgo()         [" + roc_s[j].getFactorDeRiesgo()           + "]");
                        }
                        /** Seteo objeto roc_s, que será enviado a el ingreso de una OPC */
                        roc_sAmpliada[i] = new InputIngresoRocAmpliada();
                        roc_sAmpliada[i].setCaiOperacion(roc_s[j].getCaiOperacion());
                        roc_sAmpliada[i].setIicOperacion(roc_s[j].getIicOperacion());
                        roc_sAmpliada[i].setExtOperacion(roc_s[j].getExtOperacion());
                        roc_sAmpliada[i].setCodSistema(roc_s[j].getCodSistema());
                        roc_sAmpliada[i].setCodEvento(roc_s[j].getCodEvento());
                        roc_sAmpliada[i].setNumero(roc_s[j].getNumero());
                        roc_sAmpliada[i].setCodigoConcepto(roc_s[j].getCodigoConcepto());
                        roc_sAmpliada[i].setCodigoSubConcepto(roc_s[j].getCodigoSubConcepto());
                        roc_sAmpliada[i].setCodModalidad(roc_s[j].getCodModalidad());
                        roc_sAmpliada[i].setIndCobro(roc_s[j].getIndCobro());
                        roc_sAmpliada[i].setCaiPlantilla(roc_s[j].getCaiPlantilla());
                        roc_sAmpliada[i].setIicPlantilla(roc_s[j].getIicPlantilla());
                        roc_sAmpliada[i].setIndTipoPlantilla(roc_s[j].getIndTipoPlantilla());
                        roc_sAmpliada[i].setCodigoMoneda(roc_s[j].getCodigoMoneda());
                        roc_sAmpliada[i].setFechaPlantilla(roc_s[j].getFechaPlantilla());
                        roc_sAmpliada[i].setHoraPlantilla(roc_s[j].getHoraPlantilla());
                        roc_sAmpliada[i].setIndVigencia(roc_s[j].getIndVigencia());
                        roc_sAmpliada[i].setCodAnulacion(roc_s[j].getCodAnulacion());
                        roc_sAmpliada[i].setFechaUno(roc_s[j].getFechaUno());
                        roc_sAmpliada[i].setRutCliente(roc_s[j].getRutCliente());
                        roc_sAmpliada[i].setDigitoVerificador(roc_s[j].getDigitoVerificador());
                        roc_sAmpliada[i].setFechaDos(roc_s[j].getFechaDos());
                        roc_sAmpliada[i].setTasaMontoFinal(roc_s[j].getTasaMontoFinal());
                        roc_sAmpliada[i].setIndTipTasBas(roc_s[j].getIndTipTasBas());
                        roc_sAmpliada[i].setIndPerBasTas(roc_s[j].getIndPerBasTas());
                        roc_sAmpliada[i].setInsBajPerBas(roc_s[j].getInsBajPerBas());
                        roc_sAmpliada[i].setIndSobPerBas(roc_s[j].getIndSobPerBas());
                        roc_sAmpliada[i].setTasaMontoInformado(roc_s[j].getTasaMontoInformado());
                        roc_sAmpliada[i].setIndBasTasVar(roc_s[j].getIndBasTasVar());
                        roc_sAmpliada[i].setIndTipFecPerRep(roc_s[j].getIndTipFecPerRep());
                        roc_sAmpliada[i].setNumPerRep(roc_s[j].getNumPerRep());
                        roc_sAmpliada[i].setIndPerRep(roc_s[j].getIndPerRep());
                        roc_sAmpliada[i].setCostoFondo(roc_s[j].getCostoFondo());
                        roc_sAmpliada[i].setFactorDeRiesgo(roc_s[j].getFactorDeRiesgo());
                        roc_sAmpliada[i].setDescuentoAcumulado(0D); // double
                        roc_sAmpliada[i].setDescuentoAplicado(roc_s[j].getTasaMontoInformado());   //Double
                        roc_sAmpliada[i].setGlosaTipoSeguro((TablaValores.getValor("multilinea.parametros", "seguros" + roc_s[j].getCodigoSubConcepto(), roc_s[j].getCodigoSubConcepto())));
                        roc_sAmpliada[i].setGlosaTipoCobro((TablaValores.getValor("multilinea.parametros", "tipoCobro" + arregloSPR[i].getCodSubConcepto3Cpt(), arregloSPR[i].getCodSubConcepto3Cpt())));
                        roc_sAmpliada[i].setIndSegObligatorio(arregloSPR[i].getIndSeleccionado()); // char
                        roc_sAmpliada[i].setTasaMinima(0D); // double
                        roc_sAmpliada[i].setDescuentoMaximo(0D); // double
                        roc_sAmpliada[i].setValMonto(arregloSPR[i].getValMonto()); // double
                        roc_sAmpliada[i].setTmcInformado(arregloSPR[i].getTmcInformado()); // double
                    }
                }


            if (logger.isDebugEnabled())  logger.debug("========== LIQ PRO============");

            String    caiOperacion_LIQ = null;
            int       iicOperacion_LIQ;
            InputLiquidacionDeOperacionDeCreditoOpc liq_opc = new InputLiquidacionDeOperacionDeCreditoOpc();
            liq_opc.setCim_reqnum("032");
            liq_opc.setCim_uniqueid(origen);
            liq_opc.setCim_indseq(indseq);

            if (logger.isDebugEnabled())  logger.debug("========== liquidacionCredito ============");
            ResultLiquidacionDeOperacionDeCreditoOpc resLiqOpc = new ResultLiquidacionDeOperacionDeCreditoOpc();
            if (logger.isDebugEnabled())  logger.debug("========== antes multi ============");
            if (idReq.equals("096")){
                multiEnvironment.setIndreq(0,'1');//pos(0)=RollBack? --> '0'=No '1'=Si
                multiEnvironment.setIndreq(1,'0');//pos(1)=Con ACA?  --> '0'=No '1'=Si
                if (logger.isDebugEnabled())  logger.debug("es 096");
            }
            if (idReq.equals("097")){
                multiEnvironment.setIndreq(0,'0');//pos(0)=RollBack? --> '0'=No '1'=Si
                multiEnvironment.setIndreq(1,'1');//pos(1)=Con ACA?  --> '0'=No '1'=Si
                if (logger.isDebugEnabled())  logger.debug("es 097");
            }
            multiEnvironment.setIndreq(2,'1');//pos(2)=Solo BD Mañana?  --> '0'=No '1'=Si
//            multiEnvironment.setIndreq(5,'2');  // prorrogarMultilinea  0=normal      1=valide ejecutivo inexistente

            if (logger.isDebugEnabled())  logger.debug("========== antes operaCredito ============");
            resLiqOpc =  operaCredito(multiEnvironment,
                                       can_s,
                                       opc_s,
                                       rdc_s,
                                       dlc_s,
                                       cya_s,
                                       evc_s,
                                       icg_s,
                                       ven_s,
                                       roc_s,
                                       liq_opc);

            //******************************************//
            //********  FIN prorrogaCredito **********//
            //******************************************//
            if (logger.isDebugEnabled())  logger.debug("========== despues operaCredito ============");
            obean.setResultLiqOpc(resLiqOpc);
            if (logger.isDebugEnabled())  logger.debug("llenando 'tasa web' [" + obeanSpr.getInstanciaDeConsultaSpr(0).getValPropuesto() + "]");
            //si la tasa es anualizada (A o X) se 'muestra' la tasa dividida por 12 (CFRANCO)
            if (logger.isDebugEnabled())  logger.debug("valor de 'IndPerBasTas' [" + obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas() + "]");
            if (obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas()=='A' || obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas()=='X') {
                obean.setTasa(opc_s[0].getTasaSprea()/12);
                if (logger.isDebugEnabled())  logger.debug("llenando 'tasa' [" + opc_s[0].getTasaSprea()/12 + "]");
            } else {
                obean.setTasa(opc_s[0].getTasaSprea());
            }
            if (logger.isDebugEnabled())  logger.debug("llenando 'tasa orig' ...(" + obeanSpr.getInstanciaDeConsultaSpr(0).getValMonto() + ")");
            if (obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas()=='A' || obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas()=='X') {
                if (logger.isDebugEnabled())  logger.debug("llenando 'tasa orig' ...(" + obeanSpr.getInstanciaDeConsultaSpr(0).getValMonto()/12 + ")");
                obean.setTasaOriginal(obeanSpr.getInstanciaDeConsultaSpr(0).getValMonto()/12);
            } else {
                obean.setTasaOriginal(obeanSpr.getInstanciaDeConsultaSpr(0).getValMonto());
            }
            if (logger.isDebugEnabled())  logger.debug("llenando 'fechaCurse' [" + fechaCurse + "]");
            obean.setFechaCurse(fechaCurse);
            if ((datosLog != null) && idReq.equals("097")) {
                datosLog.put("tipoOpe", "POK");
                if (logger.isDebugEnabled()){ 
	                logger.debug("res_ctx.getEjecutivo()=[" + res_ctx.getEjecutivo() + "]");
	                logger.debug("datosLog.get(CAI)=[" + datosLog.get("CAI") + "]");
	                logger.debug("datosLog.get(IIC)=[" + datosLog.get("IIC") + "]");
                }
                datosLog.put("CAI", resLiqOpc.getCaiOperacion());
                datosLog.put("IIC", String.valueOf(resLiqOpc.getIicOperacion()));
                datosLog.put("MSG", resLiqOpc.getCim_respuesta());
                if (logger.isDebugEnabled()){  
	                logger.debug("datosLog.get(CAI)=[" + datosLog.get("CAI") + "]");
	                logger.debug("datosLog.get(IIC)=[" + datosLog.get("IIC") + "]");
                }
                registraLogMultilinea(datosLog);
                if (logger.isDebugEnabled())  logger.debug("FIN 097***prorrogarMultilinea...");
            } else {
                if (logger.isDebugEnabled())  logger.debug("FIN 096***prorrogarMultilinea...");
            }

            return obean;

        }catch(Exception e) {
        	if (logger.isEnabledFor(Level.ERROR)) logger.error("ERROR*** prorrogarMultilinea...Exception [" + e.getMessage() + "]");
            if (datosLog!=null) {
                datosLog.put("ERR",datosLog.get("ERR")==null? (!controlError.equals("") ? controlError + " " : "") + Utils.hackle(e.getMessage()): (!controlError.equals("") ? controlError + " " : "") + Utils.hackle(e.getMessage()) + datosLog.get("ERR"));
                registraLogMultilinea(datosLog);
            }
            else {
            	if (logger.isEnabledFor(Level.ERROR)) logger.error("datosLog es null");
            }
            throw new MultilineaException("ESPECIAL", (!controlError.equals("") ? controlError + " " : "") + e.getMessage());
        }

    }

    /**
     * Consulta de Operaciones de Cliente Super Ampliada
     *
     * Registro de versiones:<ul>
     * <li>1.0 02/05/2004 Hector Carranza   (Bee)- version inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param nombreTitular <b>Nombre / r.social</b>
     * @param rutDeudor <b>Idc deudor</b>
     * @param digitoVerificador <b>Idc deudor dv</b>
     * @param indicadorExtIdc <b>Idc cliente ind</b>
     * @param glosaExtIdc <b>Idc cliente gls</b>
     * @param tipoOperacion <b>Tipo de operacion</b>
     * @param codigoAuxiliar <b>Codigo auxiliar</b>
     * @param moneda <b>Moneda</b>
     * @param tipoDeudor <b>Tipo deudor</b>
     * @param codEstadoCredito <b>Estado</b>
     * @param fechaInicio <b>Fecha inicio</b>
     * @param fechaTermino <b>Fecha termino</b>
     * @param tipoOperacionCredito <b>Tipo operacion</b>
     * @param indRenov <b>Tipo renovacion</b>
     * @param codCanal <b>Codigo Canal</b>
     * @param numOperacion <b>Nro Operacion cai</b>
     * @param numOperacionIIC <b>Nro Operacion iic</b>
     * @return {@link ResultConsultaOperClienteSuperAmp}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.18
     */
    public ResultConsultaOperClienteSuperAmp consultaOperClienteSuperAmp(MultiEnvironment multiEnvironment, String nombreTitular, int rutDeudor, char digitoVerificador, char indicadorExtIdc, String glosaExtIdc, String tipoOperacion, String codigoAuxiliar, String moneda, char tipoDeudor, char codEstadoCredito, Date fechaInicio, Date fechaTermino, char tipoOperacionCredito, String indRenov, String codCanal, String numOperacion, int numOperacionIIC) throws MultilineaException, EJBException {

        InputConsultaOperClienteSuperAmp ibean = new InputConsultaOperClienteSuperAmp("047",
                                                                                      nombreTitular,
                                                                                      rutDeudor,
                                                                                      digitoVerificador,
                                                                                      indicadorExtIdc,
                                                                                      glosaExtIdc,
                                                                                      tipoOperacion,
                                                                                      codigoAuxiliar,
                                                                                      moneda,
                                                                                      tipoDeudor,
                                                                                      codEstadoCredito,
                                                                                      fechaInicio,
                                                                                      fechaTermino,
                                                                                      tipoOperacionCredito,
                                                                                      indRenov,
                                                                                      codCanal,
                                                                                      numOperacion,
                                                                                      numOperacionIIC);

        return (ResultConsultaOperClienteSuperAmp) consultaOperClienteSuperAmp(multiEnvironment, ibean, new ResultConsultaOperClienteSuperAmp());

    }

    /**
     * Consulta de Operaciones de Cliente Super Ampliada
     *
     * Registro de versiones:<ul>
     * <li>1.0 02/05/2004 Hector Carranza   (Bee)- version inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param nombreTitular <b>Nombre / r.social</b>
     * @param rutDeudor <b>Idc deudor</b>
     * @param digitoVerificador <b>Idc deudor dv</b>
     * @param indicadorExtIdc <b>Idc cliente ind</b>
     * @param glosaExtIdc <b>Idc cliente gls</b>
     * @param tipoOperacion <b>Tipo de operacion</b>
     * @param codigoAuxiliar <b>Codigo auxiliar</b>
     * @param moneda <b>Moneda</b>
     * @param tipoDeudor <b>Tipo deudor</b>
     * @param codEstadoCredito <b>Estado</b>
     * @param fechaInicio <b>Fecha inicio</b>
     * @param fechaTermino <b>Fecha termino</b>
     * @param tipoOperacionCredito <b>Tipo operacion</b>
     * @param indRenov <b>Tipo renovacion</b>
     * @param codCanal <b>Codigo Canal</b>
     * @return {@link ResultConsultaOperClienteSuperAmp}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.18
     */
    public ResultConsultaOperClienteSuperAmp consultaOperClienteSuperAmp(MultiEnvironment multiEnvironment, String nombreTitular, int rutDeudor, char digitoVerificador, char indicadorExtIdc, String glosaExtIdc, String tipoOperacion, String codigoAuxiliar, String moneda, char tipoDeudor, char codEstadoCredito, Date fechaInicio, Date fechaTermino, char tipoOperacionCredito, String indRenov, String codCanal) throws MultilineaException, EJBException {

        Vector vect = new Vector();

        if (logger.isDebugEnabled())  logger.debug("consultaOperClienteSuperAmp()");

        InputConsultaOperClienteSuperAmp ibean = new InputConsultaOperClienteSuperAmp("047",
                                                                                      nombreTitular,
                                                                                      rutDeudor,
                                                                                      digitoVerificador,
                                                                                      indicadorExtIdc,
                                                                                      glosaExtIdc,
                                                                                      tipoOperacion,
                                                                                      codigoAuxiliar,
                                                                                      moneda,
                                                                                      tipoDeudor,
                                                                                      codEstadoCredito,
                                                                                      fechaInicio,
                                                                                      fechaTermino,
                                                                                      tipoOperacionCredito,
                                                                                      indRenov,
                                                                                      codCanal,
                                                                                      "",
                                                                                      0);

        ResultConsultaOperClienteSuperAmp obean = new ResultConsultaOperClienteSuperAmp();

        if (logger.isDebugEnabled())  logger.debug("antes de consultaOperClienteSuperAmp(ibean, obean)");

        consultaOperClienteSuperAmp(multiEnvironment, ibean, obean);

        OperacionCreditoSuperAmp[] operacionesSuperAmp = obean.getOperacionesSuperAmp();

        if (logger.isDebugEnabled())  logger.debug("operacionesSuperAmp.length [" + operacionesSuperAmp.length + "]");

        for (int i = 0; i < operacionesSuperAmp.length; i++) {

            if (logger.isDebugEnabled())  logger.debug("vect.add(operacionesSuperAmp[" + Integer.toString(i) + "])");

            vect.add(operacionesSuperAmp[i]);
        }

        ResultConsultaOperClienteSuperAmp aux  = new ResultConsultaOperClienteSuperAmp();

        aux.setIndicador(obean.getIndicador());

        while (aux.getIndicador() == 'S') {

            ibean.setNumOperacion(operacionesSuperAmp[operacionesSuperAmp.length - 1].getIdOperacion());
            ibean.setNumOperacionIIC(operacionesSuperAmp[operacionesSuperAmp.length - 1].getNumOperacion());

            if (logger.isDebugEnabled())  logger.debug("antes de consultaOperClienteSuperAmp(ibean, aux)");

            consultaOperClienteSuperAmp(multiEnvironment, ibean, aux);

            operacionesSuperAmp = aux.getOperacionesSuperAmp();

            if (logger.isDebugEnabled())  logger.debug("operacionesSuperAmp.length [" + operacionesSuperAmp.length + "]");

            for (int i = 0; i < operacionesSuperAmp.length; i++) {

                if (logger.isDebugEnabled())  logger.debug("vect.add(operacionesSuperAmp[" + Integer.toString(i) + "])");

                vect.add(operacionesSuperAmp[i]);
            }

            if (logger.isDebugEnabled())  logger.debug("aux.getIndicador() [" + aux.getIndicador() + "]");
        }

        if (logger.isDebugEnabled())  logger.debug("obean.setOperacionesSuperAmp(): vect.size() : " + Integer.toString(vect.size()) + "");

        obean.setOperacionesSuperAmp((OperacionCreditoSuperAmp[]) vect.toArray(new OperacionCreditoSuperAmp[0]));

        return obean;
    }

    /**
     * Consulta de Operaciones de Cliente Super Ampliada
     *
     * Registro de versiones:<ul>
     * <li>1.0 02/05/2004 Hector Carranza   (Bee)- version inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.18
     */
    public Object consultaOperClienteSuperAmp(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("getting InitialContext()");


            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled())  logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled())  logger.debug("consultaOperClienteSuperAmp");

            return servicioscreditosglobales_bean.consultaOperClienteSuperAmp(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Consulta de Operaciones de Cliente Super Ampliada (Todas)
     *
     * Registro de versiones:<ul>
     * <li>1.0 02/05/2004 Hector Carranza   (Bee)- version inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.18
     */
    public Object consultaOperClienteSuperAmpAll(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        Vector vect = new Vector();

        if (logger.isDebugEnabled())  logger.debug("INI consultaOperClienteSuperAmpAll");
        if (logger.isDebugEnabled())  logger.debug("antes de consultaOperClienteSuperAmp(ibean, obean)");

        consultaOperClienteSuperAmp(multiEnvironment, ibean, obean);

        OperacionCreditoSuperAmp[] operacionesSuperAmp = ((ResultConsultaOperClienteSuperAmp) obean).getOperacionesSuperAmp();

        if (logger.isDebugEnabled())  logger.debug("operacionesSuperAmp.length [" + operacionesSuperAmp.length + "]");

        for (int i = 0; i < operacionesSuperAmp.length; i++) {

            vect.add(operacionesSuperAmp[i]);
        }

        ResultConsultaOperClienteSuperAmp aux  = new ResultConsultaOperClienteSuperAmp();

        aux.setIndicador(((ResultConsultaOperClienteSuperAmp) obean).getIndicador());

        while (aux.getIndicador() == 'S') {

            ((InputConsultaOperClienteSuperAmp) ibean).setNumOperacion(operacionesSuperAmp[operacionesSuperAmp.length - 1].getIdOperacion());
            ((InputConsultaOperClienteSuperAmp) ibean).setNumOperacionIIC(operacionesSuperAmp[operacionesSuperAmp.length - 1].getNumOperacion());

            if (logger.isDebugEnabled())  logger.debug("antes de consultaOperClienteSuperAmp(ibean, aux)");

            consultaOperClienteSuperAmp(multiEnvironment, ibean, aux);

            operacionesSuperAmp = aux.getOperacionesSuperAmp();

            if (logger.isDebugEnabled())  logger.debug("operacionesSuperAmp.length [" + operacionesSuperAmp.length + "]");

            for (int i = 0; i < operacionesSuperAmp.length; i++) {

                vect.add(operacionesSuperAmp[i]);
            }

            if (logger.isDebugEnabled())  logger.debug("aux.getIndicador() [" + aux.getIndicador() + "]");
        }

        if (logger.isDebugEnabled())  logger.debug("FIN consultaOperClienteSuperAmpAll");

        ((ResultConsultaOperClienteSuperAmp) obean).setOperacionesSuperAmp((OperacionCreditoSuperAmp[]) vect.toArray(new OperacionCreditoSuperAmp[0]));

        return obean;
    }


    /**
     * Consulta Operaciones Prorrogadas
     *
     * Registro de versiones:<ul>
     * <li>1.0 02/05/2005 Hector Carranza   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param caiOperacion <b>Nro. Operacion</b>
     * @param iicOperacion <b>Nro. Operacion</b>
     * @param tipoRenov <b>Tipo</b>
     * @param fecExpiracion2 <b>Fecha INICIO</b>
     * @param fecha2 <b>Fecha TERMINO</b>
     * @return {@link ResultConsultaOperacionesProrrogadas}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.18
     */
    public ResultConsultaOperacionesProrrogadas consultaOperacionesProrrogadas(MultiEnvironment multiEnvironment, String caiOperacion, int iicOperacion, String tipoRenov, Date fecExpiracion2, Date fecha2) throws MultilineaException, EJBException {

        InputConsultaOperacionesProrrogadas ibean = new InputConsultaOperacionesProrrogadas("010",
                                                                                            caiOperacion,
                                                                                            iicOperacion,
                                                                                            tipoRenov,
                                                                                            fecExpiracion2,
                                                                                            fecha2);

        return (ResultConsultaOperacionesProrrogadas) consultaOperacionesProrrogadas(multiEnvironment, ibean, new ResultConsultaOperacionesProrrogadas());

    }


    /**
     * Consulta Operaciones Prorrogadas
     *
     * Registro de versiones:<ul>
     * <li>1.0 02/05/2005 Hector Carranza   (Bee)- versión inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param ibean
     * @param obean
     * @return {@link java.lang.Object}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.18
     */
    public Object consultaOperacionesProrrogadas(MultiEnvironment multiEnvironment, Object ibean, Object obean) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("getting InitialContext()");

            InitialContext ic = JNDIConfig.getInitialContext();

            if (logger.isDebugEnabled())  logger.debug("getting ServiciosCreditosGlobalesLocalHome");

            ServiciosCreditosGlobalesLocalHome servicioscreditosglobales_home = (ServiciosCreditosGlobalesLocalHome) ic.lookup(JNDI_NAME_SCG);

            if (logger.isDebugEnabled())  logger.debug("ServiciosCreditosGlobalesLocalHome creado");

            ServiciosCreditosGlobalesLocal servicioscreditosglobales_bean = servicioscreditosglobales_home.create();

            if (logger.isDebugEnabled())  logger.debug("consultaOperacionesProrrogadas");

            return servicioscreditosglobales_bean.consultaOperacionesProrrogadas(multiEnvironment, ibean, obean);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }
    }

    /**
     * Consulta de Operaciones que han sido negociadas y que estan en estado liquidada
     * sobre los productos AVC010,AVC721,AVC326,AVC426.
     *
     * Registro de versiones:<ul>
     * <li>1.0 01/06/2005 Hector Carranza (Bee)- versión inicial
     * <li>1.1 21/06/2005 Hector Carranza (Bee)- cambio en Logs
     * <li>1.2 12/07/2005 Hector Carranza (Bee)- se realizan las consultas de todas las operaciones de los
     *                                           cuatro productos principales de Avances Multilinea AVC010,AVC721,AVC326,AVC426
     *                                           (el filtro es debido a que existen, mas AVC???)
     *                                           Para Renovacion y Prorroga solo productos del tipo AVC010, AVC326
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param rutDeudor <b>Idc deudor</b>
     * @param digitoVerificador <b>Idc deudor dv</b>
     * @param indicadorExtIdc <b>Idc cliente ind</b>
     * @param glosaExtIdc <b>Idc cliente gls</b>
     * @param tipoOperacion <b>Tipo de operacion</b>
     * @param codigoAuxiliar <b>Codigo auxiliar</b>
     * @param moneda <b>Moneda</b>
     * @param tipoDeudor <b>Tipo deudor</b>
     * @param codEstadoCredito <b>Estado</b>
     * @param fechaInicio <b>Fecha inicio</b>
     * @param fechaTermino <b>Fecha termino</b>
     * @param tipoOperacionCredito <b>Tipo operacion</b>
     * @param indRenov <b>Tipo renovacion</b>
     * @param codCanal <b>Codigo Canal</b>
     * @return {@link ResultConsultaOperClienteSuperAmp}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 1.21
     */
    public ResultConsultaOperClienteSuperAmp consultaOperacionesLiquidadasPorBciCorp (MultiEnvironment multiEnvironment, int rutDeudor, char digitoVerificador, char indicadorExtIdc, String glosaExtIdc, String tipoOperacion, String codigoAuxiliar, String moneda, char tipoDeudor, char codEstadoCredito, Date fechaInicio, Date fechaTermino, char tipoOperacionCredito, String indRenov, String codCanal) throws MultilineaException, EJBException {

        try {
            if (logger.isDebugEnabled())  logger.debug("Antes consultaOperacionesLiquidadasPorBciCorp");

            Vector vResult = new Vector();
            int    contador     = 0;
            int    contador_aux = 0;

            InputConsultaOperClienteSuperAmp ibean = new InputConsultaOperClienteSuperAmp();

            ibean.setRutDeudor(rutDeudor);
            ibean.setDigitoVerificador(digitoVerificador);
            ibean.setIndicadorExtIdc(indicadorExtIdc);
            ibean.setGlosaExtIdc(glosaExtIdc);
            ibean.setTipoOperacion(tipoOperacion);
            ibean.setCodigoAuxiliar("010");
            ibean.setMoneda(moneda);
            ibean.setTipoDeudor(tipoDeudor);
            ibean.setCodEstadoCredito(codEstadoCredito);
            ibean.setFechaInicio(fechaInicio);
            ibean.setFechaTermino(fechaTermino);
            ibean.setTipoOperacionCredito(tipoOperacionCredito);
            ibean.setIndRenov(indRenov);
            ibean.setCodCanal(codCanal);

            if (logger.isDebugEnabled()){  
	            logger.debug("RutDeudor           =" + rutDeudor           );
	            logger.debug("DigitoVerificador   =" + digitoVerificador   );
	            logger.debug("IndicadorExtIdc     =" + indicadorExtIdc     );
	            logger.debug("GlosaExtIdc         =" + glosaExtIdc         );
	            logger.debug("TipoOperacion       =" + tipoOperacion       );
	            logger.debug("CodigoAuxiliar      =" + "010"               );
	            logger.debug("Moneda              =" + moneda              );
	            logger.debug("TipoDeudor          =" + tipoDeudor          );
	            logger.debug("CodEstadoCredito    =" + codEstadoCredito    );
	            logger.debug("FechaInicio         =" + fechaInicio         );
	            logger.debug("FechaTermino        =" + fechaTermino        );
	            logger.debug("TipoOperacionCredito=" + tipoOperacionCredito);
	            logger.debug("IndRenov            =" + indRenov            );
	            logger.debug("CodCanal            =" + codCanal            );
            }
            ResultConsultaOperClienteSuperAmp  obean = (ResultConsultaOperClienteSuperAmp ) consultaOperClienteSuperAmpAll(multiEnvironment, ibean, new ResultConsultaOperClienteSuperAmp ());

            OperacionCreditoSuperAmp[] operacionesSuperAmp = ((ResultConsultaOperClienteSuperAmp) obean).getOperacionesSuperAmp();

            if (logger.isDebugEnabled())  logger.debug("operacionesSuperAmp.length [" + operacionesSuperAmp.length + "]");

            for (int i = 0; i < operacionesSuperAmp.length; i++) {
                if (operacionesSuperAmp[i] == null) {
                    break;
                } else {
                    contador_aux++;
                vResult.add(operacionesSuperAmp[i]);
            }
            }
            if (logger.isDebugEnabled())  logger.debug("Existen [" + contador_aux + "] AVC010"       );

            contador += contador_aux;
            if (logger.isDebugEnabled()){  
	            logger.debug("Consulta operacion siguiente."                );
	            logger.debug("CodigoAuxiliar      = 721"               );
            }
            contador_aux = 0;

            ibean.setCodigoAuxiliar("721");
            ResultConsultaOperClienteSuperAmp  aux = null;
            aux = (ResultConsultaOperClienteSuperAmp ) consultaOperClienteSuperAmpAll(multiEnvironment, ibean, new ResultConsultaOperClienteSuperAmp ());

            operacionesSuperAmp = aux.getOperacionesSuperAmp();
            if (logger.isDebugEnabled())  logger.debug("operacionesSuperAmp.length [" + operacionesSuperAmp.length + "]");
            for (int i = 0; i < operacionesSuperAmp.length; i++) {
                if (operacionesSuperAmp[i] == null) {
                    break;
                } else {
                    if ((operacionesSuperAmp[i].getTipoRenovacion()).trim().equals("")) {   //solo AVANCES
                        vResult.add(operacionesSuperAmp[i]);
                        contador_aux++;
                    }
                }
            }
            if (logger.isDebugEnabled())  logger.debug("Existen [" + contador_aux + "] AVC721"       );

            contador += contador_aux;
            if (logger.isDebugEnabled()){  
	            logger.debug("Consulta operacion siguiente.."                );
	            logger.debug("CodigoAuxiliar      = 326"               );
            }
            contador_aux = 0;

            ibean.setCodigoAuxiliar("326");
            aux = null;
            aux = (ResultConsultaOperClienteSuperAmp ) consultaOperClienteSuperAmpAll(multiEnvironment, ibean, new ResultConsultaOperClienteSuperAmp ());

            operacionesSuperAmp = aux.getOperacionesSuperAmp();
            if (logger.isDebugEnabled())  logger.debug("operacionesSuperAmp.length [" + operacionesSuperAmp.length + "]");
            for (int i = 0; i < operacionesSuperAmp.length; i++) {
                if (operacionesSuperAmp[i] == null) {
                    break;
                } else {
                    contador_aux++;
                vResult.add(operacionesSuperAmp[i]);
            }
            }
            if (logger.isDebugEnabled())  logger.debug("Existen [" + contador_aux + "] AVC326"       );

            contador += contador_aux;
            if (logger.isDebugEnabled()){  
	            logger.debug("Consulta operacion siguiente..."                );
	            logger.debug("CodigoAuxiliar      = 426"               );
            }
            contador_aux = 0;

            ibean.setCodigoAuxiliar("426");
            aux = null;
            aux = (ResultConsultaOperClienteSuperAmp ) consultaOperClienteSuperAmpAll(multiEnvironment, ibean, new ResultConsultaOperClienteSuperAmp ());

            operacionesSuperAmp = aux.getOperacionesSuperAmp();
            if (logger.isDebugEnabled())  logger.debug("operacionesSuperAmp.length [" + operacionesSuperAmp.length + "]");
            for (int i = 0; i < operacionesSuperAmp.length; i++) {
                if (operacionesSuperAmp[i] == null) {
                    break;
                } else {
                    if ((operacionesSuperAmp[i].getTipoRenovacion()).trim().equals("")) {  //solo AVANCES
                        contador_aux++;
                        vResult.add(operacionesSuperAmp[i]);
                    }
                }
            }
            if (logger.isDebugEnabled())  logger.debug("Existen [" + contador_aux + "] AVC426"       );
            contador += contador_aux;

            ((ResultConsultaOperClienteSuperAmp) obean).setOperacionesSuperAmp((OperacionCreditoSuperAmp[]) vResult.toArray(new OperacionCreditoSuperAmp[0]));
            if (logger.isDebugEnabled())  logger.debug("despues de consultaOperacionesLiquidadasPorBciCorp");

            obean.setTotVencimiento(contador);

            return obean;

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }

    }

    /**
     * Metodo con el cual realizaremos el deposito del avance multilinea solicitado
     * en la cuenta dada, considerando el horario en el cual se esta realizando la operacion
     * para ello verifica si existe en archivo 'archivoParametros' los parametros segun horario
     *
     *
     * Registro de versiones:<ul>
     * <li>1.0 31/05/2007   Hector Carranza     (Bee) - versión inicial
     *
     * </ul>
     * <p>
     *
     * @param rut                     Rut cliente
     * @param cuentaAbono             Cuenta de Abono
     * @param montoaAbonar            Monto a Abonar
     * @param caiOperacion            numero de operacion cai
     * @param iicOperacion            numero de operacion iic
     * @param archivoParametros       archivo de parametros a leer
     * @return Mensaje de la operacion
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 2.0
     */
    public String depositoEnCuenta(String rut, String cuentaAbono, double montoaAbonar, String caiOperacion, int iicOperacion, String archivoParametros) throws MultilineaException, EJBException {

        String mensaje_salida = "NO";

        try {

            if (logger.isDebugEnabled())  logger.debug("Vamos a realizar deposito.");

            CuentaCorriente cc = new CuentaCorriente(cuentaAbono); // Cargamos a la cuenta corriente del cliente el avance solicitado.

            String nemonicoAbono   = "";
            String trxAbono        = "";
            String nemonicoAbono_e = "";
            String trxAbono_e      = "";

            try {
                // buscamos los parametros default ....
                try{
                    if (logger.isDebugEnabled())  logger.debug("Buscando nemonicoAbono y trxAbono en "+ archivoParametros +" Default : " + "transaccionNormal");
                    nemonicoAbono = TablaValores.getValor(archivoParametros, "transaccionNormal", "TRXNEM");
                    trxAbono      = TablaValores.getValor(archivoParametros, "transaccionNormal", "TRXABO");
                } catch (Exception e) {
                	if (logger.isEnabledFor(Level.ERROR)) logger.error("No encontramos parametros default.");
                }
                if (logger.isDebugEnabled()){  
	                logger.debug("nemonico      = [" + nemonicoAbono  + "]");
	                logger.debug("trx           = [" + trxAbono       + "]");
                }
                // buscamos los parametros excepcionales ....
                try{
                    if (logger.isDebugEnabled())  logger.debug("Buscando nemonicoAbono y trxAbono en " + archivoParametros + " Excepcional : " + "transaccionExcepc");
                    nemonicoAbono_e = TablaValores.getValor(archivoParametros, "transaccionExcepc", "TRXNEM");
                    trxAbono_e      = TablaValores.getValor(archivoParametros, "transaccionExcepc", "TRXABO");
                    int horaini     = Integer.parseInt(TablaValores.getValor(archivoParametros, "transaccionExcepc", "HORATOPEINI"));
                    int horafin     = Integer.parseInt(TablaValores.getValor(archivoParametros, "transaccionExcepc", "HORATOPEFIN"));

                    int horanow     = Integer.parseInt(FechasUtil.fechaActualFormateada("HHmm"));

                    if (logger.isDebugEnabled()){ 
	                    logger.debug("nemonico      = [" + nemonicoAbono_e  + "]");
	                    logger.debug("trx           = [" + trxAbono_e       + "]");
	                    logger.debug("horanow       = [" + horanow          + "]");
	                    logger.debug("horaini       = [" + horaini          + "]");
	                    logger.debug("horafin       = [" + horafin          + "]");
                    }
                    if ((horanow >= horaini)&&(horanow <= horafin)){
                        //si estamos en el rango excepcional entonces lo establecemos
                        nemonicoAbono   = nemonicoAbono_e;
                        trxAbono        = trxAbono_e;
                    }

                } catch (Exception e) {
                	if (logger.isEnabledFor(Level.ERROR)) logger.error("No encontramos parametros excepcionales.");
                }

                if ( !trxAbono.trim().equals("") && !nemonicoAbono.trim().equals("") ){

                    if (logger.isDebugEnabled()){  
	                    logger.debug("Abonando en Cta Cte ...");
	                    logger.debug("cuentaAbono   = [" + cuentaAbono   + "]");
	                    logger.debug("trxAbono      = [" + trxAbono      + "]");
	                    logger.debug("montoaAbonar  = [" + montoaAbonar  + "]");
	                    logger.debug("nemonicoAbono = [" + nemonicoAbono + "]");
                    }
                    cc.setMovCtaCteGenerico(trxAbono, montoaAbonar, nemonicoAbono); //abonamos ya!
                    mensaje_salida = "Deposito Realizado con Exito";
                    if (logger.isDebugEnabled()){  
	                    logger.debug("Num Operacion = [" + caiOperacion + iicOperacion + "]");
	                    logger.debug("OK..ABONADO");
                    }

                } else{
                    mensaje_salida = "NO trxAbono y/o nemonicoAbono en blanco";
                    if (logger.isDebugEnabled()){  
	                    logger.debug("trxAbono      = [" + trxAbono      + "]");
	                    logger.debug("nemonicoAbono = [" + nemonicoAbono + "]");
	                    logger.debug("RUT Cliente   = [" + rut           + "]");
	                    logger.debug("cuentaAbono   = [" + cuentaAbono   + "]");
	                    logger.debug("Num Operacion = [" + caiOperacion + iicOperacion + "]");
	                }
                }

            } catch (Exception e) {

                mensaje_salida   = "NO Error (" + e.toString() + ")";
                if (logger.isEnabledFor(Level.ERROR)){
	                logger.error("---- NO PUDO ABONAR EN LA CUENTA CORRIENTE !!!");
	                logger.error("RUT Cliente   = [" + rut + "]");
	                logger.error("cuentaAbono   = [" + cuentaAbono + "]");
	                logger.error("Num Operacion = [" + caiOperacion + iicOperacion + "]");
	                logger.error("Exception     = [" + e.toString() + "]");
                }
            }

            if (logger.isDebugEnabled())  logger.debug("Despues de abono en Cta.Cte.");

            return mensaje_salida;


        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }

    }

    /**
     * Metodo con el cual consultaremos las Plantillas de Operacion de Credito (POC)
     * dado el archivo .parametro
     *
     *
     * Registro de versiones:<ul>
     * <li>1.0 31/05/2007   Hector Carranza     (Bee) - versión inicial
     *
     * </ul>
     * <p>
     *
     * @param htTablaCreditos  tabla de parametros
     * @return Vector          vector con poc's
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 2.0
     */
    public Vector obtenerPlantillasProductos(Hashtable htTablaCreditos) throws MultilineaException, EJBException {

        Vector vectorPOC                = new Vector();
        PlantillaOperacionesCredito poc = null;

        try {

            if (logger.isDebugEnabled())  logger.debug("INI obtenerPlantillasProductos");

            InitialContext            ic                   = JNDIConfig.getInitialContext();
            ServiciosColocacionesHome servColocacionesHome = (ServiciosColocacionesHome) ic.lookup("wcorp.serv.colocaciones.ServiciosColocaciones");
            ServiciosColocaciones     srvColocacionesBean  = servColocacionesHome.create();

            if (logger.isDebugEnabled())  logger.debug("srvColocacionesBean creado");

            String htelem           = "";
            String moneda           = "";
            String tipoOperacion    = "";
            String codigoAuxiliar   = "";

            for(Enumeration codigos = htTablaCreditos.keys(); codigos.hasMoreElements();) {

                htelem          = (String) codigos.nextElement();
                moneda          = htelem.substring(0, 5).trim();
                tipoOperacion   = htelem.substring(6, 9).trim();
                codigoAuxiliar  = htelem.substring(9).trim();
                if (logger.isDebugEnabled())  logger.debug("Buscando POC : tipoOperacion=[" + tipoOperacion + "] codigoAuxiliar=[" + codigoAuxiliar + "] moneda=[" + moneda + "]");

                poc = srvColocacionesBean.getPlantillaOpCredito(tipoOperacion + codigoAuxiliar, moneda);

                vectorPOC.add(poc);
            }

            if (logger.isDebugEnabled())  logger.debug("FIN obtenerPlantillasProductos");

            return vectorPOC;

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("MultilineaException " + e.toString());

            throw new MultilineaException(e.getCodigo(), e.getInfoAdic());

        } catch (Exception e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("Exception " + e.toString());

            throw new MultilineaException("UNKNOW", e.toString());
        }

    }

    /**
     * Consulta Cartola de Multilinea
     * metodo creado para mantener compatibilidad con metodo del mismo nombre
     * para versiones anteriores.
     *
     * Registro de versiones:<ul>
     * <li>1.0 30/11/2005 Hector Carranza (Bee)- version inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param rutDeudor
     * @param digitoVerificador
     * @param datosLog
     * @return {@link ResultCartolaMultilinea}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 2.1
     */
    public ResultCartolaMultilinea consultaCartolaMultilinea(MultiEnvironment multiEnvironment, int rutDeudor, char digitoVerificador, Hashtable datosLog) throws MultilineaException, EJBException {

        try {
             if (logger.isDebugEnabled())  logger.debug("INI consultaCartolaMultilinea 1.0");

             ResultCartolaMultilinea obean = (ResultCartolaMultilinea) consultaCartolaMultilinea(multiEnvironment, rutDeudor, digitoVerificador, datosLog, true);

             if (logger.isDebugEnabled())  logger.debug("FIN consultaCartolaMultilinea 1.0");

             return (obean);

         } catch (Exception e) {

             throw new MultilineaException("ESPECIAL", e.getMessage());
         }

    }
     
    /**
     * Consulta Cartola de Multilinea
     * metodo creado para mantener compatibilidad con metodo del mismo nombre
     * para versiones anteriores.
     *
     * Registro de versiones:<ul>
     * <li>1.0 28/04/2015 Eduardo Pérez (Bee)- version inicial
     *
     * </ul>
     * <p>
     *
     * @param multiEnvironment
     * @param rutDeudor
     * @param digitoVerificador
     * @param datosLog
     * @param grabaLog
     * @return {@link ResultCartolaMultilinea}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 2.1
     */
    public ResultCartolaMultilinea consultaCartolaMultilinea(MultiEnvironment multiEnvironment, int rutDeudor, char digitoVerificador, Hashtable datosLog, boolean grabaLog) throws MultilineaException, EJBException {

        try {
             if (logger.isDebugEnabled())  logger.debug("INI consultaCartolaMultilinea 1.7");

             ResultCartolaMultilinea obean = (ResultCartolaMultilinea) consultaCartolaMultilinea(multiEnvironment, rutDeudor, digitoVerificador, datosLog, grabaLog, "AVC");

             if (logger.isDebugEnabled())  logger.debug("FIN consultaCartolaMultilinea 1.7");

             return (obean);

         } catch (Exception e) {

             throw new MultilineaException("ESPECIAL", e.getMessage());
         }

    }     

    /**
     * Ingreso Operacion Credito por Firmar
     *
     * <p>
     * Almacena la operacion en BD de operaciones por firmar (MLT)
     *
     * <p>
     *
     * Registro de versiones:<ul>
     * <li>1.0 16/05/2008   Hector Carranza   (BEE) - versión inicial
     *
     * </ul>
     * <p>
     *
     * @param numeroAutorizacion     <b>numero autorizacion</b>
     * @param rutUsuario             <b>Rut Usuario</b>
     * @param dvUsuario              <b>digito Ver Usuario</b>
     * @param numero                 <b>num convenio</b>
     * @param rutEmpresa             <b>RutEmpresa</b>
     * @param digitoVerifEmp         <b>digitoVerifEmp</b>
     * @param tipoOperacion          <b>tipoOperacion</b>
     * @param auxiliarOpe            <b>codigoAuxiliar</b>
     * @param glosaTipoCredito       <b>glosaTipoCredito</b>
     * @param tipoAbono              <b>cod abono</b>
     * @param tipoCargoAbono         <b>cod cargo</b>
     * @param oficinaIngreso         <b>oficinaIngreso</b>
     * @param ctaAbono               <b>cta abono</b>
     * @param ctaCargo               <b>cta cargo</b>
     * @param indicador              <b>indicadorNP01 mes no pago</b>
     * @param indicadorAplic         <b>indicadorNP02 mes no pago</b>
     * @param montoCredito           <b>montoCredito</b>
     * @param codigoMoneda           <b>cod moneda</b>
     * @param totalVencimientos      <b>num total vctos</b>
     * @param fechaPrimerVenc        <b>fecha primer vcto</b>
     * @param fechaInicio            <b>fecha ini curse</b>
     * @param fechaFin               <b>fecha fin curse</b>
     * @param estadoSolicit          <b>estado solicitud</b>
     * @param procesoNegocio         <b>procesoNegocio</b>
     * @param numOperacion           <b>cai ope</b>
     * @param auxiliarCredito        <b>iic ope</b>
     * @param numOperacionCan        <b>cai ren</b>
     * @param codAuxiliarCredito     <b>iic ren</b>
     * @param codigoMoneda2          <b>cod moneda origen ren</b>
     * @param monedaLinea            <b>glosa moneda origen ren</b>
     * @param fecExpiracion          <b>fecha vcto ren</b>
     * @param montoAbonado           <b>monto abono capital prorroga</b>
     * @return {@link ResultIngresoOperacionCreditoMultilinea}
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 2.3
     */
    public ResultIngresoOperacionCreditoMultilinea ingresoOperacionCreditoPorFirmar(String numeroAutorizacion, String rutUsuario, String dvUsuario, String idConvenio, String rutEmpresa, char digitoVerifEmp, String tipoOperacion, String auxiliarOpe, String glosaTipoCredito, String tipoAbono, String tipoCargoAbono, String oficinaIngreso, String ctaAbono, String ctaCargo, String indicador, String indicadorAplic, String montoCredito, String codigoMoneda, String totalVencimientos, String fechaPrimerVenc, String fechaInicio, String fechaFin, String estadoSolicit, String procesoNegocio, String numOperacion, String auxiliarCredito, String numOperacionCan, String codAuxiliarCredito, String codigoMoneda2, String monedaLinea, String fecExpiracion2, String montoAbonado) throws MultilineaException, EJBException {

        ResultIngresoOperacionCreditoMultilinea res_ing = null;

        try {

            if (logger.isDebugEnabled())  logger.debug("INI ingresoOperacionCreditoPorFirmar" );

            String msgError      = "";
            String autorizacion  = "";

            if (logger.isDebugEnabled()){ 
	            logger.debug("numeroAutorizacion =[" + numeroAutorizacion    + "]");
	            logger.debug("numOperacion       =[" + numOperacion          + "]");
	            logger.debug("auxiliarCredito    =[" + auxiliarCredito       + "]");
	            logger.debug("numOperacionCan    =[" + numOperacionCan       + "]");
	            logger.debug("codAuxiliarCredito =[" + codAuxiliarCredito    + "]");
            }
            String cai  = "";
            String iic  = "";

            if (logger.isDebugEnabled())  logger.debug("procesoNegocio     =[" + procesoNegocio + "]");
            if (procesoNegocio.trim().equals("REN") || procesoNegocio.trim().equals("PRO")) {
                cai = numOperacionCan;
                iic = codAuxiliarCredito;
            } else {
                cai = numOperacion;
                iic = auxiliarCredito;
            }

            if (numeroAutorizacion.trim().equals("")){

                    try {
                        if (logger.isDebugEnabled())  logger.debug("Verificando en Operaciones por Firmar [" + cai + iic + "]");
                        if (!existOperacionenFirmas(idConvenio, rutEmpresa, digitoVerifEmp, cai, iic)){

                            if (logger.isDebugEnabled())  logger.debug("Ini ingresoOperacionCreditoMultilinea [" + cai + iic + "]" );
                            res_ing = (ResultIngresoOperacionCreditoMultilinea) ingresoOperacionCreditoMultilinea( idConvenio,
                                                                                                                   rutEmpresa,
                                                                                                                   digitoVerifEmp,
                                                                                                                   tipoOperacion,
                                                                                                                   auxiliarOpe,
                                                                                                                   glosaTipoCredito,
                                                                                                                   tipoAbono,
                                                                                                                   tipoCargoAbono,
                                                                                                                   oficinaIngreso,
                                                                                                                   ctaAbono,
                                                                                                                   ctaCargo,
                                                                                                                   indicador,
                                                                                                                   indicadorAplic,
                                                                                                                   montoCredito,
                                                                                                                   codigoMoneda,
                                                                                                                   totalVencimientos,
                                                                                                                   fechaPrimerVenc,
                                                                                                                   fechaInicio,
                                                                                                                   fechaFin,
                                                                                                                   "PEN",             //pendiente
                                                                                                                   procesoNegocio,
                                                                                                                   numOperacion,
                                                                                                                   auxiliarCredito,
                                                                                                                   numOperacionCan,
                                                                                                                   codAuxiliarCredito,
                                                                                                                   codigoMoneda2,
                                                                                                                   monedaLinea,
                                                                                                                   fecExpiracion2,
                                                                                                                   montoAbonado );

                            if (logger.isDebugEnabled())  logger.debug("numope obtenido    =[" + res_ing.getIdentificador() + "]");
                            if (logger.isDebugEnabled())  logger.debug("Fin ingresoOperacionCreditoMultilinea [" + cai + iic + "]" );

                         }else{
                            if (logger.isDebugEnabled())  logger.debug("Ya estaba Ingresada para Firmar.[" + cai + iic + "]");
                         }

                    } catch (Exception e) {
                    	if (logger.isEnabledFor(Level.ERROR)) logger.error("*** Error en ingreso de operacion por firmar [" + cai + iic + "]");
                    	if (logger.isEnabledFor(Level.ERROR)) logger.error(e.getClass() + " : " + e.getMessage());
                    }
            }else{
                if (logger.isDebugEnabled())  logger.debug("Operacion ya fue ingresada anteriormente ..." );
            }

            if (logger.isDebugEnabled())  logger.debug("FIN ingresoOperacionCreditoPorFirmar. [" + cai + iic + "]" );

            return res_ing;

        } catch(Exception e) {
        	if (logger.isEnabledFor(Level.ERROR)) logger.error("ERROR*** ingresoOperacionCreditoPorFirmar...Exception [" + e.getMessage() + "]");
            return res_ing;
        }

    }

    /*******************************************************************************************************
     * obtieneSegurosDesdePrecios
     *******************************************************************************************************/
    /**
     * Obtiene Seguros para Simulacion Desde sistema de Precios
     *
     * Registro de versiones:<ul>
     * <li>1.0 24/04/2009 Hector Carranza  (Bee)  - versión inicial
     *
     * </ul>
     * <p>
     *
     * @param  multiEnvironment
     * @param  codigo de Evento
     * @param  codigo de Concepto
     * @param  fecha de Proceso
     * @param  codigo Tipo Consulta
     * @param  numero de operacion cai
     * @param  numero de operacion iic
     * @param  extOperacion
     * @param  rut Cliente
     * @param  digito vrf Cliente
     * @param  indicador Cliente
     * @param  glosa Cliente
     * @param  codigo Tipo Canal
     * @param  codigo Canal
     * @param  codigo Moneda
     * @param  Tipo Operacion
     * @param  Tipo Operacion Auxiliar
     * @param  numero Plazo Operacion
     * @param  indicador Plazo Operacion
     * @param  fecha Vencimiento Inicial
     * @param  valor Monto Operacion
     * @param  valaor Saldo Operacion
     * @param  vencimiento Operacion
     * @param  valor Interes Operacion
     * @param  valaor Monto Adicional
     * @param  codigo Rentabilidad
     * @param  codigo Factor Riesgo
     * @param  indicador Tipo Pago
     * @param  Tipo Operacion cai Rcc
     * @param  codigo Segmento
     *
     * @return    {@link ResultConsultaSpr}
     *
     * @exception wcorp.bprocess.multilinea.MultilineaException
     * @exception javax.ejb.EJBException
     * @since 2.5
     */
    public ResultConsultaSpr obtieneSegurosDesdePrecios(MultiEnvironment multiEnvironment, String codEvento, String codConcepto, Date fecProceso, String codTipoConsulta, String caiOperacion, int iicOperacion, String extOperacion, int numCliente, char vrfCliente, char indCliente, String glsCliente, String codTipoCanal, String codCanal, String codMoneda, String codTipoOperacion, String codAuxiliar, int numPlazoOperacion, char indPlazoOperacion, Date fecVctoInicial, double valMontoOperacion, double valSaldoOperacion, double vctoOperacion, double valInteOperacion, double valMontoAdicional, String codClfRentabilidad, String codFactorRiesgo, String indTipoPago, String caiOperacionRcc, String codigoSegmento) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("en obtieneSegurosDesdePrecios");

            PreciosContextos     precioscontextosBean = null;
            InitialContext       ic                   = JNDIConfig.getInitialContext();
            Object               obj_pre              = ic.lookup(JNDI_NAME_PRE);
            PreciosContextosHome home_pre             = (PreciosContextosHome) PortableRemoteObject.narrow(obj_pre, PreciosContextosHome.class);

            precioscontextosBean = (PreciosContextos) PortableRemoteObject.narrow(home_pre.create(), PreciosContextos.class);

            if (logger.isDebugEnabled())  logger.debug("precioscontextosBean creado");

            return precioscontextosBean.casoDeUsoConsultaSpr(multiEnvironment,
                                                             codEvento,
                                                             codConcepto,
                                                             fecProceso,
                                                             codTipoConsulta,
                                                             caiOperacion,
                                                             iicOperacion,
                                                             extOperacion,
                                                             numCliente,
                                                             vrfCliente,
                                                             indCliente,
                                                             glsCliente,
                                                             codTipoCanal,
                                                             codCanal,
                                                             codMoneda,
                                                             codTipoOperacion,
                                                             codAuxiliar,
                                                             numPlazoOperacion,
                                                             indPlazoOperacion,
                                                             fecVctoInicial,
                                                             valMontoOperacion,
                                                             valSaldoOperacion,
                                                             vctoOperacion,
                                                             valInteOperacion,
                                                             valMontoAdicional,
                                                             codClfRentabilidad,
                                                             codFactorRiesgo,
                                                             indTipoPago,
                                                             caiOperacionRcc,
                                                             codigoSegmento);


        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("[obtieneSegurosDesdePrecios] GeneralException " + e.getMessage());

            throw new MultilineaException("ESPECIAL", e.getMessage());

         } catch (Exception e) {

        	 if (logger.isEnabledFor(Level.ERROR)) logger.error("[obtieneSegurosDesdePrecios] Exception " + e.getMessage());

            throw new MultilineaException("ESPECIAL", e.getMessage());
         }

    }

    /*******************************************************************************************************
     * obtieneDetalleSegurosPrecios
     *******************************************************************************************************/
    /**
     * Obtiene Detalles de los Seguros consolidados asociados a una operación en Particular
     *
     * Registro de versiones:<ul>
     * <li>1.0 24/04/2009   Hector Carranza (Bee) - versión inicial
     *
     * </ul>
     * <p>
     *
     * @param MultiEnvironment multiEnvironment
     * @param String           caiOperacion
     * @param String           iicOperacion
     * @param String           extOperacion
     * @param String           macroProducto
     * @param String           codigoEvento
     * @param int              rutCliente
     * @param char             digitoVerificador
     * @param char             idcCliente
     * @param String           glsCliente
     * @param String           glsNomCliente
     * @param String           codigoConcepto
     * @param char             tipoConsulta
     * @param char             siguiente
     * @param String           caiOperacionNext
     * @param String           iicOperacionNext
     * @param String           extOperacionNext
     * @param String           codigoSistemaNext
     * @param String           codigoEventoNext
     * @param int              numero
     *
     * @return    {@link ResultConsultaCgr}
     *
     * @exception wcorp.bprocess.simulacion.SimulaCursaCreditoException
     * @exception javax.ejb.EJBException
     * @since     2.5
     */
    public ResultConsultaCgr obtieneDetalleSegurosPrecios(MultiEnvironment multiEnvironment, String caiOperacion, String iicOperacion, String extOperacion, String macroProducto, String codigoEvento, int rutCliente, char digitoVerificador, char idcCliente, String glsCliente, String glsNomCliente, String codigoConcepto, char tipoConsulta, char siguiente, String caiOperacionNext, String iicOperacionNext, String extOperacionNext, String codigoSistemaNext, String codigoEventoNext, int numero) throws MultilineaException, EJBException {

        try {

            if (logger.isDebugEnabled())  logger.debug("en obtieneDetalleSegurosPrecios");

            PreciosContextos     precioscontextosBean = null;
            InitialContext       ic                   = JNDIConfig.getInitialContext();
            Object               obj_pre              = ic.lookup(JNDI_NAME_PRE);
            PreciosContextosHome home_pre             = (PreciosContextosHome) PortableRemoteObject.narrow(obj_pre, PreciosContextosHome.class);

            precioscontextosBean = (PreciosContextos) PortableRemoteObject.narrow(home_pre.create(), PreciosContextos.class);

            if (logger.isDebugEnabled())  logger.debug("precioscontextosBean creado");

            return precioscontextosBean.consultaCgr(multiEnvironment,
                                                    caiOperacion,
                                                    iicOperacion,
                                                    extOperacion,
                                                    macroProducto,
                                                    codigoEvento,
                                                    rutCliente,
                                                    digitoVerificador,
                                                    idcCliente,
                                                    glsCliente,
                                                    glsNomCliente,
                                                    codigoConcepto,
                                                    tipoConsulta,
                                                    siguiente,
                                                    caiOperacionNext,
                                                    iicOperacionNext,
                                                    extOperacionNext,
                                                    codigoSistemaNext,
                                                    codigoEventoNext,
                                                    numero);

        } catch (GeneralException e) {

        	if (logger.isEnabledFor(Level.ERROR)) logger.error("[obtieneDetalleSegurosPrecios] GeneralException " + e.getMessage());

            throw new MultilineaException("ESPECIAL", e.getMessage());

         } catch (Exception e) {

        	 if (logger.isEnabledFor(Level.ERROR)) logger.error("[obtieneDetalleSegurosPrecios] Exception " + e.getMessage());

            throw new MultilineaException("ESPECIAL", e.getMessage());
         }

    }

    /*******************************************************************************************************
     * verificaPertenenciaBanca
     *******************************************************************************************************/
    /**
     * Verifica si la banca del cliente este en la lista de bancas permitidas
     * <p>
     * Registro de Versiones
     * <ul>
     * <li>1.0 (24/04/2009      Hector Carranza - BEE ):   Version Inicial
     *
     * </ul>
     * </p>
     *
     * @param codigo banca cliente
     * @param lista de Bancas permitidas
     * @return boolean
     * @since 2.5
     *
     */
    private boolean verificaPertenenciaBanca(String codbanca, String listaBancas) {

        StringTokenizer st          = null;
        String elem                 = "";
        String cp                   = codbanca.trim();
        boolean retorno             = false;

        if (listaBancas != null){
            st = new StringTokenizer(listaBancas, ",");
            while (st.hasMoreTokens()) {
                elem = st.nextToken().trim();
                if (elem.equals(cp)){
                    retorno = true;
                }
            }
        }

        return retorno;
    }
    
    /*******************************************************************************************************
     * iniciarAvance
     *******************************************************************************************************/
    /**
     * Consulta las opciones iniciales para el proceso de avance.
     * <p>
     * Registro de Versiones
     * <ul>
     * <li>1.0 (26/12/2014 Manuel Escárate - BEE  - Jimmy Muñoz D. (ing.Soft.BCI) ):   Version Inicial.</li>
     * <li>1.1 (12/11/2015 Manuel Escárate - BEE  - Jimmy Muñoz D. (ing.Soft.BCI) ):   Se agregan elementos asociados
     *                                               a la condición de garantía y control de excepciones.</li>
     * <li>1.2 (22/02/2016 Hector Carranza - BEE  - Felipe Ojeda . (ing.Soft.BCI) ):   Se modifica lógica de selección de líneas.</li>
     * <li>1.3 (01/03/2016 Manuel Escárate - BEE  - Felipe Ojeda . (ing.Soft.BCI) ):   Se agregan logs y excepciones.</li>
     * <li>1.4 (01/04/2016 Manuel Escárate - BEE  - Felipe Ojeda . (ing.Soft.BCI) ):   Se agrega tieneLineaDisponible para validar monto. </li>
     * <li>1.5 (10/05/2016 Manuel Escárate - BEE  - Pablo Paredes . (ing.Soft.BCI) ):  Se agregan más excepciones, se separa la consulta de avales con la visación de estos. </li>
     * </ul>
     * </p>
     *
     * @param multiEnvironment multiEnvironment.
     * @param rut rut.
     * @param dv digito verificador.
     * @return DatosParaAvanceTO datos para el avance.
     * @throws MultilineaException multilinea excepcion.
     * @throws EJBException  ejb excepcion.
     * @throws RemoteException  excepcion remota.
     * @since 3.0
     */
    public DatosParaAvanceTO iniciarAvance(MultiEnvironment multiEnvironment,long rut,char dv)
    		throws MultilineaException, EJBException,  RemoteException {
    	if (getLogger().isInfoEnabled()) {
              getLogger().info("[iniciarAvance][BCI_INI]");
        }
    	DatosParaAvanceTO datosAvanceTO = null;
    	RetornoTipCli datosCliente = null;
    	InputConsultaCln ibean = null;
    	ResultConsultaCln resultcln = null;
    	ArrayList excepcionesArray = new ArrayList();
		boolean tieneLineaDisponible = false;
    	try {
    		try {
    			if (getLogger().isDebugEnabled()) {
        			getLogger().debug("[iniciarAvance] [consultaAntecedentesGenerales] [rut] [" + rut + "] [dv] [" + dv + "]");
        		}
    			datosCliente = consultaAntecedentesGenerales(rut,dv);
    			if (getLogger().isDebugEnabled()) {
        			getLogger().debug("[iniciarAvance] [termine] [consultaAntecedentesGenerales]");
        		}
    		}
    		catch (Exception e){
    			if(getLogger().isEnabledFor(Level.ERROR)){
        			getLogger().error("[iniciarAvance] [Exception] [consultaAntecedentesGenerales] "
        					+ "mensaje=< " + e.getMessage() + ">", e);
        		}
    			excepcionesArray.add("ERRORCAG");
    			excepcionesArray.add(e.getMessage());
    			throw new MultilineaException("ESPECIAL", e.getMessage());
    		}
    		if (datosCliente != null){
    			if (getLogger().isDebugEnabled()) {
    				getLogger().debug("[iniciarAvance] [consultaAntecedentesGenerales] datosCliente != null");
        		}
    			datosAvanceTO = new DatosParaAvanceTO();
    			datosAvanceTO.setCodBanca((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.TipoBca:datosCliente.DatEmpresa.TipoBca);
    			datosAvanceTO.setPlan((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.codigoPlan.charAt(0):datosCliente.DatEmpresa.codigoPlan.charAt(0));
    			datosAvanceTO.setOficinaIngreso((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.CodOficina:datosCliente.DatEmpresa.CodOficina);
    			datosAvanceTO.setCodigoEjecutivo((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.CodigoEjecutivo:datosCliente.DatEmpresa.CodigoEjecutivo);
    			datosAvanceTO.setCodSegmento((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.segmento.codigo:datosCliente.DatEmpresa.segmento.codigo);
    			datosAvanceTO.setRazonSocial((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.Nombres:datosCliente.DatEmpresa.RazonSocial);
    			datosAvanceTO.setNombreFantasia((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.Nombres:datosCliente.DatEmpresa.NombreFantasia);
    		} 
    		try{
    			if (getLogger().isDebugEnabled()) {
        			getLogger().debug("[iniciarAvance] [consultaCln] [rut] [" + rut + "] [dv] [" + dv + "]");
        		}
 			ibean = new InputConsultaCln("025", "", Integer.parseInt(String.valueOf(rut)), dv, ' ',"", "", 0, 0);
    			resultcln = (ResultConsultaCln) consultaCln(multiEnvironment, ibean, new ResultConsultaCln());
    			if (getLogger().isDebugEnabled()) {
        			getLogger().debug("[iniciarAvance] [termine] [consultaCln]");
        		}
    		}
    		catch (Exception e){
    			if(getLogger().isEnabledFor(Level.ERROR)){
        			getLogger().error("[iniciarAvance] [Exception] [consultaCln] "
        					+ "mensaje=< " + e.getMessage() + ">", e);
        		}
    			throw new MultilineaException("ESPECIAL", e.getMessage());
    			
    		}
    	    if (getLogger().isDebugEnabled()) {
		    	getLogger().debug("[iniciarAvance] pasé la consulta CLN");
		    }
    		double montoMargen = 0;
			double montolcr = 0;
			double montoDisponible = 0;
			double montoAprobado = 0;
			double montoMaximo = 0;
			boolean visacion = false;
			boolean soloG = false;
			boolean tieneMultilinea = false;
			boolean tieneAval = false;
			boolean tieneFoga = false;
			boolean tieneCual = false; 
			boolean tieneAvales =false;
			String codTipoGarantia = "";
			List avalesObtenidos = new ArrayList();
			Aval[] avales = null;
			PlantillaOperacionesCredito poc = null; 
            DatosPlantillaProductoTO[] datosPlantilla = null;
            if (getLogger().isDebugEnabled()) {
		    	getLogger().debug("[iniciarAvance] antes de consultr por resultCln");
		    }
			if (resultcln != null){
			    if (getLogger().isDebugEnabled()) {
			    	getLogger().debug("[iniciarAvance] [resultado cln != null");
			    }
				datosAvanceTO.setTieneMargen(true);
				datosAvanceTO.setTieneMargenActivo(resultcln.getCodEstado() == 'A' ? true : false); 
			    if (resultcln.getFechaVencimiento() !=null){
			    	if (getLogger().isDebugEnabled()) {
	        			getLogger().debug("[iniciarAvance] [fechaVencimiento != null]");
	        		}
			    	datosAvanceTO.setTieneMargenAlDia(FechasUtil.comparaDias(new Date(), resultcln.getFechaVencimiento()) < 1 ? true : false);  //fecha hoy <= a vcmto margen
			    }
			  	datosAvanceTO.setTieneMargenMonto(resultcln.getMontoDisponible() > 0 ? true : false);  
				montoMargen = Double.parseDouble(String.valueOf(resultcln.getMontoDisponible()));
    			datosAvanceTO.setMontoDisponible(resultcln.getMontoDisponible());
    			Linea[] lineas = resultcln.getLineas();
    			String tipoLinea = "";
    			String fechaVencimientoMLT ="";
    			if (lineas !=null && lineas.length > 0){  
    				if (getLogger().isDebugEnabled()) {
    					getLogger().debug("[iniciarAvance] [lineas distinto de null");
    				}
					
    				String tipoLineaOcupada = definirLDC(multiEnvironment, lineas);
    				for (int i = 0; i < lineas.length; i++) {
    		            if (lineas[i] == null) {
    		                break;
    		            }
    		            tipoLinea = lineas[i].getCodigoTipoLinea();
    		            if (getLogger().isDebugEnabled()) {
    				    	getLogger().debug("[iniciarAvance] [tipoLinea]" + tipoLinea);
    				    }
    		            if (tipoLinea.equals(tipoLineaOcupada)) { //verifica si tipo de linea es multilinea
    		                soloG      = lineas[i].getCodigoTipoInfo() == 'G' ? true : false;
    		                tieneAval  = (lineas[i].getTipoOperacion()).equals("AVL") ? true : tieneAval;
    		                tieneFoga  = (lineas[i].getTipoOperacion()).equals("952") ? ((lineas[i].getCodAuxiliarLinea()).trim().equals("101") ? true : tieneFoga) : tieneFoga;
    		                tieneCual  = soloG ? (!(lineas[i].getTipoOperacion()).trim().equals("") ? true : tieneCual) : tieneCual;
    		                if(tieneCual) codTipoGarantia = (lineas[i].getTipoOperacion());
    		            }
    		        }
    				
    				
    				for (int i = 0; i < lineas.length; i++) {
					    if (getLogger().isDebugEnabled()) {
						   getLogger().debug("[iniciarAvance] [recorriendo el for de líneas]"+i);
						}
						
						if (lineas[i] == null) {
							if (getLogger().isDebugEnabled()) {
								getLogger().debug("[iniciarAvance] Multinea - Linea [es nula]");
							}
							break;
						}
						if (lineas[i].getCodigoTipoLinea() != null){
							if (getLogger().isDebugEnabled()) {
								getLogger().debug("[iniciarAvance] [código tipo de línea distinto de null]");
							}
							tipoLinea = lineas[i].getCodigoTipoLinea();	
						}
						if (getLogger().isDebugEnabled()) {
							getLogger().debug("[iniciarAvance] [tipo de línea]" +tipoLinea);
						}
						if (tipoLinea.equals(tipoLineaOcupada)) {  
							tieneLineaDisponible = true;
							if (getLogger().isDebugEnabled()) {
				    			getLogger().debug("[iniciarAvance] [tipoLinea] ["+ tipoLineaOcupada +"]");
				    		}
							datosAvanceTO.setTieneMultilinea(true);
							datosAvanceTO.setTieneMultilineaVigente(lineas[i].getIndVigencia() == 'S' ? true : false);
                            datosAvanceTO.setTieneMultilineaAlDia(FechasUtil.comparaDias(new Date(), lineas[i].getFechaVencimiento()) < 1 ? true : false);//fecha hoy <= a vcmto linea
                        
							if (!datosAvanceTO.isTieneMultilinea()) {
								if (getLogger().isDebugEnabled()) {
									getLogger().debug("[iniciarAvance] Cliente [rut]" + rut + "-" + "[dv]"+ dv + " No posee Multilinea (MLT)");
								}
							}
						    String condicionGar = tieneAval ? "2  " : (tieneFoga ? "10 " : (tieneCual ? "8  " : "4  "));
					    	if (getLogger().isDebugEnabled()) {
					    		getLogger().debug("[iniciarAvance] ["+ condicionGar+"] ");
					    	}
					    	datosAvanceTO.setCondicionGarantia(condicionGar);
					  
						    tieneAvales = condicionGar.equals("2  ") ? true : false;
					        if (tieneAvales){
					         	if (getLogger().isDebugEnabled()) {
					         		getLogger().debug("[iniciarAvance] tieneAvales [true] Cliente[" + rut + "-" + dv + "]");
					         	}
					            try{
						            ResultConsultaAvales obeanAval = new ResultConsultaAvales();
						            InputConsultaAvales abean = new InputConsultaAvales("026",
						            											(int) rut,
						                                                                dv,
						                                                                ' ',
						                                                                " ",
						                                                                " ",
						                                                                " ",
						                                                                0,
						                                                                0,
						                                                                "AVL",
						                                                                "AVC");
						            consultaAvales(multiEnvironment, abean, obeanAval);
						            avales    = obeanAval.getAvales();
						            for (int j = 0; j < avales.length; j++) {
						            	if (getLogger().isDebugEnabled()) {
											getLogger().debug("[iniciarAvance] [recorriendo consulta avales]" + j);
										}
						                char   dvfAval = avales[j].getDigitoVerificaAval();
						                char   indvige = avales[j].getVigente();
						            	if (getLogger().isDebugEnabled()) {
						            		getLogger().debug("[iniciarAvance] [Esta vigente el rut "+ avales[j].getRutAval() +" ? vigente["+ indvige +"]]");
						            	}
						            	if (indvige == 'S'){
						            		avalesObtenidos.add(avales[j]);
						            	}
						            }
						        
					            }
					            catch(Exception e) {
					            	if(getLogger().isEnabledFor(Level.ERROR)){
					            		getLogger().error("[iniciarAvance]ERROR [consultaAvales] Cliente[" + rut + "-" + dv + "]    Exception [" + "COD-123" + "]");
					            	}
					            	excepcionesArray.add(e.getMessage());
					            	excepcionesArray.add("ERRORAVALES");
					            	datosAvanceTO.setCondicionGarantia("4  ");
					            }
						      
					            String respuestaVisacion = "";
					            int numeroAvalesVisar = Integer.parseInt(TablaValores.getValor("multilinea.parametros", "numeroAvalesVisar", "DESC"));
					            int visados = 0;
					            try{
					               	if (avales != null && avales.length > 0){
					            		for (int k = 0; k < avales.length; k++) {
					            			if (getLogger().isDebugEnabled()) {
					            				getLogger().debug("[iniciarAvance] [recorriendo avales]" + k);
					            			}
					            			if (visados >= numeroAvalesVisar)
					            				break;
					            			String rutAval = Integer.toString(avales[k].getRutAval());
					            			char   dvfAval = avales[k].getDigitoVerificaAval();
					            			char   indvige = avales[k].getVigente();
					            			if (indvige == 'S'){
					            				respuestaVisacion = visacionRut(rutAval, dvfAval, multiEnvironment.getCanal(), "EMP");
					            				if ((respuestaVisacion == null) 
					            						|| (respuestaVisacion != null && !respuestaVisacion.trim().equals("AP"))){
					            					if (getLogger().isDebugEnabled()) {	
					            						getLogger().debug("Visacion NOK:" + respuestaVisacion);
					            					}
					            					throw new MultilineaException("ESPECIAL", "ERRVISACION"+respuestaVisacion);
					            				}
					            				visados++;
					            			}
					            		}
					            		if (getLogger().isDebugEnabled()) {	
					            			getLogger().debug("visacion de avales OK");
					            		}
					            		datosAvanceTO.setVisaAvalesOK(true);
					               	}
					            }
					            catch(Exception e) {
					            	if(getLogger().isEnabledFor(Level.ERROR)){
					            		getLogger().error("[iniciarAvance]ERROR [visacionRut] Cliente[" + rut + "-" + dv + "]    Exception [" + "COD-123" + "]");
					            	}
					            	excepcionesArray.add(e.getMessage());
					            	excepcionesArray.add("ERRORVISACIONAVALES");
					            }
					        }
					        else{
					        	if (getLogger().isDebugEnabled()) {	
					        		  getLogger().debug("[iniciarAvance] tieneAvales [false]   Cliente[" + rut + "-" + dv + "]");
					        	}
					        }
					        String respuestaVisacion = null;
					    	if (getLogger().isDebugEnabled()) {	
				        		  getLogger().debug("[iniciarAvance] visando Empresa [" + rut + "-" + dv + "]");
					    	}
					        try{
					        respuestaVisacion = visacionRut(String.valueOf(rut), dv, multiEnvironment.getCanal(), "EMP");
					        if ((respuestaVisacion == null) || (respuestaVisacion != null && !respuestaVisacion.trim().equals("AP"))){
					        	if (getLogger().isDebugEnabled()) {	
					        		getLogger().debug("[iniciarAvance] Visacion NOK:" + respuestaVisacion);
					        	}
					        }
					        else{
					        	if (getLogger().isDebugEnabled()) {	
					        		getLogger().debug("[iniciarAvance] visacion Empresa [" + rut + "-" + dv + "]    OK !!!");
					        	}
					        	visacion = true;
					        }
					        }
					        catch(Exception e) {
				            	if(getLogger().isEnabledFor(Level.ERROR)){
				            		getLogger().error("[iniciarAvance]ERROR [visacionRut] Cliente[" + rut + "-" + dv + "]");
				            	}
				            	excepcionesArray.add(e.getMessage());
				            	excepcionesArray.add("ERRORVISACION");
				            }
                            
                            if (lineas[i].getFechaVencimiento() != null) {
                            	fechaVencimientoMLT = FechasUtil.convierteDateAString(lineas[i].getFechaVencimiento(), "dd/MM/yyyy");
                            	if (getLogger().isDebugEnabled()) {
                            		getLogger().debug("[iniciarAvance] Multinea - Cliente linea MLT al dia  ?[" + fechaVencimientoMLT + "]");
                            	}
                            	datosAvanceTO.setFechaVencimientoMLT(fechaVencimientoMLT);
                            	if (getLogger().isDebugEnabled()) {
                            		getLogger().debug("[iniciarAvance] [fechaVencimientoMLT]" + fechaVencimientoMLT + "]");
                            	}
                            }
                            else{
                            	if (getLogger().isDebugEnabled()) {
                            		getLogger().debug("[iniciarAvance] Multinea - Cliente linea MLT al dia  ?[es nulo]");
                            	}
                            }
                            datosAvanceTO.setTieneMultilineaMonto(
                            		lineas[i].getMontoDisponible4() > 0. ? true : false); 
                            montolcr = (double) lineas[i].getMontoDisponible4();
                            montoDisponible =  Math.min(montolcr,montoMargen);
                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("[iniciarAvance] montolcr " + montolcr);
                                getLogger().debug("[iniciarAvance] montoMargen " + montoMargen);
                            }
                            if (resultcln.getCodigoMoneda().trim().equals("0998")){
                            	String fechaHoy = new SimpleDateFormat("ddMMyyyy").format(new Date());
                            	ResultConsultaValoresCambio rtcf  = obtieneConversionMoneda(
                            			"UFP", fechaHoy, fechaHoy, "*", montoDisponible);
                            	montoDisponible = Double.parseDouble(String.valueOf(Math.rint(rtcf.getTotalCalculado())));
                            	if (getLogger().isDebugEnabled()) {
                            		getLogger().debug("[iniciarAvance] montoDisponible " + montoDisponible);
                            	}
                            }
							String porcentajeAjusteAux  = (TablaValores.getValor("multilinea.parametros"
									, "ajustemontodisponible", "desc")).trim();
							double porcentajeAjuste  = Double.parseDouble(
									porcentajeAjusteAux.equals("") ? "0" : porcentajeAjusteAux);
							if (getLogger().isDebugEnabled()) {
								getLogger().debug("[iniciarAvance] Porcentaje de ajuste ["+ porcentajeAjuste +"]");
							}
							porcentajeAjuste = porcentajeAjuste/VALOR_100;
							montoDisponible = (montoDisponible - (montoDisponible * porcentajeAjuste));
							if (getLogger().isDebugEnabled()) {
								getLogger().debug("[iniciarAvance] Monto Disponible Ajustado ["+ montoDisponible +"]");
							}
							montoDisponible = (montoDisponible > 0 ? montoDisponible : 0);
							if (getLogger().isDebugEnabled()) {
								getLogger().debug("[iniciarAvance] Monto Disponible real ["+ montoDisponible +"]");
							}
							datosAvanceTO.setMontoDisponible(montoDisponible);
							montoAprobado = lineas[i].getValor();
							montoMaximo   = lineas[i].getCupoMaximo();
							if (getLogger().isDebugEnabled()) {
								getLogger().debug("[iniciarAvance] Multinea - Monto Aprobado [" + montoAprobado + "]");
								getLogger().debug("[iniciarAvance] Multinea - Monto Maximo [" + montoMaximo   + "]");
							}
							if (montoAprobado > 0)
								break;
						}
					}
				    fechaVencimientoMLT = fechaVencimientoMLT == null ? "" : fechaVencimientoMLT;
					Vector vectorPOC = llenaVectorPlantillaProducto("simmultilinea.parametros",
                			multiEnvironment, fechaVencimientoMLT); 
					double maximo = 0;
					double valorUf = 0;
					String fechaHoy = new SimpleDateFormat("ddMMyyyy").format(new Date());
					SvcSimulaCursaCredito svcscc;
					ResultConsultaValoresCambio  rtcf = null;
					try {
						if (getLogger().isDebugEnabled()) {
							getLogger().debug("[iniciarAvance] [obtieneCostodeFondo]");
						}
						svcscc = new SvcSimulaCursaCreditoImpl();
						rtcf = svcscc.ObtieneCostodeFondo("UFP", fechaHoy, fechaHoy, "*", 1 );
						valorUf =  rtcf.getEquivalencia();
						if (getLogger().isDebugEnabled()) {
							getLogger().debug("[iniciarAvance] [Valor UF Multilinea] : ["+ valorUf +"]");
						}
					}
				    catch (Exception e) {
				    	  if(getLogger().isEnabledFor(Level.ERROR)){
				    		  getLogger().error("[iniciarAvance] No fue posible obtener valores de cambio");
				    	  } 
					}
					
                	if (vectorPOC != null){
                		datosPlantilla = new DatosPlantillaProductoTO[vectorPOC.size()];
                		for (int k = 0; k < vectorPOC.size(); k++) {
                			poc = (PlantillaOperacionesCredito) vectorPOC.elementAt(k);
                			datosPlantilla[k] = new DatosPlantillaProductoTO();
                			datosPlantilla[k].setCreditoMinimo(poc.creditoMinimo);
                		 	datosPlantilla[k].setCreditoMaximo(poc.creditoMaximo);
                			datosPlantilla[k].setProducto(poc.tipoOperacion);
                			datosPlantilla[k].setCodigoAuxiliar(poc.codigoAuxiliar);
                			datosPlantilla[k].setCuotaInicial(poc.perMinPrimPagoInt);
                			datosPlantilla[k].setCuotaFinal(poc.perMaxPrimPagoInt);
                			datosPlantilla[k].setMinimoPrimerPago(poc.perMinPrimPagoCap);
                			datosPlantilla[k].setMaximoPrimerPago(poc.perMaxPrimPagoCap);
                			if (poc.valorCambio.trim().equals("0999")){
                				if (getLogger().isDebugEnabled()) {
                					getLogger().debug("[iniciarAvance] [valor cambio igual 0999]");
                				}
                				maximo= datosAvanceTO.getMontoDisponible() >= poc.creditoMaximo ? poc.creditoMaximo 
                						:datosAvanceTO.getMontoDisponible();
                			}
                			else {
                				if (getLogger().isDebugEnabled()) {
        							getLogger().debug("[iniciarAvance] [valor cambio != 0999]");
        						}
                				maximo =datosAvanceTO.getMontoDisponible()/valorUf >= poc.creditoMaximo  
                						? poc.creditoMaximo : Math.rint(datosAvanceTO.getMontoDisponible()/valorUf);
                			}
                			datosAvanceTO.setMontoMaximo(maximo);
                		}
                		if (getLogger().isDebugEnabled()) {
    	        			getLogger().debug("[iniciarAvance] [se setean datos plantilla]");
    	        		}
                		datosAvanceTO.setDatosPlantilla(datosPlantilla);
                	}
				} 
    		}
			String bancapermitidas ="";
			try{
				if (getLogger().isDebugEnabled()) {
					getLogger().debug("[iniciarAvance] [voy a obtener bancas permitidas]");
				}
				bancapermitidas = TablaValores.getValor("multilinea.parametros", "bancapermitidasEmpresario", "desc");
			} 
			catch (Exception e) {
				if(getLogger().isEnabledFor(Level.ERROR)){
					getLogger().error("[iniciarAvance] Multinea - Exception no se pudo obtener : [" + e.getMessage() +"]");
				}
			}
			 if(verificaPertenenciaBanca(datosAvanceTO.getCodBanca(), bancapermitidas)){
				 if (getLogger().isDebugEnabled()) {
					 getLogger().debug("[iniciarAvance] Multinea - esEmpresario  : [S]");
				 }
				 datosAvanceTO.setEsEmpresario("S");
			 }
			 else{
				 if (getLogger().isDebugEnabled()) {
					 getLogger().debug("[iniciarAvance] Multinea - esEmpresario  : [N]");
				 }
				 datosAvanceTO.setEsEmpresario("N");
			 }
			 
			 if (getLogger().isDebugEnabled()) {
				 getLogger().debug("[iniciarAvance] visacion: ["+ visacion+"]");
			 }
			 if(datosAvanceTO.getMontoDisponible() > 0 && visacion) {
				 datosAvanceTO.setOfertaCrediticia(CURSE_ACT_MTL);
				 if (getLogger().isDebugEnabled()) {
					 getLogger().debug("[iniciarAvance] ofertaCrediticia : CURSE_ACT_MTL");
				 }
				 if((avalesObtenidos != null  && avalesObtenidos.size() > 0 && !datosAvanceTO.isVisaAvalesOK()) 
						 || (tieneAvales && avalesObtenidos != null && avalesObtenidos.size() == 0 )){
					 if (getLogger().isDebugEnabled()) {
						 getLogger().debug("[iniciarAvance] ofertaCrediticia : SIM_CUR_CLI");
					 }
					 datosAvanceTO.setOfertaCrediticia(SIM_CUR_CLI);  
				 }
			 }
			 else{
				 if (getLogger().isDebugEnabled()) {
					 getLogger().debug("[iniciarAvance] ofertaCrediticia SIM_EJE_COM");
				 }
				 datosAvanceTO.setOfertaCrediticia(SIM_EJE_COM);
			 }
    	}
    	catch (Exception e) {
    		if(getLogger().isEnabledFor(Level.ERROR)){
    			getLogger().error("[iniciarAvance] [general] Exception " + e.getMessage());
    		}
    		throw new MultilineaException("ESPECIAL", e.getMessage());
    	}
        
         if (!tieneLineaDisponible){
    		datosAvanceTO.setMontoDisponible(0);
    	}
       	datosAvanceTO.setExcepciones((String[]) excepcionesArray.toArray(new String[excepcionesArray.size()]));
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[iniciarAvance] [BCI_FIN]");
    	}
      	return datosAvanceTO;
    }
    
    /*******************************************************************************************************
     * consultaAntecedesGenerales
     *******************************************************************************************************/
    /**
     * Consulta los datos iniciales y generales para el avance
     * <p>
     * Registro de Versiones
     * <ul>
     * <li>1.0 (26/12/2014 Manuel Escárate - BEE  - Jimmy Muñoz D. (ing.Soft.BCI) ):   Version Inicial
     * </ul>
     * </p>
     * @param rut Rut.
     * @param dv Digito Verificador.
     * @return retorno datos de cliente.
     * @throws MultilineaException  multilinea excepcion.
     * @throws EJBException ejb excepcion.
     * @since 3.0
     * 
     */
    public RetornoTipCli consultaAntecedentesGenerales(long rut,char dv) throws  EJBException,
    MultilineaException{
    	try{
    	    if (getLogger().isDebugEnabled()) {
    	    	getLogger().debug("[consultaAntecedentesGenerales] [BCI_INI]");
    	    }
    	    Cliente clienteBean = null;
    		InitialContext ic = JNDIConfig.getInitialContext();
    		Object objPre = ic.lookup(JNDI_NAME_SCB);
    		ClienteHome homePre = (ClienteHome) PortableRemoteObject.narrow(objPre, ClienteHome.class);
    		clienteBean = (Cliente) PortableRemoteObject.narrow(homePre.create(), Cliente.class);
    		if (getLogger().isDebugEnabled()) {
      	    	getLogger().debug("[consultaAntecedentesGenerales] [BCI_FIN]");
      	    }
    		return clienteBean.getDatosBasicosGenerales(rut,dv);
    	} 
    	catch (Exception e) {
    	   	if(getLogger().isEnabledFor(Level.ERROR)){
    	   		getLogger().debug("[consultaAntecedentesGenerales] Exception " + e.getMessage());
    	   	}
    		throw new MultilineaException("ESPECIAL", e.getMessage());
    	}
    }
    
    /*******************************************************************************************************
     * obtenerCondicionesCredito
     *******************************************************************************************************/
    /**
     * Obtiene los datos a desplegar en las condiciones del crédito.
     * <p>
     * Registro de Versiones
     * <ul>
     * <li>1.0 (26/12/2014 Manuel Escárate - BEE  - Jimmy Muñoz D. (ing.Soft.BCI) ):   Version Inicial.</li>
     * <li>1.1 (12/11/2015 Manuel Escárate - BEE  - Jimmy Muñoz D. (ing.Soft.BCI) ):   Se agregan elementos asociados
     *                                               a la condición de garantía y se agregan logs.
     * <li>1.2 (01/03/2016 Manuel Escárate - BEE  - Felipe Ojeda . (ing.Soft.BCI) ):   Se agregan logs y excepciones.</li>                                              
     * <li>1.3 (20/07/2016 Manuel Escárate - BEE  - Felipe Ojeda . (ing.Soft.BCI) ):   Se agrega seteo para valor de gasto.</li>
     * </ul>
     * </p>
     *
     * @param multiEnvironment multiambiente.
     * @param vencimientos  estructura de vencimientos.
     * @param datosCondCred datos condiciones del credito.
     * 
     * @return DatosPaaraCondicionesCredito datos para el avance.
     * @throws MultilineaException  multilinea excepcion.
     * @throws EJBException ejb excepcion.
     * @throws RemoteException excepcion remota.
     * @since 3.0
     *
     */  
    public DatosResultadoOperacionesTO obtenerCondicionesCredito(MultiEnvironment multiEnvironment,
    		EstructuraVencimiento[] vencimientos,DatosParaCondicionesCreditoTO datosCondCred) throws MultilineaException, EJBException, RemoteException{
    	if (getLogger().isDebugEnabled())  getLogger().debug("[obtenerCondicionesCredito] [BCI_INI]"); 
    	DatosResultadoOperacionesTO datosCondicionesCredito = null;
 		ResultAvanceMultilinea resultadoAvance = null;
		Hashtable datosLog = new Hashtable();
		ArrayList excepcionesArray = new ArrayList();
    	try{
    		if (getLogger().isDebugEnabled()) {
    			getLogger().debug("[obtenerCondicionesCredito] [avanceMultilinea]");
    		}
    		resultadoAvance = avanceMultilinea(
    				multiEnvironment, 
    				datosCondCred.getTipoOperacion(), 
    				datosCondCred.getMoneda(), 
    				datosCondCred.getCodigoAuxiliar(), 
    				datosCondCred.getOficinaIngreso(), 
    				datosCondCred.getRutDeudor(), 
    				datosCondCred.getDigitoVerificador(), 
    				datosCondCred.getMontoCredito(), 
    				datosCondCred.getAbono(), 
    				datosCondCred.getCargo(), 
    				datosCondCred.getCtaAbono(), 
    				datosCondCred.getCtaCargo(), 
    				datosCondCred.getIndicadorNP01(), 
    				datosCondCred.getIndicadorNP02(), 
    				vencimientos, 
    				datosCondCred.getCodBanca(),
    				datosCondCred.getPlan(),
    				datosLog,
    				datosCondCred.getCodSegmento(), 
    				datosCondCred.getCaiNumOpe(), 
    				datosCondCred.getIicNumOpe(),
    				datosCondCred.getTasaPropuesta(), 
    				datosCondCred.getBandera(), 
    				datosCondCred.getEjecutivo(), 
    				datosCondCred.getSeguros(),
    				datosCondCred.getCondicionGarantia());
    		
    		if (resultadoAvance != null){
    			if (getLogger().isDebugEnabled()){
    				getLogger().debug("[obtenerCondicionesCredito] resultadoAvance != null"); 
    				getLogger().debug("[obtenerCondicionesCredito] gasto notarial: " + resultadoAvance.getValorGastoNotarial()); 

    			}
    		    double valInteOperacion        = VALOR_01;
    	        int rutAvalDPS                 = 0;
    	        char rutdigAvalDPS             = ' ';
    			datosCondicionesCredito = new DatosResultadoOperacionesTO();
    			CalendarioPago[] calendario = resultadoAvance.getResultLiqOpc().getCalendarioPago();
    			datosCondicionesCredito.setValorCuota(calendario[0].getCuota());
    			datosCondicionesCredito.setTasaInteresInternet(resultadoAvance.getTasa());
    			datosCondicionesCredito.setImpuestos(resultadoAvance.getResultLiqOpc().getImpuesto());
    			datosCondicionesCredito.setCondicionGarantia(resultadoAvance.getCondicionGarantia());
    			datosCondicionesCredito.setValorGastoNotarial(resultadoAvance.getValorGastoNotarial());
    			if (getLogger().isDebugEnabled()){
    				getLogger().debug("[obtenerCondicionesCredito] tieneAvales:"+datosCondCred.isTieneAvales()); 
    			}
    			if (!datosCondCred.isTieneAvales()){
    				if (getLogger().isDebugEnabled()){
    					getLogger().debug("[obtenerCondicionesCredito] no tiene avales"); 
    				}
    				if (resultadoAvance.getCondicionGarantia().trim().equals("2")){
    					try{
    					ResultConsultaAvales obeanAval = new ResultConsultaAvales();
    					InputConsultaAvales abean = new InputConsultaAvales("026",
    							datosCondCred.getRutDeudor(),
    							datosCondCred.getDigitoVerificador(),
    							' ',
    							" ",
    							" ",
    							" ",
    							0,
    							0,
    							"AVL",
    							"AVC");
    					consultaAvales(multiEnvironment, abean, obeanAval);
    						if (getLogger().isDebugEnabled()) { getLogger().debug("[obtenerCondicionesCredito] despues consultaAvales");}
    					Aval[] avales    = obeanAval.getAvales();
    					DatosAvalesTO[] avalesMultilinea = new DatosAvalesTO[avales.length];
    					DatosAvalesTO[] avalesMultilineaCorreo = new DatosAvalesTO[avales.length];
    					if (getLogger().isDebugEnabled()) { 
    						getLogger().debug("avales.length ["+ avales.length +"]");
    					}
    					if (avales != null){
    						for (int i = 0; i < avales.length; i++) {
    								if (avales[i].getVigente() == 'S'){
    									avalesMultilinea[i]	= new DatosAvalesTO();
    									avalesMultilinea[i].setRutAval(String.valueOf(avales[i].getRutAval()));
    									avalesMultilinea[i].setDvAval(String.valueOf(avales[i].getDigitoVerificaAval()));
    									break;
    								}
    						}
    						int contAvl = 0;
    						for (int j = 0; j < avales.length; j++) {
    							if (avales[j] != null){
    								if (avales[j].getVigente() == 'S'){
    									avalesMultilineaCorreo[contAvl]	= new DatosAvalesTO();
    									avalesMultilineaCorreo[contAvl].setRutAval(String.valueOf(avales[j].getRutAval()));
    									avalesMultilineaCorreo[contAvl].setDvAval(String.valueOf(avales[j].getDigitoVerificaAval()));
    									contAvl++;
    								}
    							}
    						}
    					}
    					
    					if (getLogger().isDebugEnabled()){
    						getLogger().debug("[obtenerCondicionesCredito] antes del seteo de avales"); 
        				}
    					datosCondicionesCredito.setAvalesCorreo(avalesMultilineaCorreo);
    					datosCondicionesCredito.setAvales(avalesMultilinea);
    				}
    					catch(Exception e) {
    						if(getLogger().isEnabledFor(Level.ERROR)){
    							getLogger().error("[iniciarAvance]ERROR [consultaAvales] Cliente  Exception [ " + e.getMessage() + "]");
    						}
    						excepcionesArray.add(e.getMessage());
    						excepcionesArray.add("ERRORAVALESCONDICIONES");
    					}
    				}
    			}
    			else {
    				if (getLogger().isDebugEnabled()){
    					getLogger().debug("[obtenerCondicionesCredito] seteo de avales en null"); 
    				}
    				datosCondicionesCredito.setAvales(null);
    			}
    			datosCondicionesCredito.setCaiOperacion(resultadoAvance.getResultLiqOpc().getCaiOperacion());
    			datosCondicionesCredito.setIicOperacion(resultadoAvance.getResultLiqOpc().getIicOperacion());
    			datosCondicionesCredito.setFechaCurse(resultadoAvance.getFechaCurse());
    			datosCondicionesCredito.setCalendario(calendario);
    			double  costoFinalCredito = 0;
    			if (getLogger().isDebugEnabled()){
					getLogger().debug("[obtenerCondicionesCredito] antes de recorrer calendario de pago"); 
				}
    			for (int i = 0; i < resultadoAvance.getResultLiqOpc().getCalendarioPago().length; i++) {
    				if (resultadoAvance.getResultLiqOpc().getCalendarioPago()[i] !=null 
    						&& resultadoAvance.getResultLiqOpc().getCalendarioPago()[i].getNumVencimiento() != 0){
    					costoFinalCredito = costoFinalCredito 
    							+ resultadoAvance.getResultLiqOpc().getCalendarioPago()[i].getCuota();
    				}
    			}
    			
    			datosCondicionesCredito.setMontoFinalCredito(resultadoAvance.getResultLiqOpc().getMontoNeto());
    			datosCondicionesCredito.setCostoFinalCredito(costoFinalCredito);
    			datosCondicionesCredito.setCostoTotalSeguros(resultadoAvance.getResultLiqOpc().getValorSeguro());
    			datosCondicionesCredito.setCodigoMoneda(resultadoAvance.getResultLiqOpc().getCodigoMoneda());
    			ResultLiquidacionDeOperacionDeCreditoOpc rlopc = (ResultLiquidacionDeOperacionDeCreditoOpc) resultadoAvance.getResultLiqOpc();
    			if (rlopc != null){
    				if (getLogger().isDebugEnabled()){
    					getLogger().debug("[obtenerCondicionesCredito] resultado operacion de crédito opc != null"); 
    				}
    				double montoTotalCredito      = rlopc.getMontoCredito();
    				int    numeroCuotas           = datosCondCred.getCuotaSeleccionada();
    				double  interesEfectivo        = resultadoAvance.getTasa();
    				Date fechaPrimerVencimiento = null;
    				try {
    					if (getLogger().isDebugEnabled()){
    						getLogger().debug("[obtenerCondicionesCredito] se setea fecha de vencimiento"); 
        				}
    					fechaPrimerVencimiento = ddMMyyyy_form.parse(datosCondCred.getFechaPrimerVcto());
    				} 
					catch (ParseException e) {
                          if (getLogger().isEnabledFor(Level.ERROR)) { getLogger().error("[obtenerCondicionesCredito] Error al parsear fecha: " + e);}
    				}
    				double gastosImpuesto         = rlopc.getImpuesto();
    				double gastosDesgravamen      = rlopc.getValorSeguro();
    				double gastosNotario          = rlopc.getValorGasto();
    				double factorCaeObtenido      = obtieneFactorCAEPorcentaje(
    						montoTotalCredito, numeroCuotas,interesEfectivo, fechaPrimerVencimiento,
    						gastosImpuesto, gastosDesgravamen, gastosNotario, null);
    				datosCondicionesCredito.setFactorCae(factorCaeObtenido);
    			}
                if (resultadoAvance.getArregloROC() != null){
                	if (getLogger().isDebugEnabled()){
						getLogger().debug("[obtenerCondicionesCredito] arreglo ROC != null"); 
    				}
                	DatosSegurosTO[] datosSeguro = new DatosSegurosTO[resultadoAvance.getArregloROC().length];
                        if (getLogger().isDebugEnabled()) { getLogger().debug("[obtenerCondicionesCredito] [SEGUROSSSABC]"); }
                   	int requiereDPS = 0; 
                	for (int i = 0; i < resultadoAvance.getArregloROC().length; i++) {
                		if (getLogger().isDebugEnabled()){
    						getLogger().debug("[obtenerCondicionesCredito] recorriendo arreglo roc"); 
        				}
                		datosSeguro[i]  = new DatosSegurosTO();
                		datosSeguro[i].setIdSeguro(i);
                                if (getLogger().isDebugEnabled()) { getLogger().debug("[obtenerCondicionesCredito] [Numero] :"+resultadoAvance.getArregloROC()[i].getNumero()); }
                		datosSeguro[i].setGlosaTipoSeguro(resultadoAvance.getArregloROC()[i].getGlosaTipoSeguro());
                                if (getLogger().isDebugEnabled()) { getLogger().debug("[obtenerCondicionesCredito] [Glosa tipo de seguro] :" + resultadoAvance.getArregloROC()[i].getGlosaTipoSeguro()); }
                		datosSeguro[i].setMontoSeguro(resultadoAvance.getArregloROC()[i].getTasaMontoFinal());
                		datosSeguro[i].setCodClfRentabilidadRcc(resultadoAvance.getArregloROC()[i].getCodClfRentabilidadRcc());
                		datosSeguro[i].setCodigoSubConcepto(resultadoAvance.getArregloROC()[i].getCodigoSubConcepto());
                                if (getLogger().isDebugEnabled()) { getLogger().debug("[obtenerCondicionesCredito] [codigo subconcepto] :"+resultadoAvance.getArregloROC()[i].getCodigoSubConcepto()); }
                		datosSeguro[i].setIndCobro(resultadoAvance.getArregloROC()[i].getIndCobro());
                		datosSeguro[i].setIndVigencia(resultadoAvance.getArregloROC()[i].getIndVigencia());
                		datosSeguro[i].setNumClienteRcc(resultadoAvance.getArregloROC()[i].getNumClienteRcc());
                		datosSeguro[i].setVrfClienteRcc(resultadoAvance.getArregloROC()[i].getVrfClienteRcc());
                		datosSeguro[i].setCodigoConcepto(resultadoAvance.getArregloROC()[i].getCodigoConcepto());
                                if (getLogger().isDebugEnabled()) { getLogger().debug("[obtenerCondicionesCredito] [codigoconcepto] :"+resultadoAvance.getArregloROC()[i].getCodigoConcepto()); }
                		datosSeguro[i].setCodFactorRiesgoRcc(resultadoAvance.getArregloROC()[i].getCodFactorRiesgoRcc());
                		datosSeguro[i].setIndTipoPlantilla(resultadoAvance.getArregloROC()[i].getIndTipoPlantilla());
                		datosSeguro[i].setIndSegObligatorio(resultadoAvance.getArregloROC()[i].getIndSegObligatorio());
                		resultadoAvance.getArregloROC()[i].getTasaMontoFinal();
                		if (resultadoAvance.getArregloROC()[i].getIndSegObligatorio() == 'S'){
                			datosSeguro[i].setDeshabilitado(true);
                		}
                		if (resultadoAvance.getArregloROC()[i].getIndVigencia() == 'S'){
                			datosSeguro[i].setCheckeado(true);
                		}
                		if (resultadoAvance.getArregloROC()[i].getCodFactorRiesgoRcc().equals("DPS")){
                                        if (getLogger().isDebugEnabled()) { getLogger().debug("[obtenerCondicionesCredito] [factorRiesgos = a DPS]"); }
                			if (resultadoAvance.getArregloROC()[i].getIndVigencia() == 'S'){
                				requiereDPS++;
                				if (resultadoAvance.getArregloROC()[i].getCodClfRentabilidadRcc().equals("IND")){
                					datosSeguro[i].setRutAsegurado(String.valueOf(resultadoAvance.getArregloROC()[i].getRutCliente()));
                					datosSeguro[i].setDigAsegurado(String.valueOf(
                							resultadoAvance.getArregloROC()[i].getDigitoVerificador()));
                				}
                				else if (!resultadoAvance.getArregloROC()[i].getCodClfRentabilidadRcc().equals("IND")){
                					datosSeguro[i].setRutAsegurado(String.valueOf(datosCondCred.getRutDeudor()));
                					datosSeguro[i].setDigAsegurado(String.valueOf(datosCondCred.getDigitoVerificador()));
                				}
                			}
                			datosSeguro[i].setRequiereDPS(true);
                		}
					}
                	if (getLogger().isDebugEnabled()){
						getLogger().debug("[obtenerCondicionesCredito] termine arreglo roc"); 
    				}
                	datosCondicionesCredito.setRequiereDPS(requiereDPS);
                	datosCondicionesCredito.setDatosSeguros(datosSeguro);
                }
                else {
                    if (getLogger().isDebugEnabled()) { getLogger().debug("[obtenerCondicionesCredito] [seguros es null]"); }
                }
    		}
    	}
    	catch (Exception e) {
            if (getLogger().isEnabledFor(Level.ERROR)) { getLogger().error("[obtenerCondicionesCredito] Exception " + e.getMessage()); }
    		throw new MultilineaException("ESPECIAL", e.getMessage());
    	} 
    	datosCondicionesCredito.setExcepciones((String[]) excepcionesArray.toArray(new String[excepcionesArray.size()]));
    	return datosCondicionesCredito;
    }
    
    /*******************************************************************************************************
     * ingresoOperacionFirma.
     *******************************************************************************************************/
    /**
     * Método que realizar operaciones sobre firmas.
     * <p>
     * Registro de Versiones
     * <ul>
     * <li>1.0 (10/04/2015 Manuel Escárate(BEE)  - Jimmy Muñoz D. (ing.Soft.BCI) ):   Version Inicial
     * <li>1.1 (01/03/2016 Manuel Escárate - BEE  - Felipe Ojeda . (ing.Soft.BCI) ):   Se agregan logs y excepciones.</li>   
     * <li>1.2 (10/05/2016 Manuel Escárate - BEE  - Pablo Paredes . (ing.Soft.BCI) ):  Se agregan excepciones y se agrega método para cambiar estado
     *                                                        de operación a través de la renovación.</li>   
     * </ul>
     * </p>
     *
     * @param multiEnvironment multiambiente.
     * @param datosOpe datos de operacion.
     * @param paso paso en la firma.
     * @return DatosResultadoOperacionesTO datos resultado operacion.
     * @throws MultilineaException  multilinea excepcion.
     * @throws EJBException ejb excepcion.
     * @throws RemoteException excepcion remota.
     * @since 3.0
     *
     */
    public DatosResultadoOperacionesTO ingresarOperacionFirma(MultiEnvironment multiEnvironment,
    		DatosParaOperacionesFirmaTO datosOpe,int paso) 
    				throws MultilineaException, EJBException, RemoteException{
    	DatosResultadoOperacionesTO datosResultado = new DatosResultadoOperacionesTO();
    	ArrayList excepcionesArray = new ArrayList();
    	datosResultado.setRespuesta(false);
    	if (getLogger().isInfoEnabled()) {
    		getLogger().info("[ingresarOperacionFirma][BCI_INI]");
    	}
    	if (paso == 0){
    		try {
    			if (!existOperacionenFirmas(datosOpe.getIdConvenio(), 
    					datosOpe.getRutEmpresa(), datosOpe.getDigitoVerifEmp(), 
    					datosOpe.getNumOperacion(), datosOpe.getAuxiliarCredito())){
    				if (getLogger().isDebugEnabled()) getLogger().debug("[ingresarOperacionFirma] Antes de ingresoOperacionCreditoMultilinea" );
    				ResultIngresoOperacionCreditoMultilinea resIng = null;
    				resIng = (ResultIngresoOperacionCreditoMultilinea) ingresoOperacionCreditoMultilinea(datosOpe.getIdConvenio(),
    						datosOpe.getRutEmpresa(),
    						datosOpe.getDigitoVerifEmp(),
    						datosOpe.getTipoOperacion(),
    						datosOpe.getAuxiliarOpe(),
    						datosOpe.getGlosaTipoCredito(),
    						datosOpe.getTipoAbono(),
    						datosOpe.getTipoCargoAbono(),
    						datosOpe.getOficinaIngreso(),
    						datosOpe.getCuentaAbono(),
    						datosOpe.getCuentaCargo(),
    						datosOpe.getIndicador(),
    						datosOpe.getIndicadorAplic(),
    						datosOpe.getMontoCredito(),
    						datosOpe.getCodigoMoneda(),
    						datosOpe.getTotalVencimientos(),
    						datosOpe.getFechaPrimerVencimiento(),
    						datosOpe.getFechaInicio(),
    						datosOpe.getFechaFin(),
    						"PEN",             //pendiente
    						datosOpe.getProcesoNegocio(),
    						datosOpe.getNumOperacion(),
    						datosOpe.getAuxiliarCredito(),
    						datosOpe.getNumOperacionCan(),
    						datosOpe.getCodAuxiliarCredito(),
    						datosOpe.getCodigoMoneda2(),
    						datosOpe.getMonedaLinea(),
    						datosOpe.getFechaExpiracion2(),
    						datosOpe.getMontoAbonado());
    				if (resIng != null){
    					datosResultado.setRespuesta(true);
    					datosResultado.setNumOperacion(resIng.getIdentificador());
    				} 
    			}
    			else{
    				datosResultado.setRespuesta(false);
    				if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] Operacion ya fue Ingresada para Firmar.");
    			}
    		} 
    		catch (Exception e) {
    			if (getLogger().isEnabledFor(Level.ERROR)) getLogger().error(e.getClass() + "[ingresarOperacionFirma] error [ingresoOperacionCreditoMultilinea] : " + e.getMessage());
    			excepcionesArray.add(e.getMessage());
    			excepcionesArray.add("ERRORINGRESOOPERACIONCREDITO");
    		}
    	}
    	else if (paso == 1){
    		if (getLogger().isDebugEnabled()){  
	    		getLogger().debug("[ingresarOperacionFirma] Firmaron todos y quedo Ok.");
	    		getLogger().debug("[ingresarOperacionFirma] datosOpe.getProcesoNegocio()." + datosOpe.getProcesoNegocio());
    		}
    		if(datosOpe.getProcesoNegocio().trim().equalsIgnoreCase("AVC") || datosOpe.getProcesoNegocio().trim().equalsIgnoreCase("AVN")){
    			try{
	    		ResultCambiaEstadoOperacionCreditoMultilinea resCam = null;
	    		resCam = (ResultCambiaEstadoOperacionCreditoMultilinea) cambiaEstadoOperacionCreditoMultilinea(
	    				datosOpe.getIdentificadorFirma(),
	    				datosOpe.getIdConvenio(),
	    				datosOpe.getRutEmpresa(),
	    				datosOpe.getDigitoVerifEmp(),
	    				"OKE");
	    		datosResultado.setRespuesta(true);
    		} 
    			catch (Exception e) {
        			if (getLogger().isEnabledFor(Level.ERROR)) getLogger().error(e.getClass() + "[ingresarOperacionFirma] error [cambiaEstadoOperacionCreditoMultilinea]: " + e.getMessage());
        			excepcionesArray.add(e.getMessage());
        			excepcionesArray.add("ERRORCAMBIAESTADOOPERACION");
        		}
    		} 
			else if(datosOpe.getProcesoNegocio().trim().equalsIgnoreCase("REN") || datosOpe.getProcesoNegocio().trim().equalsIgnoreCase("RNN")){
    			
    			if (getLogger().isDebugEnabled()){  
	    			getLogger().debug("[ingresarOperacionFirma] datosOpe.getProcesoNegocio() en REN");
	    			getLogger().debug("[ingresarOperacionFirma] datosOpe.toString()" + datosOpe.toString());
	    		}
    			
    			try{
    				ResultCambiaEstadoOperacionCreditoMultilinea resCam = null;
    				resCam = (ResultCambiaEstadoOperacionCreditoMultilinea) cambiaEstadoOperacionCreditoMultilinea(
    						datosOpe.getIdentificadorFirma(),
    						datosOpe.getIdConvenio(),
    						datosOpe.getRutEmpresa(),
    						datosOpe.getDigitoVerifEmp(),
    						"OKE");
    				datosResultado.setRespuesta(true);
    			}
    			catch (Exception e) {
    				if (getLogger().isEnabledFor(Level.ERROR)) getLogger().error(e.getClass() + "[ingresarOperacionFirma] error [cambiaEstadoOperacionCreditoMultilinea]: " + e.getMessage());
    				excepcionesArray.add(e.getMessage());
    				excepcionesArray.add("ERRORCAMBIAESTADOOPERACION");
    			}
    		
			    if (getLogger().isDebugEnabled()){  
				    getLogger().debug("[ingresarOperacionFirma] Antes de consultaOperacionCreditoMultilinea con...");
				    getLogger().debug("[ingresarOperacionFirma] datosOpe.getIdConvenio()." + datosOpe.getIdConvenio());
				    getLogger().debug("[ingresarOperacionFirma] datosOpe.getRutEmpresa()" + datosOpe.getRutEmpresa());
				    getLogger().debug("[ingresarOperacionFirma] datosOpe.getDigitoVerifEmp()." + datosOpe.getDigitoVerifEmp());
			    }
			    RetornoTipCli datosCliente = consultaAntecedentesGenerales(Integer.parseInt(datosOpe.getRutEmpresa()),datosOpe.getDigitoVerifEmp());
    			if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] consultaAntecedentesGenerales datosCliente" + datosCliente);
    			
    			String codBanca = ((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.TipoBca:datosCliente.DatEmpresa.TipoBca);
    			if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] consultaAntecedentesGenerales codBanca" + codBanca);
    			char plan = ((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.codigoPlan.charAt(0):datosCliente.DatEmpresa.codigoPlan.charAt(0));
    			if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] consultaAntecedentesGenerales plan" + plan);
    			String oficinaIngreso = ((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.CodOficina:datosCliente.DatEmpresa.CodOficina);
    			if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] consultaAntecedentesGenerales oficinaIngreso" + oficinaIngreso);
    			String codSegmento = ((datosCliente.DatEmpresa == null)?datosCliente.DatPersona.segmento.codigo:datosCliente.DatEmpresa.segmento.codigo);
    			if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] consultaAntecedentesGenerales codSegmento" + codSegmento);
			    
			    Hashtable datosLog = new Hashtable();
        		
			    if (getLogger().isDebugEnabled()){  
				    getLogger().debug("[ingresarOperacionFirma] operacionSeleccionadaCancelacion codSegmento");
				    getLogger().debug("[ingresarOperacionFirma] operacionSeleccionadaCancelacion datosOpe.getNumOperacionCan()" + datosOpe.getNumOperacionCan());
				    getLogger().debug("[ingresarOperacionFirma] operacionSeleccionadaCancelacion datosOpe.getCodAuxiliarCredito()" + datosOpe.getCodAuxiliarCredito());
				    getLogger().debug("[ingresarOperacionFirma] operacionSeleccionadaCancelacion datosOpe.getCodigoMoneda2()" + datosOpe.getCodigoMoneda2());
				    getLogger().debug("[ingresarOperacionFirma] operacionSeleccionadaCancelacion oficinaIngreso" + oficinaIngreso); 
			    }
			    ResultCalculoValoresCancelacion operacionSeleccionadaCancelacion = 
    					(ResultCalculoValoresCancelacion) consultaCancelacionMultilinea(multiEnvironment,
    							datosOpe.getNumOperacion(),
    					Integer.parseInt(datosOpe.getAuxiliarCredito()),
    					datosOpe.getCodigoMoneda2(),
    					oficinaIngreso,
    					datosLog);
        		String key = StringUtils.rightPad(datosOpe.getCodigoMoneda(), VALOR_6)  + datosOpe.getTipoOperacion().trim() + datosOpe.getAuxiliarOpe().trim();
    			if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] consultaAntecedentesGenerales key" +key);
    			int topeMora = Integer.parseInt(TablaValores.getValor("renmultilinea.parametros", key, "topeMora"));
    			if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] consultaAntecedentesGenerales topeMora" +topeMora);
    			Date fechaVencimiento2 = null;
    			if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] consultaAntecedentesGenerales datosOpe.getFechaExpiracion2()" +datosOpe.getFechaExpiracion2());
    			try {
					if(datosOpe.getFechaExpiracion2()!=null){
						SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyyMMdd");
						fechaVencimiento2 = sdfYMD.parse(datosOpe.getFechaExpiracion2());
					}
				} 
				catch (ParseException e) {
					if (getLogger().isEnabledFor(Level.ERROR)) getLogger().error("[ingresarOperacionFirma] Error parseando tipo de fecha de expiracion" +datosOpe.getFechaExpiracion2());
				}
    			
    			int codigoSubConceptoLength = VALOR_3;
    			int indCobroLength = 1; 
    			String glosaSeguros = datosOpe.getGlosaTipoCredito().trim();
    			int totalSeguros = glosaSeguros.length() / (codigoSubConceptoLength + indCobroLength);
    			String[] seguros = new String[totalSeguros];
    			String segurosObtenidos = "";
    			int contSegurosSeleccionados = 0;
    			
    			String  tipoCobroSegurosCodigo = ((String) TablaValores.getValor("multilinea.parametros", "tipoCobroSegurosCodigo" , "lista"));
				String  tipoCobroSegurosNumero = ((String) TablaValores.getValor("multilinea.parametros", "tipoCobroSegurosNumero" , "lista"));
				Vector tipoCobroSegurosCodigoV = buscaListaCodigos(tipoCobroSegurosCodigo, ",");
				Vector tipoCobroSegurosNumeroV = buscaListaCodigos(tipoCobroSegurosNumero, ",");
				
    			for (int i = 0; i < glosaSeguros.length(); i+=codigoSubConceptoLength + indCobroLength) {
    				int indCobroIndex = tipoCobroSegurosNumeroV.indexOf(glosaSeguros.substring(i+codigoSubConceptoLength, i+codigoSubConceptoLength+1));
    				if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] consultaAntecedentesGenerales indCobroIndex" +indCobroIndex);
    				String subConcepto = glosaSeguros.substring(i, i+codigoSubConceptoLength);
    				if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] consultaAntecedentesGenerales subConcepto" +subConcepto);
    				String indCobro = (String)tipoCobroSegurosCodigoV.get(indCobroIndex);
    				if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] consultaAntecedentesGenerales indCobro" +indCobro);
    				seguros[contSegurosSeleccionados] = subConcepto + indCobro;
    				contSegurosSeleccionados++;
    			}
    			
    			if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] consultaAntecedentesGenerales fechaVencimiento2" +fechaVencimiento2);
        		DatosParaCondicionesCreditoTO datosConCred = new DatosParaCondicionesCreditoTO();
				datosConCred.setIdRequerimiento("097"); 
				datosConCred.setCaiNumOpe(datosOpe.getNumOperacion());
				datosConCred.setIicNumOpe(Integer.parseInt(datosOpe.getAuxiliarCredito()));
				datosConCred.setOficinaIngreso(datosOpe.getOficinaIngreso().trim());
				datosConCred.setEjecutivo("");
				datosConCred.setMontoCredito(Double.parseDouble(datosOpe.getMontoCredito()));
				datosConCred.setTipoOperacion(datosOpe.getTipoOperacion().trim());
				datosConCred.setMoneda(datosOpe.getCodigoMoneda());
				datosConCred.setCodigoAuxiliar(datosOpe.getAuxiliarOpe().trim());
				datosConCred.setCtaAbono(Integer.parseInt(datosOpe.getCuentaAbono().trim()));
				datosConCred.setCtaCargo(Integer.parseInt(datosOpe.getCuentaCargo().trim()));
				datosConCred.setCodBanca(codBanca);			
				datosConCred.setPlan(plan);
				datosConCred.setRutDeudor(Integer.parseInt(datosOpe.getRutEmpresa().trim()));
				datosConCred.setDigitoVerificador(datosOpe.getDigitoVerifEmp());
				datosConCred.setFecVencimiento2(fechaVencimiento2);
				datosConCred.setWhoVisa(VALOR_2); 
				datosConCred.setTotalPagado(operacionSeleccionadaCancelacion.getTotalPagado());
				datosConCred.setCodSegmento(codSegmento);
				datosConCred.setSeguros(seguros);
				datosConCred.setCodMonedaOrig(datosOpe.getCodigoMoneda2());
				datosConCred.setTopeMora(topeMora);
				datosConCred.setFechaPrimerVcto(datosOpe.getFechaPrimerVencimiento());
				datosConCred.setRutUsuario(datosOpe.getRutUsuario());
				datosConCred.setDigitoVerificadorUsuario(datosOpe.getDvUsuario() != null ? datosOpe.getDvUsuario().charAt(0) : ' ');
				datosConCred.setConvenio(datosOpe.getIdConvenio());
				
			
				if (getLogger().isDebugEnabled()){  
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getIdRequerimiento() -> " + datosConCred.getIdRequerimiento() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getCaiNumOpe() -> " + datosConCred.getCaiNumOpe() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getIicNumOpe() -> " + datosConCred.getIicNumOpe() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getOficinaIngreso() -> " + datosConCred.getOficinaIngreso() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getEjecutivo() -> " + datosConCred.getEjecutivo() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getMontoCredito() -> " + datosConCred.getMontoCredito() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getTipoOperacion() -> " + datosConCred.getTipoOperacion() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getMoneda() -> " + datosConCred.getMoneda() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getCodigoAuxiliar() -> " + datosConCred.getCodigoAuxiliar() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getCtaAbono() -> " + datosConCred.getCtaAbono() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getCtaCargo() -> " + datosConCred.getCtaCargo() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getCodBanca() -> " + datosConCred.getCodBanca() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getPlan() -> " + datosConCred.getPlan() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getRutDeudor() -> " + datosConCred.getRutDeudor() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getDigitoVerificador() -> " + datosConCred.getDigitoVerificador() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getFecVencimiento2() -> " + datosConCred.getFecVencimiento2() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getWhoVisa() -> " + datosConCred.getWhoVisa() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getTotalPagado() -> " + datosConCred.getTotalPagado() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getCodSegmento() -> " + datosConCred.getCodSegmento() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getCodMonedaOrig() -> " + datosConCred.getCodMonedaOrig() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getTopeMora() -> " + datosConCred.getTopeMora() );
					getLogger().debug("[ingresarOperacionFirma] datosConCred.getFechaPrimerVcto() -> " + datosConCred.getFechaPrimerVcto() );
				}
				Date fechaPrimerVenc = null;
    			try {
    				SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyyMMdd");
    				fechaPrimerVenc = sdfYMD.parse(datosConCred.getFechaPrimerVcto());
				} 
				catch (ParseException e) {
					if (getLogger().isEnabledFor(Level.ERROR)) getLogger().error("[ingresarOperacionFirma] Error al parsear fecha: " + e);
				}
    			
				EstructuraVencimiento[] vencimientos = new EstructuraVencimiento[1];
                vencimientos[0] = new EstructuraVencimiento(0, ' ', 0, Integer.parseInt(datosOpe.getTotalVencimientos().trim()), fechaPrimerVenc, 0, 0, ' ', "", 0.0, 1, 'M', ' ');
                
                DatosResultadoOperacionesTO datosResultadoRen = null;
                if (getLogger().isDebugEnabled()){  
					getLogger().debug("[ingresarOperacionFirma] antes de [obtenerCondicionesRenovacion]");
                }
                datosResultadoRen = obtenerCondicionesRenovacion(
						multiEnvironment, 
						vencimientos, 
						datosConCred);
                if (getLogger().isDebugEnabled()){  
					getLogger().debug("[ingresarOperacionFirma] despues [obtenerCondicionesRenovacion]");
                }
				if (datosResultadoRen != null){
					datosResultado.setRespuesta(true);
					datosResultado.setNumOperacion(datosResultadoRen.getNumOperacion());
					datosResultado.setCaiOperacion(datosResultadoRen.getCaiOperacion());
					datosResultado.setIicOperacion(datosResultadoRen.getIicOperacion());
				}
				else {
					datosResultado.setRespuesta(false);
				}
    		}
    		if (getLogger().isDebugEnabled())  getLogger().debug("[ingresarOperacionFirma] En operaciones por firmar quedo OKE.");
    	} 
    	datosResultado.setExcepciones((String[]) excepcionesArray.toArray(new String[excepcionesArray.size()]));
    	return datosResultado;
    }
   
   
    
    /*******************************************************************************************************
     * activarDepositarAvance.
     *******************************************************************************************************/
    /**
     * Método que realiza la activación el deposito del avance.
     * <p>
     * Registro de Versiones
     * <ul>
     * <li>1.0 (10/04/2015 Manuel Escárate(BEE - Jimmy Muñoz D. (ing.Soft.BCI) ) ):   Version Inicial
     * <li>1.1 (01/03/2016 Manuel Escárate - BEE  - Felipe Ojeda . (ing.Soft.BCI) ):   Se agregan logs y excepciones.</li>   
     * <li>1.2 (10/05/2016 Manuel Escárate - BEE  - Pablo Paredes . (ing.Soft.BCI) ):   Se agrega excepción.</li>   
     * </ul>
     * </p>
     *
     * @param multiEnvironment multiambiente.
     * @param datosOpe datosde la operacion.
     * @return DatosResultadoOperacionesTO datos de resultado de operacion.
     * @throws MultilineaException  multilinea excepcion.
     * @throws EJBException ejb excepcion.
     * @throws RemoteException excepcion remota.
     * @since 3.0
     */
    public DatosResultadoOperacionesTO activarAvanceMultilinea(MultiEnvironment multiEnvironment,
    		DatosParaOperacionesFirmaTO datosOpe) 
    				throws MultilineaException, EJBException, RemoteException{
    	if (getLogger().isEnabledFor(Level.INFO)) {
    		getLogger().debug("[activarAvanceMultilinea]: [BCI_INI]");
    	}
    	DatosResultadoOperacionesTO datosResultado = new DatosResultadoOperacionesTO();
    	datosResultado.setRespuesta(false);
    	ArrayList excepcionesArray = new ArrayList();
    	if (getLogger().isEnabledFor(Level.INFO)) {
    		getLogger().debug("[activarAvanceMultilinea]: [condicionGaranti]" + datosOpe.getCondicionGarantia());
    	}
    	try {
    		if (getLogger().isDebugEnabled()){  
    			getLogger().debug("[activarAvanceMultilinea] antes [activaAvanceMultilinea]");
    		}
    		ResultActivacionDeOpcAlDia respuestaActivacion = activaAvanceMultilinea(multiEnvironment,
    				datosOpe.getNumOperacion(),Integer.parseInt(datosOpe.getAuxiliarCredito()),
    				datosOpe.getRutEmpresa(), 
    				String.valueOf(datosOpe.getDigitoVerifEmp()),
    				datosOpe.getCondicionGarantia(),
    				datosOpe.getQuienVisa(),
    				null);
    		if (getLogger().isDebugEnabled()){  
    			getLogger().debug("[activarAvanceMultilinea] despues [activaAvanceMultilinea]");
    		}
    		if (respuestaActivacion != null && respuestaActivacion.getCim_status() != null
    				&&  respuestaActivacion.getCim_status().equalsIgnoreCase("0")){
    			if (getLogger().isDebugEnabled()){  
    				getLogger().debug("[activarAvanceMultilinea] seteando respuesta de activacion multinea");
    			}
    			datosResultado.setRespuesta(true);
    		}
    	}
    	catch (Exception e){
    		if (getLogger().isEnabledFor(Level.ERROR)) {
    			getLogger().error("[activarAvanceMultilinea] Problemas al realizar la activación ");
    		}
    		excepcionesArray.add("ERRORACTIVAMULTILINEA");
    		excepcionesArray.add(e.getMessage());
    	}
    	if (getLogger().isEnabledFor(Level.INFO)) {
    		getLogger().debug("[activarAvanceMultilinea]: [BCI_FIN]");
    	}
    	datosResultado.setExcepciones((String[]) excepcionesArray.toArray(new String[excepcionesArray.size()]));
    	return datosResultado;
 
    }

   /*******************************************************************************************************
     * obtenerCreditosPorFirmar
     *******************************************************************************************************/
    /**
     * Obtiene los creditos para firmar.
     * <p>
     * Registro de Versiones
     * <ul>
     * <li>1.0 26/12/2014 Braulio Rivas S. (BEE)  - Jimmy Muñoz D. (ing.Soft.BCI) :   Version Inicial
     * <li>1.1 (01/03/2016 Manuel Escárate - BEE  - Felipe Ojeda . (ing.Soft.BCI) ):   Se agregan logs</li>    
     * </ul>
     * </p>
     *
     * @param multiEnvironment multiEnvironment.
     * @param identificacion  identificacion.
     * @param numeroCopia numero de copia.
     * @param rutEmpresa rut empresa.
     * @param digitoVerificador digito verificador.
     * @return lista de creditos por firmar.
     * @throws MultilineaException  multilinea excepcion.
     * @throws EJBException ejb excepcion.
     * @throws RemoteException excepcion remota.
     * @since 3.0
     */
    public ArrayList obtenerCreditosPorFirmar(MultiEnvironment multiEnvironment, String identificacion, String numeroCopia, 
    		String rutEmpresa, char digitoVerificador) throws MultilineaException, EJBException, RemoteException{
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[obtenerCreditosPorFirmar][" + rutEmpresa + "]Ingreso a obtenerCreditosPorFirmar");
		}

		List operacionesCredito = null;
        SimpleDateFormat ddMMyyyyHHmmssForm        = new SimpleDateFormat("yyyyMMdd");
    	if (getLogger().isDebugEnabled()){  
			getLogger().debug("[obtenerCreditosPorFirmar] antes de [consultaOperacionCreditoMultilinea]");
		}
    	ResultConsultaOperacionCreditoMultilinea result = consultaOperacionCreditoMultilinea(identificacion, numeroCopia, rutEmpresa, digitoVerificador);
    	if (getLogger().isDebugEnabled()) {
    		getLogger().debug("[obtenerCreditosPorFirmar] result : " + result);
    	}
    	if (result != null && result.getConsultaOperCredMultilinea() != null && result.getConsultaOperCredMultilinea().length > 0){
    		if (getLogger().isDebugEnabled()){  
    			getLogger().debug("[obtenerCreditosPorFirmar] resultado consultaOperacionCreditoMultilinea != null");
    		}
    		operacionesCredito = new ArrayList();
			int diasAtras = Integer.parseInt(TablaValores.getValor("multilinea.parametros", "operacionesFirmar", "diasAtras"));
	    	if (getLogger().isDebugEnabled()) {
	    		getLogger().debug("[obtenerCreditosPorFirmar] diasAtras: " + diasAtras);
	    	}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date()); 
			calendar.add(Calendar.DAY_OF_YEAR, diasAtras * -1);
			calendar.getTime();
	    	long fechaAtras = new Long(ddMMyyyyHHmmssForm.format( calendar.getTime())).longValue();
			
    		for (int i = 0; i < result.getConsultaOperCredMultilinea().length; i++) {
    			if ( result.getConsultaOperCredMultilinea()[i] != null){
    				String fechaInicio = result.getConsultaOperCredMultilinea()[i].getFechaInicio().substring(0,VALOR_8);
    				long fechaIni = new Long(fechaInicio).longValue();
    		    	if (getLogger().isDebugEnabled()) {
    		    		getLogger().debug("[obtenerCreditosPorFirmar] fechaIni: " + fechaIni+ " fechaAtras: " + fechaAtras);
    		    	}
    				
    		    	if (fechaIni >= fechaAtras){
    		    		if (getLogger().isDebugEnabled()) {
    			    		getLogger().debug("[obtenerCreditosPorFirmar] result.getConsultaOperCredMultilinea()[i].getNumOperacion(): " + result.getConsultaOperCredMultilinea()[i].getNumOperacion());
    			    		getLogger().debug("[obtenerCreditosPorFirmar] result.getConsultaOperCredMultilinea()[i].getAuxiliarCredito().trim()).intValue(): " + result.getConsultaOperCredMultilinea()[i].getAuxiliarCredito().trim());
    			    		getLogger().debug("[obtenerCreditosPorFirmar] result.getConsultaOperCredMultilinea()[i].getMontoCredito(): " + result.getConsultaOperCredMultilinea()[i].getMontoCredito());
    			    		getLogger().debug("[obtenerCreditosPorFirmar] result.getConsultaOperCredMultilinea()[i].getGlosaTipoCredito(): " + result.getConsultaOperCredMultilinea()[i].getGlosaTipoCredito());
    			    		getLogger().debug("[obtenerCreditosPorFirmar] result.getConsultaOperCredMultilinea()[i].getCtaAbono(): " + result.getConsultaOperCredMultilinea()[i].getCtaAbono());
    			    		getLogger().debug("[obtenerCreditosPorFirmar] result.getConsultaOperCredMultilinea()[i].getCtaAbono(): " + result.getConsultaOperCredMultilinea()[i].getCtaAbono());
    			    		getLogger().debug("[obtenerCreditosPorFirmar] result.getConsultaOperCredMultilinea()[i].getIdentificador(): " + result.getConsultaOperCredMultilinea()[i].getIdentificador());
    			    		getLogger().debug("[obtenerCreditosPorFirmar] result.getConsultaOperCredMultilinea()[i]: " + result.getConsultaOperCredMultilinea()[i].toString());
    			    	}
    		    		
	    				DatosOperacionTO datosOperacionTO = new DatosOperacionTO();
	    				try{
	    					datosOperacionTO.setCaiOperacion(result.getConsultaOperCredMultilinea()[i].getNumOperacion());
		    				datosOperacionTO.setIicOperacion(new Integer(result.getConsultaOperCredMultilinea()[i].getAuxiliarCredito().trim()).intValue());
		    				datosOperacionTO.setMontoOperacion(Double.parseDouble(result.getConsultaOperCredMultilinea()[i].getMontoCredito()));
		    				datosOperacionTO.setMontoCredito(result.getConsultaOperCredMultilinea()[i].getMontoCredito());
		    				datosOperacionTO.setTipoCredito(result.getConsultaOperCredMultilinea()[i].getGlosaTipoCredito().trim());
		    				datosOperacionTO.setCuentaAbono(result.getConsultaOperCredMultilinea()[i].getCtaAbono());
		    				datosOperacionTO.setCuentaCargo(result.getConsultaOperCredMultilinea()[i].getCtaCargo());
		    				datosOperacionTO.setProcesoNegocio(result.getConsultaOperCredMultilinea()[i].getProcesoNegocio());
		    				datosOperacionTO.setIdentificador(result.getConsultaOperCredMultilinea()[i].getIdentificador());
		    				datosOperacionTO.setTipoOperacion(result.getConsultaOperCredMultilinea()[i].getTipoOperacion());
		    				datosOperacionTO.setNumOperacionCan(result.getConsultaOperCredMultilinea()[i].getNumOperacionCan());
		    				datosOperacionTO.setCodAuxiliarCredito(result.getConsultaOperCredMultilinea()[i].getCodAuxiliarCredito());
		    				datosOperacionTO.setOficinaIngreso(result.getConsultaOperCredMultilinea()[i].getOficinaIngreso());
		    				datosOperacionTO.setTotalVencimientos(result.getConsultaOperCredMultilinea()[i].getTotalVencimientos());
		    				datosOperacionTO.setFechaPrimerVcto(result.getConsultaOperCredMultilinea()[i].getFechaPrimerVenc().substring(0,VALOR_8));
		    				datosOperacionTO.setFechaExpiracion2(result.getConsultaOperCredMultilinea()[i].getFecExpiracion());
		    				datosOperacionTO.setMoneda(result.getConsultaOperCredMultilinea()[i].getCodigoMoneda());
		    				datosOperacionTO.setCodMonedaOrigen(result.getConsultaOperCredMultilinea()[i].getCodigoMoneda2());
		    				datosOperacionTO.setAuxiliarOpe(result.getConsultaOperCredMultilinea()[i].getAuxiliarOpe().trim());
        			        	try {
								datosOperacionTO.setFechaCurse(ddMMyyyyHHmmssForm.parse(fechaInicio));
							} 
							catch (ParseException e) {
			    		    	if (getLogger().isEnabledFor(Level.ERROR)) {
			    		    		getLogger().error("[obtenerCreditosPorFirmar]Problemas al parsear fecha curse");
			    		    	}
							}
		    				operacionesCredito.add(datosOperacionTO);
	    				} 
						catch(Exception ex){
	    					if (getLogger().isEnabledFor(Level.ERROR)) {
		    		    		getLogger().error("[obtenerCreditosPorFirmar] Problemas al parsear datos para la oprecación " + result.getConsultaOperCredMultilinea()[i].getNumOperacion() );
		    		    	}
	    				}
	    				
    				}
    			}
    		}
    	}
    	
    	if (getLogger().isDebugEnabled())  getLogger().debug("[obtenerCreditosPorFirmar] Salio de obtenerCreditosPorFirmar");
    	return (ArrayList) operacionesCredito;
    }
    
    /*******************************************************************************************************
     * generarDashboard
     *******************************************************************************************************/
    /**
     * Obtiene la información crediticia a desplegar en el dashboard del cliente
     * <p>
     * Registro de Versiones
     * <ul>
     * <li>1.0 (25/12/2014      Eduardo Pérez(BEE)  - Jimmy Muñoz D. (ing.Soft.BCI) ):   Version Inicial.</li>
     * <li>1.1 (12/11/2015      Eduardo Pérez(BEE)  - Jimmy Muñoz D. (ing.Soft.BCI) ):   Se modifica lógica para mostrar semáforos.</li>
     * <li>1.2 (22/02/2016 		Hector Carranza - BEE  - Felipe Ojeda . (ing.Soft.BCI) ):   Se modifica lógica de selección de líneas.</li>
     * <li>1.3 (01/03/2016       Manuel Escárate - BEE  - Felipe Ojeda . (ing.Soft.BCI) ):   Se modifican logs. </li>   
     * </ul>
     * </p>
     *
     * @param multiEnvironment multiambiente.
     * @param rutCliente rut cliente.
     * @param dvCliente digito verificador cliente.
     * @return DatosParaDashboardTO dato para el dashboard.
     * @throws MultilineaException  multilinea excepcion.
     * @throws EJBException ejb excepcion.
     * @throws RemoteException excepcion remota.
     * @since 3.0
     *
     */
    public DatosParaDashboardTO generarDashboard(MultiEnvironment multiEnvironment, 
    		long rutCliente, char dvCliente) 
    		throws MultilineaException, EJBException, RemoteException{
    	if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] generarDashboard [BCI_INI]"); 
    	DatosParaDashboardTO retorno = null;
    	DatosParaAvanceTO datosCliente = null;
    	
    	try{
    		if (getLogger().isDebugEnabled()) {
        		getLogger().debug("[generarDashboard] llama a iniciar avance");
        	}
	    	datosCliente = this.iniciarAvance(multiEnvironment,rutCliente,dvCliente);
	    	retorno = new DatosParaDashboardTO();
	    	retorno.setRazonSocial(datosCliente.getRazonSocial());
	    	retorno.setNombreFantasia(datosCliente.getNombreFantasia());
	    	retorno.setOfertaMontoDisponible(datosCliente.getMontoDisponible());
	    	retorno.setOfertaVigencia(true);
	    	retorno.setOfertaActiva(true);
	    	retorno.setVisacion(false);
	    	
	    	//Se valida la vigencia de la oferta dependiendo de si existe o no un monto disponible
	    	if(datosCliente.getMontoDisponible() <= 0) {
	    		retorno.setOfertaVigencia(false);
	    		retorno.setOfertaActiva(false);
	    	}
	    	
	    	//*********************************************************************************************//    	
	    	//***** Se obtiene la condición de garantía
	    	//*********************************************************************************************//
	    	if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] antes ResultConsultaCln");
	        ResultConsultaCln obeanCln = new ResultConsultaCln();
	        InputConsultaCln ibeanCln = new InputConsultaCln("025",
	                                                      "",                       // nombreDeudor,
	                                                      (int) rutCliente,         // rutDeudor,
	                                                      dvCliente,        		// digitoVerificador,
	                                                      ' ',                      // indicadorExtIdc,
	                                                      "",                       // glosaExtIdc,
	                                                      "",                       // idOperacion,
	                                                      0,                        // numOperacion,
	                                                      0);                       // totLinIngreso);
	        if (getLogger().isDebugEnabled()) {
        		getLogger().debug("[generarDashboard] antes de consultaCln");
        	}
	        consultaCln(multiEnvironment, ibeanCln, obeanCln);
	        Linea[] consultaLinea = obeanCln.getLineas();
	
	        boolean tieneAval = false;
	        boolean tieneFoga = false;
	        boolean tieneCual = false;
	        boolean tieneMlt  = false;
	        boolean soloG     = false;
	        String codTipoGarantia = "";
	
	        if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] consultaLinea.length [" + consultaLinea.length + "]");
	        for (int i = 0; i < consultaLinea.length; i++) {
	        	if (getLogger().isDebugEnabled()) {
	        		getLogger().debug("[generarDashboard] recorriendo consultaLinea");
	        	}
	            if (consultaLinea[i] == null) {
	                break;
	            }
	            String tipLin = consultaLinea[i].getCodigoTipoLinea();
	            String tipoLineaOcupada = definirLDC(multiEnvironment, consultaLinea);
	            if (tipLin.equals(tipoLineaOcupada)) { //verifica si tipo de linea es multilinea
	                soloG      = consultaLinea[i].getCodigoTipoInfo() == 'G' ? true : false;
	                tieneMlt   = true;
	                tieneAval  = (consultaLinea[i].getTipoOperacion()).equals("AVL") ? true : tieneAval;
	                tieneFoga  = (consultaLinea[i].getTipoOperacion()).equals("952") ? ((consultaLinea[i].getCodAuxiliarLinea()).trim().equals("101") ? true : tieneFoga) : tieneFoga;
	                tieneCual  = soloG ? (!(consultaLinea[i].getTipoOperacion()).trim().equals("") ? true : tieneCual) : tieneCual;
	                if(tieneCual) codTipoGarantia = (consultaLinea[i].getTipoOperacion());
	            }
	        }
	        if (!tieneMlt) {
	            if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] Cliente " + rutCliente + "-" + dvCliente + " No posee Multilinea (MLT)");
	            retorno.setOfertaActiva(false);
	        }
	
	        //  condicionGar -->
	        //  ("4  ";  //sin garantia default)
	        //  ("8  ";  //otras garantias)
	        //  ("2  ";  //aval);
	        //  ("10 ";  //fogape);
	        //
	        String condicionGar   = tieneAval ? "2  " : (tieneFoga ? "10 " : (tieneCual ? "8  " : "4  "));
	        retorno.setCondicionGarantia(condicionGar);
	        retorno.setCodTipoGarantia(codTipoGarantia);
	
	        if (getLogger().isDebugEnabled()){  
		        getLogger().debug("[generarDashboard] condicionGar   ["+ condicionGar +"]");
		        getLogger().debug("[generarDashboard] Despues ResultConsultaCln obeanCln");
	        }
	        boolean tieneAvales = condicionGar.equals("2  ") ? true : false;
	        retorno.setTieneAvales(tieneAvales);
	        List arregloAvales = new ArrayList();
	        if (tieneAvales){
	            if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] tieneAvales [true]    Cliente[" + rutCliente + "-" + dvCliente + "]");
	            try{
		            ResultConsultaAvales obeanAval = new ResultConsultaAvales();
		            InputConsultaAvales abean = new InputConsultaAvales("026",
		            											(int) rutCliente,
		                                                                dvCliente,
		                                                                ' ',
		                                                                " ",
		                                                                " ",
		                                                                " ",
		                                                                0,
		                                                                0,
		                                                                "AVL",
		                                                                "AVC");
		            if (getLogger().isDebugEnabled()){  
				        getLogger().debug("[generarDashboard] antes de consultar avales");
		            }
		            consultaAvales(multiEnvironment, abean, obeanAval);
		            if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] despues consultaAvales");
		            Aval[] avales    = obeanAval.getAvales();
		            for (int i = 0; i < avales.length; i++) {
		            	if (getLogger().isDebugEnabled()) {
							getLogger().debug("[generarDashboard] recorriendo consulta avales" + i);
						}
		                char   dvfAval = avales[i].getDigitoVerificaAval();
		                char   indvige = avales[i].getVigente();
		                if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] Esta vigente el rut "+ avales[i].getRutAval() +" ?    vigente["+ indvige +"]");
		                if (indvige == 'S'){
		                	if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] Agrega aval "+ avales[i]);
		                   arregloAvales.add(avales[i]);
		                   if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] Después de agregar aval "+ avales[i]);
		                }
		            }
		            Aval[] avalesCli = new Aval[arregloAvales.size()];
		            arregloAvales.toArray(avalesCli);
		            retorno.setAvales(avalesCli);
		            
		            //Visación de avales
		            String respuestaVisacion = "";
		            int numeroAvalesVisar = Integer.parseInt(TablaValores.getValor("multilinea.parametros", "numeroAvalesVisar", "DESC"));
		            if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] avales.length ["+ avales.length +"]");
		            int visados = 0;
		            
		            for (int i = 0; i < avales.length; i++) {
                        if (visados >= numeroAvalesVisar)
                        	break;
		            	String rutAval = Integer.toString(avales[i].getRutAval());
		            	char   dvfAval = avales[i].getDigitoVerificaAval();
                        char   indvige = avales[i].getVigente();
                        if (getLogger().isDebugEnabled()){  
	                        getLogger().debug("[generarDashboard] Visando aval ["+ rutAval +"] - ["+dvfAval+"]");
	                        getLogger().debug("[generarDashboard] esta vigente el rut "+ rutAval +" ? ["+ indvige +"]");
	                    }
                        if (indvige == 'S'){
                        		respuestaVisacion = 
                        				visacionRut(rutAval, dvfAval, multiEnvironment.getCanal(), "EMP");
                        		if ((respuestaVisacion == null) 
								    || 	(respuestaVisacion != null && !respuestaVisacion.trim().equals("AP"))){
                        				if (getLogger().isDebugEnabled())  getLogger().debug("Visacion NOK:" + respuestaVisacion);
                        				throw new MultilineaException("ESPECIAL", "ERRVISACION"+respuestaVisacion);
                        		}
                        		visados++;
                        }
		            }
		            if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] visacion de avales OK");
		            retorno.setVisaAvalesOK(true);
		            
	            } 
				catch(Exception e) {
	            	if (getLogger().isEnabledFor(Level.ERROR)) getLogger().error("[generarDashboard]  ERROR [consultaAvales] Cliente[" + rutCliente + "-" + dvCliente + "]    Exception [" + e.getMessage() + "]");
	            	 Aval[] avalesCli = new Aval[0];
			         arregloAvales.toArray(avalesCli);
			         retorno.setAvales(avalesCli);
				}
	         }
			else{
	            if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] tieneAvales [false]   Cliente[" + rutCliente + "-" + dvCliente + "]");
	        }
	
	    	//Se realiza la visación de la empresa        
	        String respuestaVisacion = null;
	        if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] visando Empresa [" + rutCliente + "-" + dvCliente + "]");
	        respuestaVisacion = visacionRut(String.valueOf(rutCliente), dvCliente, multiEnvironment.getCanal(), "EMP");
	        if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] Empresa Visada [" + rutCliente + "-" + dvCliente + "]");
	        if ((respuestaVisacion == null) || (respuestaVisacion != null && !respuestaVisacion.trim().equals("AP"))){
	        	if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] Visacion NOK:" + respuestaVisacion);
	        }
			else{
				retorno.setVisacion(true);
	        	if (getLogger().isDebugEnabled())  getLogger().debug("[generarDashboard] visacion Empresa [" + rutCliente + "-" + dvCliente + "]    OK !!!");
	       }
	        
    	}
    	catch (Exception e) {
    		if (getLogger().isEnabledFor(Level.ERROR)) getLogger().error("[generarDashboard] Exception " + e.getMessage());
    		throw new MultilineaException("ESPECIAL", e.getMessage());
    	} 
    	return retorno;
    }
    
	/*******************************************************************************************************
     * iniciarRenovacion
     *******************************************************************************************************/
    /**
     * Inicia la renovacion de un credito.
     * <p>
     * Registro de Versiones
     * <ul>
     * <li>1.0 (26/03/2015 Eduardo Pérez G.(BEE) - Jimmy Muñoz D. (ing.Soft.BCI) ):   Version Inicial
     * <li>1.1 (01/03/2016 Manuel Escárate (BEE)  - Felipe Ojeda . (ing.Soft.BCI) ):   Se modifican logs. </li>  
     * </ul>
     * </p>
     *
     * @param multiEnvironment multiambiente.
     * @param rut Rut.
     * @param dv Digito verificador.
     * @return DatosParaRenovacionTO datos para la renovación.
     * @throws MultilineaException  multilinea excepcion.
     * @throws EJBException ejb excepcion.
     * @throws RemoteException excepcion remota.
     * @since 3.0
     *
     */     
    public DatosParaRenovacionTO iniciarRenovacion(MultiEnvironment multiEnvironment,long rut,char dv)
    		throws MultilineaException, EJBException,  RemoteException {
    	if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion] En iniciarRenovacion [BCI_INI]");
    	TablaValores tablaCreditos   = new TablaValores();
        Hashtable    htTablaCreditos = tablaCreditos.getTabla("renmultilinea.parametros");
        Hashtable datosLog = new Hashtable();
    	DatosParaRenovacionTO datosRenovacionTO = null;
    	RetornoTipCli datosCliente = null;
    	List operaciones = new ArrayList();
    	try {
    		if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion] voy a consultaAntecedentesGenerales");
    		datosCliente = consultaAntecedentesGenerales(rut,dv);
    		if (datosCliente!= null){
    			if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion] es diferente de null datos cliente");
    			datosRenovacionTO = new DatosParaRenovacionTO();
    			if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion] Antes Cargado codBanca");
    			String codBanca = (datosCliente.DatEmpresa == null)
				     ?	datosCliente.DatPersona.TipoBca:datosCliente.DatEmpresa.TipoBca;
    			if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion] Cargado codBanca" + codBanca);
    			    			
    			if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion] obtenerOperacionesARenovar(htTablaCreditos,codBanca)");
    			datosLog.put("operacionesARenovar", obtenerOperacionesARenovar(htTablaCreditos,codBanca));
    			if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion] obtenerOperacionesARenovar(htTablaCreditos,codBanca)" + datosLog);
            	Vector creditos = (Vector) consultaOperacionesParaRenovar(multiEnvironment, (int)rut, dv, datosLog);
            	if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion] creditos" + creditos);
            	for(int x=0;x<creditos.size();x++){
            		
            		if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion][Dentro del For!!!!!]");
                    OperacionCreditoSuperAmp operacion = (OperacionCreditoSuperAmp)creditos.get(x);
                    if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion][Después de traer las operacion]");
                    if(operacion!=null){
                    	String key = StringUtils.rightPad(operacion.getCodMonedaCred(), LARGO_CODIGO_MONEDA)  + operacion.getCodigo10();
                    	if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion][key] " + key);
                    	int topeMora = Integer.parseInt(TablaValores.getValor("renmultilinea.parametros", key, "topeMora"));
                	    int diasantes = Integer.parseInt(TablaValores.getValor("renmultilinea.parametros", key, "diasantes"));
                	    if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion][topeMora] " + topeMora);
                	    if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion][diasantes] " + diasantes);
                	    Date today = new Date();
                	    int diasEnMora = (int) ((today.getTime() - operacion.getFecVencimiento2().getTime())/(VALOR_1000*VALOR_60*VALOR_60*VALOR_24));
                	    if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion][diasEnMora] " + diasEnMora);
                	    if ((diasEnMora <= topeMora) && ( ((-1*diasEnMora) < diasantes ))) {
                	    	if (getLogger().isDebugEnabled())  getLogger().debug("[iniciarRenovacion][operacion agregada] " + operacion.getNumOperacion());
                	    	operaciones.add(operacion);
                        }
            		}
            	}
            	datosRenovacionTO.setOperaciones((OperacionCreditoSuperAmp[]) operaciones.toArray(new OperacionCreditoSuperAmp[0]));
    		} 
    	}
    	catch (Exception e) {
    		if (getLogger().isEnabledFor(Level.ERROR)) getLogger().error("[iniciarRenovacion] Exception " + e.getMessage());
    		throw new MultilineaException("ESPECIAL", e.getMessage());
    	}
    	if (getLogger().isDebugEnabled())  getLogger().debug("Saliendo iniciarRenovacion");
    	return datosRenovacionTO;
    
    }
    
	/**
	 * Método que permite obtener una instancia de Logger de la clase.
	 *
	 * <p>
	 * Registro de versiones:<ul>
	 * <li>1.0 28/04/2015, Braulio Rivas S. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión inicial. </li>
	 * </ul>
	 * </p>
	 * @return con la instancia del Logger.
	 * @since 3.0
	 */
	public Logger getLogger() {
		if (logger == null){
			logger = Logger.getLogger(this.getClass());
		}
		return logger;
	}
	
	/*******************************************************************************************************
     * obtenerCondicionesRenovacion
     *******************************************************************************************************/
    /**
     * Obtiene los datos a desplegar en las condiciones del crédito.
     * <p>
     * Registro de Versiones
     * <ul>
     * <li>1.0 (26/03/2015 Eduardo Pérez G.(BEE) - Jimmy Muñoz D. (ing.Soft.BCI) ):   Version Inicial
     * <li>1.1 (01/03/2016 Manuel Escárate (BEE)  - Felipe Ojeda . (ing.Soft.BCI) ):   Se modifican logs. </li>  
     * <li>1.2 (10/05/2016 Manuel Escárate (BEE)  - Pablo Paredes . (ing.Soft.BCI) ):   Se agrega seteo a objeto de seguros. </li>  
     * <li>1.3 (29/07/2016 Manuel Escárate - BEE  - Felipe Ojeda . (ing.Soft.BCI) ):   Se agrega seteo para valor de gasto.</li>
     * </ul>
     * </p>
     *
     * @param multiEnvironment multiambiente.
     * @param vencimientos  Estructura de vencimientos.
     * @param datosCondCred datos condiciones credito.
     * @return DatosPaaraCondicionesCredito datos para la renovación.
     * @throws MultilineaException  multilinea excepcion.
     * @throws EJBException ejb excepcion.
     * @throws RemoteException excepcion remota.
     * @since 3.0
     *
     */  
	public DatosResultadoOperacionesTO obtenerCondicionesRenovacion(MultiEnvironment multiEnvironment,
	          EstructuraVencimiento[] vencimientos,DatosParaCondicionesCreditoTO datosCondCred
	          ) throws MultilineaException, EJBException, RemoteException{
	     
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("[obtenerCondicionesRenovacion][BCI_INI]");
		}
		 
		DatosResultadoOperacionesTO datosCondicionesCredito = null;
	    ResultRenovacionMultilinea resultadoRenovacion = null;
	    Hashtable datosLog = new Hashtable();
	    ArrayList excepcionesArray = new ArrayList();
	    
	    try{
		    datosLog.put("idConvenio",      datosCondCred.getConvenio().trim());
	        datosLog.put("rutEmpresa",      new Integer(datosCondCred.getRutDeudor()));
	        datosLog.put("digitoVerifEmp",  String.valueOf(datosCondCred.getDigitoVerificador()));
	        datosLog.put("rutUsuario",      datosCondCred.getRutUsuario());
	        datosLog.put("digitoVerifUsu",  String.valueOf(datosCondCred.getDigitoVerificadorUsuario()));
	        datosLog.put("tipoOpe",         datosCondCred.getTipoOperacion());
	    }
		catch(Exception e){
	    	if (getLogger().isEnabledFor(Level.ERROR)){
	    			getLogger().error("[obtenerCondicionesRenovacion][datosCondCred.getConvenio()]" + datosCondCred.getConvenio());
	    			getLogger().error("[obtenerCondicionesRenovacion][datosCondCred.getRutDeudor()]" + datosCondCred.getRutDeudor());
	    			getLogger().error("[obtenerCondicionesRenovacion][datosCondCred.getDigitoVerificador()]" + datosCondCred.getDigitoVerificador());
	    			getLogger().error("[obtenerCondicionesRenovacion][datosCondCred.getRutUsuario()]" + datosCondCred.getRutUsuario());
	    			getLogger().error("[obtenerCondicionesRenovacion][datosCondCred.getDigitoVerificadorUsuario()]" + datosCondCred.getDigitoVerificadorUsuario());
	    			getLogger().error("[obtenerCondicionesRenovacion][datosCondCred.getTipoOperacion()]" + datosCondCred.getTipoOperacion());
			}
	    }
	    
	    Date today = new Date();
	    int diasEnMora = 0;
	    if (datosCondCred.getFecVencimiento2() != null){
	    	 diasEnMora = (int)((today.getTime() - datosCondCred.getFecVencimiento2().getTime()) / (VALOR_1000*VALOR_60*VALOR_60*VALOR_24));
	    }
	    
	    if (getLogger().isDebugEnabled()) {
			getLogger().debug("[obtenerCondicionesRenovacion][Cálculo diasEnMora -> ]" + diasEnMora);
		}
	    
	    datosLog.put("codMonedaOrigen" , datosCondCred.getCodMonedaOrig());
	    
	    try{
	    	
	    	if (getLogger().isDebugEnabled()) {
				getLogger().debug("[obtenerCondicionesRenovacion][llamado a renovacionMultilinea]");
			}
	    	resultadoRenovacion = renovacionMultilinea(
	                    multiEnvironment, 
	                    datosCondCred.getIdRequerimiento(),
	                    datosCondCred.getCaiNumOpe(), 
	                    datosCondCred.getIicNumOpe(),
	                    datosCondCred.getOficinaIngreso(),
	                    datosCondCred.getEjecutivo(),
	                    datosCondCred.getMontoCredito(),
	                    datosCondCred.getTipoOperacion(), //*
	                    datosCondCred.getMoneda(), 
	                    datosCondCred.getCodigoAuxiliar(), 
	                    datosCondCred.getOficinaIngreso(),
	                    datosCondCred.getCtaAbono(), 
	                    datosCondCred.getCtaCargo(),
	                    vencimientos,
	                    datosCondCred.getCodBanca(),
	                    datosCondCred.getPlan(),
	                    datosCondCred.getRutDeudor(), 
	                    datosCondCred.getDigitoVerificador(), 
	                    diasEnMora,
	                    datosCondCred.getTopeMora(),
	                    datosCondCred.getWhoVisa(), 
	                    datosCondCred.getTotalPagado(),
	                    datosLog,
	                    datosCondCred.getCodSegmento(),
	                    datosCondCred.getSeguros());
	                    
	    	if (getLogger().isDebugEnabled()) {
	    		getLogger().debug("[obtenerCondicionesRenovacion][resultadoRenovacion]" + resultadoRenovacion);
	    	}
	    	if (resultadoRenovacion != null){
	    		if (getLogger().isDebugEnabled()) {
		    		getLogger().debug("[obtenerCondicionesRenovacion][resultadoRenovacion] != null");
		    		getLogger().debug("[obtenerCondicionesCredito] gasto notarial: " + resultadoRenovacion.getValorGastoNotarial()); 
		    	}
	    		datosCondicionesCredito = new DatosResultadoOperacionesTO();
	            CalendarioPago[] calendario = resultadoRenovacion.getResultLiqOpc().getCalendarioPago();
	            datosCondicionesCredito.setValorCuota(calendario[0].getCuota());
	            datosCondicionesCredito.setTasaInteresInternet(resultadoRenovacion.getTasa());
	            datosCondicionesCredito.setImpuestos(resultadoRenovacion.getResultLiqOpc().getImpuesto());
	            datosCondicionesCredito.setIntereses(resultadoRenovacion.getResultLiqOpc().getIntereses());
	            datosCondicionesCredito.setCaiOperacion(resultadoRenovacion.getResultLiqOpc().getCaiOperacion());
	            datosCondicionesCredito.setIicOperacion(resultadoRenovacion.getResultLiqOpc().getIicOperacion());
	            datosCondicionesCredito.setFechaCurse(resultadoRenovacion.getFechaCurse());
	            datosCondicionesCredito.setCalendario(calendario);
	            datosCondicionesCredito.setValorGastoNotarial(resultadoRenovacion.getValorGastoNotarial());
	            double  costoFinalCredito = 0;
	            for (int i = 0; i < resultadoRenovacion.getResultLiqOpc().getCalendarioPago().length; i++) {
	            	if (resultadoRenovacion.getResultLiqOpc().getCalendarioPago()[i] !=null 
	            			&& resultadoRenovacion.getResultLiqOpc().getCalendarioPago()[i].getNumVencimiento() != 0){
	                        costoFinalCredito = costoFinalCredito 
	                        + resultadoRenovacion.getResultLiqOpc().getCalendarioPago()[i].getCuota();
	            	}
	            }
	            if (getLogger().isDebugEnabled()) {
		    		getLogger().debug("[obtenerCondicionesRenovacion][termine recorrido resultadoRenovacionLiqOpc]");
		    	}
	            datosCondicionesCredito.setCostoFinalCredito(costoFinalCredito);
	            datosCondicionesCredito.setCostoTotalSeguros(resultadoRenovacion.getResultLiqOpc().getValorSeguro());
	            datosCondicionesCredito.setCodigoMoneda(resultadoRenovacion.getResultLiqOpc().getCodigoMoneda());
	            
	            ResultLiquidacionDeOperacionDeCreditoOpc rlopc = (ResultLiquidacionDeOperacionDeCreditoOpc) resultadoRenovacion.getResultLiqOpc();
	            if (rlopc != null){
					double montoTotalCredito      = rlopc.getMontoCredito();
					int    numeroCuotas           = datosCondCred.getCuotaSeleccionada();
					
					double  interesEfectivo        = resultadoRenovacion.getTasa();
					Date fechaPrimerVencimiento = null;
					try {
					     fechaPrimerVencimiento = ddMMyyyy_form.parse(datosCondCred.getFechaPrimerVcto());
					}
					catch (ParseException e) {
						if (getLogger().isEnabledFor(Level.ERROR)) getLogger().error("Error al parsear fecha: " + e);
					}
					double gastosImpuesto         = rlopc.getImpuesto();
					double gastosDesgravamen      = rlopc.getValorSeguro();
					double gastosNotario          = rlopc.getValorGasto();
					double factorCaeObtenido      = obtieneFactorCAEPorcentaje(
	                              montoTotalCredito, numeroCuotas, interesEfectivo, fechaPrimerVencimiento,
	                              gastosImpuesto, gastosDesgravamen, gastosNotario, null);
					datosCondicionesCredito.setFactorCae(factorCaeObtenido);
				    datosCondicionesCredito.setCalendario(calendario);
	            }
	                if (resultadoRenovacion.getArregloROC() != null){
	                	if (getLogger().isDebugEnabled()) {
	     		    		getLogger().debug("[obtenerCondicionesRenovacion][arregloRoc distinto null]");
	     		    	}
	                    DatosSegurosTO[] datosSeguro = new DatosSegurosTO[resultadoRenovacion.getArregloROC().length];
	                    int numSeguros = 0;
	                    int numSegurosObl = 0;
	                    int numSegurosSel = 0;
	                    int requiereDPS = 0;
	                    for (int i = 0; i < resultadoRenovacion.getArregloROC().length; i++) {
	                         datosSeguro[i]  = new DatosSegurosTO();
	                    	datosSeguro[i].setIdSeguro(i);
	                         datosSeguro[i].setGlosaTipoSeguro(resultadoRenovacion.getArregloROC()[i].getGlosaTipoSeguro());
	                         datosSeguro[i].setMontoSeguro(resultadoRenovacion.getArregloROC()[i].getTasaMontoFinal());
	                         datosSeguro[i].setCodClfRentabilidadRcc(resultadoRenovacion.getArregloROC()[i].getCodClfRentabilidadRcc());
	                         datosSeguro[i].setCodigoSubConcepto(resultadoRenovacion.getArregloROC()[i].getCodigoSubConcepto());
	                         datosSeguro[i].setIndCobro(resultadoRenovacion.getArregloROC()[i].getIndCobro());
	                         datosSeguro[i].setIndVigencia(resultadoRenovacion.getArregloROC()[i].getIndVigencia());
	                         datosSeguro[i].setNumClienteRcc(resultadoRenovacion.getArregloROC()[i].getNumClienteRcc());
	                         datosSeguro[i].setVrfClienteRcc(resultadoRenovacion.getArregloROC()[i].getVrfClienteRcc());
	                         datosSeguro[i].setCodigoConcepto(resultadoRenovacion.getArregloROC()[i].getCodigoConcepto());
	                         datosSeguro[i].setCodFactorRiesgoRcc(resultadoRenovacion.getArregloROC()[i].getCodFactorRiesgoRcc());
	                         datosSeguro[i].setIndTipoPlantilla(resultadoRenovacion.getArregloROC()[i].getIndTipoPlantilla());
	                         datosSeguro[i].setIndSegObligatorio(resultadoRenovacion.getArregloROC()[i].getIndSegObligatorio());
	                         resultadoRenovacion.getArregloROC()[i].getTasaMontoFinal();
	                         if (resultadoRenovacion.getArregloROC()[i].getIndSegObligatorio() == 'S'){
	                              datosSeguro[i].setDeshabilitado(true);
	                         }
	                         if (resultadoRenovacion.getArregloROC()[i].getIndVigencia() == 'S'){
	                              datosSeguro[i].setCheckeado(true);
	                         }
	                    	if (resultadoRenovacion.getArregloROC()[i].getCodigoConcepto().equalsIgnoreCase("SGS")){
	                              numSeguros = numSeguros +1;
	                         }
	                         if (resultadoRenovacion.getArregloROC()[i].getCodigoConcepto().equalsIgnoreCase("SGS")
      							 &&  resultadoRenovacion.getArregloROC()[i].getIndSegObligatorio() == VALOR_83){
	                              numSegurosObl = numSegurosObl +1;
	                         }
	                         if (resultadoRenovacion.getArregloROC()[i].getCodigoConcepto().equalsIgnoreCase("SGS")
       							 && resultadoRenovacion.getArregloROC()[i].getIndVigencia() == VALOR_83){
	                              numSegurosSel = numSegurosSel +1;
	                         }
	                    	if (resultadoRenovacion.getArregloROC()[i].getCodFactorRiesgoRcc().equals("DPS")){
	                              if (resultadoRenovacion.getArregloROC()[i].getIndVigencia() == 'S'){
	                                   requiereDPS++;
	                    			if (resultadoRenovacion.getArregloROC()[i].getCodClfRentabilidadRcc().equals("IND")){
	                                        datosSeguro[i].setRutAsegurado(String.valueOf(resultadoRenovacion.getArregloROC()[i].getRutCliente()));
	                                        datosSeguro[i].setDigAsegurado(String.valueOf(
	                                                  resultadoRenovacion.getArregloROC()[i].getDigitoVerificador()));
	                                   }
	                    			else if (!resultadoRenovacion.getArregloROC()[i].getCodClfRentabilidadRcc().equals("IND")){
	                                        datosSeguro[i].setRutAsegurado(String.valueOf(datosCondCred.getRutDeudor()));
	                                        datosSeguro[i].setDigAsegurado(String.valueOf(datosCondCred.getDigitoVerificador()));
	                                   }
	                              }
	                              datosSeguro[i].setRequiereDPS(true);
	                         }
	                         }
	                    datosCondicionesCredito.setRequiereDPS(requiereDPS);
	                    datosCondicionesCredito.setNumSeguros(numSeguros);
	                    datosCondicionesCredito.setNumSegurosObl(numSegurosObl);
	                    datosCondicionesCredito.setNumSegurosSel(numSegurosSel);
	                    datosCondicionesCredito.setDatosSeguros(datosSeguro);
	                }
	                else {
	                    if (getLogger().isDebugEnabled())  getLogger().debug("seguros es null");
	                }
	          }
	     }
	     catch (Exception e) {
	    	 if (getLogger().isEnabledFor(Level.ERROR)) getLogger().error("[obtenerCondicionesCredito] Exception " + e.getMessage());
	          throw new MultilineaException("ESPECIAL", e.getMessage());
	     } 
	     return datosCondicionesCredito;
	    }
	
	
	/*******************************************************************************************************
	 * llenaVectorPlantillaProducto
	 *******************************************************************************************************/
	/**
	 * llena Vector de Plantilla de Producto.
	 * <p>
	 * Registro de Versiones
	 * <ul>
	 * <li>1.0 (22/06/2015 Manuel Escárate - BEE - Jimmy Muñoz D. (ing.Soft.BCI)): Version Inicial
	 *
	 * </ul>
	 * </p>
	 *
	 * @param archivo contiene la data de la plantilla de productos.
	 * @param multiEnvironment multiEnvironment.
	 * @param fechaVencimientoMLT fecha vencimiento de la Multilinea.
	 * @return Vector con productos.
	 * @since 1.0
	 *
	 */
	private  Vector llenaVectorPlantillaProducto(String archivo,
			MultiEnvironment multiEnvironment, String fechaVencimientoMLT) {

		Vector vectorPOC = new Vector();
		PlantillaOperacionesCredito poc = null;
		Date fechaCurse = ManejoEvc.fechaHabil(new Date());
		Date fechaCurseMaximo = null;
		Date fechaCuotasMaximo = null;
		Date fechaCuotasMinimo = null;
		Calendar fechaDespuesVcto = null;
		Calendar fechaFinalMaxima = null;
		Calendar fechabaseCalcCredito = null;
		Calendar fechaintPeriodosLargos = null;
		String unidadPerMaxPrimCap = "";
		String unidadPerMinPrimCap = "";
		int perMaxPrimPagoCap = 0;
		int perMinPrimPagoCap = 0;
		String unidadPerMaxVcto = "";
		int perMaxPrimVcto = 0;
		String unidadPerMinVcto = "";
		int perMinPrimVcto = 0;
		String htelem = "";
		String moneda = "";
		String tipoOperacion = "";
		String codigoAuxiliar = "";
		Double creditoMinimo = null;
		Double creditoMaximo = null;
		int cantMinVencim = 0;
		int cantMaxVencim = 0;
		int perMinPrimPagoInt = 0;
		String unidadPerMinPrimInt = "";
		int perMaxPrimPagoInt = 0;
		String unidadPerMaxPrimInt = "";
		int periodoRepacto = 0;

		TablaValores tablaCreditos = new TablaValores();
		Hashtable htTablaCreditos = tablaCreditos.getTabla(archivo);
		Enumeration codigos = htTablaCreditos.keys();
		Object elem = codigos.nextElement();
		
		Hashtable htDesc = (Hashtable) htTablaCreditos.get(elem);

		for (codigos = htTablaCreditos.keys(); codigos.hasMoreElements();) {
			poc = new PlantillaOperacionesCredito();
			htelem = (String) codigos.nextElement();
			moneda = htelem.substring(0, VALOR_5).trim();
			tipoOperacion = htelem.substring(VALOR_6, VALOR_9).trim();
			codigoAuxiliar = htelem.substring(VALOR_9).trim();
			htDesc = (Hashtable) htTablaCreditos.get(htelem);

			perMaxPrimPagoCap = Integer.parseInt(((String) htDesc
					.get("perMaxPrimPagoCap".toUpperCase())).trim());
			perMinPrimPagoCap = Integer.parseInt(((String) htDesc
					.get("perMinPrimPagoCap".toUpperCase())).trim());
			unidadPerMaxVcto = ((String) htDesc.get("unidadPerMaxVcto"
					.toUpperCase())).trim();
			perMaxPrimVcto = Integer.parseInt(((String) htDesc
					.get("perMaxPrimVcto".toUpperCase())).trim());
			unidadPerMinVcto = ((String) htDesc.get("unidadPerMinVcto"
					.toUpperCase())).trim();
			perMinPrimVcto = Integer.parseInt(((String) htDesc
					.get("perMinPrimVcto".toUpperCase())).trim());
			perMinPrimPagoInt = Integer.parseInt(((String) htDesc
					.get("perMinPrimPagoInt".toUpperCase())).trim());
			unidadPerMinPrimInt = ((String) htDesc.get("unidadPerMinPrimInt"
					.toUpperCase())).trim();
			perMaxPrimPagoInt = Integer.parseInt(((String) htDesc
					.get("perMaxPrimPagoInt".toUpperCase())).trim());
			unidadPerMaxPrimInt = ((String) htDesc.get("unidadPerMaxPrimInt"
					.toUpperCase())).trim();
			fechaFinalMaxima = new GregorianCalendar(
					multiEnvironment.getLocale());
			fechaFinalMaxima.setTime(fechaCurse);
			fechabaseCalcCredito = new GregorianCalendar(
					multiEnvironment.getLocale());
			fechabaseCalcCredito.setTime(fechaCurse);
			fechaintPeriodosLargos = new GregorianCalendar(
					multiEnvironment.getLocale());
			fechaintPeriodosLargos.setTime(fechaCurse);
			unidadPerMaxPrimCap = ((String) htDesc.get("unidadPerMaxPrimCap"
					.toUpperCase())).trim();
			unidadPerMinPrimCap = ((String) htDesc.get("unidadPerMinPrimCap"
					.toUpperCase())).trim();

			fechaCurseMaximo = (sumaPeriodoaFecha(
					unidadPerMaxPrimCap, perMaxPrimPagoCap, fechaFinalMaxima,
					"A")).getTime();
			fechaCuotasMinimo = (sumaPeriodoaFecha(
					unidadPerMinPrimInt, perMinPrimPagoInt,
					fechaintPeriodosLargos, "")).getTime();
			fechaCuotasMaximo = (sumaPeriodoaFecha(
					unidadPerMaxPrimInt, perMaxPrimPagoInt,
					fechabaseCalcCredito, "")).getTime();

			creditoMinimo = new Double(Double.parseDouble(((String) htDesc
					.get("creditoMinimo".toUpperCase())).trim()));
			creditoMaximo = new Double(Double.parseDouble(((String) htDesc
					.get("creditoMaximo".toUpperCase())).trim()));

			int[] mnp = { 0, 0 };
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(fechaCurseMaximo);

			Date[] fechas = UtilitarioCalculoCuota.getPlanPagos(
					gc.get(Calendar.DAY_OF_MONTH), gc.get(Calendar.MONTH),
					gc.get(Calendar.YEAR), perMaxPrimPagoInt, mnp);
			fechas[0] = fechaCurse;

			periodoRepacto = perMaxPrimPagoInt;
			if (fechaVencimientoMLT != null) {
				if (!fechaVencimientoMLT.trim().equals("")
						&& !fechaVencimientoMLT.trim().equals("No Disponible")) {

					fechaDespuesVcto = FechasUtil.convierteStringACalendar(
							fechaVencimientoMLT, new SimpleDateFormat(
									"dd/MM/yyyy"));
					fechaDespuesVcto = sumaPeriodoaFecha(unidadPerMaxPrimCap,
							perMinPrimVcto, fechaDespuesVcto, "");

					for (int j = 1; j < fechas.length; j++) {
						if (FechasUtil.comparaDias(fechaDespuesVcto.getTime(),
								fechas[j]) < 0) {
							periodoRepacto = j - 1;
							break;
						}
					}
				}
			}

			poc.valorCambio = moneda; 
			poc.tipoOperacion = tipoOperacion;
			poc.codigoAuxiliar = codigoAuxiliar;
			poc.creditoMinimo = creditoMinimo.doubleValue(); 
			poc.creditoMaximo = creditoMaximo.doubleValue(); 
			poc.unidadPerMaxPrimCap = unidadPerMaxPrimCap; 
			poc.perMaxPrimPagoCap = perMaxPrimPagoCap; 
			poc.unidadPerMinPrimCap = unidadPerMinPrimCap;
			poc.perMinPrimPagoCap = perMinPrimPagoCap; 
			poc.fechaTransac = fechaCurseMaximo; 
			poc.unidadTpoPlMax = unidadPerMaxVcto;
			poc.plazoMax = perMaxPrimVcto; 
			poc.unidadTpoPlMin = unidadPerMinVcto; 
			poc.plazoMin = perMinPrimVcto; 
											
			poc.perMinPrimPagoInt = perMinPrimPagoInt;
			poc.unidadPerMinPrimInt = unidadPerMinPrimInt; 
			poc.perMaxPrimPagoInt = perMaxPrimPagoInt; 
			poc.unidadPerMaxPrimInt = unidadPerMaxPrimInt; 
			poc.baseCalcCredito = FechasUtil.convierteDateAString(
					fechaCuotasMaximo, "dd/MM/yyyy"); 
			poc.intPeriodosLargos = FechasUtil.convierteDateAString(
					fechaCuotasMinimo, "dd/MM/yyyy"); 
			poc.periodoRepacto = periodoRepacto; 

			vectorPOC.add(poc);
		}

		return vectorPOC;

	}
	
	/*******************************************************************************************************
	 * sumaPeriodoaFecha
	 *******************************************************************************************************/
	/**
	 * Suma periodo a fecha.
	 * <p>
	 * Registro de Versiones
	 * <ul>
	 * <li>1.0 (22/06/2015 Eduardo Perez - BEE - Jimmy Muñoz D. (ing.Soft.BCI) ): Version Inicial
	 *
	 * Nota : no se utilizo FechasUtil.calculaDiaDeVencimiento para calculos de
	 * sumar periodos pues este metodo siempre retorna fecha proxima
	 * habil...para este caso mediante el parametro "opcion" se determina tipo
	 * de retorno de fecha.
	 * </ul>
	 * </p>
	 *
	 * @param unidadPeriodo  unidad de periodo "D"ia "M"eses "A"ños.
	 * @param numeroPeriodo  numero de periodo a sumar.
	 * @param fechaCalculada fecha a la cual le sumamos el periodos.
	 * @param opcion opcion de calculo final.
	 * @return fecha Calculada
	 * @since 1.0
	 *
	 */
	private static Calendar sumaPeriodoaFecha(String unidadPeriodo,
			int numeroPeriodo, Calendar fechaCalculada, String opcion) {

		fechaCalculada.set(Calendar.HOUR_OF_DAY, VALOR_2); 
														
		fechaCalculada.set(Calendar.MINUTE, 0);
		fechaCalculada.set(Calendar.SECOND, 0);

		switch (Character.toUpperCase(unidadPeriodo.charAt(0))) {
		case 'D':
			fechaCalculada.add(Calendar.DAY_OF_MONTH, numeroPeriodo);
			break;
		case 'M':
			fechaCalculada.add(Calendar.MONTH, numeroPeriodo);
			break;
		case 'A':
		default:
			fechaCalculada.add(Calendar.YEAR, numeroPeriodo);
			break;
		}

		if (opcion.equals("A")) {
			if (!Feriados.esHabil(fechaCalculada)) {
													
				fechaCalculada = FechasUtil.diaHabilAnteriorAl(fechaCalculada);
																				
			}
		} 
		else if (opcion.equals("P")) { 
			if (!Feriados.esHabil(fechaCalculada)) {
													
				fechaCalculada = FechasUtil.diaHabilSiguienteAl(fechaCalculada);
			}
		}

		return fechaCalculada;
	}
	
	/**
	 * obtieneFactorCAE. obtiene el cálculo del índice de
	 * "Carga Anual Equivalente" (CAE).
	 *
	 * <p>
	 * Registro de versiones:
	 * <ul>
         * <li>1.0 (22/06/2015 Eduardo Perez - BEE - Jimmy Muñoz D. (ing.Soft.BCI) ): Version Inicial. </li>
	 * <li>1.1 (01/03/2016 Manuel Escárate (BEE)  - Felipe Ojeda . (ing.Soft.BCI) ):   Se modifican logs. </li>  
	 * </ul>
	 * <p>
	 * @param montoCredito  monto crédito.
	 * @param mesesCuotas meses cuotas.
	 * @param interesEfectivo interes efectivo.
	 * @param fechaPrimerVcto fecha primer vencimiento.
	 * @param gastosImpuesto gastos impuesto.
	 * @param gastosDesgravamen gastos desgravamen.
	 * @param gastosNotario gastos notario.
	 * @param mnp lista de meses de no pago.
	 * @return Factor CAE
	 * @since 1.0
	 */
	private double obtieneFactorCAEPorcentaje(double montoCredito,
			int mesesCuotas, double interesEfectivo, Date fechaPrimerVcto,
			double gastosImpuesto, double gastosDesgravamen,
			double gastosNotario, Integer[] mnp) {

		double factorcae = 0D;
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("obtieneFactorCAEPorcentaje - montoCredito       ["
					+ montoCredito + "]");
			getLogger().debug("obtieneFactorCAEPorcentaje - mesesCuotas        ["
					+ mesesCuotas + "]");
			getLogger().debug("obtieneFactorCAEPorcentaje - interesEfectivo    ["
					+ interesEfectivo + "]");
			getLogger().debug("obtieneFactorCAEPorcentaje - fechaPrimerVcto    ["
					+ fechaPrimerVcto + "]");
			getLogger().debug("obtieneFactorCAEPorcentaje - gastosImpuesto     ["
					+ gastosImpuesto + "]");
			getLogger().debug("obtieneFactorCAEPorcentaje - gastosDesgravamen  ["
					+ gastosDesgravamen + "]");
			getLogger().debug("obtieneFactorCAEPorcentaje - gastosNotario      ["
					+ gastosNotario + "]");
		}

		try {
			ArrayList mesesDeNoPago = new ArrayList();
			for (int i = 0; (mnp != null) && (mnp.length > 0)
					&& (i < mnp.length); i++) {
				mesesDeNoPago.add(mnp[i]);
			}

			factorcae = FactorCae.obtenerIndiceCAE(montoCredito, mesesCuotas,
					(float)interesEfectivo, fechaPrimerVcto, gastosImpuesto,
					gastosDesgravamen, gastosNotario, mesesDeNoPago);
		} 
		catch (Exception e) {
                 	 getLogger().debug("obtieneFactorCAEPorcentaje - Error en FactorCae.obtenerIndiceCAE");
               		if (getLogger().isDebugEnabled()) {
				getLogger().debug("obtieneFactorCAEPorcentaje - Exception ["
						+ e.getMessage() + "]");
			}
		}

		return factorcae;
	}
	
	/*******************************************************************************************************
	 * buscaListaCodigos
	 *******************************************************************************************************/
	/**
	 * busca lista de codigos delimitada por separador y los agrupa en vector.
	 * <p>
	 * Registro de Versiones
	 * <ul>
     * <li>1.0 (22/06/2015 Eduardo Perez - BEE - Jimmy Muñoz D. (ing.Soft.BCI) ): Version Inicial
	 *
	 * </ul>
	 * </p>
	 *
	 * @param lista  de codigos.
	 * @param separador de codigos.
	 * @return Vector con codigos.
	 * @since 1.0
	 *
	 */
	private Vector buscaListaCodigos(String lista, String separador) {
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
	
	/*******************************************************************************************************
	 * obtenerOperacionesARenovar
	 *******************************************************************************************************/
	/**
	 * <b>Obtención de Operaciones a renovar.</b>
	 *
	 * <p>
	 * Este método obtiene todos los tipos de créditos que pueden ser renovados
	 * y/o prorrogados para un tipo de banca en particular.
	 * </p>
	 *
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 (22/06/2015 Eduardo Perez - BEE - Jimmy Muñoz D. (ing.Soft.BCI) ): Version Inicial. </li>
         * <li>1.1 (01/03/2016 Manuel Escárate (BEE)  - Felipe Ojeda . (ing.Soft.BCI) ):   Se modifican logs. </li>  
	 * </ul>
	 *
	 * @param hash Elementos de la tabla "*.parametros".
	 * @param codBanca Código de la Banca.
	 *
	 * @return String[]
	 * @since 1.0
	 */
	private String[] obtenerOperacionesARenovar(Hashtable hash,
			String codBanca) {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("obtenerOperacionesARenovar - INI");
		}
		Object htelem = null;
		Hashtable htDesc = new Hashtable();
		Hashtable htBancas = null;
		String[] listaBancas = null; 
									
		StringTokenizer st = null;
		String str = null;
		String bancaProducto = null;
		String[] elem = null;
		int contador = 0;
		int ind = 0;

		for (Enumeration codigos = hash.keys(); codigos.hasMoreElements();) {
			htelem = codigos.nextElement();
			htBancas = (Hashtable) hash.get(htelem);
			bancaProducto = (String) htBancas.get("banca_producto"
					.toUpperCase());
			if (bancaProducto != null && !bancaProducto.trim().equals("")) {
				st = new StringTokenizer(bancaProducto, ",");
				listaBancas = new String[st.countTokens()]; 
															
				ind = 0;
				while (st.hasMoreTokens()) {
					str = st.nextToken().trim();
					listaBancas[ind] = str;
					ind++;
				}
				for (int i = 0; i < listaBancas.length; i++) {
					if (listaBancas[i].equals(codBanca.trim())) {
						str = (String) htelem.toString().trim();
						String moneda = (String) htelem.toString().trim()
								.substring(0, VALOR_4);
						String tio = (String) htelem.toString().trim()
								.substring(VALOR_6, VALOR_9);
						String aux = (String) htelem.toString().trim()
								.substring(VALOR_9, VALOR_12);
						if (getLogger().isDebugEnabled()) {
							getLogger().debug("obtenerOperacionesARenovar - moneda =["
								+ moneda + "] tio =[" + tio + "] aux =[" + aux + "]");
						}
						htDesc.put(str, "");
						break;
					}
				}
			}
		}
		elem = new String[htDesc.size()];
		for (Enumeration codigos = htDesc.keys(); codigos.hasMoreElements();) {
			htelem = codigos.nextElement();
			elem[contador] = (String) htelem.toString();
			contador++;
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("obtenerOperacionesARenovar - FIN");
		}
		return elem;
	}
	
	  /**
	   * Avances de Multilinea.
	   * <P>
	   * Registro de versiones:
	   * <ul>
	   * <li> 
       *        1.0 21/09/2015 Manuel Escárate (BEE)) -  Jimmy Muñoz (ing. Soft. BCI): Versión Inicial.
	   * </li>
	   * <li>   1.1 12/05/2016 Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI) - se agrega atributo dentro del flujo para identificar tipo de línea.</li>
	   * <li>   1.2 20/07/2016 Manuel Escárate (BEE) - Felipe Ojeda (ing.Soft.BCI) - se agrega lógica para consulta y obtener valor de gasto notarial.</li>
	   * </ul>
	   * <p>
	   * 
	   * @param multiEnvironment multienviroment.
	   * @param tipoOperacion tipo de operación seleccionado.
	   * @param moneda moneda.
	   * @param codigoAuxiliar código auxiliar.
	   * @param oficinaIngreso oficina ingreso.
	   * @param rutDeudor rut deudor.
	   * @param digitoVerificador dídigot verificador.
	   * @param montoCredito monto crédito.
	   * @param abono abono.
	   * @param cargo cargo.
	   * @param ctaAbono cuenta abono. 
	   * @param ctaCargo cuenta cargo.
	   * @param indicadorNP01 indicador NP01.
	   * @param indicadorNP02 indicador NP02.
	   * @param vencimientos vencimientos.
	   * @param codBanca código de la banca.
	   * @param plan plan.
	   * @param datosLog datos para loguear.
	   * @param codSegmento código de segmento.
	   * @param caiNumOpe número operación.
	   * @param iicNumOpe número de operación.
	   * @param tasaPropuesta tasa propuesta.
	   * @param bandera bandera.
	   * @param ejecutivo ejecutivo.
	   * @param seguros detalle de seguros.
	   * @param condicionGarantiaIni condición de garantía.
	   * @return resultado avance multilinea.
	   * @throws MultilineaException controla error multilenea.
	   * @throws EJBException contrala error ejb.
	   */
	  public ResultAvanceMultilinea avanceMultilinea(MultiEnvironment multiEnvironment, String tipoOperacion, String moneda, String codigoAuxiliar, String oficinaIngreso, int rutDeudor, char digitoVerificador, double montoCredito, String abono, String cargo, int ctaAbono, int ctaCargo, int indicadorNP01, int indicadorNP02, EstructuraVencimiento[] vencimientos, String codBanca, char plan, Hashtable datosLog, String codSegmento, String caiNumOpe, int iicNumOpe, double tasaPropuesta, String bandera, String ejecutivo, String[] seguros,String condicionGarantiaIni) throws MultilineaException, EJBException {
		  String ctaAbonoSt              = "0000";
		  String ctaCargoSt              = "0000";
		  Date   fechaCurse              = ManejoEvc.fechaHabil(new Date());
		  String caiOperacion            = null;
		  String codNotaria              = null;
		  String codigoSegDesgrav        = "SEDECO";
		  String condicionGar            = "8  ";  //"4  "sin garantia
		  String destinoCredito          = "288";  //cap de trab
		  //String ejecutivo               = null;
		  String glosaCliente            = null;
		  String glosaExtIdc             = null;
		  String idCancelacion           = null;
		  String indClasificRiesgo       = null;
		  String plazaCobro              = null;
		  String situacionCartera        = null;
		  String situacionCobranza       = null;
		  String situacionContableLdc    = null;
		  String tipoDocum               = null;
		  char   analisisFeriado         = ' ';
		  char   calculoValorFinal       = ' ';
		  char   canalContacto           = ' ';
		  char   digitoVerifAval         = ' ';
		  char   estructuraVenc          = ' ';
		  char   indicExtIdc             = ' ';
		  char   indicadorExtIdc         = ' ';
		  char   insistencia             = ' ';
		  char   interesEspecial         = ' ';
		  char   vigenciaCargo           = 'S';
		  double comision                = 0.0D;
		  double gastosNotario           = 0.0D;
		  double impuestos               = 0.0D;
		  double primaSegDesgrav         = 0.0D;
		  double tasaComisionCancelacion = 0.0D;
		  double tasaComisionCurse       = 0.0D;
		  double valorDocumento          = 0.0D;
		  double valorGasto              = 0.0D;
		  double valorOtroSeguro         = 0.0D;
		  double valorRenovado           = 0.0D;
		  double valorSegCesantia        = 0.0D;
		  int    correlativo             = 0;
		  int    iicOperacion            = 0;
		  int    indicadorNP03           = 0;
		  int    indicadorNP04           = 0;
		  int    nroDireccion            = 0;
		  int    numCancelacion          = 0;
		  int    numeroDireccion         = 0;
		  int    rutAval                 = 0;
		  String canal                   = multiEnvironment.getCanal();
		  String reqNum                 = "527";
		  ResultConsultaSpr obeanSprSGS  = null;
		  Vector vectorSPR               = new Vector();
		  boolean consultaSeguros        = false;
		  double valInteOperacion        = VALOR_01;
		  int rutAvalDPS                 = 0;
		  char rutdigAvalDPS             = ' ';
		  String canalCredito = TablaValores.getValor(ARCHIVO_PARAMETROS, "canalCreditoWeb", "canales");
		  try {
              if (getLogger().isDebugEnabled()){
                  getLogger().debug("INI avanceMultilinea (24042009) ********************");
                  getLogger().debug("bandera ["+bandera+"]");
                  getLogger().debug("validando array 'vencimientos'");
			  }
			  if (vencimientos == null || vencimientos[0] == null) {
				  throw new Exception("vencimientos indefinidos");
			  }

              if (getLogger().isDebugEnabled()){
                  getLogger().debug("Plazo......");
                  getLogger().debug("......en meses:"+ManejoEvc.getPlazoEnMeses(fechaCurse,analisisFeriado,vencimientos,0,0,indicadorNP03,indicadorNP04));
                  getLogger().debug("......en dias :"+ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,indicadorNP03,indicadorNP04));
                  getLogger().debug("vencimientos[0].getTotalVencimientosGrupo():" + vencimientos[0].getTotalVencimientosGrupo());
                  getLogger().debug("rutDeudor        [" + rutDeudor + "]");
                  getLogger().debug("digitoVerificador[" +  digitoVerificador + "]");
                  getLogger().debug("reqNum           [" + reqNum + "]");
                  getLogger().debug("codigoSegDesgrav [" + codigoSegDesgrav + "]");
                  getLogger().debug("condicionGar     [" + condicionGar + "]");
                  getLogger().debug("destinoCredito   [" + destinoCredito + "]");
                  getLogger().debug("InputSimulacionCredito ibean");
			  }

			  //*********************************************************************************************//
			  //*** INI consulta CLN para obtener condicionGar necesario para el ingreso de la simulacion ***//
			  //*** INI Desarrollo BEE 03/2004 hcf                                                        ***//
			  //*** avance                                                                                ***//
			  //*********************************************************************************************//
              if (getLogger().isDebugEnabled()) { getLogger().debug("Antes ResultConsultaCln"); }

			  ResultConsultaCln obeanCln = new ResultConsultaCln();

			  InputConsultaCln ibeanCln = new InputConsultaCln("025",
					  "",                       // nombreDeudor,
					  rutDeudor,                // rutDeudor,
					  digitoVerificador,        // digitoVerificador,
					  ' ',                      // indicadorExtIdc,
					  "",                       // glosaExtIdc,
					  "",                       // idOperacion,
					  0,                        // numOperacion,
					  0);                       // totLinIngreso);

			  consultaCln(multiEnvironment, ibeanCln, obeanCln);

			  Linea[] consultaLinea = obeanCln.getLineas();

			  boolean tieneAval = false;
			  boolean tieneFoga = false;
			  boolean tieneCual = false;
			  boolean tieneMlt  = false;
			  boolean soloG     = false;

              if (getLogger().isDebugEnabled()) { getLogger().debug("consultaLinea.length [" + consultaLinea.length + "]"); }
			  for (int i = 0; i < consultaLinea.length; i++) {
				  if (consultaLinea[i] == null) {
					  break;
				  }

				  String tipLin = consultaLinea[i].getCodigoTipoLinea();
                  if (getLogger().isDebugEnabled()){
                      getLogger().debug("TipLin   ["+ tipLin +"]");
                      getLogger().debug("consultaLinea[i].getCodigoTipoInfo   ["+ consultaLinea[i].getCodigoTipoInfo() +"]");
                      getLogger().debug("consultaLinea[i].getTipoOperacion    ["+ consultaLinea[i].getTipoOperacion() +"]");
                      getLogger().debug("consultaLinea[i].getCodAuxiliarLinea ["+ consultaLinea[i].getCodAuxiliarLinea() +"]");
				  }
                  String tipoLineaOcupada = definirLDC(multiEnvironment, consultaLinea);
				  if (tipLin.equals(tipoLineaOcupada)) { //verifica si tipo de linea es multilinea
					  soloG      = consultaLinea[i].getCodigoTipoInfo() == 'G' ? true : false;
					  tieneMlt   = true;
					  tieneAval  = (consultaLinea[i].getTipoOperacion()).equals("AVL") ? true : tieneAval;
					  tieneFoga  = (consultaLinea[i].getTipoOperacion()).equals("952") ? ((consultaLinea[i].getCodAuxiliarLinea()).trim().equals("101") ? true : tieneFoga) : tieneFoga;
					  tieneCual  = soloG ? (!(consultaLinea[i].getTipoOperacion()).trim().equals("") ? true : tieneCual) : tieneCual;

				  }
			  }

			  if (!tieneMlt) {
                  if (getLogger().isDebugEnabled()) { getLogger().debug("Cliente " + rutDeudor + "-" + digitoVerificador + " No posee Multilinea (MLT)"); }
			  }

			  //  condicionGar -->
			  //  ("4  ";  //sin garantia default)
			  //  ("8  ";  //otras garantias)
			  //  ("2  ";  //aval);
			  //  ("10 ";  //fogape);
			  //

			  condicionGar = tieneAval ? "2  " : (tieneFoga ? "10 " : (tieneCual ? "8  " : "4  "));
              if (getLogger().isDebugEnabled()) { getLogger().debug("condicionGarantiaIni:::"+condicionGarantiaIni); }

			  if (condicionGarantiaIni != null){
				  condicionGar = condicionGarantiaIni;
			  }

			  // destinoCredito -->
			  // ("288" : // captrab default)
			  // ("301" : // captrab fogape )
			  //
			  destinoCredito = tieneFoga ? "301" : "288"; //si tiene "fogape" entonces destinocredito="cap de trab fogape" sino "captrab"

              if (getLogger().isDebugEnabled()){
                  getLogger().debug("condicionGar   ["+ condicionGar +"]");
                  getLogger().debug("destinoCredito ["+ destinoCredito +"]");
                  getLogger().debug("Despues ResultConsultaCln obeanCln");
			  }
			  //*********************************************************************************************//
			  //*** FIN Desarrollo BEE 03/2004 hcf                                                        ***//
			  //*** FIN consulta CLN                                                                      ***//
			  //*********************************************************************************************//

			  boolean tieneAvales = condicionGar.equals("2  ") ? true : false;

			  if (tieneAvales){
                  if (getLogger().isDebugEnabled()) { getLogger().debug("tieneAvales [true]    Cliente[" + rutDeudor + "-" + digitoVerificador + "]"); }

				  try{
					  ResultConsultaAvales obeanAval = new ResultConsultaAvales();

					  InputConsultaAvales abean = new InputConsultaAvales("026",
							  rutDeudor,
							  digitoVerificador,
							  ' ',
							  " ",
							  " ",
							  " ",
							  0,
							  0,
							  "AVL",
							  "AVC");

					  consultaAvales(multiEnvironment, abean, obeanAval);

                      if (getLogger().isDebugEnabled()) { getLogger().debug("despues consultaAvales"); }

					  Aval[] avales    = obeanAval.getAvales();

                      if (getLogger().isDebugEnabled()) { getLogger().debug("avales.length ["+ avales.length +"]"); }

					  for (int i = 0; i < avales.length; i++) {

						  char   dvfAval = avales[i].getDigitoVerificaAval();
						  char   indvige = avales[i].getVigente();

                          if (getLogger().isDebugEnabled()) { getLogger().debug("Esta vigente el rut "+ avales[i].getRutAval() +" ?    vigente["+ indvige +"]"); }

						  if (indvige == 'S'){
							  valInteOperacion = avales[i].getRutAval();
							  rutAvalDPS       = avales[i].getRutAval();
							  rutdigAvalDPS    = avales[i].getDigitoVerificaAval();
							  break;
						  }
					  }

				  } 
				  catch(Exception e) {
                      if (getLogger().isEnabledFor(Level.ERROR)) { getLogger().error("ERROR [consultaAvales] Cliente[" + rutDeudor + "-" + digitoVerificador + "]    Exception [" + e.getMessage() + "]"); }
				  }

			  }
			  else{
                  if (getLogger().isDebugEnabled()) { getLogger().debug("tiene_avales [false]   Cliente[" + rutDeudor + "-" + digitoVerificador + "]"); }
			  }

              if (getLogger().isDebugEnabled()) { getLogger().debug("new ResultAvanceMultilinea()"); }

			  ResultAvanceMultilinea res = new ResultAvanceMultilinea();

			  //*****************************************************************************************************************************//


			  //******************************************//
			  //********  INI Consulta SPR ***************//
			  //******************************************//

              if (getLogger().isDebugEnabled()) { getLogger().debug("llenando vencimientos[0].getPeriodoEntreVctoExpresaEn()=[" + vencimientos[0].getPeriodoEntreVctoExpresaEn() + "]"); }

			  ResultConsultaSpr  obeanSpr = null;

			  //            if (!bandera.equals("S")){

			  obeanSpr = obtieneTasaMultilineaPrecios(multiEnvironment,
					  "CUR",
					  "INT",
					  fechaCurse,//ddMMyyyy_form.parse("02092003"),
					  rutDeudor,
					  digitoVerificador,
					  canal,//"130",
					  "", //subcanal (oficina)
					  moneda,
					  tipoOperacion,
					  codigoAuxiliar,
					  ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,indicadorNP03,indicadorNP04) < 1 ? 1 : ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,indicadorNP03,indicadorNP04),
							  'D',
							  montoCredito,
							  (double) vencimientos[0].getTotalVencimientosGrupo(),
							  "",//codClfRentabilidad
							  "",//codFactorRiesgo
							  "",//indTipoPago
							  codSegmento,
							  "",
							  0);

			  //******************************************//
			  //********  FIN Consulta SPR ***************//
			  //******************************************//

			  if (obeanSpr==null || obeanSpr.getTotOcurrencias()==0) {
				  throw new MultilineaException("ESPECIAL","Error en obtencion de tasa: SPR nulo o 0 ocurrencias");
			  }




			  //********  INI Consulta SEGUROS AVANCE ***************//
			  String  segurosVigentes   = ((String) TablaValores.getValor("multilinea.parametros", "AvanceBancasSeguros", "vigente"));
			  consultaSeguros           = (segurosVigentes == null ? " " : segurosVigentes).trim().equals("S");
              if (getLogger().isDebugEnabled()) {
                  getLogger().debug("segurosVigentes   [" + segurosVigentes + "]  codBanca[" + codBanca + "]  canal[" + canal + "] Avance");
                  getLogger().debug("segurosss consulta seguros"+consultaSeguros);
              }
			  if (consultaSeguros){
				  String  bancapermitidas = TablaValores.getValor("multilinea.parametros", "AvanceBancasSeguros", "banca");
                  if (getLogger().isDebugEnabled()) { getLogger().debug("bancas permitidas [" + bancapermitidas + "]  codBanca[" + codBanca + "]  canal[" + canal + "] Avance"); }
				  consultaSeguros   =verificaPertenenciaBanca(codBanca, bancapermitidas);

				  if (consultaSeguros){
					  try{

                          if (getLogger().isDebugEnabled()) { getLogger().debug("Antes de la consulta SPR para Seguros Multilinea Avance"); }

						  obeanSprSGS = obtieneSegurosDesdePrecios(multiEnvironment,
								  "CUR",                               //codEvento
								  "SGS",                               //codConcepto
								  fechaCurse,
								  "CON",                               //codTipoConsulta siempre CON para SGS
								  "",                                  //caiOperacion
								  0,                                   //iicOperacion
								  "",                                  //extOperacion
								  rutDeudor,
								  digitoVerificador,
								  ' ',                                 //indCliente
								  "",                                  //glsCliente
								  canal,                               //130 o 230
								  "",                     //codigo oficina va si solo si canal = 110
								  moneda,                              //0999
								  tipoOperacion,                       //CON
								  codigoAuxiliar,                      //050
								  ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,indicadorNP03,indicadorNP04) < 1 ? 1 : ManejoEvc.getPlazoEnDias(fechaCurse,analisisFeriado,vencimientos,0,0,indicadorNP03,indicadorNP04),          //numPlazoOperacion
										  'D',                                 // expresa en
										  vencimientos[0].getFechaPrimerVcto(),//fecVctoInicial
										  montoCredito,
										  0D,                                  //valSaldoOperacion
										  (double) vencimientos[0].getTotalVencimientosGrupo(), //(double) numCuotas,
										  valInteOperacion,                    //valInteOperacion
										  0D,                                  //valMontoAdicional
										  "",                                  //codClfRentabilidad,
										  "",                                  //codFactorRiesgo,
										  "",                                  //indTipoPago,
										  "",                                  //caiOperacionRcc
										  codSegmento                          //codSegmento
								  );

                          if (getLogger().isDebugEnabled()) { getLogger().debug("Despues de la consulta SPR a Seguros Multilinea Avance"); }

					  } 
					  catch(Exception e) {
                          if (getLogger().isEnabledFor(Level.ERROR)) { getLogger().error("ERROR***SimulaCursaCreditoException...Exception [" + e.getMessage() + "]"); }
					  }

					  if (obeanSprSGS==null || obeanSprSGS.getTotOcurrencias()==0) {
                          if (getLogger().isDebugEnabled()) { getLogger().debug("La consulta SPR no encuentra ocurrencias en Seguros"); }
					  } 
					  else {
						  for(int i = 0; i < obeanSprSGS.getTotOcurrencias(); i++){
							  if (obeanSprSGS.getInstanciaDeConsultaSpr(i) == null){
                                  if (getLogger().isDebugEnabled()) { getLogger().debug("Saliendo del llenando vector de SPR's SEGUROS"); }
								  break;
							  } 
							  else {
								  vectorSPR.add(obeanSprSGS.getInstanciaDeConsultaSpr(i));
                                  if (getLogger().isDebugEnabled()) { getLogger().debug("llenando vector de SPR's :" + i); }
							  }
						  }
					  }
				  }
				  else{
                      if (getLogger().isDebugEnabled()) { getLogger().debug("No se consultaron seguros porque banca no esta inscrita. Avance"); }
				  }
			  }
			  //********  INI Consulta SEGUROS avance ***************//

			  //******************************************//
			  //********  INI Consulta PPC ***************//
			  //******************************************//
			  ResultConsultaSpr  obeanPpc = null;

			  //            if (!bandera.equals("S")){
			  try {
				  obeanPpc = obtieneTasaMultilineaPrecios(multiEnvironment,
						  "CUR",
						  "PPC",
						  fechaCurse,//ddMMyyyy_form.parse("02092003")
						  rutDeudor,
						  digitoVerificador,
						  canal,//"130"
						  "", //subcanal (oficina)
						  moneda,
						  tipoOperacion,
						  codigoAuxiliar,
						  ManejoEvc.getPlazoEnMeses(fechaCurse,analisisFeriado,vencimientos,0,0,indicadorNP03,indicadorNP04),
						  vencimientos[0].getPeriodoEntreVctoExpresaEn(),//'M',
						  montoCredito,
						  (double) vencimientos[0].getTotalVencimientosGrupo(),
						  "",//codClfRentabilidad
						  "",//codFactorRiesgo
						  "",//indTipoPago
						  codSegmento,
						  "",
						  0);
			  } 
			  catch (Exception e) {
                  if (getLogger().isEnabledFor(Level.ERROR)) { getLogger().error("consulta PPC::" + e.getMessage()); }
			  }

			  //******************************************//
			  //********  FIN Consulta PPC ***************//
			  //******************************************//

			  if (obeanPpc==null || obeanPpc.getTotOcurrencias()==0) {
                  if (getLogger().isDebugEnabled()) { getLogger().debug("obeanPpc nulo o 0 ocurrencias"); }
			  } 
			  else {
                  if (getLogger().isDebugEnabled()) { getLogger().debug("Rango (" + form.format(obeanPpc.getInstanciaDeConsultaSpr(0).getValMonto()) + " - " + form.format(obeanPpc.getInstanciaDeConsultaSpr(0).getValTasaMonto())+")");}
				  if (montoCredito > obeanPpc.getInstanciaDeConsultaSpr(0).getValTasaMonto()) {
					  throw new MultilineaException("ESPECIAL","Monto mayor que el permitido (" + form.format(obeanPpc.getInstanciaDeConsultaSpr(0).getValTasaMonto()) + ")");
				  }
				  if (montoCredito < obeanPpc.getInstanciaDeConsultaSpr(0).getValMonto()){
					  throw new MultilineaException("ESPECIAL","Monto menor que el permitido (" + form.format(obeanPpc.getInstanciaDeConsultaSpr(0).getValMonto()) +")");
				  }
			  }

			  double valorNotario = 0;
			  ResultConsultaSpr  obeanSprCGN = null;

			  if (canal != null && canal.equals(canalCredito)){  

				  if (getLogger().isDebugEnabled()){
					  getLogger().debug("[avanceMultilinea] Antes de obtieneGastosNotariales");
				  }

				  try{
					  if (getLogger().isDebugEnabled()){
						  getLogger().debug("[avanceMultilinea] Antes de la consulta SPR a Gastos Notarios");
					  }

					  obeanSprCGN = obtieneTasaMultilineaPrecios(multiEnvironment,
							  "CUR",
							  "CGN",
							  fechaCurse,
							  rutDeudor,
							  digitoVerificador,
							  canal,
							  "", 
							  moneda,
							  tipoOperacion,
							  codigoAuxiliar,
							  ManejoEvc.getPlazoEnMeses(fechaCurse,analisisFeriado,vencimientos,0,0,indicadorNP03,indicadorNP04),
							  vencimientos[0].getPeriodoEntreVctoExpresaEn(),//'M',
							  montoCredito,
							  (double) vencimientos[0].getTotalVencimientosGrupo(),
							  "",
							  "",
							  "",
							  codSegmento,
							  "",
							  0);
					  if (getLogger().isDebugEnabled()){ 
						  getLogger().debug("[avanceMultilinea] Despues de la consulta SPR a Gastos Notariales");
					  }
				  }
				  catch(Exception e) {
					  if (getLogger().isEnabledFor(Level.ERROR)){
						  getLogger().error("[avanceMultilinea] [BCI_FINEX] Error controlado en la consulta SPR de CGN" + e.getMessage());
					  }
				  }

				  if (obeanSprCGN == null || obeanSprCGN.getTotOcurrencias() == 0) {
					  if (getLogger().isDebugEnabled()){
						  getLogger().debug("[avanceMultilinea] No existen ocurrencias en la consulta SPR para Gastos Notariales");
					  }
				  } 
				  else {
					  for(int i = 0; i < obeanSprCGN.getTotOcurrencias(); i++){
						  if (obeanSprCGN.getInstanciaDeConsultaSpr(i) == null){
							  if (getLogger().isDebugEnabled()){
								  getLogger().debug("[avanceMultilinea] Saliendo del llenando vector de SPR's Gastos Notariales");
							  }
							  break;
						  }
						  else{
							  valorNotario = obeanSprCGN.getInstanciaDeConsultaSpr(i).getValMonto();
							  if (getLogger().isDebugEnabled()){ 
								  getLogger().debug("[avanceMultilinea] llenando vector de SPR's :" + i);
								  getLogger().debug("[avanceMultilinea] valorNotario: " +  valorNotario);
							  }
							  break;
						  }
					  }
				  }
				  if (getLogger().isDebugEnabled()){
					  getLogger().debug("[avanceMultilinea] Despues de la consulta SPR a Gastos Notarios");
				  }
			  }

			  //******************************************//
			  //********  INI ingresoCredito *************//
			  //******************************************//
			  String origen = "XXX";
			  char indseq = 'X';


			  double tasaPropuestaLocal;

			  if (!bandera.equals("S")){
				  tasaPropuestaLocal = obeanSpr.getInstanciaDeConsultaSpr(0).getValPropuesto();
			  }
			  else{
				  tasaPropuestaLocal = tasaPropuesta;
                  if (getLogger().isDebugEnabled()) { getLogger().debug("tasa propuesta =["+ tasaPropuestaLocal +"] Tasa fue negociada !"); }
			  }


              if (getLogger().isDebugEnabled()) { getLogger().debug("========== OPC ============"); }
			  //un arreglo de opc con 1 sola ocurrencia
			  InputIngresoUnitarioDeOperacionDeCreditoOpc [] opcS = new InputIngresoUnitarioDeOperacionDeCreditoOpc[1];
			  opcS[0] = new InputIngresoUnitarioDeOperacionDeCreditoOpc();
			  opcS[0].setCim_reqnum("046");
			  opcS[0].setCim_uniqueid(origen);
			  opcS[0].setCim_indseq(indseq);

			  if (!caiNumOpe.trim().equals("") && iicNumOpe >0){   //seteamos el numero de operacion
                  if (getLogger().isDebugEnabled()) { getLogger().debug("/*** Numero de Operacion ***/"); }
				  opcS[0].setCaiOperacion(caiNumOpe);
                  if (getLogger().isDebugEnabled()) { getLogger().debug("caiOperacion (String)     : '" + caiNumOpe + "'" ); }
				  opcS[0].setIicOperacion(iicNumOpe);
                  if (getLogger().isDebugEnabled()) { getLogger().debug("iicOperacion (int)        : '" + iicNumOpe + "'" ); }

				  multiEnvironment.setIndreq(0,'0');//pos(0)=RollBack?        --> '0'=No '1'=Si
				  multiEnvironment.setIndreq(1,'0');//pos(1)=Con ACA?         --> '0'=No '1'=Si
				  multiEnvironment.setIndreq(VALOR_2,'0');//pos(1)=Con ACA?         --> '0'=No '1'=Si
				  multiEnvironment.setIndreq(VALOR_3,'1');//pos(1)=Con ELI e ING?   --> '0'=No '1'=Si

			  }

			  multiEnvironment.setIndreq(VALOR_5,'2');  // avanceMultilinea  0=normal      1=valide ejecutivo inexistente

              if (getLogger().isDebugEnabled()) { getLogger().debug("tipoOperacion (String)    : '" + tipoOperacion + "'"); }
			  opcS[0].setTipoOperacion(tipoOperacion);
              if (getLogger().isDebugEnabled()) { getLogger().debug("moneda        (String)    : '" + moneda + "'"); }
			  opcS[0].setMoneda(moneda);
              if (getLogger().isDebugEnabled()) { getLogger().debug("codigoAuxiliar(String)    : '" + codigoAuxiliar + "'"); }
			  opcS[0].setCodigoAuxiliar(codigoAuxiliar);
              if (getLogger().isDebugEnabled()) { getLogger().debug("oficinaIngreso(String)    : '" + oficinaIngreso + "'"); }
			  opcS[0].setOficinaIngreso(oficinaIngreso);
              if (getLogger().isDebugEnabled()) { getLogger().debug("rutDeudor     (int)       : '" + rutDeudor + "'"); }
			  opcS[0].setRutDeudor(rutDeudor);
              if (getLogger().isDebugEnabled()) { getLogger().debug("digitoVerificador(Char)   : '" + digitoVerificador + "'"); }
			  opcS[0].setDigitoVerificador(digitoVerificador);
              if (getLogger().isDebugEnabled()) { getLogger().debug("montoCredito  (double)    : '" + montoCredito + "'"); }
			  opcS[0].setMontoCredito(montoCredito);
              if (getLogger().isDebugEnabled()) { getLogger().debug("codigoSegDesgrav(String)  : '" + codigoSegDesgrav + "'"); }

			  if (obeanSprSGS != null && obeanSprSGS.getTotOcurrencias() > 0){
				  codigoSegDesgrav = "PRECIO";
                  if (getLogger().isDebugEnabled()) { getLogger().debug("codigoSegDesgrav **AVC**  : '" + codigoSegDesgrav + "'"); }
			  }

			  opcS[0].setCodigoSegDesgrav(codigoSegDesgrav);
              if (getLogger().isDebugEnabled()) { getLogger().debug("fechaCurse (Date)         : '" + fechaCurse + "'"); }
			  opcS[0].setFechaCurse(fechaCurse);


              if (getLogger().isDebugEnabled()) { getLogger().debug("tasaSprea (double)         : '" + tasaPropuestaLocal + "'"); }
			  opcS[0].setTasaSprea(tasaPropuestaLocal);                                       //new 06/2005
              if (getLogger().isDebugEnabled()) { getLogger().debug("condicionGar (String)      : '" + condicionGar + "'"); }
			  opcS[0].setCondicionGar(condicionGar);
              if (getLogger().isDebugEnabled()) { getLogger().debug("abono (String)             : '" + abono + "'"); }
			  opcS[0].setAbono(abono);
              if (getLogger().isDebugEnabled()) { getLogger().debug("cargo (String)             : '" + cargo + "'"); }
			  opcS[0].setCargo(cargo);
              if (getLogger().isDebugEnabled()) { getLogger().debug("ctaAbono (String)          : '" + ctaAbonoSt + "'"); }
			  opcS[0].setCtaAbono(ctaAbonoSt);
              if (getLogger().isDebugEnabled()) { getLogger().debug("ctaAbonoTer (int)          : '" + ctaAbono + "'"); }
			  opcS[0].setCtaAbonoTer(ctaAbono);
              if (getLogger().isDebugEnabled()) { getLogger().debug("destinoCredito (String)    : '" + destinoCredito + "'"); }
			  opcS[0].setDestinoCredito(destinoCredito);
              if (getLogger().isDebugEnabled()) { getLogger().debug("vigenciaCargo (char)       : '" + vigenciaCargo + "'"); }
			  opcS[0].setVigenciaCargo(vigenciaCargo);
              if (getLogger().isDebugEnabled()) { getLogger().debug("ctaCargo (String)          : '" + ctaCargoSt + "'"); }
			  opcS[0].setCtaCargo(ctaCargoSt);
              if (getLogger().isDebugEnabled()) { getLogger().debug("pinCtaCargo (int)          : '" + ctaCargo + "'"); }
			  opcS[0].setPinCtaCargo(ctaCargo);

              if (getLogger().isDebugEnabled()) { getLogger().debug("ejecutivo   (String)       : '" + ejecutivo + "'"); }
              opcS[0].setEjecutivo2(ejecutivo);

              if (getLogger().isDebugEnabled()) { getLogger().debug("========== RDCs ============"); }
			  InputIngresoUnitarioDeRdc []                    rdcS   = null;

              if (getLogger().isDebugEnabled()) { getLogger().debug("========== DLC ============"); }
			  InputIngresoDeDlcLlavesYCampos []               dlcS   = new InputIngresoDeDlcLlavesYCampos[1];
			  dlcS[0] = new  InputIngresoDeDlcLlavesYCampos();
			  dlcS[0].setCim_reqnum("013");
			  dlcS[0].setCim_uniqueid(origen);
			  dlcS[0].setCim_indseq(indseq);

			  if (canal != null && canal.equals(canalCredito)){ 
				  if (getLogger().isDebugEnabled()) {
					  getLogger().debug("[avanceMultilinea] Se agregan los campos CodNotaria y GastosNotario en la DLC");
				  }
				  dlcS[0].setCodNotaria("PRECIO");
				  dlcS[0].setGastosNotario(valorNotario);
			  }
              if (getLogger().isDebugEnabled()) { getLogger().debug("========== CYA ============"); }
			  InputIngresoUnitarioCya []                      cyaS   = null;

              if (getLogger().isDebugEnabled()) { getLogger().debug("========== EVC ============"); }
			  InputIngresoUnitarioDeEvc []                    evcS   = new InputIngresoUnitarioDeEvc[1];
			  evcS[0] = new InputIngresoUnitarioDeEvc();
			  evcS[0].setCim_reqnum("016");
			  evcS[0].setCim_uniqueid(origen);
			  evcS[0].setCim_indseq(indseq);

              if (getLogger().isDebugEnabled()) { getLogger().debug("docLegalNumero (int)          : '" + 1 + "'"); }
			  evcS[0].setDocLegalNumero(1);
              if (getLogger().isDebugEnabled()) { getLogger().debug("totalVencimientosGrupo (int)  : '" + vencimientos[0].getTotalVencimientosGrupo() + "'"); }
			  evcS[0].setTotalVencimientosGrupo(vencimientos[0].getTotalVencimientosGrupo());
              if (getLogger().isDebugEnabled()) { getLogger().debug("fechaPrimerVcto (Date)        : '" + vencimientos[0].getFechaPrimerVcto() + "'"); }
			  evcS[0].setFechaPrimerVcto(vencimientos[0].getFechaPrimerVcto());
              if (getLogger().isDebugEnabled()) { getLogger().debug("periodoEntreVcto (int)        : '" + vencimientos[0].getPeriodoEntreVcto() + "'"); }
			  evcS[0].setPeriodoEntreVcto(vencimientos[0].getPeriodoEntreVcto());
              if (getLogger().isDebugEnabled()) { getLogger().debug("periodoEntreVctoExpresaEn (char): '" + vencimientos[0].getPeriodoEntreVctoExpresaEn() + "'"); }
			  evcS[0].setPeriodoEntreVctoExpresaEn(vencimientos[0].getPeriodoEntreVctoExpresaEn());

              if (getLogger().isDebugEnabled()) { getLogger().debug("========== ICG ============"); }
			  InputIngresoUnitarioIcg []                      icgS   = null;

              if (getLogger().isDebugEnabled()) { getLogger().debug("========== VEN ============"); }
			  InputIngresoUnitarioDeVen []                    venS   = null;

              if (getLogger().isDebugEnabled()) { getLogger().debug("========== ROC AVC ============"); }

			  DetalleConsultaSpr [] arregloSPR            = null;
			  InputIngresoRocAmpliada [] rocAmpliada    = null;
			  Vector vectorSGS                            = new Vector();

			  if (consultaSeguros){
                  if (getLogger().isDebugEnabled()) { getLogger().debug("Se consultaron los seguros en AVC"); }
				  arregloSPR      = new DetalleConsultaSpr[vectorSPR.size()];
				  arregloSPR      = (DetalleConsultaSpr[]) vectorSPR.toArray(new DetalleConsultaSpr[0]);
				  rocAmpliada   = new InputIngresoRocAmpliada[vectorSPR.size()];

				  if (seguros != null){
					  for(int i = 0; i < seguros.length; i++){
                          if (getLogger().isDebugEnabled()) { getLogger().debug("seguros[" + i +"] [" + seguros[i] + "]"); }
						  vectorSGS.add(seguros[i]);
					  }
				  }
			  }

			  InputIngresoRoc []   rocS   = null;

			  int indiceInclusion = 0;
			  if (bandera.equals("S")){
                  if (getLogger().isDebugEnabled()) { getLogger().debug("indice_inclusion = 1"); }
				  indiceInclusion = 1;
			  }
			  else{
                  if (getLogger().isDebugEnabled()) { getLogger().debug("indice_inclusion = 0"); }
			  }

			  int j=0;
			  int totOcurrenciasRoc = obeanSpr.getTotOcurrencias();
              if (getLogger().isDebugEnabled()) { getLogger().debug("totOcurrenciasRoc=" + totOcurrenciasRoc); }

			  if (obeanPpc!=null) {
				  totOcurrenciasRoc = totOcurrenciasRoc + obeanPpc.getTotOcurrencias();
                  if (getLogger().isDebugEnabled()) { getLogger().debug("totOcurrenciasRoc=" + totOcurrenciasRoc + " (PPC)"); }
			  }

			  if (obeanSprSGS != null){
				  totOcurrenciasRoc = totOcurrenciasRoc + obeanSprSGS.getTotOcurrencias();
                  if (getLogger().isDebugEnabled()) { getLogger().debug("totOcurrenciasRoc=" + totOcurrenciasRoc + " (SGS)"); }
			  }

			  if (obeanSprCGN != null){
				  totOcurrenciasRoc = totOcurrenciasRoc + obeanSprCGN.getTotOcurrencias();
                  if (getLogger().isDebugEnabled()) { 
                	  getLogger().debug("[avanceMultilinea] totOcurrenciasRoc=" + totOcurrenciasRoc + " (CGN)"); 
                  }
			  }
			  
              if (getLogger().isDebugEnabled()) { getLogger().debug("totOcurrenciasRoc=" + (totOcurrenciasRoc + indiceInclusion) + " (inclusion)"); }

			  rocS   = new InputIngresoRoc[totOcurrenciasRoc + indiceInclusion];

              if (getLogger().isDebugEnabled()) {
                getLogger().debug("================ INT ======");
                getLogger().debug("0 - " + (obeanSpr.getTotOcurrencias()-1));
              }
			  if (bandera.equals("S")){
				  //// registro en ROC's donde se indica quien hizo rebaja de tasa
				  String codEjecutivo = ejecutivo;
				  String codejeCai = ( codEjecutivo.length() > VALOR_3 ) ? codEjecutivo.substring(0, VALOR_4)     : codEjecutivo ;        // RJUN
				  String codejeIic = ( codEjecutivo.length() > VALOR_4 ) ? codEjecutivo.substring(VALOR_4).trim() : " " ;                 // CO
				  Date   fechanow  = new Date();
				  String horanow   = new SimpleDateFormat("HHmmss").format(new Date());

                  if (getLogger().isDebugEnabled()){
                      getLogger().debug("codejeCai  = : " + codejeCai);
                      getLogger().debug("codejeIic  = : " + codejeIic);
                      getLogger().debug("fechanow   = : " + fechanow);
                      getLogger().debug("horanow    = : " + horanow);
                      getLogger().debug("Registro ROC Multilinea");
				  }
				  rocS[0] = new InputIngresoRoc();
				  rocS[0].setCodSistema(obeanSpr.getInstanciaDeConsultaSpr(0).getCodSistemaCpt());
				  rocS[0].setCodEvento(obeanSpr.getInstanciaDeConsultaSpr(0).getCodEventoCpt());
				  rocS[0].setCodigoConcepto(obeanSpr.getInstanciaDeConsultaSpr(0).getCodConceptoCpt());
				  rocS[0].setCodigoSubConcepto(" ");
				  rocS[0].setCodModalidad(" ");
				  rocS[0].setIndCobro(" ");
				  rocS[0].setCaiPlantilla(codejeCai);                                                   // String
				  rocS[0].setIicPlantilla(codejeIic);                                                   // String
				  rocS[0].setFechaPlantilla(fechanow);                                                  // Date
				  rocS[0].setHoraPlantilla(horanow);                                                    // String
				  rocS[0].setRutCliente(0);  // int
				  rocS[0].setDigitoVerificador(' ');   // char
				  rocS[0].setTasaMontoFinal(tasaPropuestaLocal);
				  rocS[0].setIndVigencia('S');
				  rocS[0].setIndTipoPlantilla(obeanSpr.getInstanciaDeConsultaSpr(0).getIndTipoPlantillaCpt());
				  rocS[0].setCodigoMoneda(obeanSpr.getInstanciaDeConsultaSpr(0).getCodMonedaCpt());
				  rocS[0].setTasaMontoInformado(obeanSpr.getInstanciaDeConsultaSpr(0).getValTasaMonto());
				  rocS[0].setIndTipTasBas(obeanSpr.getInstanciaDeConsultaSpr(0).getIndTipTasBas());
				  rocS[0].setIndPerBasTas(obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas());
				  rocS[0].setInsBajPerBas(obeanSpr.getInstanciaDeConsultaSpr(0).getInsBajPerBas());
				  rocS[0].setIndSobPerBas(obeanSpr.getInstanciaDeConsultaSpr(0).getIndSobPerBas());
				  rocS[0].setIndBasTasVar(obeanSpr.getInstanciaDeConsultaSpr(0).getIndBasTasVar());
				  rocS[0].setIndTipFecPerRep(obeanSpr.getInstanciaDeConsultaSpr(0).getIndTipFecPerRep());
				  rocS[0].setNumPerRep(obeanSpr.getInstanciaDeConsultaSpr(0).getNumPerRep());
				  rocS[0].setIndPerRep(obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerRep());
				  rocS[0].setCostoFondo(obeanSpr.getInstanciaDeConsultaSpr(0).getCostoFondoInformado());
				  rocS[0].setFactorDeRiesgo((double) obeanSpr.getInstanciaDeConsultaSpr(0).getFactorRiesgoInformado());
                  if (getLogger().isDebugEnabled()){
                      getLogger().debug("rocS[0] *******************");
                      getLogger().debug("rocS[0].setCodSistema()      =" + rocS[0].getCodSistema());
                      getLogger().debug("rocS[0].setCodEvento()       =" + rocS[0].getCodEvento());
                      getLogger().debug("rocS[0].setCodigoConcepto()  =" + rocS[0].getCodigoConcepto());
                      getLogger().debug("rocS[0].setCodModalidad()    =" + rocS[0].getCodModalidad());
                      getLogger().debug("rocS[0].setCaiPlantilla()    =" + rocS[0].getCaiPlantilla());
                      getLogger().debug("rocS[0].setIicPlantilla()    =" + rocS[0].getIicPlantilla());
                      getLogger().debug("rocS[0].setFechaPlantilla()  =" + rocS[0].getFechaPlantilla());
                      getLogger().debug("rocS[0].setHoraPlantilla()   =" + rocS[0].getHoraPlantilla());
                      getLogger().debug("rocS[0].setIndCobro()        =" + rocS[0].getIndCobro());
                      getLogger().debug("rocS[0].setIndTipoPlantilla()=" + rocS[0].getIndTipoPlantilla());
                      getLogger().debug("rocS[0].setCodigoMoneda()    =" + rocS[0].getCodigoMoneda());
                      getLogger().debug("rocS[0].setIndVigencia()     =" + rocS[0].getIndVigencia());
                      getLogger().debug("rocS[0] ingresada... Tasa fue negociada !");
				  }
			  }

			  for (int i=0; i<obeanSpr.getTotOcurrencias();i++) {

				  rocS[i + indiceInclusion] = new InputIngresoRoc();
				  rocS[i + indiceInclusion].setCodSistema(obeanSpr.getInstanciaDeConsultaSpr(i).getCodSistemaCpt());
				  rocS[i + indiceInclusion].setCodEvento(obeanSpr.getInstanciaDeConsultaSpr(i).getCodEventoCpt());
				  rocS[i + indiceInclusion].setCodigoConcepto(obeanSpr.getInstanciaDeConsultaSpr(i).getCodConceptoCpt());
				  rocS[i + indiceInclusion].setCodModalidad(obeanSpr.getInstanciaDeConsultaSpr(i).getCodSubConcepto2Cpt());
				  rocS[i + indiceInclusion].setCaiPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getCaiConceptoCpt());
				  rocS[i + indiceInclusion].setIicPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getIicConceptoCpt());
				  rocS[i + indiceInclusion].setFechaPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getFecIniVigenciaCpt());
				  rocS[i + indiceInclusion].setHoraPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getHraIniVigenciaCpt());
				  rocS[i + indiceInclusion].setIndCobro(obeanSpr.getInstanciaDeConsultaSpr(i).getCodSubConcepto3Cpt());
				  rocS[i + indiceInclusion].setIndTipoPlantilla(obeanSpr.getInstanciaDeConsultaSpr(i).getIndTipoPlantillaCpt());
				  rocS[i + indiceInclusion].setCodigoMoneda(obeanSpr.getInstanciaDeConsultaSpr(i).getCodMonedaCpt());
				  rocS[i + indiceInclusion].setTasaMontoInformado(obeanSpr.getInstanciaDeConsultaSpr(i).getValTasaMonto());
				  rocS[i + indiceInclusion].setIndTipTasBas(obeanSpr.getInstanciaDeConsultaSpr(i).getIndTipTasBas());
				  rocS[i + indiceInclusion].setIndPerBasTas(obeanSpr.getInstanciaDeConsultaSpr(i).getIndPerBasTas());
				  rocS[i + indiceInclusion].setInsBajPerBas(obeanSpr.getInstanciaDeConsultaSpr(i).getInsBajPerBas());
				  rocS[i + indiceInclusion].setIndSobPerBas(obeanSpr.getInstanciaDeConsultaSpr(i).getIndSobPerBas());
				  rocS[i + indiceInclusion].setIndBasTasVar(obeanSpr.getInstanciaDeConsultaSpr(i).getIndBasTasVar());
				  rocS[i + indiceInclusion].setIndTipFecPerRep(obeanSpr.getInstanciaDeConsultaSpr(i).getIndTipFecPerRep());
				  rocS[i + indiceInclusion].setNumPerRep(obeanSpr.getInstanciaDeConsultaSpr(i).getNumPerRep());
				  rocS[i + indiceInclusion].setIndPerRep(obeanSpr.getInstanciaDeConsultaSpr(i).getIndPerRep());
				  rocS[i + indiceInclusion].setCostoFondo(obeanSpr.getInstanciaDeConsultaSpr(i).getCostoFondoInformado());
				  rocS[i + indiceInclusion].setFactorDeRiesgo((double) obeanSpr.getInstanciaDeConsultaSpr(i).getFactorRiesgoInformado());
				  rocS[i + indiceInclusion].setTasaMontoFinal(obeanSpr.getInstanciaDeConsultaSpr(i).getValMonto());

				  if (indiceInclusion > 0){
					  rocS[i + indiceInclusion].setIndVigencia('N');
				  }
				  else{
					  rocS[i + indiceInclusion].setIndVigencia('S');
				  }

				  if (obeanSpr.getInstanciaDeConsultaSpr(i).getIndTipoPlantillaCpt()=='P' && i==0) {
					  rocS[i + indiceInclusion].setTasaMontoFinal(obeanSpr.getInstanciaDeConsultaSpr(i).getValPropuesto());
				  }
                  if (getLogger().isDebugEnabled()){
                      getLogger().debug("set rocS[" + j + "] int");
                      getLogger().debug("rocS[" + (i + indiceInclusion) + "].setCodigoConcepto()=" + rocS[i + indiceInclusion].getCodigoConcepto());
                      getLogger().debug("rocS[" + (i + indiceInclusion) + "].setCaiPlantilla()="   + rocS[i + indiceInclusion].getCaiPlantilla());
                      getLogger().debug("rocS[" + (i + indiceInclusion) + "].setIicPlantilla()="   + rocS[i + indiceInclusion].getIicPlantilla());
                      getLogger().debug("rocS[" + (i + indiceInclusion) + "].setTasaMontoFinal()=" + rocS[i + indiceInclusion].getTasaMontoFinal());
				  }
				  j = i + indiceInclusion;
			  }


              if (getLogger().isDebugEnabled()) { getLogger().debug("================ PPC ======"); }
			  if (obeanPpc==null || obeanPpc.getTotOcurrencias()==0) {
                  if (getLogger().isDebugEnabled()) { getLogger().debug("PPC: SPR nulo o 0 ocurrencias."); }
			  } 
			  else {
				  for (int i=0; i<(obeanPpc.getTotOcurrencias());i++) {
					  j = j+1;
                      if (getLogger().isDebugEnabled()) { getLogger().debug("set rocS[" + j + "] ppc"); }
					  rocS[j] = new InputIngresoRoc();
					  rocS[j].setCodSistema(obeanPpc.getInstanciaDeConsultaSpr(i).getCodSistemaCpt());
					  rocS[j].setCodEvento(obeanPpc.getInstanciaDeConsultaSpr(i).getCodEventoCpt());
					  rocS[j].setCodigoConcepto(obeanPpc.getInstanciaDeConsultaSpr(i).getCodConceptoCpt());
					  rocS[j].setCodModalidad(obeanPpc.getInstanciaDeConsultaSpr(i).getCodSubConcepto2Cpt());
					  rocS[j].setCaiPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getCaiConceptoCpt());
					  rocS[j].setIicPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getIicConceptoCpt());
					  rocS[j].setFechaPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getFecIniVigenciaCpt());
					  rocS[j].setHoraPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getHraIniVigenciaCpt());
					  rocS[j].setIndCobro(obeanPpc.getInstanciaDeConsultaSpr(i).getCodSubConcepto3Cpt());
					  rocS[j].setIndTipoPlantilla(obeanPpc.getInstanciaDeConsultaSpr(i).getIndTipoPlantillaCpt());
					  rocS[j].setCodigoMoneda(obeanPpc.getInstanciaDeConsultaSpr(i).getCodMonedaCpt());
					  rocS[j].setIndVigencia('N');

                      if (getLogger().isDebugEnabled()){
                          getLogger().debug("rocS[" + (j) + "].setCodEvento()=" + rocS[j].getCodEvento());
                          getLogger().debug("rocS[" + (j) + "].setCodigoConcepto()=" + rocS[j].getCodigoConcepto());
                          getLogger().debug("rocS[" + (j) + "].setCodModalidad()=" + rocS[j].getCodModalidad());
                          getLogger().debug("rocS[" + (j) + "].setCaiPlantilla()=" + rocS[j].getCaiPlantilla());
                          getLogger().debug("rocS[" + (j) + "].setIicPlantilla()=" + rocS[j].getIicPlantilla());
                          getLogger().debug("rocS[" + (j) + "].setFechaPlantilla()=" + rocS[j].getFechaPlantilla());
                          getLogger().debug("rocS[" + (j) + "].setHoraPlantilla()=" + rocS[j].getHoraPlantilla());
                          getLogger().debug("rocS[" + (j) + "].setIndCobro()=" + rocS[j].getIndCobro());
                          getLogger().debug("rocS[" + (j) + "].setIndTipoPlantilla()=" + rocS[j].getIndTipoPlantilla());
                          getLogger().debug("rocS[" + (j) + "].setCodigoMoneda()=" + rocS[j].getCodigoMoneda());
                          getLogger().debug("rocS[" + (j) + "].setIndVigencia()=" + rocS[j].getIndVigencia());
					  }
				  }
			  }

              if (getLogger().isDebugEnabled()) { getLogger().debug("======== SEGUROS AVANCE ======"); }
			  if (obeanSprSGS == null || obeanSprSGS.getTotOcurrencias() == 0) {
                  if (getLogger().isDebugEnabled()) { getLogger().debug("SGS: SPR nulo o 0 ocurrencias"); }
			  } 
			  else {
                  if (getLogger().isDebugEnabled()) { getLogger().debug("roc[" + j + "] SGS ..."); }
				  for(int i = 0; i < obeanSprSGS.getTotOcurrencias(); i++){
					  j = j+1;
					  /** Seteo del objeto rocS, que será enviado a el ingreso de una OPC */
                      if (getLogger().isDebugEnabled()) {
                        getLogger().debug("****************************************************************************************");
                        getLogger().debug("set rocS[" + j + "] seguros");
                      }
					  rocS[j] = new InputIngresoRoc();
					  rocS[j].setCodSistema(arregloSPR[i].getCodSistemaCpt());                       // String
					  rocS[j].setCodEvento(arregloSPR[i].getCodEventoCpt());                         // String
					  rocS[j].setCodigoConcepto(arregloSPR[i].getCodConceptoCpt());                  // String
					  rocS[j].setCodigoSubConcepto(arregloSPR[i].getCodSubConcepto1Cpt());           // String
					  rocS[j].setCodModalidad(arregloSPR[i].getCodSubConcepto2Cpt());                // String
					  rocS[j].setIndCobro(arregloSPR[i].getCodSubConcepto3Cpt());                    // String
					  rocS[j].setCaiPlantilla(arregloSPR[i].getCaiConceptoCpt());                    // String
					  rocS[j].setIicPlantilla(arregloSPR[i].getIicConceptoCpt());                    // String
					  rocS[j].setIndTipoPlantilla(arregloSPR[i].getIndTipoPlantillaCpt());           // char
					  rocS[j].setCodigoMoneda(arregloSPR[i].getCodMonedaCpt());                      // String
					  rocS[j].setFechaPlantilla(arregloSPR[i].getFecIniVigenciaCpt());               // Date
					  rocS[j].setHoraPlantilla(arregloSPR[i].getHraIniVigenciaCpt());                // String

					  if(arregloSPR[i].getIndSeleccionado() == 'S')
						  rocS[j].setIndVigencia('S');                                               // char
					  else
						  if(arregloSPR[i].getIndSeleccionado() == 'P' && vectorSGS.contains("SIN_SEGUROS"))
							  rocS[j].setIndVigencia('S');                                           // char
						  else
							  if(vectorSGS.contains(arregloSPR[i].getCodSubConcepto1Cpt() + arregloSPR[i].getCodSubConcepto3Cpt()))
								  rocS[j].setIndVigencia('S');                                       // char
							  else
								  rocS[j].setIndVigencia('N');                                       // char

					  rocS[j].setRutCliente(Integer.parseInt(arregloSPR[i].getIicContextoRcc()));    // int
					  rocS[j].setDigitoVerificador(arregloSPR[i].getCaiContextoRcc().charAt(0));     // char

					  rocS[j].setTasaMontoFinal(arregloSPR[i].getValMonto());                        // double
					  rocS[j].setIndTipTasBas(arregloSPR[i].getIndTipTasBas());                      // char
					  rocS[j].setIndPerBasTas(arregloSPR[i].getIndPerBasTas());                      // char
					  rocS[j].setInsBajPerBas(arregloSPR[i].getInsBajPerBas());                      // char
					  rocS[j].setIndSobPerBas(arregloSPR[i].getIndSobPerBas());                      // char
					  rocS[j].setTasaMontoInformado(0D);
					  rocS[j].setIndBasTasVar(arregloSPR[i].getIndBasTasVar());                      // String
					  rocS[j].setIndTipFecPerRep(arregloSPR[i].getIndTipFecPerRep());                // char
					  rocS[j].setNumPerRep(arregloSPR[i].getNumPerRep());                            // int
					  rocS[j].setIndPerRep(arregloSPR[i].getIndPerRep());                            // char
					  rocS[j].setCostoFondo(arregloSPR[i].getCostoFondoInformado());                 // double
					  rocS[j].setFactorDeRiesgo(arregloSPR[i].getFactorRiesgoInformado());           // double

                      if (getLogger().isDebugEnabled()){
                          getLogger().debug("rocS["+ j +"].setCodSistema()             [" + rocS[j].getCodSistema()               + "]");
                          getLogger().debug("rocS["+ j +"].setCodEvento()              [" + rocS[j].getCodEvento()                + "]");
                          getLogger().debug("rocS["+ j +"].setCodigoConcepto()         [" + rocS[j].getCodigoConcepto()           + "]");
                          getLogger().debug("rocS["+ j +"].setCodigoSubConcepto()      [" + rocS[j].getCodigoSubConcepto()        + "]");
                          getLogger().debug("rocS["+ j +"].setCodModalidad()           [" + rocS[j].getCodModalidad()             + "]");
                          getLogger().debug("rocS["+ j +"].setIndCobro()               [" + rocS[j].getIndCobro()                 + "]");
                          getLogger().debug("rocS["+ j +"].setCaiPlantilla()           [" + rocS[j].getCaiPlantilla()             + "]");
                          getLogger().debug("rocS["+ j +"].setIicPlantilla()           [" + rocS[j].getIicPlantilla()             + "]");
                          getLogger().debug("rocS["+ j +"].setIndTipoPlantilla()       [" + rocS[j].getIndTipoPlantilla()         + "]");
                          getLogger().debug("rocS["+ j +"].setCodigoMoneda()           [" + rocS[j].getCodigoMoneda()             + "]");
                          getLogger().debug("rocS["+ j +"].setFechaPlantilla()         [" + rocS[j].getFechaPlantilla()           + "]");
                          getLogger().debug("rocS["+ j +"].setHoraPlantilla()          [" + rocS[j].getHoraPlantilla()            + "]");
                          getLogger().debug("rocS["+ j +"].setIndVigencia()            [" + rocS[j].getIndVigencia()              + "]");
                          getLogger().debug("rocS["+ j +"].setRutCliente()             [" + rocS[j].getRutCliente()               + "]");
                          getLogger().debug("rocS["+ j +"].setDigitoVerificador()      [" + rocS[j].getDigitoVerificador()        + "]");
                          getLogger().debug("rocS["+ j +"].setTasaMontoFinal()         [" + rocS[j].getTasaMontoFinal()           + "]");
                          getLogger().debug("rocS["+ j +"].setIndTipTasBas()           [" + rocS[j].getIndTipTasBas()             + "]");
                          getLogger().debug("rocS["+ j +"].setIndPerBasTas()           [" + rocS[j].getIndPerBasTas()             + "]");
                          getLogger().debug("rocS["+ j +"].setInsBajPerBas()           [" + rocS[j].getInsBajPerBas()             + "]");
                          getLogger().debug("rocS["+ j +"].setIndSobPerBas()           [" + rocS[j].getIndSobPerBas()             + "]");
                          getLogger().debug("rocS["+ j +"].setTasaMontoInformado()     [" + rocS[j].getTasaMontoInformado()       + "]");
                          getLogger().debug("rocS["+ j +"].setIndBasTasVar()           [" + rocS[j].getIndBasTasVar()             + "]");
                          getLogger().debug("rocS["+ j +"].setIndTipFecPerRep()        [" + rocS[j].getIndTipFecPerRep()          + "]");
                          getLogger().debug("rocS["+ j +"].setNumPerRep()              [" + rocS[j].getNumPerRep()                + "]");
                          getLogger().debug("rocS["+ j +"].setIndPerRep()              [" + rocS[j].getIndPerRep()                + "]");
                          getLogger().debug("rocS["+ j +"].setCostoFondo()             [" + rocS[j].getCostoFondo()               + "]");
                          getLogger().debug("rocS["+ j +"].setFactorDeRiesgo()         [" + rocS[j].getFactorDeRiesgo()           + "]");
					  }

					  /** Seteo objeto rocS, que será enviado a el ingreso de una OPC */
					  rocAmpliada[i] = new InputIngresoRocAmpliada();
					  rocAmpliada[i].setCaiOperacion(rocS[j].getCaiOperacion());
					  rocAmpliada[i].setIicOperacion(rocS[j].getIicOperacion());
					  rocAmpliada[i].setExtOperacion(rocS[j].getExtOperacion());
					  rocAmpliada[i].setCodSistema(rocS[j].getCodSistema());
					  rocAmpliada[i].setCodEvento(rocS[j].getCodEvento());
					  rocAmpliada[i].setNumero(rocS[j].getNumero());
					  rocAmpliada[i].setCodigoConcepto(rocS[j].getCodigoConcepto());
					  rocAmpliada[i].setCodigoSubConcepto(rocS[j].getCodigoSubConcepto());
					  rocAmpliada[i].setCodModalidad(rocS[j].getCodModalidad());
					  rocAmpliada[i].setIndCobro(rocS[j].getIndCobro());
					  rocAmpliada[i].setCaiPlantilla(rocS[j].getCaiPlantilla());
					  rocAmpliada[i].setIicPlantilla(rocS[j].getIicPlantilla());
					  rocAmpliada[i].setIndTipoPlantilla(rocS[j].getIndTipoPlantilla());
					  rocAmpliada[i].setCodigoMoneda(rocS[j].getCodigoMoneda());
					  rocAmpliada[i].setFechaPlantilla(rocS[j].getFechaPlantilla());
					  rocAmpliada[i].setHoraPlantilla(rocS[j].getHoraPlantilla());
					  rocAmpliada[i].setIndVigencia(rocS[j].getIndVigencia());
					  rocAmpliada[i].setCodAnulacion(rocS[j].getCodAnulacion());
					  rocAmpliada[i].setFechaUno(rocS[j].getFechaUno());
					  rocAmpliada[i].setRutCliente(rocS[j].getRutCliente());
					  rocAmpliada[i].setDigitoVerificador(rocS[j].getDigitoVerificador());
					  rocAmpliada[i].setFechaDos(rocS[j].getFechaDos());
					  rocAmpliada[i].setTasaMontoFinal(rocS[j].getTasaMontoFinal());
					  rocAmpliada[i].setIndTipTasBas(rocS[j].getIndTipTasBas());
					  rocAmpliada[i].setIndPerBasTas(rocS[j].getIndPerBasTas());
					  rocAmpliada[i].setInsBajPerBas(rocS[j].getInsBajPerBas());
					  rocAmpliada[i].setIndSobPerBas(rocS[j].getIndSobPerBas());
					  rocAmpliada[i].setTasaMontoInformado(rocS[j].getTasaMontoInformado());
					  rocAmpliada[i].setIndBasTasVar(rocS[j].getIndBasTasVar());
					  rocAmpliada[i].setIndTipFecPerRep(rocS[j].getIndTipFecPerRep());
					  rocAmpliada[i].setNumPerRep(rocS[j].getNumPerRep());
					  rocAmpliada[i].setIndPerRep(rocS[j].getIndPerRep());
					  rocAmpliada[i].setCostoFondo(rocS[j].getCostoFondo());
					  rocAmpliada[i].setFactorDeRiesgo(rocS[j].getFactorDeRiesgo());
					  rocAmpliada[i].setDescuentoAcumulado(0D); // double
					  rocAmpliada[i].setDescuentoAplicado(rocS[j].getTasaMontoInformado());   //Double
					  rocAmpliada[i].setGlosaTipoSeguro((TablaValores.getValor("multilinea.parametros", "seguros" + rocS[j].getCodigoSubConcepto(), rocS[j].getCodigoSubConcepto())));
					  rocAmpliada[i].setGlosaTipoCobro((TablaValores.getValor("multilinea.parametros", "tipoCobro" + arregloSPR[i].getCodSubConcepto3Cpt(), arregloSPR[i].getCodSubConcepto3Cpt())));
					  rocAmpliada[i].setIndSegObligatorio(arregloSPR[i].getIndSeleccionado()); // char
					  rocAmpliada[i].setTasaMinima(0D); // double
					  rocAmpliada[i].setDescuentoMaximo(0D); // double
					  rocAmpliada[i].setValMonto(arregloSPR[i].getValMonto()); // double
					  rocAmpliada[i].setTmcInformado(arregloSPR[i].getTmcInformado()); // double

					  //seteamos aval si lo hubiere para informacion DPS
					  rocAmpliada[i].setCodFactorRiesgoRcc(arregloSPR[i].getCodFactorRiesgoRcc());
					  rocAmpliada[i].setCodClfRentabilidadRcc(arregloSPR[i].getCodClfRentabilidadRcc());

					  if (rutAvalDPS > 0 ) {
						  rocAmpliada[i].setNumClienteRcc(rutAvalDPS);
						  rocAmpliada[i].setVrfClienteRcc(rutdigAvalDPS);
					  }


				  }
			  }

			  
			  
			  if (obeanSprCGN == null || obeanSprCGN.getTotOcurrencias() == 0) {
				  if (getLogger().isDebugEnabled()){
					  getLogger().debug("[avanceMultilinea] No existen ocurrencias en la consulta SPR para Gastos Notariales");
				  }
			  } 
			  else {
				  for(int i = 0; i < obeanSprCGN.getTotOcurrencias(); i++){
					  if (obeanSprCGN.getInstanciaDeConsultaSpr(i) == null){
						  if (getLogger().isDebugEnabled()){
							  getLogger().debug("[avanceMultilinea] Saliendo del llenando vector de SPR's Gastos Notariales");
						  }
						  break;
					  }
					  else{
						  vectorSPR.add(obeanSprCGN.getInstanciaDeConsultaSpr(i));
						  valorNotario = obeanSprCGN.getInstanciaDeConsultaSpr(i).getValMonto();
						  if (getLogger().isDebugEnabled()){ 
							  getLogger().debug("[avanceMultilinea] llenando vector de SPR's :" + i);
							  getLogger().debug("[avanceMultilinea] valorNotario: " +  valorNotario);
						  }
						  break;
					  }
				  }
			  }
			  
			  
			  if (getLogger().isDebugEnabled()) { 
				  getLogger().debug("[avanceMultilinea] ================ CGN ======"); 
		      }
			  if (obeanSprCGN==null || obeanSprCGN.getTotOcurrencias()==0) {
                  if (getLogger().isDebugEnabled()) { 
                	  getLogger().debug("[avanceMultilinea] CGN: SPR nulo o 0 ocurrencias."); 
                  }
			  } 
			  else {
				  
				  for(int i = 0; i < obeanSprCGN.getTotOcurrencias(); i++){
					  if (obeanSprCGN.getInstanciaDeConsultaSpr(i) == null){
						  if (getLogger().isDebugEnabled()) { 
							  getLogger().debug("[avanceMultilinea] Saliendo del llenando vector de SPR's Gastos Notariales");
						  }
						  break;
					  }
					  else{
						  j = j+1;
						  if (getLogger().isDebugEnabled()) { 
							  getLogger().debug("[avanceMultilinea] set rocS[" + j + "] ppc"); 
						  }
						  rocS[j] = new InputIngresoRoc();
						  rocS[j].setCodSistema(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodSistemaCpt());
						  rocS[j].setCodEvento(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodEventoCpt());
						  rocS[j].setCodigoConcepto(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodConceptoCpt());
						  rocS[j].setCodigoSubConcepto(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodSubConcepto1Cpt());
						  rocS[j].setCodModalidad(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodSubConcepto2Cpt());
						  rocS[j].setCaiPlantilla(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCaiConceptoCpt());
						  rocS[j].setIicPlantilla(obeanSprCGN.getInstanciaDeConsultaSpr(i).getIicConceptoCpt());
						  rocS[j].setFechaPlantilla(obeanSprCGN.getInstanciaDeConsultaSpr(i).getFecIniVigenciaCpt());
						  rocS[j].setHoraPlantilla(obeanSprCGN.getInstanciaDeConsultaSpr(i).getHraIniVigenciaCpt());
						  rocS[j].setIndCobro(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodSubConcepto3Cpt());
						  rocS[j].setIndTipoPlantilla(obeanSprCGN.getInstanciaDeConsultaSpr(i).getIndTipoPlantillaCpt());
						  rocS[j].setCodigoMoneda(obeanSprCGN.getInstanciaDeConsultaSpr(i).getCodMonedaCpt());
						  rocS[j].setIndVigencia(obeanSprCGN.getInstanciaDeConsultaSpr(i).getIndSeleccionado());
		                  rocS[j].setTasaMontoFinal(obeanSprCGN.getInstanciaDeConsultaSpr(i).getValMonto()); 

	                      if (getLogger().isDebugEnabled()){
	                          getLogger().debug("rocS[" + (j) + "].setCodEvento()=" + rocS[j].getCodEvento());
	                          getLogger().debug("rocS[" + (j) + "].setCodigoConcepto()=" + rocS[j].getCodigoConcepto());
	                          getLogger().debug("rocS[" + (j) + "].setCodigoSubConcepto()=" + rocS[j].getCodigoSubConcepto());
	                          getLogger().debug("rocS[" + (j) + "].setCodModalidad()=" + rocS[j].getCodModalidad());
	                          getLogger().debug("rocS[" + (j) + "].setCaiPlantilla()=" + rocS[j].getCaiPlantilla());
	                          getLogger().debug("rocS[" + (j) + "].setIicPlantilla()=" + rocS[j].getIicPlantilla());
	                          getLogger().debug("rocS[" + (j) + "].setFechaPlantilla()=" + rocS[j].getFechaPlantilla());
	                          getLogger().debug("rocS[" + (j) + "].setHoraPlantilla()=" + rocS[j].getHoraPlantilla());
	                          getLogger().debug("rocS[" + (j) + "].setIndCobro()=" + rocS[j].getIndCobro());
	                          getLogger().debug("rocS[" + (j) + "].setIndTipoPlantilla()=" + rocS[j].getIndTipoPlantilla());
	                          getLogger().debug("rocS[" + (j) + "].setCodigoMoneda()=" + rocS[j].getCodigoMoneda());
	                          getLogger().debug("rocS[" + (j) + "].setIndVigencia()=" + rocS[j].getIndVigencia());
	                          getLogger().debug("rocS[" + (j) + "].setTasaMontoFinal()=" + rocS[j].getTasaMontoFinal());
						  }
						  break;
					  }
				  }

			  }
			  
              if (getLogger().isDebugEnabled()) { getLogger().debug("========== LIQ ============"); }
			
			  InputLiquidacionDeOperacionDeCreditoOpc liqOpc = new InputLiquidacionDeOperacionDeCreditoOpc();
			  liqOpc.setCim_reqnum("032");
			  liqOpc.setCim_uniqueid(origen);
			  liqOpc.setCim_indseq(indseq);

              if (getLogger().isDebugEnabled()) { getLogger().debug("========== liquidacionCredito ============"); }
			  ResultLiquidacionDeOperacionDeCreditoOpc resLiqOpc = new ResultLiquidacionDeOperacionDeCreditoOpc();

              if (getLogger().isDebugEnabled()) { getLogger().debug("========== antes ============");  }
			  resLiqOpc =  operaCredito(multiEnvironment,
					  null,
					  opcS,
					  rdcS,
					  dlcS,
					  cyaS,
					  evcS,
					  icgS,
					  venS,
					  rocS,
					  liqOpc  );
              if (getLogger().isDebugEnabled()) { getLogger().debug("========== despues ============"); }

			  res.setResultLiqOpc(resLiqOpc);

			  if (consultaSeguros){
                  if (getLogger().isDebugEnabled()) { getLogger().debug("Asignando seguros en salida..."); }
				  // llamada a CGR con numero de operacion, para obtener valores consolidados de los seguros asociados.
				  ResultConsultaCgr obeanCGRSGS = new ResultConsultaCgr();

				  if (codigoSegDesgrav.equals("PRECIO")){
					  try{
                          if (getLogger().isDebugEnabled()) { getLogger().debug("Antes de obtieneDetalleSegurosPrecios ..."); }
						  obeanCGRSGS = obtieneDetalleSegurosPrecios(multiEnvironment,
								  resLiqOpc.getCaiOperacion(),      //("caiOperacion"),
								  resLiqOpc.getIicOperacion() + "", //("iicOperacion"),
								  "",                               //("extOperacion"),
								  "CRE",                            //("macroProducto"),
								  "CUR",                            //("codigoEvento"),
								  0,                                //Integer.parseInt(("rutCliente").trim()),
								  ' ',                              //("digitoVerificador").charAt(0),
								  ' ',                              //("idcCliente").charAt(0),
								  "",                               //("glsCliente"),
								  "",                               //("glsNomCliente"),
								  "SGS",                            //("codigoConcepto"),
								  'E',                              //("tipoConsulta").charAt(0),
								  ' ',                              //("siguiente").charAt(0),
								  "",                               //("caiOperacionNext"),
								  "",                               //("iicOperacionNext"),
								  "",                               //("extOperacionNext"),
								  "",                               //("codigoSistemaNext"),
								  "",                               //("codigoEventoNext"),
								  0);                               //Integer.parseInt(("numero").trim()));
					  } 
					  catch(Exception e) {
                          if (getLogger().isEnabledFor(Level.ERROR)) { getLogger().error("ERROR*** obtieneDetalleSegurosPrecios...Exception [" + e.getMessage() + "]"); }
					  }

                      if (getLogger().isDebugEnabled()) { getLogger().debug("ANTES del nuevo seteo de seguros avance"); }

					  if (obeanCGRSGS != null){
						  for(int i = 0; i < rocAmpliada.length; i++){
							  for(int k = 0; k < obeanCGRSGS.getInstanciaDeConsultaCgr().length; k++){
								  if(rocAmpliada[i].getCodigoSubConcepto().equals(obeanCGRSGS.getInstanciaDeConsultaCgr()[k].getCodSubConceptoInst())){
									  if(rocAmpliada[i].getCodModalidad().equals(obeanCGRSGS.getInstanciaDeConsultaCgr()[k].getCodModalidad())){
										  if(rocAmpliada[i].getIndCobro().equals(obeanCGRSGS.getInstanciaDeConsultaCgr()[k].getIndCobro())){
											  rocAmpliada[i].setTasaMontoFinal(obeanCGRSGS.getInstanciaDeConsultaCgr()[k].getTasaMontoFinal());
										  }
										  else{
											  continue;
										  }
									  }
									  else{
										  continue;
									  }
								  }
								  else{
									  continue;
								  }
							  }
						  }
					  }
				  }
                  if (getLogger().isDebugEnabled()) { getLogger().debug("DESPUES del nuevo seteo de seguros"); }
			  }
              if (getLogger().isDebugEnabled()) { getLogger().debug("Set Roc Ampliada...avance"); }
			  res.setArregloROC(rocAmpliada);
			  //******************************************//
			  //********  FIN ingresoCredito *************//
			  //******************************************//
              if (getLogger().isDebugEnabled()) { getLogger().debug("========== setResultLiqOpc ============"); }

			  if (!bandera.equals("S")){

                  if (getLogger().isDebugEnabled()) { getLogger().debug("llenando 'tasa web' [" + obeanSpr.getInstanciaDeConsultaSpr(0).getValPropuesto() + "]"); }

				  //si la tasa es anualizada (A o X) se 'muestra' la tasa dividida por 12 (CFRANCO)
                  if (getLogger().isDebugEnabled()) { getLogger().debug("llenando 'IndPerBasTas' [" + obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas() + "]"); }
				  if (obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas()=='A' || obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas()=='X') {
					  res.setTasa(opcS[0].getTasaSprea()/VALOR_12);
                      if (getLogger().isDebugEnabled()) { getLogger().debug("llenando 'tasa web' [" + opcS[0].getTasaSprea()/VALOR_12 + "]"); }
				  } 
				  else {
					  res.setTasa(opcS[0].getTasaSprea());
				  }

                  if (getLogger().isDebugEnabled()) { getLogger().debug("llenando 'tasa_suc' ...(" + obeanSpr.getInstanciaDeConsultaSpr(0).getValMonto() + ")"); }
				  if (obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas()=='A' || obeanSpr.getInstanciaDeConsultaSpr(0).getIndPerBasTas()=='X') {
					  res.setTasaOriginal(obeanSpr.getInstanciaDeConsultaSpr(0).getValMonto()/VALOR_12);
                      if (getLogger().isDebugEnabled()) { getLogger().debug("llenando 'tasa_suc' ...(" + obeanSpr.getInstanciaDeConsultaSpr(0).getValMonto()/VALOR_12 + ")"); }
				  } 
				  else {
					  res.setTasaOriginal(obeanSpr.getInstanciaDeConsultaSpr(0).getValMonto());
				  }
			  }
			  else{
                  if (getLogger().isDebugEnabled()) {
                    getLogger().debug("llenando ... Tasa fue negociada !");
                    getLogger().debug("llenando 'tasa web' [" + tasaPropuesta + "]");
                    getLogger().debug("llenando 'tasa_suc' [" + tasaPropuesta + "]");
                  }
				  res.setTasa(tasaPropuesta);
				  res.setTasaOriginal(tasaPropuesta);

			  }
			  //* hcf *//
              if (getLogger().isDebugEnabled()) { getLogger().debug("llenando 'condicionGarantia' ...(" + condicionGar + ")"); }
			  res.setCondicionGarantia(condicionGar);

			  //* hcf *//
              if (getLogger().isDebugEnabled()) { getLogger().debug("llenando 'fechaCurse' ...(" + fechaCurse + ")"); }
			  res.setFechaCurse(fechaCurse);
			  if (getLogger().isDebugEnabled()) {
				  getLogger().debug("llenando 'valorNotario' ...(" + valorNotario + ")"); 
			  }
			  res.setValorGastoNotarial(valorNotario);
              if (getLogger().isDebugEnabled()) { getLogger().debug("FIN avanceMultilinea (24042009) **************************"); }
			  return res;

		  } 
		  catch(Exception e) {
              if (getLogger().isEnabledFor(Level.ERROR)) { getLogger().error("ERROR***avanceMultilinea...Exception [" + e.getMessage() + "]"); }
			  if (datosLog!=null) {
				  datosLog.put("tipoOpe", "SIM");
				  datosLog.put("ERR",datosLog.get("ERR")==null? Utils.hackle(e.getMessage()): Utils.hackle(e.getMessage()) + datosLog.get("ERR"));
				  registraLogMultilinea(datosLog);
			  }
			  else {
                  if (getLogger().isEnabledFor(Level.ERROR)) { getLogger().error("datosLog es null"); }
			  }
			  throw new MultilineaException("ESPECIAL", e.getMessage());
		  }

	  }
	
	  /**
	     * Define linea de credito que se debe ocupar.
	     * <p>
	     * Registro de Versiones
	     * <ul>
	     * <li>1.0 (01/02/2016 Hector Carranza (Bee S.A) - Felipe Ojeda (ing.Soft.BCI) ): Version Inicial. </li>
	     *
	     * </ul>
	     * </p>
	     *
	     * @param multiEnvironment multiambiente.
	     * @param lineas arreglos de lineas de credito.
	     * @return tipo de linea a ocupar.
	     * @since 3.2
	     *
	     */
	    private String definirLDC(MultiEnvironment multiEnvironment, Linea[] lineas) {

	        if (getLogger().isDebugEnabled()) {
	            getLogger().debug("[definirLDC] - INICIO");
	        }
	        String clave         = "lineasdecredito";
	        String parametro     = TablaValores.getValor(ARCHIVO_PARAMETROS, clave, "vigencia");
	        boolean vigente      = parametro == null  ? false : parametro.trim().equalsIgnoreCase(RESPUESTA_ESPERADA);
	        String ldcPorDefecto = TablaValores.getValor(ARCHIVO_PARAMETROS, clave, "porDefecto");
	        String retorno       = ldcPorDefecto == null ? LINEA_POR_DEFECTO : ldcPorDefecto.trim();
	        if (vigente) {
	            if (lineas != null) {
	                if (lineas.length > 0) {
	                    boolean continua = true;
	                    parametro = TablaValores.getValor(ARCHIVO_PARAMETROS, clave, "ocupaCanal");
	                    if (getLogger().isDebugEnabled()) {
	                        getLogger().debug("[definirLDC] - ocupaCanal["+ parametro +"]");
	                    }
	                    vigente   = parametro == null  ? false : parametro.trim().equalsIgnoreCase(RESPUESTA_ESPERADA);
	                    if (vigente) {
	                        String listaCanalesHabilesAux = TablaValores.getValor(ARCHIVO_PARAMETROS, clave, "canalesHabiles");
	                        if (getLogger().isDebugEnabled()) {
	                            getLogger().debug("[definirLDC] - canal["+multiEnvironment.getCanal()+"] listaCanalesHabiles["+ listaCanalesHabilesAux +"]");
	                        }
	                        if (listaCanalesHabilesAux != null){
	                            String[] listaCanalesHabiles = StringUtil.divide(listaCanalesHabilesAux, ",");
	                            continua = StringUtil.estaContenidoEn(multiEnvironment.getCanal(), listaCanalesHabiles, true);
	                        }
	                    }
	                    if (continua) {
	                        String lista = TablaValores.getValor(ARCHIVO_PARAMETROS, clave, "listaTiposLineas");
	                        if (lista != null) {
	                            if (getLogger().isDebugEnabled()) {
	                                getLogger().debug("[definirLDC] - listaTiposLineas["+ lista +"]");
	                            }
	                            String[] codigos = StringUtil.divide(lista, ",");
	                            if (codigos != null){
	                                if (codigos.length > 0){
	                                    for (int i = 0; i < codigos.length; i++) {
	                                        if (buscarTipoLinea(codigos[i], lineas)) {
	                                            retorno = codigos[i];
	                                            break;
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        if (getLogger().isDebugEnabled()) {
	            getLogger().debug("[definirLDC] - retorno["+ retorno +"]");
	        }
	        return retorno;
	    }
	    	   
	    /**
	     * Lee las linea de credito para buscar la dada.
	     * <p>
	     * Registro de Versiones
	     * <ul>
	     * <li>1.0 (01/02/2016 Hector Carranza (Bee S.A) - Felipe Ojeda (ing.Soft.BCI) ): Version Inicial. </li>
	     *
	     * </ul>
	     * </p>
	     *
	     * @param tipoLinea a buscar en las lineas de credito.
	     * @param lineas arreglos de lineas de credito.
	     * @return verdadero si existe sino falso.
	     * @since 3.2
	     *
	     */
	    private boolean buscarTipoLinea(String tipoLinea, Linea[] lineas) {

	        boolean retorno = false;
	        if (lineas != null) {
	            if (lineas.length > 0) {
	                for (int i = 0; i < lineas.length; i++) {
	                    if (lineas[i] == null) {
	                        break;
	                    }
	                    if (lineas[i].getCodigoTipoLinea() != null){
	                        if (lineas[i].getCodigoTipoLinea().trim().equalsIgnoreCase(tipoLinea)){
	                            retorno = true;
	                            break;
	                        }
	                    }
	                }
	            }
	        }
	        return retorno;
	    }
	    
	    
	    /**
	     * Genera disclaimer con los datos de entrada.
	     * <p>
	     * Registro de Versiones
	     * <ul>
	     * <li>1.0 (03/08/2016 Manuel Escarate(Bee S.A) - Felipe Ojeda (ing.Soft.BCI) ): Version Inicial. </li>
	     *
	     * </ul>
	     * </p>
	     *
	     * @param datosDisclaimerTO datos para llenar el disclaimer.
	     * @return disclaimer en string.
	     * @since 3.6
	     *
	     */	    
		public String obtieneDisclaimerMandato(DatosDisclaimerTO datosDisclaimerTO){
	    	if (getLogger().isDebugEnabled()) {
	    		getLogger().debug("[obtieneDisclaimerMandato][" + datosDisclaimerTO.getRutCliente() + "][BCI_INI] datosDisclaimerTO:" + datosDisclaimerTO);
	    	}
			String disclaimer =  TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato1", "value")
					+ " " + datosDisclaimerTO.getNombreCliente()
					+ " " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato2", "value")
					+ " " + datosDisclaimerTO.getRutCliente()
					+ " " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato3", "value")
					+ " " + datosDisclaimerTO.getMonto()
					+ " " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato4", "value")
					+ " " + datosDisclaimerTO.getNombreempresa()
					+ " " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato5", "value")
					+ " " + datosDisclaimerTO.getRutEmpresa()
					+ " " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato6", "value") + ";" + TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato15", "value") + "\n"
					+ " " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato7", "value") + "\n" 
					+ " " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato8", "value") + "\n" 
					+ " " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato9", "value") + "\n" 
					+ " " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato10", "value") + "\n" 
					+ " " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato11", "value") + "\n" 
					+ " " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato12", "value") + "\n" + "\n"
					+ "_____________________________________________"+ "\n"
					+ TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato13", "value") + "\n"
					+ TablaValores.getValor(ARCHIVO_PARAMETROS , "pmandato14", "value");
			
	    	if (getLogger().isDebugEnabled()) {
	    		getLogger().debug("[obtieneDisclaimerMandato][" + datosDisclaimerTO.getRutCliente() + "][BCI_FINOK]");
	    	}
			return disclaimer;
		}
		
		
		 /**
	     * Genera datos de avales para el correo de contacto.
	     * <p>
	     * Registro de Versiones
	     * <ul>
	     * <li>1.0 (03/08/2016 Manuel Escarate(Bee S.A) - Felipe Ojeda (ing.Soft.BCI) ): Version Inicial. </li>
	     *
	     * </ul>
	     * </p>
	     *
	     * @param datosAval datos para el aval.
	     * @return aval en string.
	     * @since 3.6
	     *
	     */
		public String obtenerGlosasPorAval(DatosAvalesTO datosAval){
	    	if (getLogger().isDebugEnabled()) {
	    		getLogger().debug("[obtenerGlosasPorAval][" + datosAval.getRutAval() + "][BCI_INI] datosAvales:" + datosAval);
	    	}
	    	
	    	String bloqueAval = "                   _________________________________                      " +  "\n" 
	    	                  + "                      " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales1", "value")  + "                     " + "\n" + "\n"   
	    	                  + TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales2", "value")
	    	                  + " " + datosAval.getNombreAval() + "     " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales3", "value")
	    	                  + " " + datosAval.getRutAval() + "-" + datosAval.getDvAval() +  "\n" 
	    	                  + TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales4", "value") + " " + datosAval.getDireccionAval() + "    "
	    	                  + TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales5", "value") + " " + datosAval.getComunaAval() +  "   "
	    	                  + TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales6", "value") + " " + datosAval.getCiudadAval() + "\n" + "\n" 
	    	                  + TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales7", "value") + "\n"
	    	                  + TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales8", "value") 
	    	                  + "\n" + "\n";
	    	
	    	if (getLogger().isDebugEnabled()) {
	    		getLogger().debug("[obtenerGlosasPorAval][" + datosAval.getRutAval() + "][BCI_FINOK]");
	    	}
			return bloqueAval;
		}
		
		/**
		 * Genera datos de avales para el correo de contacto.
		 * <p>
		 * Registro de Versiones
		 * <ul>
		 * <li>1.0 (03/08/2016 Manuel Escarate(Bee S.A) - Felipe Ojeda (ing.Soft.BCI) ): Version Inicial. </li>
		 *
		 * </ul>
		 * </p>
		 *
		 * @return texto aval en string.
		 * @since 3.6
		 *
		 */	
		public String obtenerTextosParaAvales(){
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[obtenerTextosParaAvales][BCI_INI]");
			}

			String bloqueTextosAval  = "                    " + TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales9", "value") +"                      " +  "\n"  + "\n"
					+ TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales10", "value") + "\n" 
					+ TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales11", "value") + "\n"    
					+ TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales12", "value") + "\n"    
					+ TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales13", "value") + "\n"    
					+ TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales14", "value") + "\n"    
					+ TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales15", "value") + "\n"    
					+ TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales16", "value") + "\n"    
					+ TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales17", "value") + "\n"    
					+ TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales18", "value") + "\n"    
					+ TablaValores.getValor(ARCHIVO_PARAMETROS , "pavales19", "value") + "\n"    
					+ "\n" + "\n";

			if (getLogger().isDebugEnabled()) {
				getLogger().debug("[obtenerTextosParaAvales][BCI_FINOK]");
			}
			return bloqueTextosAval;
		}
		
}