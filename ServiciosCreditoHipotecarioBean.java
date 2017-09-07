package wcorp.serv.hipotecario;

import java.io.IOException;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.NamingException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import wcorp.aplicaciones.creditos.hipotecarios.dividendos.to.CertificadoDividendoTO;
import wcorp.aplicaciones.creditos.hipotecarios.dividendos.to.DeudaHipotecariaTO;
import wcorp.aplicaciones.creditos.hipotecarios.dividendos.to.DividendoImpagoTO;
import wcorp.aplicaciones.creditos.hipotecarios.dividendos.to.DividendoPagadoTO;
import wcorp.aplicaciones.creditos.hipotecarios.dividendos.to.DividendoTO;
import wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to.DatosInstanciaProcesoCHIPTO;
import wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to.EtapasChipTO;
import wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to.HitosChipTO;
import wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to.SeguimientoChipTO;
import wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to.SimulacionProcesoCHIPTO;
import wcorp.bprocess.cuentas.CuentasDelegate;
import wcorp.env.WCorpConfig;
import wcorp.env.util.WCorpUtils;
import wcorp.hipotecario.to.ProductoCreditoTO;
import wcorp.hipotecario.vo.AvisoVencimientoVO;
import wcorp.hipotecario.vo.CertificadoDFL2VO;
import wcorp.hipotecario.vo.CertificadoInteresesVO;
import wcorp.hipotecario.vo.CertificadosDisponiblesVO;
import wcorp.hipotecario.vo.DatosCalculoCAEVO;
import wcorp.hipotecario.vo.DatosOperacionVO;
import wcorp.hipotecario.vo.FichaSimulacionVO;
import wcorp.hipotecario.vo.GastosOperacionVO;
import wcorp.hipotecario.vo.ProductoVO;
import wcorp.hipotecario.vo.ResultCalculoCAEVO;
import wcorp.hipotecario.vo.SettingsVO;
import wcorp.hipotecario.vo.SimulacionPlazosTasasVO;
import wcorp.hipotecario.vo.SolicitudVO;
import wcorp.hipotecario.vo.TablaVO;
import wcorp.hipotecario.vo.TasasVO;
import wcorp.serv.direcciones.to.CiudadTO;
import wcorp.serv.direcciones.to.ComunaTO;
import wcorp.serv.direcciones.to.RegionTO;
import wcorp.serv.hipotecario.dao.DAOFactory;
import wcorp.serv.hipotecario.dao.DividendosDAO;
import wcorp.serv.hipotecario.dao.HipotecarioDAO;
import wcorp.serv.hipotecario.dao.SimulacionDAO;
import wcorp.serv.hipotecario.dao.jdbc.ProcesoCHIPDAO;
import wcorp.serv.hipotecario.dto.ProductoCreditoHipotecarioDTO;
import wcorp.serv.seguros.SeguroCliente;
import wcorp.util.GeneralException;
import wcorp.util.NumerosUtil;
import wcorp.util.StringUtil;
import wcorp.util.TablaValores;
import wcorp.util.TextosUtil;
import wcorp.util.com.JOLTPoolConnection;
import wcorp.util.com.TuxedoException;
import wcorp.util.workstation.ServicioNoDisponibleException;
import ws.bci.productos.servicios.colocaciones.creditos.serviciohipotecario.wscliente.ServicioConsultaEstadoSolicitudCHipPortType_Stub;
import ws.bci.productos.servicios.colocaciones.creditos.serviciohipotecario.wscliente.ServicioConsultaEstadoSolicitudCHipService;
import ws.bci.productos.servicios.colocaciones.creditos.serviciohipotecario.wscliente.ServicioConsultaEstadoSolicitudCHipService_Impl;
import bea.jolt.pool.ApplicationException;
import bea.jolt.pool.DataSet;
import bea.jolt.pool.Result;
import bea.jolt.pool.servlet.ServletSessionPool;
import cl.bci.chip.integracion.svc.DatosSolicitudCreditoTO;
import cl.bci.esb.common.HeaderRequest.ConsumerAnonType;
import cl.bci.esb.common.HeaderRequest.MessageAnonType;
import cl.bci.esb.common.HeaderRequest.RequestHeader;
import cl.bci.esb.common.HeaderRequest.ServiceAnonType;
import cl.bci.esb.productos.servicios.colocaciones.creditos.ServicioHipotecario.schema.ConsultaEstadoSolicitudCHipRequestBodytype;
import cl.bci.esb.productos.servicios.colocaciones.creditos.ServicioHipotecario.schema.ConsultaEstadoSolicitudCHipRequestSOAP;
import cl.bci.esb.productos.servicios.colocaciones.creditos.ServicioHipotecario.schema.ConsultaEstadoSolicitudCHipResponseBodytype;
import cl.bci.esb.productos.servicios.colocaciones.creditos.ServicioHipotecario.schema.ConsultaEstadoSolicitudCHipResponseSOAP;
import cl.bci.esb.productos.servicios.colocaciones.creditos.ServicioHipotecario.schema.ConsultaOperacionesPorRutRequestBodytype;
import cl.bci.esb.productos.servicios.colocaciones.creditos.ServicioHipotecario.schema.ConsultaOperacionesPorRutRequestSOAP;
import cl.bci.esb.productos.servicios.colocaciones.creditos.ServicioHipotecario.schema.ConsultaOperacionesPorRutResponseBodytype;
import cl.bci.esb.productos.servicios.colocaciones.creditos.ServicioHipotecario.schema.ConsultaOperacionesPorRutResponseSOAP;
import cl.bci.esb.productos.servicios.colocaciones.creditos.ServicioHipotecario.schema.Etapatype;
import cl.bci.esb.productos.servicios.colocaciones.creditos.ServicioHipotecario.schema.Hitotype;
import cl.bci.esb.productos.servicios.colocaciones.creditos.ServicioHipotecario.schema.Info_Generaltype;
import cl.bci.esb.productos.servicios.colocaciones.creditos.ServicioHipotecario.schema.SolicitudList;

import com.schema.util.XmlUtils;
import com.schema.util.dao.DAOFactoryTypes;

/**
 * <p>Title: ServiciosCreditoHipotecarioBean</p>
 * <p>Description: Enterprise Java Bean de Credito Hipotecario</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: BCI</p>
 * <p>
 * Registro de versiones:<ul>
 * <li>1.0 21/11/2002 Pablo Pantoja - Versión inicial
 * <li>2.0 15/02/2004 Francisco Sandoval (<b>schema Ltda.</b>) - Agrega los métodos para el Simulador Hipotecario
 * <li>2.5 09/08/2005 Francisco Sandoval (<b>schema Ltda.</b>) - Agrega método getAvisoVencimientoDividendo() para obtener el
 *                    Aviso de Vencimiento de un crédito hipotecario.
 * <li>3.0 23/09/2005 Marcelo Román (SEnTRA) - Se modifica el método generaPagoDividendo() y se sobrecarga dicho
 *                    método para que reciba la oficina y forma de pago del dividendo.
 * <li>3.1 03/10/2005 Oscar A. Otárola Torres (SEnTRA): Se modifica el método getHipDivImpago() para obtener un atributo más
 *                    desde el servicio tuxedo.
 * <li>3.2 11/10/2005 Francisco Sandoval, <b>schema Ltda.</b>) - Incorpora funcionalidad de productos por canal:<ul>
 *                    <li>Cambio de implementación del método getSettings(),  se cambia forma de obtener el valor de la UF, de la
 *                    clase obsoleta InfoFinanciera por clase utilitaria WCorpUtils.getValorUF(), por efectos de mejor
 *                    encapsulamiento y uniformidad de código (con otras aplicaciones)
 *                    <li>Agrega método getTablaProductoCanal(), el que obtiene la tabla de productos asociada al canal dado.
 *                    </ul>
 * <li>3.3 03/11/2005 Oscar A. Otárola Torres (SEnTRA): Se modifica el método generaPagoDividendo(). Se cambia nombre de
 *                    parámetro de entrada al servicio tuxedo. (oficina a oficinaPago).
 * <li>3.4 15/11/2005 Oscar A. Otárola Torres (SEnTRA)): Se modifican los métodos generaPagoDividendo() para que
 *                    reciban un atributo más (operador).
 * <li>3.5 06/02/2006 Alejandro Ituarte, <b>schema Ltda.</b>) - Incorpora metodos consultaCertificadosDisponibles,
 *                    consultaCertificadoIntereses y consultaCertificadoDfl2
 * <li>4.0 27/06/2006 Andrés Morán Ortiz, <b>SEnTRA </b>) - Incorpora método consultaDividendosPagados
 * <li>4.1 05/10/2006 Christopher Finch (ImageMaker IT) - Se agrega el método calculaSimulacionMonto para simular
 * 					  por monto del crédito y se modifica getSettings para obtener los plazos y meses de gracia.
 *                    Se deprecó el metodo getTasas. Se modificaron las salidas de las simulaciones hipotecaris
 *                    agregando los valores de las tasas.
 * <li>4.2 27/11/2006 Luis Cruz (Imagemaker IT): Se modifica metodo getSettings agregando control de excepción cuando
 *                    se obtiene la UF.
 * <li>4.3 19/12/2006 Luis Cruz (ImageMaker IT): Se agrega metodo {@link #guardarSimulacion(FichaSimulacionVO)}, para guardar
 *         las simulaciones realizadas por Portal y WS
 * <li>4.4 03/05/2007 Luis Cruz (ImageMaker IT): Se agrega método {@link #getProducto(int, String, int, String, String)} que permite
 *         realizar las consultas de productos por canal (último parámetro).
 * <li>5.0 23/05/2007 Marco Aicón D., <b>SEnTRA</b> - Incorpora métodos: pagaDividendoVerificaPACCliente para realizar el proceso de pago de dividendos y
 * 	 																		consultaDividendosPagadosMonto parra obtener los dividendos cancelados.
 * <li>5.1 26/12/2007 Diego Olivares Bolton (TInet): Se agrega método 
 *         {@link #obtenerDividendos(int)} que permite obtener los dividendos 
 *         pagados e impagos asociados a un número de operación. 
 *         
 * <li>5.2 21/01/2008 Gonzalo Oviedo (ADA Ltda.): Se agrega método traerListaCreditoHipotecario(), el cual trae una lista de los créditos
 *                                                hipotecarios del cliente, asociados a su rut. 																	
 * 
 * <li>5.3 30/07/2008 Pedro Carmona Escobar (SEnTRA): Se agregan y mejoran líneas de log en toda la clase.
 * <li>5.4 04/08/2008 Jessica Ramírez (ImageMaker IT): Se agrega método 
 *         {@link #efectuarPagoDividendo(String, String, String, String, String, String,
 *          HipConDivImpago, String)} que permite a un cliente realizar el pago de dividendos, además
 *          se agrega un nuevo parámetro el método {@link #pagaDividendoVerificaPACCliente(String, 
 *          String, String, String, String, String, String, String, String, String)} este nuevo 
 *          parámetro es útil para identificar que operación se va a realizar una "Consulta" o un "Pago".
 *          Cuando se trata de una "Consulta" permite determinar si el cliente puede o no realizar el pago
 *          de dividendo. En caso de un "Pago" se efectúa el pago de dividendo.
 * <li>5.5  27/10/2008, Yasmin Rocha Alvarez (Global Works S.A.): Dentro del método efectuarPagoDividendo(...) se cambia el
 *          método movCtaCte(...) por el método movCtaStvarios3p(...) que realiza el cargo a una cuenta.</li>
* <li>5.6  27/01/2009, José Flores (TINet): Se agregan cuatro nuevos métodos, para cubrir los nuevos
 *          créditos, BCI Home Mixto, y BCI Home Clásico mas crédito complementario, además de un método
 *          destinado a los seguros vinculados, estos métodos son
 *          <ul>
 *          <li>simulacionConComplementario()</li>
 *          <li>detalleCreditoConComplementario()</li>
 *          <li>simulacionMixto()</li>
 *          <li>detalleMixto()</li>
 *          <li>segurosVinculados()</li>
 *          </ul></li>
 * <li>5.6 09/07/2009 Yon Sing Sius (ImageMaker IT): Se modifica metodo {@link #getProducto(int, String, int, String, String)}
 *          para realizar el filtro del producto por el canal enviado. </li>
 * <li>5.7 27/08/2009 Patricio Valenzuela S. (Sermaluc Ltda): Se agrega metodo {@link #listaSegurosVinculados()} para generar listado
 * 			seguros adicionales usada en el Simulador </li> 
 * <li>5.8 30/09/2009 Carlos Cerda Iglesias (SEnTRA): Se agrega catch para capturar Exception en los métodos:
 * 			<ul>
 *     			<li>{@link #getListaCreditoHipotecario(long)}</li>
 *          	<li>{@link #getDetalleCreditoHipotecario(String)}</li>
 *          	<li>{@link #getLiqPrepCredHipExt(String, Date)}</li>
 *          	<li>{@link #getHipConDivCanCre(String)}</li>
 *         		<li>{@link #getHipDivImpago(String)}</li>
 *         		<li>{@link #getHipConDivImpago(String)}</li>
 *         		<li>{@link #getHipConLetraAsig(String)}</li>
 *         		<li>{@link #getDetalleDfl2(String)}</li>
 *         		<li>{@link #getDivImpago(String, String, long, char)}</li>
 *         		<li>{@link #getDetalleInteres(String)}</li>
 *         		<li>{@link #generaPagoDividendo(String, String, String, String, String, String, String, String, String)}</li>
 *         		<li>{@link #traerListaCreditoHipotecario(long)}</li>
 *			</ul>
 * 			y se cambian los códigos de las excepciones lanzadas en los siguientes métodos: 
 * 			<ul>
 *     			<li>{@link #getSettings(String)}</li>
 *          	<li>{@link #selectSolicitudes(long)}</li>
 *          	<li>{@link #calculaSimulacion(DatosOperacionVO)}</li>
 *          	<li>{@link #calculaSimulacionNegociada(DatosOperacionVO)}</li>
 *         		<li>{@link #calculaSimulacionPromocion(DatosOperacionVO)}</li>
 *         		<li>{@link #calculaSimulacionProyeccion(DatosOperacionVO)}</li>
 *         		<li>{@link #ingresaSolicitud(SolicitudVO)}</li>
 *         		<li>{@link #ingresaSolicitud(SolicitudVO, GastosOperacionVO)}</li>
 *         		<li>{@link #calculaGastosOperacion(DatosOperacionVO)}</li>
 *         		<li>{@link #getTabla(String)}</li>
 *         		<li>{@link #getTablaProducto()}</li>
 *         		<li>{@link #getTablaProducto(String)}</li>
 *         		<li>{@link #getTablaProductoCanal()}</li>
 *         		<li>{@link #getEstadoSolicitud(long, int)}</li>
 *         		<li>{@link #getRangosCreditoProducto(ProductoVO, String, int, String)}</li>
 *         		<li>{@link #getProducto(int, String, int, String, String)}</li>
 *         		<li>{@link #getTasas(DatosOperacionVO)}</li>
 *         		<li>{@link #calculaSimulacionMonto(DatosOperacionVO)}</li>
 *         		<li>{@link #guardarSimulacion(FichaSimulacionVO)}</li>
 *			</ul></li>     
 * <li>5.9  06/11/2009 Pedro Carmona Escobar (SEnTRA):Se agregan métodos {@link #obtenerCertificadoDividendosPagados(String)},
 * 														{@link #obtenerCertificadoInteresesPagados(String, int)},
 * 														{@link #obtenerDeudaHipotecaria(String)}.
 * <li>5.9.1 27/10/2010 Elías Zacarías Vilches (Sermaluc):Se modifica método  {@link #getProducto(int, String, int, String, String)}.                
 * <li>5.9.2 04/10/2011 Jorge San Martín (ImageMaker IT): Se agrega método {@link #calculaCAE(DatosCalculoCAEVO)}
 * <li>5.9.3 18/05/2012 Jorge San Martín (ImageMaker IT): Se agrega log en caso de recibir una exception desde el 
 *     dao, en el método {@link #calculaCAE(DatosCalculoCAEVO)}.
 * <li>5.9.4 02/08/2012 Cristian Recabarren R. (Sermaluc ltda.): Se agregan los siguientes métodos:
 * 											{@link #obtenerRegiones()},{@link #obtenerCiudadesPorRegion(String)} y {@link #obtenerComunasPorCiudad(String)}
 * <li>5.9.5 27/11/2012 Cristian Recabarren R. (Sermaluc ltda.): Se agrega el método {@link #getTablaProducto(String, int)}
 * <li>5.9.6 08/07/2013 Cristian Recabarren R. (Sermaluc ltda.): Se agrega el método 
 * {@link #calculaSimulacionTotal(DatosOperacionVO)} y 
 *                        {@link #calculaSimulacionRenegociadaTotal(DatosOperacionVO)}
 *                        Se regulariza normativa log.
 * <li>6.0   17/10/2013 Yasmin Rocha, Esteban Ramirez (Orand S.A.): Se exportan los métodos de Hipotecario Asicom a HipotecarioDAO.  Además se modifica
 *                      el método {@link #obtenerDividendos(String)}</li>
 * <li>6.1   17/11/2014 Victor Caroca V. (SEnTRA): Se crearon los nuevos Metodos 
 *                      el método {@link #detalleSolicitud(long, String) 
 *                      y {@link #obtenerSolicitudCHIP(long, char)}</li>
 * <li>6.2 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): 
 *  Se agregan los siguientes metodos para el proyecto Integración Web Services para CHIP:
 *<ul>
 *<li>
 *{@link #convertirDatosInstanciaADatosWsCHIP(DatosInstanciaProcesoCHIPTO)}
 *<li>{@link #generarInstanciaProcesoCHIP(DatosInstanciaProcesoCHIPTO)}
 *</li> <li>
 *{@link #guardarSimulacionProcesoCHIP(SimulacionProcesoCHIPTO)}</li> 
 *<li>{@link #marcarSimulacionProcesoCHIP(long)}</li> <li>
 *{@link #normalizarASCII(String)}</li> <li>
 *{@link #poseeProcesoCHIPVigente(long)}</li> <li>
 *{@link #registrarInicioProcesoCHIP(long)}</li>
 * </ul>
 * Además se agregan los atributos:
 * {@link #COD_RESP_SI} y {@link COD_RESP_NO}
 *</ul>
 * <p>
 *
 * <B>Todos los derechos reservados por Banco de Crédito e Inversiones.</B>
 * <P>
 */
public class ServiciosCreditoHipotecarioBean implements SessionBean {

    /**
     * Identificador del error al momento de consultar por los dividendos. 
     */
    public static final String ERROR_RECUPERANDO_DIVIDENDOS = "ERROR_RECUPERANDO_DIVIDENDOS";
    
    /**
     * Serial.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Tabla de parametros para hipotecario.
     */
    private static final String TABLA_PARAMETRO = "hipotecarios.parametros";
    
    /**
     * Canal utilizado para consultar tablas.   Este código indica que el manejo
     * de canal esta deshabilitado.
     */
    private static final String CODIGO_CONSULTA_SIN_CANAL = "SIN_CANAL";
    
    /**
     * Formato de las fechas.
     */
    private static final String FORMATO_FECHA = "dd/MM/yyyy";
    
    /**
     * Código para almacenar el valor SI en los atributos que lo requieren.
     */
    private static final String COD_RESP_SI = "S";

    /**
     * Código para almacenar el valor NO en los atributos que lo requieren.
     */
    private static final String COD_RESP_NO = "N";   
    
    private SessionContext sessionContext;
    private String ejbName = this.getClass().getName();
    private JOLTPoolConnection joltPool;
    private SimulacionDAO simulacionDAO;
    
    /**
    *DAO para el proceso chip.
    */
    private ProcesoCHIPDAO procesoCHIPDAO;

    private DividendosDAO dividendosDAO;

    /*
    * Clase que contiene la implementación del servicio llamado.
    */
    private HipotecarioDAO hipotecarioDAO;
    private static final String SOLICITUDES_SETTINGS = "Solicitudes.parametros";
    static private final String PAGO_DIVIDENDO_SETTING = "PagoDividendos.parametros";
    private static final String USUARIO = TablaValores.getValor(PAGO_DIVIDENDO_SETTING, "operadorSN", "Desc");

    private transient Logger logger = Logger.getLogger(ServiciosCreditoHipotecarioBean.class);

    /**
     * Atributo que permite acceder al servicioCuentasBean
     */
    private CuentasDelegate cuentasDelegate = null;
    
    /**
     * Tabla de parámetros que contienes datos para el Pago de Dividendo.
     */
    private static final String TABLA_PARAMETRO_PAGO_DIVIDENDOS = "PagoDividendos.parametros";
    
    /**
     * Indica el éxito del pago de dividendo.
     */
    private static final String PAGO_DIVIDENDOS_EXITO = (String) (TablaValores.getValor(
            TABLA_PARAMETRO_PAGO_DIVIDENDOS, "codError", "exitoPago"));
    
    /**
     * Indica que ocurrió un error al realizar el  cargo a la cuenta corriente.
     */
    private static final String PAGO_DIVIDENDOS_ERROR_CARGO = (String) (
            TablaValores.getValor(TABLA_PARAMETRO_PAGO_DIVIDENDOS, "codError", "errorCargo"));
    
    /**
     * Indica que ocurrió un error al realizar el pago de dividendo.
     */
    private static final String PAGO_DIVIDENDOS_ERROR_PAGO = (String) (TablaValores.getValor(
            TABLA_PARAMETRO_PAGO_DIVIDENDOS, "codError", "errorSistema"));
    
    /**
     * Indica que se debe realizar el pago
     */
    private static final String SERVICIO_PAGO = "P";
    
    /**
     * Indica que se debe realizar la consulta 
     */
    private static final String SERVICIO_CONSULTA = "C";
    
    /**
     * Indica que el cliete posee PAC.
     */
    private static final String CLIENTE_POSEE_PAC = (String) (TablaValores.getValor(
            TABLA_PARAMETRO_PAGO_DIVIDENDOS, "estadosPago", "poseePAC"));

    /**
     * Entrega lista de créditos hipotecarios de un cliente.
     * <p>
     * 
     * Registro de versiones:<ul>
     * <li>1.0 ../../.... Desconocido
     * <li>2.0 11/03/2013 Yasmin Rocha (Orand): Se importa el método desde ServicioHipotecarioBean.java. 
     *     Se migra a web services inicialmente es opcional.</li>
     * </ul></p>
     * @param rut rut del cliente.
     * @return lista de los productos.
     * @throws TuxedoException En caso error en Tuxedo
     * @throws GeneralException  En caso de ocurrir un error.
     * @throws RemoteException En caso de error de conexion
     */
     public ProductoCreditoHipotecario[] getListaCreditoHipotecario( long rut )
    throws TuxedoException, GeneralException, RemoteException {

     try {

        logger.info("[getListaCreditoHipotecario] ejb");
        return hipotecarioDAO.getListaCreditoHipotecario(rut);
    }
    catch (TuxedoException e) {
        throw e;
    }
    catch (GeneralException e) {
        throw e;
    }
   }


	 /**
      * Entrega un detalle de los creditos hipotecarios.
      * <p>
      * 
      * Registro de versiones:<ul>
      * <li>1.0 ../../.... Desconocido
      * <li>2.0 11/03/2013 Esteban Ramirez (Orand): Se importa el método desde ServicioHipotecarioBean.java. 
      *     Se migra a web services inicialmente es opcional.</li>
      * </ul></p>
      * @param numope número de operacion.
      * @return detalle un crédito hipotecario
      * @throws TuxedoException En caso error en Tuxedo
      * @throws GeneralException  En caso de ocurrir un error.
      * @throws RemoteException En caso de error de conexion
      */
     public DetalleCreditoHipotecario getDetalleCreditoHipotecario( String numope )
          throws TuxedoException, GeneralException, RemoteException {
	    logger.debug("[getDetalleCreditoHipotecario]::inicio método Bean");
	    return hipotecarioDAO.getDetalleCreditoHipotecario(numope);
     }

	
	/**
     * Entrega un detalle de los creditos hipotecarios.
     * <p>
     * 
     * Registro de versiones:<ul>
     * <li>1.0 ../../.... Desconocido
     * <li>2.0 11/03/2013 Esteban Ramirez (Orand): Se importa el método desde ServicioHipotecarioBean.java. 
     *     Se migra a web services inicialmente es opcional.</li>
     * </ul></p>
     * @param numope número de operacion.
	 * @param fecha fecha de la operación.
     * @return detalle un crédito hipotecario
     * @throws TuxedoException En caso error en Tuxedo
     * @throws GeneralException  En caso de ocurrir un error.
     * @throws RemoteException En caso de error de conexion 
     * @since 6.0
     */
    public LiqPrepCredHipExt getLiqPrepCredHipExt(String numope, Date fecha) throws TuxedoException,
        GeneralException, RemoteException {
        logger.debug("[getLiqPrepCredHipExt]::inicio método Bean");
        return hipotecarioDAO.getLiqPrepCredHipExt(numope, fecha);
    }
     public HipConDivCanCre getHipConDivCanCre( String numOperacion )
          throws TuxedoException, GeneralException, RemoteException {
    	  
    	  getLogger().info( "[getHipConDivCanCre] numOperacion: "+numOperacion );
          ServletSessionPool sesion = joltPool.getSesion( ejbName );
          Result resultado = null;
          DataSet parametros = new DataSet();
          HipConDivCanCre hipConDivCanCre = null;

          parametros.setValue( "numOperacion", numOperacion );

          try {
        	   getLogger().info( "[getHipConDivCanCre] antes llamado HipConDivCanCre" );
               resultado = sesion.call( "HipConDivCanCre", parametros, null );
               hipConDivCanCre = new HipConDivCanCre();

               hipConDivCanCre.estado = Double.parseDouble( ( ( String )resultado.getValue(
                    "estado", "0" ) ).replace( ',', '.' ) );
               hipConDivCanCre.numDividendo = Double.parseDouble( ( ( String )resultado.getValue(
                    "numDividendo", "0" ) ).replace( ',', '.' ) );
               hipConDivCanCre.amortizacion = Double.parseDouble( ( ( String )resultado.getValue(
                    "amortizacion", "0" ) ).replace( ',', '.' ) );
               hipConDivCanCre.intereses = Double.parseDouble( ( ( String )resultado.getValue(
                    "intereses", "0" ) ).replace( ',', '.' ) );
               hipConDivCanCre.comision = Double.parseDouble( ( ( String )resultado.getValue(
                    "comision", "0" ) ).replace( ',', '.' ) );
               hipConDivCanCre.seguroIncendio = Double.parseDouble( ( ( String )resultado.getValue(
                    "seguroIncendio", "0" ) ).replace( ',', '.' ) );
               hipConDivCanCre.seguroDesgravamen = Double.parseDouble( ( ( String )resultado.
                    getValue( "seguroDesgravamen", "0" ) ).replace( ',', '.' ) );
               hipConDivCanCre.seguroCesantia = Double.parseDouble( ( ( String )resultado.getValue(
                    "seguroCesantia", "0" ) ).replace( ',', '.' ) );
               hipConDivCanCre.subTotal = Double.parseDouble( ( ( String )resultado.getValue(
                    "subTotal", "0" ) ).replace( ',', '.' ) );
               hipConDivCanCre.interesPenal = Double.parseDouble( ( ( String )resultado.getValue(
                    "interesPenal", "0" ) ).replace( ',', '.' ) );
               hipConDivCanCre.otrosCargos = Double.parseDouble( ( ( String )resultado.getValue(
                    "otrosCargos", "0" ) ).replace( ',', '.' ) );
               hipConDivCanCre.totalPagado = Double.parseDouble( ( ( String )resultado.getValue(
                    "totalPagado", "0" ) ).replace( ',', '.' ) );
               hipConDivCanCre.fechaPago = ( String )resultado.getValue( "fechaPago", null );
               hipConDivCanCre.estadoDividendo = ( String )resultado.getValue( "estadoDividendo", null );
               hipConDivCanCre.fecVencimiento = ( String )resultado.getValue( "fecVencimiento", null );
               hipConDivCanCre.fechaContable = ( String )resultado.getValue( "fechaContable", null );
               hipConDivCanCre.oficinaPago = ( String )resultado.getValue( "oficinaPago", null );
               getLogger().info( "[getHipConDivCanCre] hipConDivCanCre: "+hipConDivCanCre );
          }
          catch ( ApplicationException e ) {
               // Errores Aplicativos
        	   getLogger().error( "[getHipConDivCanCre] ERROR: "+e.toString() );
               resultado = e.getResult();
               logger( "/",
                       ( String )resultado.getValue( "codigoError", 0, "[Sin Codigo Error]" ),
                       ( String )resultado.getValue( "descripcionError", 0,
                    "[Sin Descripcion de Error]" ),
                       String.valueOf( resultado.getApplicationCode() ), e );

               if ( resultado.getApplicationCode() == 0 ) {
                    //Error de Negocio Cobro Comisiones
                    throw new HipotecarioException( ( String )resultado.getValue( "codigoError", 0,
                         "DESC" ),
                         ( String )resultado.getValue( "descripcionError", 0, "[Sin Informacion]" ) );
               }
               else {
                    // Error Tuxedo
                    throw new wcorp.util.com.TuxedoException
                         ( ( String )( resultado.getApplicationCode() == 1 ?
                                       resultado.getValue( "codigoError", 0, "DESC" ) : "TUX" ) );
               }
          }
		catch (Exception ex){
			getLogger().info( "[getListaCreditoHipotecario] Exception: "+ex.toString());
			throw new wcorp.util.GeneralException("TUX");
		}
          return hipConDivCanCre;
     }

    /**
     * Obtiene un arreglo con los dividendo impagos de un Crédito Hipotecario
     *
     *  @param  numOperacion  Código Operación
     *
     *  @return HipConDivImpago Clase.
     *
     *  @exception TuxedoException
     *  @exception RemoteException
     *  @exception GeneralException
     *
     * Registro de versiones:
     * 1.0 15/10/2004 Claudia Castañón (Novared) - version inicial
     * 1.1 03/10/2005 Oscar A. Otárola Torres (SEnTRA): Se modifica el método para
     *                obtener un atributo más desde el servicio tuxedo.(descripcion)
     * 1.2 13/03/2013 Esteban Ramirez (Orand): Se migra método a Hipotecario DAO.
     *
     * @version 1.0
     */
	 public HipConDivImpago[] getHipDivImpago(String numOperacion) throws TuxedoException, GeneralException,
        RemoteException {
        logger.debug("[getHipDivImpago]::inicio método Bean");
        return hipotecarioDAO.getHipDivImpago(numOperacion);
    }

	 /**
      * Obtiene dividendo impago de un Crédito Hipotecario
      *
      *  @param  numOperacion  Código Operación
      *
      *  @return HipConDivImpago Clase.
      *
      *  @exception TuxedoException
      *  @exception RemoteException
      *  @exception GeneralException
      *
      * Registro de versiones:
      * <li>1.0 ../../.... Desconocido</li>
      * <li>1.1 13/03/2013 Esteban Ramirez (Orand): Se migra método a Hipotecario DAO.</li>
      *
      */
    public HipConDivImpago getHipConDivImpago(String numOperacion) throws TuxedoException, GeneralException,
        RemoteException {

     logger.info("[getHipConDivImpago] numOperacion = " + numOperacion);
     return hipotecarioDAO.getHipDivImpago(numOperacion)[0];
    }


     public HipConLetraAsig getHipConLetraAsig( String numOperacion )
          throws TuxedoException, GeneralException, RemoteException {

    	  getLogger().info( "[getHipConLetraAsig] numOperacion= "+numOperacion);
          ServletSessionPool sesion = joltPool.getSesion( ejbName );
          Result resultado = null;
          DataSet parametros = new DataSet();
          HipConLetraAsig hipConLetraAsig = null;

          parametros.setValue( "numOperacion", numOperacion );

          try {
        	   getLogger().info( "[getHipConLetraAsig] antes de llamado a HipConLetraAsig");
               resultado = sesion.call( "HipConLetraAsig", parametros, null );
               hipConLetraAsig = new HipConLetraAsig();

               hipConLetraAsig.estado = Double.parseDouble( ( ( String )resultado.getValue(
                    "estado", "0" ) ).replace( ',', '.' ) );
               hipConLetraAsig.folio = Double.parseDouble( ( ( String )resultado.getValue( "folio",
                    "0" ) ).replace( ',', '.' ) );
               hipConLetraAsig.numCorte = Double.parseDouble( ( ( String )resultado.getValue(
                    "numCorte", "0" ) ).replace( ',', '.' ) );
               hipConLetraAsig.numActa = Double.parseDouble( ( ( String )resultado.getValue(
                    "numActa", "0" ) ).replace( ',', '.' ) );
               hipConLetraAsig.estadoLetra = Double.parseDouble( ( ( String )resultado.getValue(
                    "estadoLetra", "0" ) ).replace( ',', '.' ) );
               hipConLetraAsig.ultimoCupon = Double.parseDouble( ( ( String )resultado.getValue(
                    "ultimoCupon", "0" ) ).replace( ',', '.' ) );
               hipConLetraAsig.numCertificado = Double.parseDouble( ( ( String )resultado.getValue(
                    "numCertificado", "0" ) ).replace( ',', '.' ) );
               hipConLetraAsig.numeroSerial = ( String )resultado.getValue( "numeroSerial", null );
               hipConLetraAsig.rut = ( String )resultado.getValue( "rut", null );
               hipConLetraAsig.nombre = ( String )resultado.getValue( "nombre", null );
               getLogger().info( "[getHipConLetraAsig] hipConLetraAsig: "+hipConLetraAsig);
          }
          catch ( ApplicationException e ) {
               // Errores Aplicativos
        	   getLogger().error( "[getHipConLetraAsig] ERROR: "+e.toString());
               resultado = e.getResult();
               logger( "/",
                       ( String )resultado.getValue( "codigoError", 0, "[Sin Codigo Error]" ),
                       ( String )resultado.getValue( "descripcionError", 0,
                    "[Sin Descripcion de Error]" ),
                       String.valueOf( resultado.getApplicationCode() ), e );

               if ( resultado.getApplicationCode() == 0 ) {
                    //Error de Negocio Cobro Comisiones
                    throw new HipotecarioException( ( String )resultado.getValue( "codigoError", 0,
                         "DESC" ),
                         ( String )resultado.getValue( "descripcionError", 0, "[Sin Informacion]" ) );
               }
               else {
                    // Error Tuxedo
                    throw new wcorp.util.com.TuxedoException
                         ( ( String )( resultado.getApplicationCode() == 1 ?
                                       resultado.getValue( "codigoError", 0, "DESC" ) : "TUX" ) );
               }
          }
		catch (Exception ex){
			getLogger().info( "[getListaCreditoHipotecario] Exception: "+ex.toString());
			throw new wcorp.util.GeneralException("TUX");
		}
          return hipConLetraAsig;
     }


    /**
	 * Obtiene datos para la impresión de certificado de dividendos.
	 *
	 *  @param  numOperacion  Código Operación
	 *
	 *  @return DetalleDfl2 Clase.
	 *
	 *  @exception TuxedoException
	 *  @exception RemoteException
	 *  @exception GeneralException
	 *
	 * Registro de versiones:
	 * 1.0 ../../.. Desconocido - version inicial
     * 1.1 13/03/2013 Esteban Ramirez (Orand): Se migra método a Hipotecario DAO.
     *
     * @version 1.0
     */
    public DetalleDfl2 getDetalleDfl2(String numOperacion) throws TuxedoException, GeneralException,
        RemoteException {
        logger.debug("[getDetalleDfl2]::inicio método Bean");
        return hipotecarioDAO.getDetalleDfl2(numOperacion);
    }
	/**
	 * Obtiene los datos los dividendo impagos de un Crédito Hipotecario.
	 * <p>
	 * Registro de versiones:<ul>
	 * <li>1.0 15/10/2004 Claudia Castañón (Novared) - version inicial</li>
	 * <li>2.0 11/03/2013 Esteban Ramirez (Orand) - se migra a web services, que inicialmente es opcional.</li>
	 * <ul></p>
	 * @param numOperacion Código Operación
     * @param nroDividendo Identificador del dividendo.
     * @param rut rut del cliente.
     * @param dv dig. verificador del cliente.
     * 
     * @return HipConDivImpago Clase.
     * 
     * @throws TuxedoException En caso error en Tuxedo
     * @throws GeneralException  En caso de ocurrir un error.
     * @throws RemoteException En caso de error de conexion
     * @version 1.0
     */
    public HipConDivImpago getDivImpago(String numOperacion, String nroDividendo, long rut, char dv)
        throws TuxedoException, GeneralException, RemoteException {
        logger.debug("[getDivImpago]::inicio método Bean");
        return hipotecarioDAO.getDivImpago(numOperacion, nroDividendo, rut, dv);
    }

    /**
     * Retorna un DetalleInteres dado un número de operación.
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0 ../../.... Desconocido.</li>
     * <li>2.0 24/12/2012 Esteban Ramirez (Orand): Se migra de Tuxedo a web services. Inicialmente se
     *         permitirá elegir la opción de elegir que tipo servicio se
     *         utilizará web o tuxedo.</li>
     * 
     * @param numOperacion
     * @return DetalleInteres Clase.
     * 
     * @throws TuxedoException En caso error en Tuxedo
     * @throws GeneralException  En caso de ocurrir un error.
     * @throws RemoteException En caso de error de conexion
     * 
     */
    public DetalleInteres getDetalleInteres(String numOperacion) throws TuxedoException, GeneralException,
        RemoteException {
        logger.debug("[getDetalleInteres]::inicio método Bean");
        return hipotecarioDAO.getDetalleInteres(numOperacion);
    }

    /**
     * Metodo para crear las conexiones.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 ??/??/???? ?????: Versión inicial.</li>
     * <li>1.1 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): agrega instancia para procesoCHIPDAO 
     * y se soluciona observacion de checkstyle</li>
     * </ul>
     * <p>
     * 
     * @throws RemoteException
     *          Error de Conexion.
     * @throws CreateException
     *            Error al crear la conexion
     */
     public void ejbCreate() throws RemoteException, CreateException {
        // Instancia JOLT Manager de POOL
        logger.info("------------ Iniciando ServicioCobrosBean ------------");
        try {
            joltPool = new JOLTPoolConnection();

            WCorpConfig.ejbInit();
            DAOFactory factory = DAOFactory.getDAOFactory(DAOFactoryTypes.TUXEDO);
            simulacionDAO = factory.getSimulacionDAO();
            dividendosDAO = factory.getDividendosDAO();
            hipotecarioDAO = new HipotecarioDAO();
            procesoCHIPDAO = new ProcesoCHIPDAO();
            logger.info("------------ Inicio concluido ------------");
        }
        catch (javax.naming.NamingException ne) {
            logger.error("create - Naming: [" + ne + "]");
            logger.error("############################");
            throw new CreateException("No se logró Conexión al Pool Manager de JOLT - " + ne);
        }
        catch (Exception ex) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error("[ejbCreate] ERROR: " , ex);
            }
            throw new CreateException(ex.getMessage());
        }
    }


     private void logger( String method, String codigoRetorno, String descripcionError,
                          String applicationCode, Exception e ) {
        getLogger().error(method + " - Codigo Retorno   : " + codigoRetorno);
        getLogger().error(method + " - Descripcion Error: " + descripcionError);
        getLogger().error(method + " - Application Code : " + applicationCode);
        getLogger().error(method + "Exception           : [" + e + "]");
        getLogger().error("#######################");
    }


     public void ejbRemove() {}


     public void ejbActivate() {}


     public void ejbPassivate() {}


     public void setSessionContext( SessionContext sessionContext ) {
          this.sessionContext = sessionContext;
     }

     /**
      * Obtiene todas las solicitudes de hipotecario asociadas al rut dado
      * @param rut Rut del solicitante
      * @return Conjunto de solicitudes (SolicitudVO) asociadas al rut dado
      * @throws com.schema.util.GeneralException En caso de invocación con
      * parámetros inválidos, o no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      */
     public Collection selectSolicitudes( long rut )
          throws com.schema.util.GeneralException, wcorp.env.BusinessException,
          TuxedoException, RemoteException {

    	  getLogger().info("[selectSolicitudes] ["+rut+"] rut: "+ rut );
          if ( rut == 0 ) {
			throw new TuxedoException("PARAM");
          }

		try {
          return simulacionDAO.selectSolicitudes( rut );
		} catch (Exception ex) {
			getLogger().error("[calculaSimulacion]  ERROR: "+ ex.toString() );
			throw new TuxedoException("TUX");
		}

     }


     /**
      * Hace simulación de una operación de Crédito Hipotecario a partir de los
      * datos ingresados.
      * @param data Datos ingresados para la simulación
      * @return Objeto (SimulacionPlazosVO) con los datos asociados a la simulación
      * @throws com.schema.util.GeneralException En caso de invocación con
      * parámetros inválidos, o no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      */
     public Collection calculaSimulacion( DatosOperacionVO data )
          throws com.schema.util.GeneralException, wcorp.util.GeneralException,
          TuxedoException, RemoteException {

    	  getLogger().info("[calculaSimulacion]  data: "+ data );
          if ( data == null ) {
			throw new wcorp.util.GeneralException("PARAM");
          }

          try {
               return simulacionDAO.calculaSimulacion( data );
          }
          catch ( Exception ex ) {
        	   getLogger().error("[calculaSimulacion]  ERROR: "+ ex.toString() );
			throw new wcorp.util.GeneralException("TUX");
          }
     }


     /**
      * Hace simulación de una operación de Crédito Hipotecario con negociación
      * a partir de los datos ingresados
      * @param data Datos ingresados para la simulación
      * @return Objeto (SimulacionPlazosVO) con los datos asociados a la simulación
      * @throws com.schema.util.GeneralException En caso de invocación con
      * parámetros inválidos, o no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      */
     public Collection calculaSimulacionNegociada( DatosOperacionVO data )
          throws com.schema.util.GeneralException, wcorp.util.GeneralException,
          TuxedoException, RemoteException {

    	  getLogger().info("[calculaSimulacionNegociada]  data: "+ data );
          if ( data == null ) {
			throw new wcorp.util.GeneralException("PARAM");
          }

          try {
               return simulacionDAO.calculaSimulacionNegociada( data );
          }
          catch ( Exception ex ) {
        	   getLogger().error("[calculaSimulacionNegociada]  ERROR: "+ ex.toString() );
			throw new wcorp.util.GeneralException("TUX");
          }
     }


     /**
      * Hace simulación de una operación de Crédito Hipotecario para los productos en promición
      * a partir de los datos ingresados
      * @param data Datos ingresados para la simulación
      * @return Objeto (SimulacionPlazosVO) con los datos asociados a la simulación
      * @throws com.schema.util.GeneralException En caso de invocación con
      * parámetros inválidos, o no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      */
     public Collection calculaSimulacionPromocion( DatosOperacionVO data )
          throws com.schema.util.GeneralException, wcorp.util.GeneralException,
          TuxedoException, RemoteException {

    	  getLogger().info("[calculaSimulacionPromocion]  data: "+ data );
          if ( data == null ) {
			throw new wcorp.util.GeneralException("PARAM");
          }

          try {
               return simulacionDAO.calculaSimulacionPromocion( data );
          }
          catch ( Exception ex ) {
        	   getLogger().error("[calculaSimulacionPromocion]  ERROR: "+ ex.toString() );
			throw new wcorp.util.GeneralException("TUX");
          }
     }


     /**
      * Hace simulación de una operación de Crédito Hipotecario para el producto proyección
      * a partir de los datos ingresados
      * @param data Datos ingresados para la simulación
      * @return Objeto (SimulacionPlazosVO) con los datos asociados a la simulación
      * @throws com.schema.util.GeneralException En caso de invocación con
      * parámetros inválidos, o no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      */
     public Collection calculaSimulacionProyeccion( DatosOperacionVO data )
          throws com.schema.util.GeneralException, wcorp.util.GeneralException,
          TuxedoException, RemoteException {

    	  getLogger().info("[calculaSimulacionProyeccion]  data: "+ data );
          if ( data == null ) {
			throw new wcorp.util.GeneralException("PARAM");
          }

          try {
               return simulacionDAO.calculaSimulacionProyeccion( data );
          }
          catch ( Exception ex ) {
        	   getLogger().error("[calculaSimulacionProyeccion]  ERROR: "+ ex.toString() );
			throw new wcorp.util.GeneralException("TUX");
          }
     }


     /**
      * Ingresa una solicitud de Crédito Hipotecario en el sistema
      * @param solicitud Objeto (SolicitudVO) con los datos asociados a la solicitud
      * @throws com.schema.util.GeneralException En caso de invocación con
      * parámetros inválidos, o no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      */
     public void ingresaSolicitud( SolicitudVO solicitud )
          throws com.schema.util.GeneralException, wcorp.util.GeneralException,
          TuxedoException, RemoteException {

    	  getLogger().info("[ingresaSolicitud]  solicitud: "+ solicitud );
          if ( solicitud == null ) {
			throw new wcorp.util.GeneralException("PARAM");
          }

          try {
               simulacionDAO.ingresaSolicitud( solicitud );
          }
          catch ( Exception ex ) {
        	   getLogger().error("[ingresaSolicitud ]  ERROR: "+ ex.toString() );
			throw new wcorp.util.GeneralException("TUX");
          }
     }


     /**
      * Ingresa una solicitud de Crédito Hipotecario en el sistema, con los valores de gastos
      * de operación negociados, de acuerdo al parámetro entrergado.
      * @param solicitud Objeto (SolicitudVO) con los datos asociados a la solicitud
           * @param gastosOpUf Objeto (GastosOperacionVO) con los valores de gastos de operación negociados
      * @throws com.schema.util.GeneralException En caso de invocación con
      * parámetros inválidos, o no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      */
     public void ingresaSolicitud( SolicitudVO solicitud, GastosOperacionVO gastosOpUf )
          throws com.schema.util.GeneralException, wcorp.util.GeneralException,
          TuxedoException, RemoteException {

    	  getLogger().info("[ingresaSolicitud]  solicitud: "+ solicitud );
    	  getLogger().info("[ingresaSolicitud]  gastosOpUf: "+ gastosOpUf );
          if ( solicitud == null || gastosOpUf == null ) {
			throw new wcorp.util.GeneralException("PARAM");
          }

          try {
               simulacionDAO.ingresaSolicitud( solicitud, gastosOpUf );
          }
          catch ( Exception ex ) {
        	   getLogger().error("[ingresaSolicitud ]  ERROR: "+ ex.toString() );
			throw new wcorp.util.GeneralException("TUX");
          }
     }


     /**
      * Calcula los gastos operacionales asociados a la operación
      * @param data Datos ingresados para la simulación
      * @return
      * @throws com.schema.util.GeneralException En caso de invocación con
      * parámetros inválidos, o no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      */
     public GastosOperacionVO calculaGastosOperacion( DatosOperacionVO data )
          throws
          com.schema.util.GeneralException, wcorp.util.GeneralException,
          TuxedoException, RemoteException {

    	  getLogger().info("[calculaGastosOperacion]  data: "+ data );
    	  if ( data == null ) {
			throw new wcorp.util.GeneralException("PARAM");
          }

          try {
               return simulacionDAO.calculaGastosOperacion( data );
          }
          catch ( Exception ex ) {
        	   getLogger().error("[ingresaSolicitud ]  ERROR: "+ ex.toString() );
			throw new wcorp.util.GeneralException("TUX");
          }
     }


     /**
      * Obtiene la tabla solicitada del sistema
      * @param tabla Tabla a solicitar
      * @return Conjunto de objetos con par codigo/descripción de la tabla
      * @throws com.schema.util.GeneralException En caso de invocación con
      * parámetros inválidos, o no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      */
     public Collection getTabla( String tabla ) throws com.schema.util.GeneralException, wcorp.util.GeneralException,
          TuxedoException, RemoteException {

    	  getLogger().info("[getTabla]  tabla: "+ tabla );
          if ( tabla == null ) {
			throw new wcorp.util.GeneralException("PARAM");
          }
          try {
               return simulacionDAO.getTabla( tabla );
          }
          catch ( Exception ex ) {
        	   getLogger().error("[getTabla ]  ERROR: "+ ex.toString() );
			throw new wcorp.util.GeneralException("TUX");
          }
     }

     /**
      * Obtiene la tabla de productos del sistema
      * @return Conjunto de objetos con la descripción de productos
      * @throws com.schema.util.GeneralException
      * @throws com.schema.util.GeneralException En caso de no poder ejecutar el servicio.
      * @throws TuxedoException
      * @throws RemoteException
      */
     public Collection getTablaProducto() throws
          com.schema.util.GeneralException, wcorp.util.GeneralException, TuxedoException, RemoteException {
    	 
    	  getLogger().info("[getTablaProducto]  inicio.");
          try {
               return simulacionDAO.getTablaProducto();
          }
          catch ( Exception ex ) {
        	   getLogger().error("[getTablaProducto ]  ERROR: "+ ex.toString() );
			throw new wcorp.util.GeneralException("TUX");
          }
     }

     /**
      * Obtiene la tabla de productos del sistema por canal
      * <P>
      *
      * Registro de versiones:<UL>
      *
      * <LI>1.0 03/05/2007 Luis Cruz Campos (ImageMaker IT): versión inicial.
      *
      * </UL><P>
      *
      * @param canal Código del canal para el cual se desean obtener los datos.
      * @return Conjunto de objetos con la descripción de productos
      * @throws com.schema.util.GeneralException En caso de no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      * @since 4.4
      */
     public Collection getTablaProducto(String canal) throws
          com.schema.util.GeneralException, wcorp.util.GeneralException, TuxedoException, RemoteException {
    	 
    	  getLogger().info("[getTablaProducto]  canal: "+canal);
          try {
               return simulacionDAO.getTablaProducto(canal);
          }
          catch ( Exception ex ) {
        	   getLogger().error("[getTablaProducto ]  ERROR: "+ ex.toString() );
			throw new wcorp.util.GeneralException("TUX");
          }
     }

     /**
      * Obtiene un mapa con las tablas de productos del sistema para todos los canales configurados en el archivo wcorp-settings.xml
      *
      * <p>
      * Registro de versiones:<ul>
      * <li>1.0 (11/10/2005 Francisco Sandoval, <b>schema Ltda.</b>) - Versión inicial
      * </ul>
      * <p>
      *
      * @return HashMap de objetos ProductoVO con la información de la tabla de productos de cada canal.
      * @throws wcorp.util.GeneralException
      * @see TablaVO, ProductoVO
      */
     private HashMap getTablaProductoCanal() throws wcorp.util.GeneralException {
    	 
    	  getLogger().info("[getTablaProductoCanal]  inicio.");
          HashMap map = new HashMap();
          try {
               XmlUtils xu = new XmlUtils( WCorpConfig.APP_SETTINGS );
               xu.stepToNode( "/application-settings/default-channel-codes" );
               xu.firstChild();
               do {
                    map.put( xu.getNodeValue(), simulacionDAO.getTablaProducto( xu.getNodeValue() ) );
               } while ( xu.nextNode() );
          }
          catch ( Exception ex ) {
               getLogger().error( "[getTablaProductoCanal] Exception: " + ex.getMessage(), ex );
			throw new wcorp.util.GeneralException("TUX");
          }
          return map;
     }

     /**
      * Obtiene el estado de una solicitud
      * @param rut Rut del Solicitante
      * @param nroOperacion Número de operación de la solicitud consultada
      * @return Conjunto de objetos (EtapaVO) con la información de la etapa en que
      * se encuentra la solicitud
      * @throws com.schema.util.GeneralException En caso de invocación con
      * parámetros inválidos, o no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      */
     public Collection getEstadoSolicitud( long rut, int nroOperacion )
          throws
          com.schema.util.GeneralException, wcorp.util.GeneralException,
          TuxedoException, RemoteException {

    	  getLogger().info("[getEstadoSolicitud]  ["+rut+"] rut: "+rut);
    	  getLogger().info("[getEstadoSolicitud]  ["+rut+"] nroOperacion: "+nroOperacion);
          if ( rut == 0 || nroOperacion == 0 ) {
			throw new wcorp.util.GeneralException("PARAM");
          }

          try {
               return simulacionDAO.getEstadoSolicitud( rut, nroOperacion );
          }
          catch ( Exception ex ) {
        	   getLogger().info("[getEstadoSolicitud]  ERROR: "+ex.toString());
			throw new wcorp.util.GeneralException("TUX");
          }
     }


     /**
      * Obtiene las características de financiamiento mínimo y máximo para los distintos rangos
      * de monto de crédito para el producto solicitado.
      * @param pvo Objeto ProductoVO a solicitar rangoos
      * @param codAntiguedad Código de antigüedad asociada al bien raíz a financiar
      * @param codObjetivo Código de objetivo asociada al bien raíza financiar
      * @param codBienRaiz Código de Bien Raíza financiar
      * @return Objeto con las características del producto solicitado
      * @throws com.schema.util.GeneralException En caso de invocación con
      * parámetros inválidos, o no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      */
     public ProductoVO getRangosCreditoProducto( ProductoVO pvo, String codAntiguedad,
                                                 int codObjetivo, String codBienRaiz )
          throws com.schema.util.GeneralException, wcorp.util.GeneralException {

    	  getLogger().info("[getRangosCreditoProducto] pvo: "+pvo);
    	  getLogger().info("[getRangosCreditoProducto] codAntiguedad: "+codAntiguedad);
    	  getLogger().info("[getRangosCreditoProducto] codObjetivo: "+codObjetivo);
    	  getLogger().info("[getRangosCreditoProducto] codBienRaiz: "+codBienRaiz);
          if ( pvo == null || codObjetivo == 0 || codAntiguedad == null ) {
			throw new wcorp.util.GeneralException("PARAM");
          }

          try {
               return simulacionDAO.getRangosCreditoProducto( pvo, codAntiguedad,
                    codObjetivo, codBienRaiz );
          }
          catch ( Exception ex ) {
        	  getLogger().error("[getRangosCreditoProducto] ERROR: "+ex.toString());
			throw new wcorp.util.GeneralException("TUX");
          }
     }

     /**
      * Obtiene las características del producto solicitado, para la antigüedad y objetivo dado
      *
      * <P>
      *
      * Registro de versiones:<UL>
      *
      * <LI>1.0 ??/??/???? Desconocido (BCI): versión inicial.
      * <LI>1.1 03/05/2007 Luis Cruz Campos (ImageMaker IT): Se elimina la lógica y se mueve a
      *         {@link #getProducto(int, String, int, String, String)}
      *
      * </UL><P>
      *
      * @param codProducto Código de producto solicitado
      * @param codAntiguedad Código de antigüedad asociada al bien raíz a financiar
      * @param codObjetivo Código de objetivo asociada al bien raíza financiar
      * @param codBienRaiz Código de Bien Raíza financiar
      * @return Objeto con las características del producto solicitado
      * @throws com.schema.util.GeneralException En caso de invocación con
      * parámetros inválidos, o no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      * @since 1.0
      */
     public ProductoVO getProducto( int codProducto, String codAntiguedad,
                                    int codObjetivo, String codBienRaiz)
          throws com.schema.util.GeneralException, wcorp.util.GeneralException,
          TuxedoException, RemoteException {
    	 
    	 getLogger().info("[getProducto] codProducto: "+codProducto);
    	 getLogger().info("[getProducto] codAntiguedad: "+codAntiguedad);
    	 getLogger().info("[getProducto] codObjetivo: "+codObjetivo);
    	 getLogger().info("[getProducto] codBienRaiz: "+codBienRaiz);
    	 return getProducto(codProducto, codAntiguedad, codObjetivo, codBienRaiz, null);
     }

     /**
      * Obtiene las características del producto solicitado, para la antigüedad, el objetivo y
      * canal dados.
      *
      * <P>
      *
      * Registro de versiones:<UL>
      *
      * <LI>1.0 03/05/2007 Luis Cruz Campos (ImageMaker IT): versión inicial.
      * <LI>1.1 09/07/2009 Yon Sing Sius (ImageMaker IT): Se filtra por canal antes de obtener el producto
      * siempre y cuando canal sea distinto de null. Esta modificación se realizó debido a que se estaba
      * obteniendo un producto de un canal distinto al enviado.
      * <LI>1.1.1 27/10/2010 Elías Zacarías Vilches (Sermaluc): Se agrega despliegue de stackTrace al
      * manejo de excepciones.
      * </UL><P>
      *
      * @param codProducto Código de producto solicitado
      * @param codAntiguedad Código de antigüedad asociada al bien raíz a financiar
      * @param codObjetivo Código de objetivo asociada al bien raíza financiar
      * @param codBienRaiz Código de Bien Raíza financiar
      * @param canal Código del canal que se desea consultar
      * @return Objeto con las características del producto solicitado
      * @throws com.schema.util.GeneralException En caso de invocación con
      * parámetros inválidos, o no poder ejecutar el servicio.
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      * @since 4.4
      */
     public ProductoVO getProducto( int codProducto, String codAntiguedad,
                                    int codObjetivo, String codBienRaiz, String canal)
          throws com.schema.util.GeneralException, wcorp.util.GeneralException,
          TuxedoException, RemoteException {

    	  getLogger().info("[getProducto] codProducto: "+codProducto);
    	  getLogger().info("[getProducto] codAntiguedad: "+codAntiguedad);
    	  getLogger().info("[getProducto] codObjetivo: "+codObjetivo);
    	  getLogger().info("[getProducto] codBienRaiz: "+codBienRaiz);
    	  getLogger().info("[getProducto] canal: "+canal);
          if ( codProducto == 0 || codObjetivo == 0 || codAntiguedad == null ) {
			throw new wcorp.util.GeneralException("PARAM");
          }

          try {
               SettingsVO settings = SettingsVO.getInstance();
               getSettings(canal);
               ProductoVO producto = null;
               if (canal != null) {
            	   producto = settings.getProductoCanal(codProducto, canal);
               }
               else {
               	   producto = settings.getProducto(codProducto);
               }

               getLogger().info("[getProducto] producto: "+producto);
               producto = simulacionDAO.getRangosCreditoProducto( producto, codAntiguedad,
                    codObjetivo, codBienRaiz );
               getLogger().info("[getProducto] producto luego de getRangosCreditoProducto: "+producto);
               return producto;
          }
          catch ( Exception ex ) {
        	   getLogger().error("[getProducto] ERROR: " + com.schema.util.GeneralException.getStackTrace(ex));
			throw new wcorp.util.GeneralException("TUX");
          }
     }

     /**
      * Obtiene las tablas y parámetros básicos del sistema desde la base de datos e inicializa
      * los objetos de negocio correspondientes. Si ya estaba inicializdo, retorna esa instancia.
      * <p>
      * Registro de versiones:<ul>
      * <li>1.0 (01/02/2004 Francisco Sandoval, <b>schema Ltda.</b>) - Versión inicial
      * <li>1.1 (11/10/2005 Francisco Sandoval, <b>schema Ltda.</b>) - Cambia forma de obtener el valor de la UF, de la clase
      *         obsoleta InfoFinanciera por clase utilitaria WCorpUtils.getValorUF(), por efectos de mejor encapsulamiento y
      *         uniformidad de código (con otras aplicaciones).
      * <li>1.2 (Christopher Finch Ureta, ImageMaker IT) - Se agregan los plazos y meses de gracia</li>
      * <li>1.3 (Luis Cruz, ImageMaker IT) - Se agrega control de excepción cuando se obtiene la UF ya que anteriormente
      *         esta excepción no arrojaba información.</li>
      * <li>1.4 03/05/2007 Luis Cruz (ImageMaker IT). Se mueve la lógica a {@link #getSettings(String)}.
      * <p>
      * @return Objeto que contiene todos los objetos de negocios del sistema.
      * @throws com.schema.util.GeneralException
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      * @see SettingsVO
      */
     public SettingsVO getSettings()
     	throws com.schema.util.GeneralException, wcorp.util.GeneralException,
     		TuxedoException, RemoteException {
    	 return getSettings(CODIGO_CONSULTA_SIN_CANAL);

     }

     /**
      * Obtiene las tablas y parámetros básicos del sistema desde la base de datos e inicializa
      * los objetos de negocio correspondientes por canal. Si ya estaba inicializdo,
      * retorna esa instancia.
      *
      * <P>
      *
      * Registro de versiones:<UL>
      *
      * <LI>1.0 03/05/2007 Luis Cruz Campos (ImageMaker IT): versión inicial.
      *
      * </UL><P>
      *
      * @param canal Canal para el que se inicializaran los servicios.
      * @return Objeto que contiene todos los objetos de negocios del sistema.
      * @throws com.schema.util.GeneralException
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      * @since 4.4
      */
     public SettingsVO getSettings(String canal)
          throws com.schema.util.GeneralException, wcorp.util.GeneralException,
          TuxedoException, RemoteException {
    	  getLogger().info("[getSettings] canal: "+canal);
          SettingsVO settings = SettingsVO.getInstance();
          getLogger().info("[getSettings]  instance: " + settings + ", settings.isInitializedOk (init): " + settings.isInitializedOk() );
          if (settings.isInitializedOk()) {
               return settings;
          }
          settings.setTablaAntiguedad( getTabla( "AN" ) );
          getLogger().info("[getSettings]  setTablaAntiguedad Ok" );

          // Filtramos los productos según el canal.
          if (canal == CODIGO_CONSULTA_SIN_CANAL || canal == null) {
        	  getLogger().info("[getSettings] onsultando productos sin canal");
        	  settings.setTablaProductos(getTablaProducto());
          } else {
        	  getLogger().info("[getSettings] Consultando productos sin con canal");
        	  settings.setTablaProductos(getTablaProducto(canal));
          }

          getLogger().info("[getSettings] setTablaProductos Ok" );

          settings.setTablaObjetivos( getTabla( "OP" ) );
          getLogger().info("[getSettings] setTablaObjetivos Ok" );
          settings.setTablaTipoBienAntiguedad( getTabla( "TO" ) );
          getLogger().info("[getSettings] setTablaTipoBienAntiguedad Ok" );
          settings.setTablaMonedas( getTabla( "MO" ) );
          getLogger().info("[getSettings] setTablaMonedas Ok" );
          settings.setTablaMonedaProducto( getTabla( "MP" ) );
          getLogger().info("[getSettings] setTablaMonedas Ok" );
          settings.setTablaTipoBien( getTabla( "TB" ) );
          getLogger().info("[getSettings] setTablaTipoBien Ok" );
          settings.setTablaSeguros( getTabla( "TS" ) );
          getLogger().info("[getSettings] setTablaSeguros Ok" );

          /**
           * Se agregan los plazos y los meses de gracia.
           */
          settings.setTablaPlazos( getTabla( "PT" ) );
          getLogger().info("[getSettings] setTablaPlazos Ok" );
          settings.setTablaMesesGracia( getTabla( "GR" ) );
          getLogger().info("[getSettings] setTablaMesesGracia Ok" );

          // Obtiene los productos definidos para cada canal
          settings.setProductosCanal( getTablaProductoCanal() );
          getLogger().info("[getSettings] setProductosCanal Ok" );

          // Arma Tabla de Financiamientos (no quedó otra...)
          Collection col = new ArrayList();
          col.add( new TablaVO( "1", "Letras" ) );
          col.add( new TablaVO( "2", "Mutuo" ) );
          settings.setTablaTipoFinanciamiento( col );
          getLogger().info("[getSettings] setTablaTipoFinanciamiento Ok" );

          // Obtiene el valor de la UF del día
          try {
        	  settings.setValorUF( WCorpUtils.getValorUF( new java.util.Date() ) );
          } catch (Exception e) {
        	  getLogger().error("[getSettings] ERROR: "+e.toString() );
			throw new GeneralException("ESPECIAL", "No se pudo obtener el valor de la UF.");
		  }

          getLogger().info("[getSettings]  getValorUF: " + settings.getValorUF() );

          // Obtiene valor de renta mínima exigida
          settings.setRentaMinimaUF( Double.parseDouble(
               TablaValores.getValor( SOLICITUDES_SETTINGS, "cantMinUfBciHome", "Desc" ) ) );
          getLogger().info("[getSettings] getRentaMinimaUF: " + settings.getRentaMinimaUF() );

          // Define la data con estado inicializada
          settings.setInitializedOk( true );
          getLogger().info("[getSettings] instance: " + settings + ", settings.setInitializedOk (end): " + settings.isInitializedOk() );

          return settings;
     }


     /**
      * Obtiene las tablas y parámetros básicos del sistema desde la base de datos e inicializa
      * los objetos de negocio correspondientes. Si ya estaba inicializdo, invalida esa instancia
      * y actualiza la información desde la base de datos.
      * @return Objeto que contiene todos los objetos de negocios del sistema.
      * @throws com.schema.util.GeneralException
      * @throws wcorp.util.GeneralException
      * @throws TuxedoException
      * @throws RemoteException
      * @see SettingsVO
      */
     public SettingsVO reloadSettings()
          throws com.schema.util.GeneralException, wcorp.util.GeneralException,
          TuxedoException, RemoteException {

    	  getLogger().info("[reloadSettings] inicio" );
          SettingsVO settings = SettingsVO.getInstance();
          getLogger().info("[reloadSettings] instance: " + settings + ", settings.isInitializedOk (init): " + settings.isInitializedOk() );

          settings.setInitializedOk( false );
          settings = getSettings();
          getLogger().info("[reloadSettings] reloadSettings --> instance: " + settings + ", settings.setInitializedOk (end): " +
                              settings.isInitializedOk() );
          return settings;
     }

     /**
      * Trae las tasas involucradas en la operación
      *
      * <p>
      * Registro de versiones:<ul>
      * <li>1.0 ??/??/???? ????????????????? - Versión inicial
      * <li>2.0 14/11/2006 Christopher Finch (ImageMaker IT) - El motivo por el cual se creó este método fue para obtener las tasas de las simulaciones realizadas.
      *             En el nuevo desarrollo del simulador hipotecario, la simulación devuelve las tasas por lo que no se
      *             necesario obtener nuevamente las tasas. Además, para calcular las tasas se necesitan más parámetros
      *             como el plazo, el cual no es un parámetro que recibe el servicio, devolviendo así tasas incorectas. Las tasas
      *             se obtienen de los métodos calculaSimulacionXXX.
      * </ul>
      * <p>
      *
      * @param data Objeto con los datos
      * @return Objeto con los valores de las tasas
      * @throws com.schema.util.GeneralException
      * @throws wcorp.util.GeneralException
      * @deprecated método no válido
      */
     public TasasVO getTasas( DatosOperacionVO data )
          throws com.schema.util.GeneralException, wcorp.util.GeneralException {

    	  getLogger().info("[getTasas] data: "+data);
    	  if ( data == null ) {
			throw new wcorp.util.GeneralException("PARAM");
          }

          try {
               return simulacionDAO.getTasas( data );
          }
          catch ( Exception ex ) {
        	   getLogger().error("[getTasas] ERROR: "+ex.toString());
			throw new wcorp.util.GeneralException("TUX");
          }

     }

    /**
	 * Realiza el pago de un dividendo del Credito Hipotecario.
	 * <p>
	 * Registro de versiones:<ul>
     * <li>1.0 15/10/2004 Claudia Castañón (Novared) - version inicial
     * <li>2.0 23/09/2005 Marcelo Román (SEnTRA) - Se modifica el método
     *         para obtener la oficina y la forma de pago del dividendo; además, se realiza
     *         la llamada al método generaPagoDividendo (Método que se sobrecarga) con  los
     *         nuevos atributos.
	 * <li>2.1 15/11/2005 Oscar A. Otárola Torres (SEnTRA)): Se agrega atributo operador.
     * <li>3.0 11/03/2013 Yasmin Rocha (Orand): Se migra método a HipotecarioDAO. También se migra a web services,
     *         que inicialmente es opcional.</li> 
     * </ul></p>
	 *  @param  numOperacion  Código Operación
	 *  @param  nroDividendo    Identificador del dividendo.
	 *  @param subTotalPesos  String
	 *  @param interesPenal Sring
	 *  @param gastoCobranza String
	 *  @param totalDividendo String
	 *
	 *  @return codigo String que representa el resultado de la transacción.
	 *
	 * @throws TuxedoException En caso error en Tuxedo
     * @throws GeneralException  En caso de ocurrir un error.
     * @throws RemoteException En caso de error de conexion
	 *
     *
     * @since 1.0
     */
    public String generaPagoDividendo(String numOperacion, String nroDividendo, String subTotalPesos,
        String interesPenal, String gastoCobranza, String totalDividendo, String operador) throws TuxedoException,
        GeneralException, RemoteException {
        logger.debug("[generaPagoDividendo]::inicio método Bean");
        return hipotecarioDAO.generaPagoDividendo(numOperacion, nroDividendo, subTotalPesos, interesPenal,
            gastoCobranza, totalDividendo, operador);
    }

    /**
     * Método que realiza el pago de un dividendo del Credito Hipotecario.
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0 (23/09/2005 Marcelo Román (SEnTRA)): Versión inicial.</li>
     * <li>1.1 (03/11/2005 Oscar Otárola Torres (SEnTRA)): Se modifica el nombre del parámetro
     * de entrada al servicio tuxedo de oficina por oficinaPago.</li>
     * <li>1.1 (15/11/2005 Oscar A. Otárola Torres (SEnTRA)): Se agrega atributo operador.
	 * <li>2.0 11/03/2013 Esteban Ramirez (Orand): Se importa método a HipotecarioDAO.java.  Se migra 
	 *         a web services.
     * </ul>
     *
     * @param numOperacion String Código Operación.
     * @param nroDividendo String Identificador del dividendo.
     * @param subTotalPesos String que representa el subTotal en pesos.
     * @param interesPenal String que representa el interes penal incurrido.
     * @param gastoCobranza String que representa el gasto de cobranza incurrido.
     * @param totalDividendo String que representa el total del dividendo.
     * @param oficina String que representa la oficina en que se realizó el pago.
     * @param formaPago String que representa la forma de pago.
     * @param operador String que representa el código del operador.
     *
     * @return codigo String que representa el resultado de la transacción.
     *
     * @throws TuxedoException En caso error en Tuxedo
     * @throws GeneralException  En caso de ocurrir un error.
     * @throws RemoteException En caso de error de conexion
     *
     * @since 3.0
     */
    public String generaPagoDividendo(String numOperacion, String nroDividendo, String subTotalPesos,
        String interesPenal, String gastoCobranza, String totalDividendo, String oficina, String formaPago,
        String operador) throws TuxedoException, GeneralException, RemoteException {
        logger.debug("[generaPagoDividendo]::inicio método Bean");
        return hipotecarioDAO.generaPagoDividendo(numOperacion, nroDividendo, subTotalPesos, interesPenal,
            gastoCobranza, totalDividendo, oficina, formaPago, operador);
    }

          /**
           * Obtiene el Aviso de Vencimiento para la operación y dividendo dado
           * <p>
           * Registro de versiones:<ul>
           * <li>1.0 09/08/2005 Francisco Sandoval (<b>schema Ltda.</b>) - Versión inicial
           * </ul>
           * <p>
           * @param codOperacion Código de la operación hipotcaria a consultar
           * @param nroDividendo Número del dividendo a consultar
           * @return Objeto AvisoVencimientoVO que representa el aviso de vencimiento
           * @throws GeneralException
           * @throws wcorp.util.GeneralException
           */
          public AvisoVencimientoVO getAvisoVencimientoDividendo( String codOperacion, int nroDividendo )
               throws com.schema.util.GeneralException, wcorp.util.GeneralException {
        	   getLogger().info("[getAvisoVencimientoDividendo] codOperacion[" + codOperacion + "]");
        	   getLogger().info("[getAvisoVencimientoDividendo] nroDividendo[" + nroDividendo + "]");
               return dividendosDAO.getAvisoVencimientoDividendo( codOperacion, nroDividendo );
          }


          /**
           * Obtiene los certificados disponibles para una operación de crédito hipotecario
           * <p>
           * Registro de versiones:<ul>
           * <li>1.0 06/02/2006 Alejandro Ituarte (<b>schema Ltda.</b>) - Versión inicial
           * </ul>
           * <p>
           * @param codOperacion String Código de la operación
           * @return CertificadosDisponiblesVO[]
           * @throws GeneralException
           * @throws GeneralException
           */
          public CertificadosDisponiblesVO[] consultaCertificadosDisponibles( String codOperacion )
               throws com.schema.util.GeneralException, wcorp.util.GeneralException {
        	   getLogger().info("[consultaCertificadosDisponibles] codOperacion[" + codOperacion + "]");
               return dividendosDAO.consultaCertificadosDisponibles( codOperacion );
          }

          /**
           * Obtiene certificado de intereses
           * <p>
           * Registro de versiones:<ul>
           * <li>1.0 06/02/2006 Alejandro Ituarte (<b>schema Ltda.</b>) - Versión inicial
           * </ul>
           * <p>
           * @param codOperacion String Código de la operación
           * @param periodo periodo de consulta
           * @return CertificadoInteresesVO
           * @throws GeneralException
           * @throws GeneralException
           */
          public CertificadoInteresesVO consultaCertificadoIntereses( String codOperacion, int periodo )
               throws com.schema.util.GeneralException, wcorp.util.GeneralException {
        	   getLogger().info("[consultaCertificadoIntereses] codOperacion[" + codOperacion + "]");
        	   getLogger().info("[consultaCertificadoIntereses] periodo[" + periodo + "]");
               return dividendosDAO.consultaCertificadoIntereses( codOperacion, periodo );
          }


          /**
           * Obtiene certificado dfl2
           * <p>
           * Registro de versiones:<ul>
           * <li>1.0 06/02/2006 Alejandro Ituarte (<b>schema Ltda.</b>) - Versión inicial
           * </ul>
           * <p>
           * @param codOperacion String Código de la operación
           * @param periodo periodo de consulta
           * @return CertificadoInteresesVO
           * @throws GeneralException
           * @throws GeneralException
           */
          public CertificadoDFL2VO consultaCertificadoDfl2( String codOperacion, int periodo )
               throws com.schema.util.GeneralException, wcorp.util.GeneralException {
        	   getLogger().info("[consultaCertificadoDfl2] codOperacion[" + codOperacion + "]");
       	       getLogger().info("[consultaCertificadoDfl2] periodo[" + periodo + "]");
               return dividendosDAO.consultaCertificadoDfl2( codOperacion, periodo );
          }

          /**
           * Obtiene los Dividendos Pagados de un crédito hipotecario por año y mes de cancelación
           * <p>
           * Registro de versiones:<ul>
           * <li>1.0 27/06/2006 Andrés Morán Ortiz (<b>SEnTRA</b>) - Versión inicial
           * </ul>
           * <p>
           *
           * @param numOperacion número operación de crédito hipotecario
           * @param mes mes correspondiente al mes de vencimiento del dividendo
           * @param ano correspondiente al año de vencimiento del dividendo
           * @return HipConDivCanCre[] objeto con los atributos de los dividendos cancelados por el cliente
           * @throws com.schema.util.GeneralException
           * @throws wcorp.util.GeneralException
           * @since 4.0
           */
          public HipConDivCanCre[] consultaDividendosPagados(String numOperacion, String mes, String ano)
              throws com.schema.util.GeneralException, wcorp.util.GeneralException {
        	  getLogger().info("[consultaDividendosPagados] numOperacion[" + numOperacion + "]");
        	  getLogger().info("[consultaDividendosPagados] mes[" + mes + "]");
        	  getLogger().info("[consultaDividendosPagados] ano[" + ano + "]");
            return dividendosDAO.consultaDividendosPagados(numOperacion, mes, ano);
          }


		/**
		* Hace simulación de una operación de Crédito Hipotecario a partir de los
		* datos ingresados, por monto del crédito.
		*
		* <p>
		* Registro de versiones:<ul>
		* <li>1.0 05/10/2006 Christopher Finch (ImageMaker IT) - Versión inicial
		* </ul>
		* <p>
		*
		* @param data Datos ingresados para la simulación
		* @return Objeto (SimulacionPlazosTasasVO) con los datos asociados a la simulación
		* @throws com.schema.util.GeneralException En caso de invocación con
		* parámetros inválidos, o no poder ejecutar el servicio.
		* @throws wcorp.util.GeneralException
		* @throws TuxedoException
		* @throws RemoteException
		*/
		  public SimulacionPlazosTasasVO[] calculaSimulacionMonto( DatosOperacionVO data )
		       throws com.schema.util.GeneralException, wcorp.util.GeneralException,
		       TuxedoException, RemoteException {

			   getLogger().info("[calculaSimulacionMonto] data= " + data);
		       if ( data == null ) {
			throw new wcorp.util.GeneralException("PARAM");
		       }

		       try {
		            return simulacionDAO.calculaSimulacionMonto( data );
		       }
		       catch ( Exception ex ) {
		    	    getLogger().error("[calculaSimulacionMonto] ERROR:  " + ex.toString());
			throw new wcorp.util.GeneralException("TUX");
		       }
		  }

	   /**
	     * Permite almacenar una simulación
	     *
	     * Registro de versiones:
	     * <ul>
	     * <li>1.0 (19/12/2006, Luis Cruz (ImageMaker IT)): versión inicial.
	     * </ul>
	     * @param data VO con los datos a guardar correspondientes a la simulación
	     * @return Verdadero si se pudo realizar el guardado
	     * @throws wcorp.util.GeneralException
	     * @throws RemoteException En caso de existir un error en en la comunicación
	     * @throws GeneralException En caso de errores aplicativos
	     * @since 1.0
	     */
      public boolean guardarSimulacion(FichaSimulacionVO data)
		   throws com.schema.util.GeneralException, wcorp.util.GeneralException,  TuxedoException, RemoteException {

    	   getLogger().info("[guardarSimulacion] data= " + data);
	       if ( data == null ) {
			throw new wcorp.util.GeneralException("PARAM");
	       }

	       try {
	            return simulacionDAO.guardaSimulacion( data );
	       }
	       catch ( Exception ex ) {
	    	   getLogger().error("[guardarSimulacion] ERROR: " + ex.toString());
			throw new wcorp.util.GeneralException("TUX");
	       }
	  }

      /**
       * Método que realiza el pago de un dividendo del Credito Hipotecario. 
       * Utiliza misma lógica de método pago de dividendo existente pero el servicio utilizado
       * en este flujo verifica que el dividendo a pagar no posea convenio PAC
       *
       * Registro de versiones:<ul>
       * <li>1.0 (23/05/2007 Marco Aicón D. (SEnTRA)): Versión inicial.</li>
       * <li>1.1 09/09/2008 Jessica Ramírez. (Imagemaker IT): Se agrega nuevo parámetro
       * "tipoServicio" que permite determinar si se efectúa una consulta o un pago.
       * Cuando se trata de una Consulta permite determinar si el cliente puede o no
       * realizar el pago de dividendo. Cuando es un Pago se efectúa el pago de dividendo.</li>
       * </ul>
       *
       * @param numOperacion String Código Operación.
       * @param nroDividendo String Identificador del dividendo.
       * @param subTotalPesos String que representa el subTotal en pesos.
       * @param interesPenal String que representa el interes penal incurrido.
       * @param gastoCobranza String que representa el gasto de cobranza incurrido.
       * @param totalDividendo String que representa el total del dividendo.
       * @param oficina String que representa la oficina en que se realizó el pago.
       * @param formaPago String que representa la forma de pago.
       * @param operador String que representa el código del operador.
       * @param tipoServicio Representa que servicio se debe realizar una consulta o un pago.
       *        si el valor es C: Indica que se debe realizar una consulta.
       *        si el valor es P: Indica que se debe realizar un pago.
       * @return codigo String que representa el resultado de la transacción.
       *
       * @throws com.schema.util.GeneralException
       * @throws wcorp.util.GeneralException
       *
       * @since 5.0
       */
    public String pagaDividendoVerificaPACCliente(String numOperacion, String nroDividendo,
            String subTotalPesos, String interesPenal,
            String gastoCobranza,
            String totalDividendo, String oficina,
            String formaPago, String operador, String tipoServicio) throws com.schema.util.GeneralException,
            wcorp.util.GeneralException {
        getLogger().info("[pagaDividendoVerificaPACCliente] ["+numOperacion+"] numOperacion= " + numOperacion);
        getLogger().info("[pagaDividendoVerificaPACCliente] ["+numOperacion+"] nroDividendo= " + nroDividendo);
        getLogger().info("[pagaDividendoVerificaPACCliente] ["+numOperacion+"] subTotalPesos= " + subTotalPesos);
        getLogger().info("[pagaDividendoVerificaPACCliente] ["+numOperacion+"] interesPenal= " + interesPenal);
        getLogger().info("[pagaDividendoVerificaPACCliente] ["+numOperacion+"] gastoCobranza= " + gastoCobranza);
        getLogger().info("[pagaDividendoVerificaPACCliente] ["+numOperacion+"] totalDividendo= " + totalDividendo);
        getLogger().info("[pagaDividendoVerificaPACCliente] ["+numOperacion+"] oficina= " + oficina);
        getLogger().info("[pagaDividendoVerificaPACCliente] ["+numOperacion+"] formaPago= " + formaPago);
        getLogger().info("[pagaDividendoVerificaPACCliente] ["+numOperacion+"] operador= " + operador);
        getLogger().info("[pagaDividendoVerificaPACCliente] [" + numOperacion + "] tipoServicio= " + tipoServicio);
        return dividendosDAO.pagaDividendoVerificaPACCliente(numOperacion, nroDividendo,
                subTotalPesos, interesPenal,
                gastoCobranza,
                totalDividendo, oficina,
                formaPago, operador, tipoServicio);
    }

	/**
	 * Obtiene los Dividendos Pagados de un crédito hipotecario por año y mes de cancelación. Este método funciona de la misma manera que el método
	 * consultaDividendosPagados y se crea para utilizar el nuevo servicio de búsqueda de los dividendos.
	 * <p>
	 * Registro de versiones:<ul>
	 * <li>1.0 (23/05/2007 Marco Aicón D. (SEnTRA)): Versión inicial.</li>
	 * </ul>
	 * <p>
	 *
	 * @param numOperacion número operación de crédito hipotecario
	 * @param mes mes correspondiente al mes de vencimiento del dividendo
	 * @param ano correspondiente al año de vencimiento del dividendo
	 * @return HipConDivCanCre[] objeto con los atributos de los dividendos cancelados por el cliente
	 * @throws com.schema.util.GeneralException
	 * @throws wcorp.util.GeneralException
	 * @since 5.0
	 */
	public HipConDivCanCre[] consultaDividendosPagadosMonto(String numOperacion, String mes, String ano)
	throws com.schema.util.GeneralException, wcorp.util.GeneralException {
		getLogger().info("[consultaDividendosPagadosMonto] ["+numOperacion+"] numOperacion= " + numOperacion);
		getLogger().info("[consultaDividendosPagadosMonto] ["+numOperacion+"] mes= " + mes);
		getLogger().info("[consultaDividendosPagadosMonto] ["+numOperacion+"] ano= " + ano);
		return dividendosDAO.consultaDividendosPagadosMonto(numOperacion, mes, ano);
	}

/**
     * Método responsable de recuperar los dividendos asociados al número de
     * operación.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 26/12/2007 Diego Olivares Bolton (TINet): Versión inicial.
     * <li>1.1 26/04/2013 Yasmin Rocha Alvarez (Orand): Se cambia la inicialización de
     *         hipotecarioDAO al ciclo try por error de compilación.
     * </ul>
     * <p>
     * 
     * @return DividendoTO[] 
     *            conjunto de dividendos impagos y pagados de un crédito
     *            hipotecario.
     * 
     * @param numeroOperacion
     *            número de operación del crédito al que se le consultarán sus
     *            dividendos.
     * 
     * @throws ServicioNoDisponibleException
     *            encapsula cualquier error en la recuperación de datos.
     *             
     * @since 5.1
     */  
    public DividendoTO[] obtenerDividendos(String numeroOperacion) throws ServicioNoDisponibleException {
    	getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] numeroOperacion= " + numeroOperacion);
        ArrayList dividendos = new ArrayList();
        DividendoImpagoTO[] impagos = null;
        DividendoPagadoTO[] pagados = null;
        boolean errorRecuperandoImpagos = false;
        boolean errorRecuperandoPagados = false;
        getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] Creando HipotecarioDao.");
        try {
			HipotecarioDAO hipotecarioDAO = new HipotecarioDAO();
            getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] Antes de recuperar los dividendos impagos.");
            impagos = hipotecarioDAO.obtenerDividendosImpagos(numeroOperacion);
            getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] Dividendos Impagos recuperados: " + StringUtil.contenidoDe(impagos));
        }
        catch (Exception e) {
            getLogger().error("[obtenerDividendos] ["+numeroOperacion+"] Error recuperando los dividendos impagos."+ e.toString());
            errorRecuperandoImpagos = true;
        }
        try {
        	getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] Antes de recuperar los dividendos pagados.");
            pagados = hipotecarioDAO.obtenerDividendosPagados(numeroOperacion);
            getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] Dividendos Pagados recuperados: " + StringUtil.contenidoDe(pagados));
        }
        catch (Exception e) {
        	getLogger().error("[obtenerDividendos] ["+numeroOperacion+"]Error recuperando los dividendos pagados: "+ e.toString());
            errorRecuperandoPagados = true;
        }
        getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] Antes de verificar si hay error en la recuperación de los dividendos.");
        if (errorRecuperandoImpagos == true && errorRecuperandoPagados == true) {
        	getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] Error recuperando los dividendos asociados al crédito.");
            throw new ServicioNoDisponibleException(ERROR_RECUPERANDO_DIVIDENDOS);
        }
        if (errorRecuperandoImpagos == false && impagos != null && impagos.length > 0) {
        	getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] Cantidad de dividendos impagos: " + impagos.length);
            for (int i = 0; i < impagos.length; i++) {
            	getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] Agregando dividendo impago: " + impagos[i]);
                dividendos.add(impagos[i]);
            }
            getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] Fin de agregar dividendos impagos");
        }
        if (errorRecuperandoPagados == false && pagados != null && pagados.length > 0) {
        	getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] Cantidad de dividendos pagados: " + pagados.length);
            for (int i = 0; i < pagados.length; i++) {
            	getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] Agregando dividendo pagados: " + pagados[i]);
                dividendos.add(pagados[i]);
            }
            getLogger().info("[obtenerDividendos] ["+numeroOperacion+"] Fin de agregar dividendos pagados");
        }
        return (DividendoTO[]) dividendos.toArray(new DividendoTO[0]);
    }
	
	/**
	 * Obtiene los créditos hipotecarios de un cliente.
	 * <p>
	 * Registro de versiones: <ul>
	 * <li>1.0 29/06/2007 Gonzalo Oviedo L (<b>ADA LTDA.</b>)- Versión inicial.</li>
	 * <li>2.0 11/03/2013 Esteban Ramirez (Orand): Se exporta servicio a HipotecarioDAO.java.
	 * </ul>
	 * <p>
	 * @return ProductoCreditoHipotecarioDTO[]
	 * @param rut long
	 * @throws TuxedoException En caso error en Tuxedo
     * @throws GeneralException  En caso de ocurrir un error.
     * @throws RemoteException En caso de error de conexion
	 * @since 5.1
	 */
    public ProductoCreditoHipotecarioDTO[] traerListaCreditoHipotecario(long rut) throws TuxedoException,
        GeneralException, RemoteException {
        logger.debug("[traerListaCreditoHipotecario]::inicio método Bean");
        return hipotecarioDAO.traerListaCreditoHipotecario(rut);
    }

     /**
     * Método que consiste en realizar un pago de dividendo. Para ejecutar el pago los pasos
     * que se realizan son:
     * 
     * 1. Se verifica si el cliente puede realizar el pago de dividendo.
     * 2. Se realiza al cargo a la cuenta corriente.
     * 3. Se realiza el pago de dividendo(Aviso a ASICOM).
     * 4. Si existe error en el punto 3, se realiza la reversa del cargo a la cuenta.
     * 
     * <p>
     * Registro de versiones:
     * <li>1.0 04/08/2008, Jessica Ramírez (Imagemaker IT): versión inicial
     * </li>
     * <li>1.1 05/11/2008, Yasmin Rocha Alvarez (Global Works S.A.): se cambia el método utilizado para
     *                                           realizar cargo a la cuenta(movCtaCte()) por el método 
     *                                           movCtaStvarios3p().  Agregándose también un parametro al cargo
     *                                           en la cuenta, el parametro es USUARIO.                                         
     * </li>
     * 
     * @param cuentaCteCargo cuenta corriente a lla cual se le realiza el cargo
     * @param totalDividendo total de dividendo a pagar
     * @param trxCargo Código de la transcación
     * @param codMnemonico Código de Mnemocico
     * @param numeroOperacion Número de la operación
     * @param numeroDividendo Número del dividendo
     * @param datosDividendoAPagar Datos del Dividendo a pagar
     * @param trxReverso Indica còdigo que realizar la reversa
     * @return Los valores de retorno son:
     *         Valor 0: Indica el éxito del pago de dividendo.
     *         Valor 8: Indica que hubo error en el cargo a la cuenta.
     *         Valor 9: Indica que hubo error en el pago y se realizó la reversa
     *         Valores entre 1-7: Valores que indican algún problema en el pago (Aviso a ASICOM)        
     * @throws RemoteException En caso de ocurrir un error
     * @throws wcorp.util.GeneralException En caso de ocurrir un error
     * @throws com.schema.util.GeneralException En caso de ocurrir un error
     * @since 5.4
     */
    public String efectuarPagoDividendo(String cuentaCteCargo, String totalDividendo, String trxCargo,
            String codMnemonico, String numeroOperacion, String numeroDividendo,
            HipConDivImpago datosDividendoAPagar, String trxReverso) throws com.schema.util.GeneralException,
            wcorp.util.GeneralException, RemoteException {
        
        String resultado = "";
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] inicio");
            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] cuentaCteCargo : " + cuentaCteCargo);
            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] totalDividendo : " + totalDividendo);
            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] trxCargo : " + trxCargo);
            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] codMnemonico : " + codMnemonico);
            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] numeroOperacion : " + numeroOperacion);
            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] numeroDividendo : " + numeroDividendo);
            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] trxReverso : " + trxReverso);
        }
        String oficina = (String) (TablaValores.getValor(TABLA_PARAMETRO_PAGO_DIVIDENDOS,
                "codOficina", "id"));
        String operador = (String) (TablaValores.getValor(TABLA_PARAMETRO_PAGO_DIVIDENDOS,
                "operador", "Desc"));
        String formaPago = (String) (TablaValores.getValor(TABLA_PARAMETRO_PAGO_DIVIDENDOS,
                "formaPago", "tipo"));
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] oficina : " + oficina);
            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] operador : " + operador);
            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] formaPago : " + formaPago);
            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] Se realiza la consulta para "
                    + "saber si el cliente puede cancelar el pago de dividendos.");
        }
        try {
            resultado = pagaDividendoVerificaPACCliente(numeroOperacion, numeroDividendo, 
                    String.valueOf(datosDividendoAPagar.getSubTotalPesos()),
                    String.valueOf(datosDividendoAPagar.interesPenal),
                    String.valueOf(datosDividendoAPagar.getGastoCobranza()), totalDividendo,
                    oficina, formaPago, operador, SERVICIO_CONSULTA);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] resultado de la consulta :"
                        + resultado);
            }
        } catch (Exception e2) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error("[efectuarPagoDividendo] [" + numeroOperacion + "] error al consultar"
                        + " el cliente no puede cancelar el dividendo :");
            }
            return PAGO_DIVIDENDOS_ERROR_PAGO;
        }
        
        if (resultado != null && resultado.equals(PAGO_DIVIDENDOS_EXITO)) {
            try {
                cuentasDelegate = new CuentasDelegate();
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] Después de iniciar"
                            + " el Delegate : " + cuentasDelegate);
                }
            } catch (Exception ex) {
                if (getLogger().isEnabledFor(Level.ERROR)) {
                    getLogger().debug("[PagoDividendosAction] [" + numeroOperacion + "]"
                            + " Error al instanciar delegate:" + ex.getMessage());
                }
                throw new GeneralException("Error al instanciar al delegate");
            }
            try {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] antes de realizar movCtaStvarios3p");
                    getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] cuentaCteCargo : "
                            + cuentaCteCargo);
                    getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] totalDividendo : " 
                            + totalDividendo);
                    getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] trxCargo : " + trxCargo);
                    getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] codMnemonico : " 
                            + codMnemonico);
                }
                resultado = cuentasDelegate.movCtaStvarios3p(USUARIO, cuentaCteCargo, Double.valueOf(totalDividendo).doubleValue(),
                        trxCargo, codMnemonico);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] resultado de movCtaStvarios3p :"
                            + resultado);
                }
            } catch (Exception e1) {
                if (getLogger().isEnabledFor(Level.ERROR)) {
                    getLogger().error("[PagoDividendosAction] [" + numeroOperacion + "]"
                            + " Error al ejecutar movCtaStvarios3p" + e1.getMessage());
                }
                
                String resultadoCargo = cuentasDelegate.movCtaStvarios3p(USUARIO, cuentaCteCargo, 
                        Double.valueOf(totalDividendo).doubleValue(), trxReverso, codMnemonico);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] reversa de cargo realiza con exito");
                    getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] resultado "
                            + "de la reversa : " + resultadoCargo);
                }
                
                return PAGO_DIVIDENDOS_ERROR_CARGO;
            }
             
            if (resultado != null) {
                try {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("[efectuarPagoDividend] [" + numeroOperacion + "] "
                                + "antes de realizar el pago dividendo");
                        getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] numeroOperacion : "
                                + numeroOperacion);
                        getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] numeroDividendo : "
                                + numeroDividendo);
                        getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] subTotal : "
                                + datosDividendoAPagar.getSubTotalPesos());
                        getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] interes penal :"
                                + datosDividendoAPagar.interesPenal);
                        getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] gasto cobranza :"
                                + datosDividendoAPagar.getGastoCobranza());
                        getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] total dividendo : "
                                + totalDividendo);
                        getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] oficina : " + oficina);
                        getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] formaPago : "
                                + formaPago);
                        getLogger().debug("[efectuarPagoDividend] [" + numeroOperacion + "] operador : " + operador);
                    }
                    resultado = pagaDividendoVerificaPACCliente(numeroOperacion, numeroDividendo, 
                            String.valueOf(datosDividendoAPagar.getSubTotalPesos()),
                            String.valueOf(datosDividendoAPagar.interesPenal),
                            String.valueOf(datosDividendoAPagar.getGastoCobranza()), totalDividendo,
                            oficina, formaPago, operador, SERVICIO_PAGO);
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] resultado de "
                                + "pagaDividendoVerificaPACCliente: " + resultado);
                    }
                    if (resultado != null && !resultado.equals(PAGO_DIVIDENDOS_EXITO)) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] resultado del pago : "
                                    + resultado);
                            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] Se comienza reversa");
                        }
                        String resultadoCargo = cuentasDelegate.movCtaStvarios3p(USUARIO, cuentaCteCargo, 
                                Double.valueOf(totalDividendo).doubleValue(), trxReverso, codMnemonico);
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("[efectuarPagoDividendo] [" + numeroOperacion + "] resultado "
                                    + "de la reversa : " + resultadoCargo);
                        }
                        return resultado;
                    }

                } catch (Exception e) {
                    if (getLogger().isEnabledFor(Level.ERROR)) {
                        getLogger().error("[efectuarPagoDividendo] [" + numeroOperacion + "] Error al realizar"
                                + " el pagaDividendoVerificaPACCliente" + e.getMessage());
                        getLogger().error("[efectuarPagoDividendo] [" + numeroOperacion + "] Se comienza a"
                                + " realiza la reversa");
                    }
                    resultado = cuentasDelegate.movCtaStvarios3p(USUARIO, cuentaCteCargo,
                            Double.valueOf(totalDividendo).doubleValue(), trxReverso, codMnemonico);
                    if (getLogger().isEnabledFor(Level.ERROR)) {
                        getLogger().error("[efectuarPagoDividendo] [" + numeroOperacion + "] resultado de "
                                + "la reversa : " + resultado);
                    }
                    return PAGO_DIVIDENDOS_ERROR_PAGO;
                }
            } else {
                return PAGO_DIVIDENDOS_ERROR_CARGO;
            }
            return PAGO_DIVIDENDOS_EXITO;
        } else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[efectuarPagoDividend] [" + numeroOperacion + "] No es posible cancelar"
                        + " el dividendo");
            }
            return resultado;
        }
    }
    
    
    /**
     * Este método se encarga de obtener los datos básicos de un crédito BCI Home Clásico + Crédito 
     * Complementario.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 23/01/2009, José Flores (TINet): versión inicial </li>
     * <li>2.0 11/03/2013 Esteban Ramirez(Orand): Se exporta método a HipotecarioDAO.</li>
     * </ul>
     * 
     * @param data contiene los datos ingresados para el calculo del crédito BCI Home Clásico + Complementario.
     * @return este método retorna un arreglo de productos correspondiente.
     * @throws GeneralException En caso de ocurrir un error
     * @since 5.6
     */
    public ProductoCreditoTO[] simulacionConComplementario(DatosOperacionVO data) throws GeneralException {
        logger.debug("[simulacionConComplementario]::inicio método Bean");
        return hipotecarioDAO.simulacionConComplementario(data);
    }

    /**
     * Este método se encarga de obtener los datos detallados de un crédito BCI Home Clásico + Crédito
     * Complementario.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 23/01/2009, José Flores (TINet): versión inicial </li>
	 * <li>2.0 11/03/2013 Yasmin Rocha (Orand): Se exporta método a HipotecarioDAO.java. </li>
     * </ul>
     * 
     * @param data datos generales necesarios para la consulta.
     * @param creditoTO Creditos que forman nuestro producto compuesto.
     * @return un arreglo de productos correspondiente.
     * @throws GeneralException En caso de ocurrir un error
     * @since 5.6
     */
    public ProductoCreditoTO[] detalleCreditoConComplementario(DatosOperacionVO data, ProductoCreditoTO[] creditoTO)
        throws GeneralException {
        logger.debug("[detalleCreditoConComplementario]::inicio método Bean");
        return hipotecarioDAO.detalleCreditoConComplementario(data, creditoTO);
    }

    /**
     * Este método se encarga de obtener los datos básicos de un crédito BCI Home Mixto.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 23/01/2009, José Flores (TINet): versión inicial </li>
	 * <li>2.0 11/03/2013 Yasmin Rocha (Orand): Se exporta método a HipotecarioDAO.java. </li>
     * </ul>
     * 
     * @param data datos generales necesarios para la consulta.
     * @return este método retorna un arreglo de productos correspondiente.
     * @throws GeneralException En caso de ocurrir un error
     * @since 5.6
     */
    public ProductoCreditoTO[] simulacionMixto(DatosOperacionVO data) throws GeneralException {
        logger.debug("[simulacionMixto]::inicio método Bean");
        return hipotecarioDAO.simulacionMixto(data);
    }

    
	/**
     * Este método se encarga de obtener los datos detallados de un crédito BCI Home Mixto.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 23/01/2009, José Flores (TINet): versión inicial </li>
     * <li>2.0 11/03/2013 Esteban Ramirez (Orand): Se exporta método a HipotecarioDAO.java. </li>
     * </ul>
     * </p>
     * @param data datos generales necesarios para la consulta.
     * @param creditoTO Creditos que forman nuestro producto compuesto.
     * @return este método retorna un arreglo de productos correspondiente.
     * @throws GeneralException En caso de ocurrir un error
     * @since 5.6
     */
    public ProductoCreditoTO[] detalleMixto(DatosOperacionVO data, ProductoCreditoTO[] creditoTO)
        throws GeneralException {
        logger.debug("[detalleMixto]::inicio método Bean");
        return hipotecarioDAO.detalleMixto(data, creditoTO);
    }

    /**
     * Este método nos devuelve un arreglo de seguros vinculados.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 13/02/2009, José Flores (TINet): versión inicial </li>
	 * <li>2.0 11/03/2013 Yasmin Rocha (Orand): Se exporta método a HipotecarioDAO.java. </li>
     * </ul>
     * 
     * @param data Datos sobre la simulación necesarios para obtener los seguros
     * @return este método retorna un arreglo de seguros correspondiente.
     * @throws GeneralException En caso de ocurrir un error
     * @since 5.6
     */
    public SeguroCliente[] segurosVinculados(DatosOperacionVO data) throws GeneralException {
        logger.debug("[segurosVinculados]::inicio método Bean");
        return hipotecarioDAO.segurosVinculados(data);
    }
    
    /**
     * Método que retorna, como un arreglo, el listado de seguros vinculados (seguros adicionales) 
     * disponibles para el Simulador de crédito hipotecario. Este método reutiliza la clase SeguroCliente
     * para modelar los seguros adicionales.
     * 
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 27/08/2009, Patricio Valenzuela S. (Sermaluc Ltda.): versión inicial </li>
     * </ul><p>
     * 
     * @return Retorna arreglo de seguros correspondiente.
     * @throws wcorp.util.GeneralException En caso de ocurrir un error
     * @throws   wcorp.util.GeneralException
     * @since 5.7
     */
    public SeguroCliente[] listaSegurosVinculados() throws wcorp.util.GeneralException {

        SeguroCliente[] listaSeguros = null;
        String MONTO_CREDITO = "2000";
        String MONTO_BIEN_RAIZ = "3000";
        String MONTO_CONTENIDO = "300";
        String ID_CONTENIDO = "2";
        
        if (getLogger().isInfoEnabled()) {
            getLogger().info("[listaSegurosVinculados] : comienza ejecución");
        }

        try {
            ServletSessionPool sesion = joltPool.getSesion(ejbName);
            DataSet parametros = new DataSet();
            Result resultado = null;
            int cantidad = 0;
            parametros.setValue("montoCredito", MONTO_CREDITO);
            parametros.setValue("montoBienesRaices", MONTO_BIEN_RAIZ);
            parametros.setValue("indicador", "0");
            parametros.setValue("glosa2",  "0");
            parametros.setValue("ciaSegCesantia", "0");
            parametros.setValue("idHogar", ID_CONTENIDO);
            parametros.setValue("monto1", MONTO_CONTENIDO);

            resultado = sesion.call("HipCalcSegVinc", parametros, null);
            cantidad = resultado.getValue("cantidad", 0, null) != null ? Integer.parseInt(String.valueOf(resultado
                .getValue("cantidad", 0, null))) : 0;

            listaSeguros = new SeguroCliente[cantidad];
            for (int i = 0; i < cantidad; i++) {
            	listaSeguros[i] = new SeguroCliente();
            	listaSeguros[i].setIdProducto((String) resultado.getValue("codSeguro", i, null));
            	listaSeguros[i].setProducto((String) resultado.getValue("nombreSeguro", i, null));
            }
            return listaSeguros;
        }
        catch (Exception e) {
            // Se captura Exception para evitar colocar sentencias catch con la
            // misma lógica para el manejo de las excepciones
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Error de servicio", e);
            }
            throw new GeneralException("TUX");
        }
    }    	
	
    
    /**
     * Método que permite obtener los dividendos pagados de un cliente.
     * <p>
     * Registro de versiones:
     * <li>1.0 06/11/2009, Pedro Carmona Escobar (SenTRA): versión inicial
     * </li>
     * 
     * @param String con el Número de Operación por la cual se consultarán los dividendos.
     * @return DividendoPagadoTO[] con arreglo de los dividendo pagados relacionados con el Número de Operación.
     * @throws wcorp.util.GeneralException En caso de ocurrir un error.
     * @since 5.9
     */
    public CertificadoDividendoTO[] obtenerCertificadoDividendosPagados(String operacion) throws GeneralException {
    	 if (getLogger().isInfoEnabled()) {getLogger().info("[obtenerCertificadoDividendosPagados] ["+operacion+"] numeroOperacion= " + operacion);}
    	 CertificadoDividendoTO[] dividendos = null;
        try {
        	HipotecarioDAO hipotecarioDAO = new HipotecarioDAO();
        	dividendos = hipotecarioDAO.obtenerCertificadoDividendosPagados(operacion);
        	 if (getLogger().isInfoEnabled()) {getLogger().info("[obtenerCertificadoDividendosPagados] ["+operacion+"] Dividendos Impagos recuperados: " + StringUtil.contenidoDe(dividendos));}
        }
        catch (Exception e) {
        	 if (getLogger().isEnabledFor(Level.ERROR)) {getLogger().error("[obtenerDividendos] ["+operacion+"] Error recuperando los dividendos pagados: "+ e.toString());}
         throw new GeneralException(ERROR_RECUPERANDO_DIVIDENDOS);
        }
        return dividendos;
    }
    
    
    /**
     * Método que permite obtener los intereses pagados de un crédito hipotecario.
     * <p>
     * Registro de versiones:
     * <li>1.0 06/11/2009, Pedro Carmona Escobar (SenTRA): versión inicial
     * </li>
     * 
     * @param String con el Número de Operación por la cual se consultarán los intereses.
     * @param int con el Périodo a consultar.
     * @return CertificadoInteresesVO con los intereses obtenidos.
     * @throws wcorp.util.GeneralException En caso de ocurrir un error.
     * @since 5.9
     */
    public CertificadoInteresesVO obtenerCertificadoInteresesPagados(String operacion, int periodo) throws wcorp.util.GeneralException {
   	 	
    	if (getLogger().isInfoEnabled()) {getLogger().info("[obtenerCertificadoInteresesPagados] ["+operacion+"] numeroOperacion [" + operacion+"]   periodo ["+periodo+"]");}
    	CertificadoInteresesVO intereses = null;
        try {
        	HipotecarioDAO hipotecarioDAO = new HipotecarioDAO();
        	intereses = hipotecarioDAO.obtenerCertificadoInteresesPagados(operacion, periodo);
        	 if (getLogger().isInfoEnabled()) {getLogger().info("[obtenerCertificadoInteresesPagados] ["+operacion+"] Intereses recuperados: " + StringUtil.contenidoDe(intereses));}
        }
        catch (Exception e) {
        	 if (getLogger().isEnabledFor(Level.ERROR)) {getLogger().error("[obtenerCertificadoInteresesPagados] ["+operacion+"] Error recuperando intereses: "+ e.toString());}
         throw new GeneralException(ERROR_RECUPERANDO_DIVIDENDOS);
        }
        return intereses;
    }
    
    
    /**
     * Método que permite obtener la deuda de un crédito hipotecario.
     * <p>
     * Registro de versiones:
     * <li>1.0 06/11/2009, Pedro Carmona Escobar (SenTRA): versión inicial
     * </li>
     * 
     * @param String con el Número de Operación por la cual se consultará la deuda.
     * @return DeudaHipotecariaTO con la deuda obtenida.
     * @throws wcorp.util.GeneralException En caso de ocurrir un error.
     * @since 5.9
     */
    public DeudaHipotecariaTO obtenerDeudaHipotecaria(String operacion) throws wcorp.util.GeneralException {
   	 	
    	if (getLogger().isInfoEnabled()) {getLogger().info("[obtenerDeudaHipotecaria] ["+operacion+"] numeroOperacion [" + operacion+"]");}
    	DeudaHipotecariaTO deuda = null;
        try {
        	HipotecarioDAO hipotecarioDAO = new HipotecarioDAO();
        	deuda = hipotecarioDAO.obtenerDeudaHipotecaria(operacion);
        	 if (getLogger().isInfoEnabled()) {getLogger().info("[obtenerDeudaHipotecaria] ["+operacion+"] Deuda recuperada: " + StringUtil.contenidoDe(deuda));}
        }
        catch (Exception e) {
        	 if (getLogger().isEnabledFor(Level.ERROR)) {getLogger().error("[obtenerDeudaHipotecaria] ["+operacion+"] Error recuperar deuda: "+ e.toString());}
         throw new GeneralException(ERROR_RECUPERANDO_DIVIDENDOS);
        }
        return deuda;
    }
    
    /**
     * Obtiene el cálculo del Costo Actual Equivalente (CAE).
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0 (04/10/2011 Jorge San Martín (ImageMaker IT)) - Versión inicial </li>
     * <li>1.1 (18/05/2012 Jorge San Martín (ImageMaker IT)): Se agrega loggeo en caso de que se reciba una
     *     exception desde el dao. Se agregan tags de exceptiones en javadoc.</li>
     * </ul>
     * </p>
     * 
     * @param data Objeto que contiene los parámetros del procedimiento
     * @return un objeto de tipo ResultCalculoCAEVO con los datos retornados por el tuxedo.
     * @throws wcorp.util.GeneralException
     * @throws com.schema.util.GeneralException 
     * @throws TuxedoException 
     * @throws RemoteException 
     * @since 5.9.2
     */
    public ResultCalculoCAEVO calculaCAE(DatosCalculoCAEVO data ) 
    throws com.schema.util.GeneralException, wcorp.util.GeneralException,
    TuxedoException, RemoteException {
    	
    	try {
    		return simulacionDAO.calculaCAE(data);
    	} 
        catch(Exception e) {
                if (getLogger().isEnabledFor(Level.ERROR) ){
    		   getLogger().error("[calculaCAE] Error al calcular CAE" + e.toString());
                }
    		throw new wcorp.util.GeneralException("TUX");
    	}    	
    }    

    /**
     * Consulta listado de Regiones
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0 (02/08/2012 Cristian Recabarren R. (Sermaluc ltda.)) : version inicial</li>
     * <li>1.1 (25/03/2015 Francisco Gonzalez P. (Imagemaker)) : Se modifican las Exception</li>
     * </ul>
     * </p>
     * @param void
     * @return un objeto de tipo RegionTO[] con listado de regiones.
     * @throws Exception Exception
     * @throws GeneralException GeneralException
     * @since 5.9.4
     */
    public RegionTO[] obtenerRegiones()
    throws Exception, GeneralException {
    	try {
    		return simulacionDAO.obtenerRegiones();
    	} 
    	catch(GeneralException ge) {
    		throw new Exception("Error al obtener las regiones." + ge);
    	}
    	catch(Exception e) {
    		throw new Exception("No se pudo recuperar las regiones.." + e);
    	}    	
    }
    
    /**
     * Consulta listado de Ciudades por Región a modelo ASICOM
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0 (02/08/2012 Cristian Recabarren R. (Sermaluc ltda.)) : version inicial</li>
     * </ul>
     * </p>
     * @param void
     * @return un objeto de tipo RegionTO[] con listado de regiones.
     * @throws com.schema.util.GeneralException 
     * @throws wcorp.util.GeneralException
     * @since 5.9.4
     */
    public CiudadTO[] obtenerCiudadesPorRegion(String codigoRegion)
    throws com.schema.util.GeneralException, wcorp.util.GeneralException,
    TuxedoException, RemoteException {
    	try {
    		return simulacionDAO.obtenerCiudadesPorRegion(codigoRegion);
    	} catch(Exception e) {
    		throw new wcorp.util.GeneralException("TUX");
    	}    	
    }
    
    /**
     * Consulta listado de Comunas por Ciudad a modelo ASICOM
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0 (02/08/2012 Cristian Recabarren R. (Sermaluc ltda.)) : version inicial</li>
     * </ul>
     * </p>
     * @param void
     * @return un objeto de tipo RegionTO[] con listado de regiones.
     * @throws com.schema.util.GeneralException 
     * @throws wcorp.util.GeneralException
     * @since 5.9.4
     */
    public ComunaTO[] obtenerComunasPorCiudad(String codigoCiudad)
    throws com.schema.util.GeneralException, wcorp.util.GeneralException,
    TuxedoException, RemoteException {
    	try {
    		return simulacionDAO.obtenerComunasPorCiudad(codigoCiudad);
    	} catch(Exception e) {
    		throw new wcorp.util.GeneralException("TUX");
    	}    	
    }

    /**
     * Obtiene la tabla de productos del sistema por canal y los filtra por un producto especifico.
     * <P>
     *
     * Registro de versiones:
     * <ul>
     * <li>1.0 27/11/2012 Cristian Recabarren R.(Sermaluc Ltda.): versión inicial.</li>
     * </ul><P>
     *
     * @param canal Código del canal para el cual se desean obtener los datos.
     * @param codigoProducto Código del prodcuto para el cual se desean obtener los datos.
     * @return Conjunto de objetos con la descripción de productos
     * @throws com.schema.util.GeneralException En caso de no poder ejecutar el servicio.
     * @throws wcorp.util.GeneralException
     * @throws TuxedoException
     * @throws RemoteException
     * @since 5.9.5
     */
    public Collection getTablaProducto(String canal, int codigoProducto) throws
         com.schema.util.GeneralException, wcorp.util.GeneralException, TuxedoException, RemoteException {
   	 
          if (getLogger().isEnabledFor(Level.INFO) ){
   	    getLogger().info("[getTablaProducto]  canal: "+canal);
   	    getLogger().info("[getTablaProducto]  codigoProducto: "+codigoProducto);
          }
         try {
              return simulacionDAO.getTablaProducto(canal, codigoProducto);
         }
         catch ( Exception ex ) {
       	   if (getLogger().isEnabledFor(Level.ERROR) ){
                getLogger().error("[getTablaProducto ]  ERROR: "+ ex.toString() );
           }
			throw new wcorp.util.GeneralException("TUX");
         }
    }


    /**
     * Calcula la simulación de una operación con el servicio Tuxedo especificado, entregando todas las combinaciones
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0 (08/07/2013 Cristian Recabarren R. (Sermaluc ltda.)): versión inicial.</li>
     * </ul>
     * </p>
     * @param data Objeto que contiene los datos asociados a la operación
     * @return Collection de objetos SimulacionPlazosVO con el resultado de la simulación,
     * es decir, las distintas alternativas de plazos, dividendos, seguros, etc. y las combinaciones de seguros.
     * @throws com.schema.util.GeneralException
     * @throws wcorp.util.GeneralException
     * @throws NamingException 
     */
    public Collection calculaSimulacionTotal( DatosOperacionVO data )
    throws com.schema.util.GeneralException, wcorp.util.GeneralException, NamingException {
        getLogger().debug("[calculaSimulacionTotal]::inicio método Bean");
        return hipotecarioDAO.calculaSimulacionTotal(data);
    }

    /**
     * Calcula la simulación de una operación con el servicio Tuxedo especificado, entregando todas las combinaciones
     * para el calculo renegociado de simulacion.
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0 (08/07/2013 Cristian Recabarren R. (Sermaluc ltda.)): versión inicial.</li>
     * </ul>
     * </p>
     * @param data Objeto que contiene los datos asociados a la operación
     * @return Collection de objetos SimulacionPlazosVO con el resultado de la simulación,
     * es decir, las distintas alternativas de plazos, dividendos, seguros, etc. y las combinaciones de seguros.
     * @throws com.schema.util.GeneralException
     * @throws wcorp.util.GeneralException
     * @throws NamingException 
     */
    public Collection calculaSimulacionRenegociadaTotal( DatosOperacionVO data )
    throws com.schema.util.GeneralException, wcorp.util.GeneralException, NamingException {
        getLogger().debug("[calculaSimulacionRenegociadaTotal]::inicio método Bean");
        return hipotecarioDAO.calculaSimulacionRenegociadaTotal(data);
    }

    /** 
     * Metodo que obtienes las solicitud CHIP.
     * <p>
     * 
     * Registro de versiones:
     * <UL>
     * <li>1.0 17/11/2014 Victor Caroca.   (Sentra): versión inicial.</LI>
     * </UL>
     * <P>
     * @param rut long.
     * @param dv char.
     * @return List.
     * @since 6.1
     */
    public List obtenerSolicitudCHIP(long rut, char dv) {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtenerSolicitudCHIP][" + rut + "][BCI_INI] dv [" + dv + "]");
        }

        List result = null;

        try {
            String codServicio = TablaValores.getValor(TABLA_PARAMETRO, "CodServicio", "SolicitudCHIP");
            String wsdl = TablaValores.getValor(TABLA_PARAMETRO, "servSolicitudChip", "url");

            if (getLogger().isEnabledFor(Level.DEBUG)) {
                getLogger().debug("[obtenerSolicitudCHIP][" + rut + "] codServicio [" + codServicio 
                    + "], wsdl [" + wsdl + "]");
            }

            ServicioConsultaEstadoSolicitudCHipService service = null;
            ServicioConsultaEstadoSolicitudCHipPortType_Stub stub = null;
            ConsultaOperacionesPorRutRequestSOAP datos = null;
            RequestHeader reqHeader = null;
            ConsumerAnonType consumer = null;
            MessageAnonType mensaje = null;
            ServiceAnonType servicio = null;
            ConsultaOperacionesPorRutRequestBodytype reqBody = null;
            ConsultaOperacionesPorRutResponseSOAP response = null;
            ConsultaOperacionesPorRutResponseBodytype respBody = null;
             
            service = new ServicioConsultaEstadoSolicitudCHipService_Impl(wsdl);
            stub = (ServicioConsultaEstadoSolicitudCHipPortType_Stub)
                service.getServicioConsultaEstadoSolicitudCHipPortType();

            datos = new ConsultaOperacionesPorRutRequestSOAP();
            reqHeader = new RequestHeader();
            consumer  = new ConsumerAnonType();

            consumer.setCode(null);
            consumer.setUserID(null);
            reqHeader.setConsumer(consumer);
            mensaje = new MessageAnonType();

            mensaje.setMessageId(null);
            reqHeader.setMessage(mensaje);
            servicio = new ServiceAnonType();
            servicio.setCode("");
            reqHeader.setService(servicio);

            reqBody = new ConsultaOperacionesPorRutRequestBodytype();

            reqBody.setCod_Servicio(codServicio);
            reqBody.setDv(String.valueOf(dv));
            reqBody.setRut(Long.toString(rut));

            datos.setRequestHeader(reqHeader);
            datos.setConsultaOperacionesPorRutRequestBody(reqBody);
            response = stub.consultaOperacionesPorRut(datos);
            respBody = response.getConsultaOperacionesPorRutResponseBody();

            if (respBody.getRespuesta() != null) {
                result = new ArrayList();
                if (respBody.getRespuesta().getCod_Estado().equals("00")) {
                    SolicitudList[] listaSolicitudes = respBody.getRespuesta().getSolicitudes();

                    for (int i = 0; i < listaSolicitudes.length; i++) {
                        SolicitudList solicitud = null;
                        solicitud = listaSolicitudes[i];

                        SolicitudVO respuesta = new SolicitudVO();
                        if (null != solicitud.getProducto()) {
                            respuesta.setProducto(solicitud.getProducto());

                            if (getLogger().isEnabledFor(Level.DEBUG)) {
                                getLogger().debug("[obtenerSolicitudCHIP][" + rut + "] producto ["
                                    + solicitud.getProducto() + "]");
                            }
                        }

                        if (null != solicitud.getObjetivo()) {
                            respuesta.setObjetivo(solicitud.getObjetivo());
                            if (getLogger().isEnabledFor(Level.DEBUG)) {
                                getLogger().debug("[obtenerSolicitudCHIP][" + rut + "] objetivo ["
                                    + solicitud.getObjetivo() + "]");
                            }
                        }

                        if (null != solicitud.getNumero_Operacion()) {
                            respuesta.setNumeroOperacion(Integer.parseInt(solicitud.getNumero_Operacion()));
                            if (getLogger().isEnabledFor(Level.DEBUG)) {
                                getLogger().debug("[obtenerSolicitudCHIP][" + rut + "] numero_Operacion ["
                                    + solicitud.getNumero_Operacion() + "]");
                            }
                        }

                        if (null != solicitud.getFecha_Calc()) {
                            SimpleDateFormat formatoDelTexto = new SimpleDateFormat("yyyyMMdd");
                            Date fecha = null;

                            fecha = formatoDelTexto.parse((String) solicitud.getFecha_Calc());
                            java.sql.Date sqlDate = new java.sql.Date(fecha.getTime());
                            respuesta.setFechaSolicitud(sqlDate);

                            if (getLogger().isEnabledFor(Level.DEBUG)) {
                                getLogger().debug("[obtenerSolicitudCHIP][" + rut + "] fecha_Calc ["
                                    + (String) solicitud.getFecha_Calc() + "]");
                            }
                        }
                        result.add(respuesta);
                            
                        if (getLogger().isEnabledFor(Level.DEBUG)) {
                            getLogger().debug("[obtenerSolicitudCHIP][" + rut 
                                + "] Los Datos han sido Guardados");
                        }
                    }
                }
                else {
                    if (getLogger().isEnabledFor(Level.DEBUG)) {
                        getLogger().debug("[obtenerSolicitudCHIP][" + rut
                            + "] No retorna Información, Descripción Estado ["
                            + respBody.getRespuesta().getDes_Estado() + "]");
                    }
                }
            }
            else {
                if (getLogger().isEnabledFor(Level.DEBUG)) {
                    getLogger().debug("[obtenerSolicitudCHIP][" + rut + "] No obtiene Datos.");
                }
            }
        }
        catch (IOException e) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error("[obtenerSolicitudCHIP][" + rut + "]"
                        + " [IOException] Mensaje =<" + e.getMessage() + ">", e);
            }
        }
        catch (ParseException e) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error("[obtenerSolicitudCHIP][" + rut + "]"
                        + " [ParseException] Mensaje =<" + e.getMessage() + ">", e);
            }
        }
        catch (Exception e) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error("[obtenerSolicitudCHIP][" + rut + "]"
                        + " [Exception] Mensaje =<" + e.getMessage() + ">", e);
            }
        }

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtenerSolicitudCHIP][" + rut + "][BCI_FINOK]");
        }
        return result;
    }

    /** 
     * Metodo que Obtiene el detalle 
     * de la solicitud.
     * <p>
     * 
     * Registro de versiones:
     * <UL>
     * <li>1.0 17/11/2014 Víctor Caroca.   (Sentra): versión inicial.</LI>
     * </UL>
     * <P>
     * @param rut long.
     * @param numeroOperacion String.
     * @return SeguimientoChipTO.
     * @since 6.1
     */
    public SeguimientoChipTO detalleSolicitud(long rut, String numeroOperacion) {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[detalleSolicitud][" + rut + "][BCI_INI] numeroOperacion ["
                + numeroOperacion + "]");
        }

        SeguimientoChipTO seguimientoChipTO = null;

        try {
            String codServicio = TablaValores.getValor(TABLA_PARAMETRO, "CodServicio", "DetallaCHIP");
            int firma = Integer.parseInt(((String) TablaValores.getValor(TABLA_PARAMETRO, "codFirma", "desc")));
            String wsdl = TablaValores.getValor(TABLA_PARAMETRO, "servSolicitudChip", "url");

            if (getLogger().isEnabledFor(Level.DEBUG)) {
                getLogger().debug("[detalleSolicitud][" + rut + "] codServicio [" + codServicio 
                    + "], wsdl [" + wsdl + "]");
            }
            ServicioConsultaEstadoSolicitudCHipService service = null;
            ServicioConsultaEstadoSolicitudCHipPortType_Stub stub = null;
            ConsultaEstadoSolicitudCHipRequestSOAP datos = null;
            RequestHeader reqHeader = null;
            ConsumerAnonType consumer = null;
            MessageAnonType mensaje = null;
            ServiceAnonType servicio = null;
            ConsultaEstadoSolicitudCHipRequestBodytype reqBody = null;
            ConsultaEstadoSolicitudCHipResponseSOAP response = null;
            ConsultaEstadoSolicitudCHipResponseBodytype respBody = null;

            if (getLogger().isEnabledFor(Level.DEBUG)) {
                getLogger().debug("[detalleSolicitud][" + rut + "] Inicio de variables" );
            }
            service = new ServicioConsultaEstadoSolicitudCHipService_Impl(wsdl);
            stub = (ServicioConsultaEstadoSolicitudCHipPortType_Stub) 
                service.getServicioConsultaEstadoSolicitudCHipPortType();

            datos = new ConsultaEstadoSolicitudCHipRequestSOAP();

            reqHeader = new RequestHeader();
            consumer = new ConsumerAnonType();

            consumer.setCode(null);
            consumer.setUserID(null);
            reqHeader.setConsumer(consumer);
            mensaje = new MessageAnonType();

            mensaje.setMessageId(null);
            reqHeader.setMessage(mensaje);
            servicio = new ServiceAnonType();
            servicio.setCode("");
            reqHeader.setService(servicio);

            reqBody = new ConsultaEstadoSolicitudCHipRequestBodytype();

            reqBody.setCod_Servicio(codServicio);
            reqBody.setNro_Solicitud(numeroOperacion);

            datos.setRequestHeader(reqHeader);
            datos.setConsultaEstadoSolicitudCHipRequestBody(reqBody);
            response = stub.consultaEstadoSolicitudCHip(datos);
            respBody = response.getConsultaEstadoSolicitudCHipResponseBody();

            if (getLogger().isEnabledFor(Level.DEBUG)) {
                getLogger().debug("[detalleSolicitud][" + rut + "] Consulta Realizada");
            }

            if (respBody.getRespuesta() != null) {
                    if (!(respBody.getRespuesta().getCod_Estado().equals("01"))) {

                    seguimientoChipTO = new SeguimientoChipTO();

                    if (getLogger().isEnabledFor(Level.DEBUG)) {
                        getLogger().debug("[detalleSolicitud][" + rut + "] Respuesta Obtenida");
                    }

                    Info_Generaltype respuesta=respBody.getRespuesta().getInfo_General();

                    if (null != respuesta.getTipo_Producto()) {
                        if (respuesta.getTipo_Producto().equals("RECURSE_INTERNO")) {
                            seguimientoChipTO.setFirma(false);
                        }
                        else {
                            seguimientoChipTO.setFirma(true);
                        }
                        if (getLogger().isEnabledFor(Level.DEBUG)) {
                            getLogger().debug("[detalleSolicitud][" + rut + "] Tipo_Producto ["
                                + respuesta.getTipo_Producto() + "]");
                        }
                    }

                    if (null != respuesta.getDireccion_Propiedad()) {
                        seguimientoChipTO.setDireccion(respuesta.getDireccion_Propiedad());

                        seguimientoChipTO.setDireccionCasa(respuesta.getDireccion_Propiedad());

                        if (getLogger().isEnabledFor(Level.DEBUG)) {
                            getLogger().debug("[detalleSolicitud][" + rut + "] Direccion_Propiedad ["
                                + respuesta.getDireccion_Propiedad() + "]");
                        }
                    }

                    if (null != respuesta.getFecha_Pago_Credito()) {

                        SimpleDateFormat formatoDelTexto = new SimpleDateFormat("yyyyMMdd");
                        Date fecha = null;

                        if (getLogger().isEnabledFor(Level.DEBUG)) {
                            getLogger().debug("[detalleSolicitud][" + rut + "] Fecha_Pago_Credito() ["
                                + respuesta.getFecha_Pago_Credito() + "]");
                        }

                        fecha = formatoDelTexto.parse((String) respuesta.getFecha_Pago_Credito());

                        seguimientoChipTO.setFechaLiberacionFondosDate(fecha);
                        seguimientoChipTO.setFechaLiberacionFondos((String) respuesta.
                            getFecha_Pago_Credito().trim());    
                    }
                    else {
                        seguimientoChipTO.setFechaLiberacionFondos("00000000");
                    }

                    if (null != respuesta.getNro_Operacion_Sist_Adm()) {
                        seguimientoChipTO.setNumerosolicitud(
                              ((BigInteger)respuesta.getNro_Operacion_Sist_Adm()).toString());

                        if (getLogger().isEnabledFor(Level.DEBUG)) {
                            getLogger().debug("[detalleSolicitud][" + rut + "] Numerosolicitud ["
                                + respuesta.getNro_Operacion_Sist_Adm() + "]");
                        }
                    }

                    if (null != respBody.getRespuesta().getEtapas()) {
                        List listaEtapas = new ArrayList();
                        List listaEtapasFinal = new ArrayList();
                        int ultimaEtapa = 0;

                        Etapatype[] listaRespuesEtapas=respBody.getRespuesta().getEtapas();

                        for (int i = 0; i < listaRespuesEtapas.length; i++) {
                            Etapatype etapa = null;
                            etapa = listaRespuesEtapas[i];
                            EtapasChipTO etapaTemp = new EtapasChipTO();

                            if (getLogger().isEnabledFor(Level.DEBUG)) {
                                getLogger().debug("[detalleSolicitud][" + rut + "] Etapas");
                            }

                            if (null != etapa.getDescripcion()) {
                                etapaTemp.setDescripcion(etapa.getDescripcion());
                                if (getLogger().isEnabledFor(Level.DEBUG)) {
                                    getLogger().debug("[detalleSolicitud][" + rut + "] Descripcion ["
                                        + etapa.getDescripcion() + "]");
                                }
                            }

                            if (null != etapa.getEstado()) {
                                etapaTemp.setEstado(etapa.getEstado());

                                if (getLogger().isEnabledFor(Level.DEBUG)) {
                                    getLogger().debug("[detalleSolicitud][" + rut + "] Estado ["
                                        + etapa.getEstado() + "]");
                                }
                            }

                            if (null != etapa.getCodigo()) {
                                etapaTemp.setCodigo(((BigInteger)etapa.getCodigo()).toString());

                                if (getLogger().isEnabledFor(Level.DEBUG)) {
                                    getLogger().debug("[detalleSolicitud][" + rut + "] Codigo ["
                                        + etapa.getCodigo() + "]");
                                }
                            }

                            if (null != etapa.getEstado()) {
                                if (etapa.getEstado().trim().equals("TERMINADO") 
                                    ||  etapa.getEstado().trim().equals("PENDIENTE")) {
 
                                    if (etapa.getCodigo().intValue() != firma) {
                                        seguimientoChipTO.setUltimaEtapa(
                                            ((BigInteger)etapa.getCodigo()).intValue());

                                        if (getLogger().isEnabledFor(Level.DEBUG)) {
                                            getLogger().debug("[detalleSolicitud][" + rut + "] Ultima Etapa ["
                                                + seguimientoChipTO.getUltimaEtapa() + "]");
                                        }
                                    }
                                    else {
                                        if (!respuesta.getTipo_Producto().equals("RECURSE_INTERNO")) {
                                            seguimientoChipTO.setUltimaEtapa(
                                                ((BigInteger)etapa.getCodigo()).intValue());
                                        }
                                    }
                                }

                                if (etapa.getEstado().equals("TERMINADO")) {
                                    etapaTemp.setFinalizado(true);
                                }
                                else {
                                    etapaTemp.setFinalizado(false);
                                }
                            }
                            etapaTemp.setMonoLogos(false);
                            etapaTemp.setMonoLogosR(false);

                            if (null != etapa.getHitos()) {
                                List listahitosFinal = new ArrayList();
                                boolean nofinalizado = false;

                                Hitotype[] listaRespuesHitos = etapa.getHitos();

                                for (int t = 0; t < listaRespuesHitos.length; t++) {
                                    Hitotype hito = null;
                                    hito = listaRespuesHitos[t];

                                    if (getLogger().isEnabledFor(Level.DEBUG)) {
                                         getLogger().debug("[detalleSolicitud][" + rut + "] Hitos ");
                                    }

                                    HitosChipTO hitoTemp = new HitosChipTO();

                                    if (null != hito.getDescripcion()) {
                                        hitoTemp.setDescripcion(hito.getDescripcion());

                                        if (getLogger().isEnabledFor(Level.DEBUG)) {
                                            getLogger().debug("[detalleSolicitud][" + rut + "] Descripcion ["
                                                + hito.getDescripcion() + "]");
                                        }
                                    }

                                    if ((hito.getFecha_Termino().trim()).equals("00000000")) {
                                        hitoTemp.setCheck(false);
                                        nofinalizado = true;
                                    }
                                    else {
                                        hitoTemp.setCheck(true);
                                    }

                                    if (null != hito.getCodigo()) {
                                        hitoTemp.setCodigo(((BigInteger)hito.getCodigo()).toString());
                                        if (getLogger().isEnabledFor(Level.DEBUG)) {
                                            getLogger().debug("[detalleSolicitud][" + rut + "] Codigo ["
                                                + hito.getCodigo() + "]");
                                        }
                                    }

                                    if (null != hito.getNumero()) {
                                        hitoTemp.setNumero(((BigInteger)hito.getNumero()).toString());

                                        if (getLogger().isEnabledFor(Level.DEBUG)) {
                                            getLogger().debug("[detalleSolicitud][" + rut + "] Numero ["
                                                + hito.getNumero() + "]");
                                        }
                                    }
                                    listahitosFinal.add(hitoTemp);
                                }
                                etapaTemp.setSubEtapas(listahitosFinal);
                            }
                            listaEtapasFinal.add(etapaTemp);
                        }
                        seguimientoChipTO.setEtapas(listaEtapasFinal);
                    }

                    if (getLogger().isEnabledFor(Level.DEBUG)) {
                        getLogger().debug("[detalleSolicitud][" + rut + "] Seguimiento ["
                            + seguimientoChipTO + "]");
                    }
                }
                else {
                    if (getLogger().isEnabledFor(Level.DEBUG)) {
                        getLogger().debug("[detalleSolicitud][" + rut 
                            + "] No retorna Información, Descripción Estado ["
                            + respBody.getRespuesta().getDes_Estado() + "]");
                    }
                }
            }
            else {
                if (getLogger().isEnabledFor(Level.DEBUG)) {
                    getLogger().debug("[detalleSolicitud][" + rut + "] No obtiene Respuesta");
                }
            }
        }
        catch (Exception e) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error("[detalleSolicitud][" + rut + "][Exception] Mensaje=<"
                    + e.getMessage() + ">", e);
            }
        }

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[detalleSolicitud][" + rut + "][BCI_FINOK]");
        }
        return seguimientoChipTO;
    }

    /**
     * Clase implementadad por exigencia Normativa Log4j-BCI
     * 
     * Registro de versiones:
     * <ul>
     * <li>1.0 08/07/2013 Cristian Recabarren R. (Sermaluc ltda.)): Version
     * Inicial</li>
     * </ul>
     * 
     * @return Logger
     */
    public Logger getLogger() {
       if (logger == null) {
           logger = Logger.getLogger(this.getClass());
       }
       return logger;
   }
    
   /**
     * Metodo para almacenar el resultado de una simulacion.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * <p>
     * 
     * @param simulacionProcesoCHIP
     *            TO con los datos obtenidos en una simulacion de credito
     *            hipotecario.
     * @throws GeneralException
     *          Error general.
     * @since 6.2
     */
    public void guardarSimulacionProcesoCHIP(
            SimulacionProcesoCHIPTO simulacionProcesoCHIP)
            throws GeneralException{

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[guardarSimulacionProcesoCHIP] [BCI_INI]: BEAN.");
        }
        procesoCHIPDAO.guardarSimulacionProcesoCHIP(simulacionProcesoCHIP);
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                    "[guardarSimulacionProcesoCHIP] [BCI_FINOK]: BEAN.");
        }
    }

    /**
     * Metodo para generar una instancia de proceso CHIP.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * <p>
     * 
     * @param datosInstanciaProcesoCHIP
     *            TO con los datos obtenidos en una simulacion de credito
     *            hipotecario.
     * @throws GeneralException
     *             Error General.
     * @since 6.2
     */
    public void generarInstanciaProcesoCHIP(
            DatosInstanciaProcesoCHIPTO datosInstanciaProcesoCHIP)
        throws GeneralException {
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[generarInstanciaProcesoCHIP] [BCI_INI]: BEAN.");
        }

        datosInstanciaProcesoCHIP.setTotalSimulaciones(
            procesoCHIPDAO.consultarTotalSimulacionesCHIP(datosInstanciaProcesoCHIP.getRutCliente()));
        
        procesoCHIPDAO.generarInstanciaProcesoCHIP(
            this.convertirDatosInstanciaADatosWsCHIP(datosInstanciaProcesoCHIP));
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[generarInstanciaProcesoCHIP] [BCI_FINOK]: BEAN.");
        }
    }

    /**
     * Transforma un DatosInstanciaProcesoCHIPTO a DatosSolicitudCreditoTO.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * <p>
     * 
     * @param datosInstancia
     *            TO con los datos recibidos desde la capa web.
     * @return DatosSolicitudCreditoTO.
     * @since 6.2
     */
    private DatosSolicitudCreditoTO convertirDatosInstanciaADatosWsCHIP(
            DatosInstanciaProcesoCHIPTO datosInstancia) {
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                    "[convertirDatosInstanciaADatosWsCHIP] ["
                            + datosInstancia.toString() + "] [BCI_INI]: BEAN.");
        }
        DatosSolicitudCreditoTO datosProceso = new DatosSolicitudCreditoTO();

        datosProceso.setRut(datosInstancia.getRutCliente());
        datosProceso.setDv((int) datosInstancia.getDvCliente());
        datosProceso.setNombreSolicitante(normalizarASCII(datosInstancia.getNombreCliente()));
        datosProceso.setApellidoPaternoSolicitante(normalizarASCII(datosInstancia.getApellidoPaterno()));
        datosProceso.setApellidoMaternoSolicitante(datosInstancia.getApellidoPaterno());
        datosProceso.setCodeudor(datosInstancia.isCodeudor() ? COD_RESP_SI : COD_RESP_NO);
        datosProceso.setIndicadorCliente(datosInstancia.isIndCliente() ? COD_RESP_SI : COD_RESP_NO);
        datosProceso.setEmailSolicitante(datosInstancia.getEmail());
        datosProceso.setTelefonoSolicitante((long) datosInstancia.getFono());
        datosProceso.setRenta(datosInstancia.getRentaLiquida());

        datosProceso.setGlosaCanal(datosInstancia.getGlosaCanalVenta());
        datosProceso.setCodigoCanal(datosInstancia.getCodCanalVenta());

        SimpleDateFormat formateador = new SimpleDateFormat(FORMATO_FECHA);
        datosProceso.setFechaSimulacion(formateador.format(datosInstancia.getFechaSimulacion()));
        datosProceso.setTotalSimulacionesPorRut(datosInstancia.getTotalSimulaciones());
        datosProceso.setResultadoFiltros(datosInstancia.getResultadoFiltros());
        datosProceso.setIndicadorContactar(datosInstancia.isContactar() ? COD_RESP_SI : COD_RESP_NO);
        datosProceso.setGlosaInmobiliaria(normalizarASCII(datosInstancia.getGlosaInmobiliaria()));
        datosProceso.setCodigoInmobiliaria(normalizarASCII(datosInstancia.getCodInmobiliaria()));
        datosProceso.setDfl2(datosInstancia.isDfl2() ? COD_RESP_SI : COD_RESP_NO);

        datosProceso.setGlosaTipoDeVivienda(normalizarASCII(datosInstancia.getGlosaTipoVivienda()));
        datosProceso.setCodigoTipoVivienda(datosInstancia.getCodTipoVivienda());
        datosProceso.setGlosaAntiguedad(normalizarASCII(datosInstancia.getGlosaAntiguedadVivienda()));
        datosProceso.setCodigoAntiguedad(datosInstancia.getCodAntiguedadVivienda());
        datosProceso.setGlosaSeguroIncendio(normalizarASCII(datosInstancia.getGlosaSeguroIncendioSismoOpcional()));
        datosProceso.setCodigoSeguroIncendio(datosInstancia.getCodSeguroIncendioSismoOpcional());
        datosProceso.setGlosaRegion(normalizarASCII(datosInstancia.getGlosaRegion()));
        datosProceso.setCodigoRegion("" + datosInstancia.getCodRegion());
        datosProceso.setGlosaCiudad(normalizarASCII(datosInstancia.getGlosaCiudad()));
        datosProceso.setCodigoCiudad("" + datosInstancia.getCodCiudad());

        datosProceso.setGlosaComuna(normalizarASCII(datosInstancia.getGlosaComuna()));
        datosProceso.setCodigoComuna("" + datosInstancia.getCodComuna());
        datosProceso.setPrecioViviendaUF(datosInstancia.getPrecioViviendaUF());
        datosProceso.setCuotaContadoUF(datosInstancia.getCuotaContadoUF());
        datosProceso.setCreditoUF(datosInstancia.getCreditoUF());
        datosProceso.setPorcentajeDefinanciamiento(datosInstancia.getPorcentajeFinanciamiento());
        datosProceso.setPlazo((long) datosInstancia.getPlazo());
        datosProceso.setGlosaProducto(normalizarASCII(datosInstancia.getGlosaProducto()));
        datosProceso.setCodigoProducto("" + datosInstancia.getCodProducto());
        datosProceso.setAniosTasaFija(datosInstancia.getAnnosTasaFija());

        datosProceso.setGlosaTipoFinanciamiento(normalizarASCII(datosInstancia.getGlosaTipoFinanciamiento()));
        datosProceso.setCodigoTipoFinanciamiento("" + datosInstancia.getCodTipoFinanciamiento());
        datosProceso.setPac(datosInstancia.isSuscribePAC() ? COD_RESP_SI : COD_RESP_NO);
        datosProceso.setMesDeGracia("" + datosInstancia.getMesesGracia());
        datosProceso.setDiaVencimiento(datosInstancia.getDiaVencimiento());
        datosProceso.setGlosaMesDeExclusion(normalizarASCII(datosInstancia.getGlosaMesExclusion()));
        datosProceso.setMesDeExclusion("" + datosInstancia.getCodMesExclusion());
        datosProceso.setTasaOtorgada(datosInstancia.getTasaCredito());
        datosProceso.setDividendoTotalUFColectivoTramo1(NumerosUtil.redondearADosDecimales(
            datosInstancia.getSimulacionPlazoColectivo().getDividendoTotal()));
        datosProceso.setDividendoTotalUFColectivoTramo2(NumerosUtil.redondearADosDecimales(
            datosInstancia.getSimulacionPlazoPagaLaMitadColectivo().getDividendoTotal()));

        datosProceso.setCae(datosInstancia.getTasaCAEColectivo());
        datosProceso.setCostoTotalCreditoUF(datosInstancia.getCostoTotalCreditoColectivo());
        datosProceso.setPrimaSeguroIncendioColectivoSeguroIncendioSismoColectivo(
            NumerosUtil.redondearADosDecimales(datosInstancia.getSimulacionPlazoColectivo().getMontoSegIncSis()));
        datosProceso.setPrimaSeguroDesgravamenColectivo(NumerosUtil.redondearADosDecimales(
            datosInstancia.getSimulacionPlazoColectivo().getMontoSegDesg()));
        datosProceso.setCostoDelFondo(datosInstancia.getTasaCostoFondo());
        datosProceso.setSpread(datosInstancia.getTasaSpread());
        datosProceso.setGlosaSeguroDesgravamenIndividual(normalizarASCII(
            datosInstancia.getGlosaSeguroDesgravamenIndividual()));
        datosProceso.setCodigoSeguroDesgravamenIndividual(datosInstancia.getCodSeguroDesgravamenIndividual());
        datosProceso.setGlosaSeguroAdicional(normalizarASCII(datosInstancia.getGlosaSeguroAdicional()));
        datosProceso.setCodigoSeguroAdicional(""+ datosInstancia.getCodSeguroAdicional());

        datosProceso.setGlosaSeguroIncendioSismoIndividual(normalizarASCII(
            datosInstancia.getGlosaSeguroIncendioSismoIndividual()));
        datosProceso.setCodigoSeguroIncendioSismoIndividual(datosInstancia.getCodSeguroIncendioSismoIndividual());
        datosProceso.setGastoTasacion(datosInstancia.getGastosOperacionalesPesos().getTasacion());
        datosProceso.setGastoEstudioTitulos(datosInstancia.getGastosOperacionalesPesos().getEstudioTitulos());
        datosProceso.setGastoBorradorEscritura(datosInstancia
            .getGastosOperacionalesPesos().getBorradorEscritura());
        datosProceso.setGastoNotaria(datosInstancia.getGastosOperacionalesPesos().getNotariales());
        datosProceso.setGastoImpuestoUF(NumerosUtil.redondearADosDecimales(
            datosInstancia.getGastosOperacionalesUF().getImptoAlMutuo()));
        datosProceso.setCodigoEjecutivo(datosInstancia.getCodEjecutivo());
        datosProceso.setGlosaCanalCredito(normalizarASCII(datosInstancia.getGlosaCanalCredito()));
        datosProceso.setCodigoCanalCredito(datosInstancia.getCodCanalCredito());

        datosProceso.setGlosaOficinaEjecutivo(normalizarASCII(datosInstancia.getGlosaOficinaEje()));
        datosProceso.setCodigoOficinaEjecutivo(datosInstancia.getCodOficinaEje());
        datosProceso.setOrigenSimulacion(datosInstancia.getOrigenSimulacion());
        datosProceso.setDividendoNeto(NumerosUtil.redondearADosDecimales(
            datosInstancia.getSimulacionPlazoColectivo().getDividendo()));
        datosProceso.setCodigoProyecto(datosInstancia.getCodigoProyecto());
        datosProceso.setGlosaProyecto(datosInstancia.getGlosaProyecto());
        datosProceso.setConvenio(datosInstancia.getCodigoConvenio());

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[convertirDatosInstanciaADatosWsCHIP] [BCI_FINOK]: [datosProceso]:" + datosProceso);
        }
        return datosProceso;
    }

    /**
     * Metodo que elimina acentos y caracteres especiales de una cadena de
     * texto.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * <p>
     * 
     * @param input
     *            Texto con tildes y caracteres especiales
     * @return cadena de texto en formato estandar.
     * @since 6.2
     */
    private String normalizarASCII(String input) {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[normalizarASCII] [BCI_INI]: BEAN.");
        }
        return TextosUtil.ASCII7(input);
    }

    /**
     * Metodo para determinar si existe un proceso CHIP para un cliente .
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * <p>
     * 
     * @param rut
     *            Rut del cliente.
     * @return true En caso que el cliente ya tenga generado un proceso CHIP.
     * @throws GeneralException
     *             Error General.
     * @since 6.2
     */
    public boolean poseeProcesoCHIPVigente(long rut) throws GeneralException {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                    "[poseeProcesoCHIPVigente] [rut: " + rut
                            + "] [BCI_INI]: BEAN.");
        }
        boolean existeProceso = false;
        existeProceso = procesoCHIPDAO.poseeProcesoCHIPVigente(rut);
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                    "[poseeProcesoCHIPVigente][existeProceso: " + existeProceso
                            + "]  [BCI_FINOK]: BEAN.");
        }
        return existeProceso;
    }

    /**
     * Metodo para registrar la fecha cuando se genero una instancia de proceso
     * CHIP para un cliente.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * <p>
     * 
     * @param rut
     *            Rut del cliente.
     * @throws GeneralException
     *             Error General.
     * @since 6.2
     */
    public void registrarInicioProcesoCHIP(long rut) throws GeneralException {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                    "[registrarInicioProcesoCHIP] [rut: " + rut
                            + "] [BCI_INI]: BEAN.");
        }
        procesoCHIPDAO.registrarInicioProcesoCHIP(rut);
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[registrarInicioProcesoCHIP] [BCI_FINOK]: BEAN.");
        }
    }

    /**
     * Metodo para indicar que el cliente desea ser contactado por un ejecutivo.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * <p>
     * 
     * @param rut
     *            Rut del cliente.
     * @throws GeneralException
     *             Error General.
     * @since 6.2
     */
    public void marcarSimulacionProcesoCHIP(long rut) throws GeneralException {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                    "[marcarSimulacionProcesoCHIP] [rut: " + rut
                            + "][BCI_INI]: BEAN.");
        }
        procesoCHIPDAO.marcarSimulacionProcesoCHIP(rut);
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger()
                    .info("[marcarSimulacionProcesoCHIP] [BCI_FINOK]: BEAN.");
        }
    }
}
