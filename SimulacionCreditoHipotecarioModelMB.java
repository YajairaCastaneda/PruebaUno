package cl.bci.aplicaciones.productos.colocaciones.simulacion.mb;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.ejb.CreateException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to.DatosInstanciaProcesoCHIPTO;
import wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to.GastosOperacionalesCHIPTO;
import wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to.SimulacionPlazoCHIPTO;
import wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to.SimulacionProcesoCHIPTO;
import wcorp.model.seguridad.SessionBCI;
import wcorp.serv.hipotecario.ServiciosCreditoHipotecario;
import wcorp.serv.hipotecario.ServiciosCreditoHipotecarioHome;
import wcorp.util.EnhancedServiceLocator;
import wcorp.util.EnhancedServiceLocatorException;
import wcorp.util.ErroresUtil;
import wcorp.util.FechasUtil;
import wcorp.util.GeneralException;
import wcorp.util.TablaValores;
import wcorp.util.xml.ConvierteXml;

import cl.bci.aplicaciones.colaborador.mb.ColaboradorModelMB;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.hipotecario.impl.SimuladorHipotecario;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.hipotecario.impl.SimuladorHipotecarioFactory;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.CalculoCAETO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.CoberturaSeguroTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.ConfiguracionSimuladorTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.GastosOperacionalesTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.InmobiliariaTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.ParametroSimulacionTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.ProductoHipotecarioTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.ResultSimulaProcesoCHIPTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.ResultadoSimulacionTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.SeguroAdicionalTO;
import cl.bci.aplicaciones.productos.colocaciones.simulacion.to.SimulacionPlazoTO;
import cl.bci.infraestructura.utilitarios.exportacion.GeneradorDocumentosUtilityMB;

/**
 * Support ManagedBean <br>
 * <b>SimulacionCreditoHipotecarioModelMB</b> <br>
 * Managed bean vista encargado de desplegar y manejar la vista de Simulacion
 * Credito Hipotecario. <br>
 * Registro de versiones:
 * <ul>
 * <li>1.0 (21/11/2013 Nicole Sanhueza(Sermaluc)): version inicial.</li>
 * <li>1.1 05/01/2015 Nicole Sanhueza (Sermaluc): se modifico el metodo:
 * {@link #generarDocumentoPDF(String, CoberturaSeguroTO[], String, String, SessionBCI)}</li>
 * <li>1.2 21/04/2015 Nicole Sanhueza (Sermaluc) - Harold Mora (Ing. BCI): se modifica el metodo:
 * {@link #generarDocumentoPDF(String, CoberturaSeguroTO[], String, String, SessionBCI)}</li>
 * <li>1.3 07/04/2015 Jose Palma (TINet), Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): 
 * Para la integración del simulador Hipotecario con el proceso CHIP BPMS se agregan los métodos:
 * <ul>
 * <li>{@link #convertirGastosOperacionales(GastosOperacionalesTO)}</li>
 * <li>
 * {@link #convertirResultSimulacionADatosInstanciaCHIP(ResultSimulaProcesoCHIPTO)}
 * </li>
 * <li>{@link #convertirSimulacionCHIP(ResultSimulaProcesoCHIPTO)}</li>
 * <li>{@link #convertirSimulacionPlazo(SimulacionPlazoTO)}
 * <li>{@link #generarInstanciaProcesoCHIP(ResultSimulaProcesoCHIPTO)}</li>
 * <li>{@link #guardarSimulacionProcesoCHIP(ResultSimulaProcesoCHIPTO)}</li>
 * <li>{@link #marcarSimulacionProcesoCHIP(long)}</li>
 * <li>{@link #poseeProcesoCHIPVigente(long)}</li>
 * <li>{@link #registrarInicioProcesoCHIP(long)}</li>
 * <li>{@link #crearEjbServiciosCreditoHipotecarios()}</li> 
 * </ul>
 * y los atributos:
 * {@link #JNDI_SERV_HIPOTECARIO} y
 * {@link #serviciosCreditoHip}.
 * </ul>
  * <p>
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 * </p>
 */

@ManagedBean
@SessionScoped
public class SimulacionCreditoHipotecarioModelMB implements Serializable {

    /**
     * Se usa para obtener el codigo de seguros desde tabla de parametros.
     */
    public static final String LLAVE_SEGUROS = "SEGUROS";

    /**
     * Se usa para obtener el codigo de seguro cesantia serviu desde tabla de
     * parametros.
     */
    public static final String LLAVE_SEGURO_CESANTIA_DS40 = "CESANTIA_SERVIU_DS40";

    /**
     * Se usa para obtener el codigo de seguro cesantia serviu desde tabla de
     * parametros.
     */
    public static final String LLAVE_SEGURO_CESANTIA_DS01 = "CESANTIA_SERVIU_DS01";
    
    /**
     * Serial de la clase.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Tabla parametro de simulador chip.
     */
    private static final String SIM_CHIP = "SimCHIP.parametros";

    /**
     * Tabla parametro de simulador .
     */
    private static final String TABLA_SIMULADOR = "simulador.parametros";

    /**
     * Formato Fecha.
     */
    private static final String FORMATO_FECHA = "dd/MM/yyyy";

    /**
     * Atributo que setea los parametros de una simulacion conveniencia.
     */
    private static final int SIMULACION_CONVENIENCIA = 2;

    /**
     * Atributo tamaño de grupo.
     */
    private static final int TAMANO_GRUPO = 3;

    /**
     * Atributo codigo producto Bci Paga la Mitad.
     */
    private static final int PAGA_LA_MITAD = 20;

    /**
     * Atributo codigo producto Bci Paga la Mitad Pesos.
     */
    private static final int PAGA_LA_MITAD_PESOS = 22;
    
    /**
     * Nombre del pdf de la simulacion.
     */
    private static final String NOMBRE_ARCHIVO = "Simulación Crédito Hipotecario";

    /**
     * JNDI de ServiciosCreditoHipotecario.
     */    
    private static final String JNDI_SERV_HIPOTECARIO = "wcorp.serv.hipotecario.ServiciosCreditoHipotecario";    

    /**
     * Log de la clase.
     */
    private transient Logger logger = (Logger) Logger
        .getLogger(SimulacionCreditoHipotecarioModelMB.class);

    /**
     * Atributo ManagedBean de ColaboradorModelMB.
     */
    @ManagedProperty(value = "#{colaboradorModelMB}")
    private ColaboradorModelMB colaboradorModelMB;

    /**
     * Atributo configuracion simulador TO.
     */
    private ConfiguracionSimuladorTO configuracionSimuladorTO;

    /**
     * Atributo instancia simulador hipotecario.
     */
    private SimuladorHipotecario instanciaSimuladorHipotecario;

    /**
     * Parametros de la simulacion.
     */
    private ParametroSimulacionTO parametrosSimulacion;

    /**
     * Resultado de la simulacion.
     */
    private ResultadoSimulacionTO resultadoSimulacionTO;

    /**
     * Referencia al Ejb de ServiciosCreditoHipotecario.
     */
    private ServiciosCreditoHipotecario srvCreditoHipotecario;	
	
    /**
     * Constructor de la clase.
     */
    public SimulacionCreditoHipotecarioModelMB() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[" + this.getClass().getSimpleName() + "]: Inicia Constructor");
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

    public ConfiguracionSimuladorTO getConfiguracionSimuladorTO() {
        return configuracionSimuladorTO;
    }

    public void setConfiguracionSimuladorTO(ConfiguracionSimuladorTO configuracionSimuladorTO) {
        this.configuracionSimuladorTO = configuracionSimuladorTO;
    }

    public SimuladorHipotecario getInstanciaSimuladorHipotecario() {
        return instanciaSimuladorHipotecario;
    }

    public void setInstanciaSimuladorHipotecario(SimuladorHipotecario instanciaSimuladorHipotecario) {
        this.instanciaSimuladorHipotecario = instanciaSimuladorHipotecario;
    }

    public ParametroSimulacionTO getParametrosSimulacion() {
        return parametrosSimulacion;
    }

    public void setParametrosSimulacion(ParametroSimulacionTO parametrosSimulacion) {
        this.parametrosSimulacion = parametrosSimulacion;
    }

    public ResultadoSimulacionTO getResultadoSimulacionTO() {
        return resultadoSimulacionTO;
    }

    public void setResultadoSimulacionTO(ResultadoSimulacionTO resultadoSimulacionTO) {
        this.resultadoSimulacionTO = resultadoSimulacionTO;
    }

    public ColaboradorModelMB getColaboradorModelMB() {
        return colaboradorModelMB;
    }

    public void setColaboradorModelMB(ColaboradorModelMB colaboradorModelMB) {
        this.colaboradorModelMB = colaboradorModelMB;
    }

    /**
     * Metodo para obtener la instancia del simulador hipotecario.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 16/09/2014 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * </ul>
     * 
     * @param canal canal de simulacion.
     * @return SimuladorHipotecario instancia de simulador hipotecario.
     * @since 1.0
     */
    private SimuladorHipotecario obtieneInstanciaSimuladorHipotecario(String canal) {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtieneInstanciaSimuladorHipotecario]: inicio metodo.");
        }
        if (this.instanciaSimuladorHipotecario == null) {
            SimuladorHipotecarioFactory factory = new SimuladorHipotecarioFactory();
            this.instanciaSimuladorHipotecario = factory
                .instanciaSimuladorHipotecario(factory.SIMULADOR_INTRANET);
            if (getLogger().isEnabledFor(Level.INFO)) {
                getLogger().info("[obtieneInstanciaSimuladorHipotecario]: fin del metodo.");
            }
        }
        return this.instanciaSimuladorHipotecario;

    }

    /**
     * Metodo para obtener la configuracion de canal del simulador de creditos
     * hipotecarios.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 13/12/2013 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * </ul>
     * 
     * @param canalId codigo del canal.
     * @return configuracionSimuladorTO configuracion del simulador de creditos
     *         hipotecarios BCI.
     * @throws Exception Excepcion en caso de error al consultar.
     * @since 1.0
     */
    public ConfiguracionSimuladorTO obtieneConfiguracion(String canalId) throws Exception {
        try {
            if (getLogger().isEnabledFor(Level.INFO)) {
                getLogger().info(
                    "[obtieneConfiguracion] inicio obtieneConfiguracion canal: " + canalId);
            }
            if (configuracionSimuladorTO == null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[obtieneConfiguracion]: obteniendo instancia simulador.");
                }
                SimuladorHipotecario simuladorHipotecario = obtieneInstanciaSimuladorHipotecario(canalId);
                configuracionSimuladorTO = simuladorHipotecario.obtieneConfiguracion(canalId);
            }
        }
        catch (Exception e) {
            getLogger()
                .error("[obtieneConfiguracion] excepcion:" + ErroresUtil.extraeStackTrace(e));
            throw new Exception("Error al obtener la configuracion del canal. " + canalId);
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtieneConfiguracion] fin del metodo: " + canalId);
        }
        return configuracionSimuladorTO;
    }

    /**
     * Metodo para obtener la lista de seguros adicionales.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 21/11/2013 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * </ul>
     * 
     * @param canalId canal de simulacion.
     * @return SeguroAdicionalTO[] lista de seguros adicionales.
     * @since 1.0
     */
    public SeguroAdicionalTO[] obtieneSegurosAdicionales(String canalId) {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtieneSegurosAdicionales] : inicio metodo.");
        }
        SeguroAdicionalTO[] listaSegurosAdicionales = null;
        try {
            SimuladorHipotecario simuladorHipotecario = obtieneInstanciaSimuladorHipotecario(canalId);
            return simuladorHipotecario.obtieneSegurosAdicionales();
        }
        catch (Exception e) {
            getLogger().error("[obtieneSegurosAdicionales]: error al obtener la lista de seguros.");
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtieneSegurosAdicionales]: fin del metodo.");
        }
        return listaSegurosAdicionales;
    }

    /**
     * Metodo para obtener la lista de inmobiliarias.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 22/09/2014 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * </ul>
     * 
     * @param canalId canal de simulacion.
     * @return InmobiliariaTO[] lista de inmobiliarias.
     * @since 1.0
     */
    public InmobiliariaTO[] obtieneInmobiliarias(String canalId) {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtieneInmobiliarias] : inicio metodo.");
        }
        InmobiliariaTO[] listaInmobiliarias = null;
        try {
            SimuladorHipotecario simuladorHipotecario = obtieneInstanciaSimuladorHipotecario(canalId);
            return simuladorHipotecario.obtieneInmobiliarias();
        }
        catch (Exception e) {
            getLogger()
                .error("[obtieneInmobiliarias]: error al obtener la lista de inmobiliarias.");
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtieneInmobiliarias]: fin del metodo.");
        }
        return listaInmobiliarias;
    }

    /**
     * Metodo para cargar los productos del simulador de creditos hipotecarios.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 26/11/2013 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * </ul>
     * 
     * @param codigoProducto codigo producto.
     * @param codigoAntiguedad codigo antiguedad.
     * @param codigoDestino codigo destino.
     * @param codigoBienRaiz codigo bien raiz.
     * @param canalId canal de simulacion.
     * @return ProductoHipotecarioTO seleccionado.
     * @since 1.0
     */
    public ProductoHipotecarioTO cargaCaracteristicasProducto(int codigoProducto,
        String codigoAntiguedad, int codigoDestino, String codigoBienRaiz, String canalId) {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[cargaCaracteristicasProducto] : inicio metodo: " + codigoProducto);
        }
        try {
            SimuladorHipotecario simuladorHipotecario = obtieneInstanciaSimuladorHipotecario(canalId);
            return simuladorHipotecario.cargaCaracteristicasProducto(codigoProducto,
                codigoAntiguedad, codigoDestino, codigoBienRaiz);
        }
        catch (Exception e) {
            getLogger().error(
                "[cargaCaracteristicasProducto] : error al cargar las caracteristicas "
                    + " del producto seleccionado.");
            if (getLogger().isEnabledFor(Level.INFO)) {
                getLogger().info(
                    "[cargaCaracteristicasProducto] : fin del metodo: " + codigoProducto);
            }
            return null;
        }
    }

    /**
     * Metodo para obtener los productos de comparación del simulador de
     * creditos hipotecarios.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 16/12/2013 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * </ul>
     * 
     * @param listaProductos lista productos.
     * @param canalId canal de simulacion.
     * @return ProductoHipotecarioTO de comparacion.
     * @since 1.0
     */
    public ArrayList<ProductoHipotecarioTO> obtieneProductosComparacion(
        ArrayList<ProductoHipotecarioTO> listaProductos, String canalId) {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtieneProductosComparacion] : inicio metodo.");
        }
        ArrayList<ProductoHipotecarioTO> productosComparacion = null;
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger()
                    .debug(
                        "[obtieneProductosComparacion] : obteniendo codigos de productos de comparacion.");
            }
            SimuladorHipotecario simuladorHipotecario = obtieneInstanciaSimuladorHipotecario(canalId);
            return simuladorHipotecario.obtieneProductosComparacion(listaProductos);
        }
        catch (Exception e) {
            getLogger().error(
                "[obtieneProductosComparacion]: error al obtener el producto de comparación.");
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtieneProductosComparacion] : fin del metodo.");
        }
        return productosComparacion;
    }

    /**
     * Metodo para calcular una simulacion.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 02/12/2013 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * </ul>
     * 
     * @param canalId canal de simulacion.
     * @param parametrosSimulacionTO parametros de la simulacion.
     * @throws Exception Excepcion en caso de error.
     * 
     * @since 1.0
     */
    public void calculaSimulacion(ParametroSimulacionTO parametrosSimulacionTO, String canalId)
        throws Exception {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[calculaSimulacion]: inicio metodo.");
        }
        try {
            SimuladorHipotecario simuladorHipotecario = obtieneInstanciaSimuladorHipotecario(canalId);
            this.parametrosSimulacion = parametrosSimulacionTO;
            resultadoSimulacionTO = simuladorHipotecario.calculaSimulacion(
                this.parametrosSimulacion, configuracionSimuladorTO);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[calculaSimulacion] : guardando ficha de simulacion.");
            }
            simuladorHipotecario
            .guardarFichaSimulacion(resultadoSimulacionTO, parametrosSimulacion);
        }
        catch (Exception e) {
            getLogger().error("[calculaSimulacion] : error al obtener o guardar la simulación.");
            throw e;
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[calculaSimulacion]: fin del metodo.");
        }
    }

    /**
     * Metodo para recalcular CAE con individuales.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 04/04/2014 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * </ul>
     * 
     * @param seguroIncendio codigo del seguro de incendio.
     * @param seguroDesgravamen codigo del seguro de desgravamen.
     * @param canalId canal de simulacion.
     * 
     * @since 1.0
     */
    public void recalculaCAEIndividuales(int seguroIncendio, int seguroDesgravamen, String canalId) {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[recalculaCAEIndividuales] : inicio metodo.");
        }
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(
                    "[recalculaCAEIndividuales]: seteando tasas de seguros individuales.");
            }
            SimuladorHipotecario simuladorHipotecario = obtieneInstanciaSimuladorHipotecario(canalId);
            parametrosSimulacion.setSeguroIndividualDesgravamen(seguroDesgravamen);
            parametrosSimulacion.setSeguroIndividualIncendio(seguroIncendio);
            resultadoSimulacionTO = simuladorHipotecario.recalculaCAEIndividuales(seguroIncendio,
                seguroDesgravamen, resultadoSimulacionTO);
            if (getLogger().isEnabledFor(Level.INFO)) {
                getLogger().info("[recalculaCAEIndividuales] : fin metodo.");
            }
        }
        catch (Exception e) {
            getLogger().error(
                "[recalculaCAEIndividuales] : error al calcular el CAE con individuales.");
        }
    }

    /**
     * Metodo para crear PDF con resultado de simulacion.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 20/01/2014 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * </ul>
     * 
     * @param coberturasSeguros coberturas de seguros.
     * @param canalId canal de simulacion.
     * @param sesion sesion.
     * @throws Exception excepcion en caso de error.
     * 
     * @since 1.0
     */
    public void creaPDF(String canalId, CoberturaSeguroTO[] coberturasSeguros, SessionBCI sesion)
        throws Exception {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[creaPDF] : inicio metodo.");
        }  
        String textosLegales = obtieneTextosLegales(canalId);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[creaPDF] : obteniendo ruta de contexto.");
        }
        String rutaContexto = FacesContext.getCurrentInstance().getExternalContext()
            .getRealPath("/");
        String rutaXsl = rutaContexto
            + TablaValores.getValor(TABLA_SIMULADOR, "simulacionbcihome", "ruta");
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[creaPDF] : generando documento PDF.");
        }
        Document documento = generarDocumentoPDF(textosLegales, coberturasSeguros, rutaXsl,
            rutaContexto, sesion);
        GeneradorDocumentosUtilityMB generadorDocumentos = new GeneradorDocumentosUtilityMB();
        generadorDocumentos.generarPDF(documento, NOMBRE_ARCHIVO, rutaXsl);
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[creaPDF]: fin del metodo.");
        }
    }

    /**
     * Metodo para obtener los textos legales a desplegar.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 17/01/2014 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * </ul>
     * 
     * @param canal canal de simulacion.
     * @return String con los textos legales.
     * @since 1.0
     */
    private String obtieneTextosLegales(String canal) {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtieneTextosLegales] : inicio metodo");
        }
        CalculoCAETO calculoCAETO = resultadoSimulacionTO.getCalculoCAETO();
        char tipoTasa = calculoCAETO.getTipoTasa() != ' ' ? calculoCAETO.getTipoTasa() : 'F';
        String textoCAE = TablaValores.getValor("textoLegalHipotecario.parametros", "textoCAE_"
            + canal + "_" + tipoTasa, "texto");
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtieneTextosLegales]: fin metodo");
        }
        return textoCAE;
    }

    /**
     * Metodo para generar los bytes del PDF con resultado de simulacion.
     * <p>
     * <b>Registro de versiones:</b>
     * <ul>
     * <li>1.0 23/01/2014 Nicole Sanhueza. (Sermaluc): version inicial</li>
     * <li>1.1 05/01/2015 Nicole Sanhueza (Sermaluc): se agregaron parametros
     * para generacion de PDF.</li>
     *  <li>1.2 21/04/2015 Nicole Sanhueza (Sermaluc) - Harold Mora (Ing. BCI): Se agrega texto para el Tipo
     *  de Vivienda Usada y se agrega el texto para Departamento Usado.</li>
     * </ul>
     * 
     * @param textosLegales textos legales.
     * @param coberturasSeguros coberturas de seguros.
     * @param rutaXsl ruta de xsl.
     * @param rutaContexto ruta de contexto.
     * @param sesion sesion.
     * @return Document documento PDF.
     * @since 1.0
     */
    private Document generarDocumentoPDF(String textosLegales,
        CoberturaSeguroTO[] coberturasSeguros, String rutaXsl, String rutaContexto,
        SessionBCI sesion) {
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[generarDocumentoPDF] : inicio metodo.");
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = (Element) document.createElement("simulacionHipotecarioXML");            
            document.appendChild(root);
            root = (Element) document.getFirstChild();
            Source xsltSrc = new StreamSource(rutaXsl);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[generarDocumentoPDF]  Source : " + xsltSrc.toString());
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer(xsltSrc);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[generarDocumentoPDF]  transformer : " + transformer.toString());
            }
            root.appendChild(ConvierteXml.generaNodo(
                document,
                "imglogo",
                String.valueOf(rutaContexto
                    + TablaValores.getValor(TABLA_SIMULADOR, "simulacionbcihome", "imgLogo"))));
            root.appendChild(ConvierteXml.generaNodo(
                document,
                "img1",
                String.valueOf(rutaContexto
                    + TablaValores.getValor(TABLA_SIMULADOR, "simulacionbcihome", "img1"))));
            if (getLogger().isDebugEnabled()) {
                getLogger().info("[generarDocumentoPDF] creando nodos del archivo xml");
            }
            colaboradorModelMB.setDatosColaborador(sesion.getColaborador().getUsuario());
            String usuarioEjecutivo = colaboradorModelMB.getUsuario();
            root.appendChild(ConvierteXml.generaNodo(document, "usuarioEjecutivo",
                String.valueOf(usuarioEjecutivo)));
            String nombreEjecutivo = colaboradorModelMB.getNombreCompleto();
            root.appendChild(ConvierteXml.generaNodo(document, "nombreEjecutivo",
                String.valueOf(nombreEjecutivo)));
            root.appendChild(ConvierteXml.generaNodo(document, "nombreCliente",
                String.valueOf(parametrosSimulacion.getNombreCliente())));
            if (parametrosSimulacion.getInmobiliaria().equals("0")) {
                if(parametrosSimulacion.getTipoVivienda().equals("0001")){
                    parametrosSimulacion.setDescripcionVivienda("Casa Usada");
                    root.appendChild(ConvierteXml.generaNodo(document, "descripcionVivienda",
                        String.valueOf(parametrosSimulacion.getDescripcionVivienda())));
                }
                else if(parametrosSimulacion.getTipoVivienda().equals("0008")){
                    parametrosSimulacion.setDescripcionVivienda("Departamento Usado");
                    root.appendChild(ConvierteXml.generaNodo(document, "descripcionVivienda",
                        String.valueOf(parametrosSimulacion.getDescripcionVivienda())));
                }
            }
            else {
                root.appendChild(ConvierteXml.generaNodo(document, "inmobiliaria",
                    String.valueOf(parametrosSimulacion.getInmobiliaria())));
            }
            root.appendChild(ConvierteXml.generaNodo(document, "proyecto",
                String.valueOf(parametrosSimulacion.getProyecto())));           
            root.appendChild(ConvierteXml.generaNodo(document, "antiguedadVivienda",
                String.valueOf(parametrosSimulacion.getAntiguedadVivienda())));
            String codeudor = "NO";
            if (parametrosSimulacion.isTipoCodeudor()) {
                codeudor = "SI";
            }
            root.appendChild(ConvierteXml.generaNodo(document, "tieneCodeudor",
                String.valueOf(codeudor)));
            String fecha = FechasUtil.convierteDateAString(resultadoSimulacionTO.getSimulacionTO()
                .getDatosOperacion().getFecha(), FORMATO_FECHA);
            root.appendChild(ConvierteXml.generaNodo(document, "fecha", String.valueOf(fecha)));
            root.appendChild(ConvierteXml.generaNodo(document, "uf",
                String.valueOf(configuracionSimuladorTO.getValorUF())));
            root.appendChild(ConvierteXml.generaNodo(document, "precioViviendaUF",
                String.valueOf(parametrosSimulacion.getPrecioViviendaUF())));
            root.appendChild(ConvierteXml.generaNodo(document, "subsidioUF",
                String.valueOf(parametrosSimulacion.getSubsidioUF())));
            root.appendChild(ConvierteXml.generaNodo(document, "cuotaContadoUF",
                String.valueOf(parametrosSimulacion.getCuotaContadoUF())));
            root.appendChild(ConvierteXml.generaNodo(document, "creditoUF",
                String.valueOf(parametrosSimulacion.getCreditoUF())));
            root.appendChild(ConvierteXml.generaNodo(document, "porcentajeFinanciamiento",
                String.valueOf(parametrosSimulacion.getPorcentajeFinanciamiento())));
            root.appendChild(ConvierteXml.generaNodo(document, "plazo",
                String.valueOf(parametrosSimulacion.getPlazo())));
            root.appendChild(ConvierteXml.generaNodo(document, "tasa",
                String.valueOf(resultadoSimulacionTO.getSimulacionPlazosTO().getTasa())));
            root.appendChild(ConvierteXml.generaNodo(document, "mesGracia",
                String.valueOf(parametrosSimulacion.getMesesGracia())));
            root.appendChild(ConvierteXml.generaNodo(document, "mesExclusion",
                String.valueOf(resultadoSimulacionTO.getNombreMesExclusion())));
            root.appendChild(ConvierteXml.generaNodo(document, "diaVencimiento",
                String.valueOf(parametrosSimulacion.getDiaVencimiento())));
            String tienePac = "NO";
            if (parametrosSimulacion.getSuscribePAC() == 1) {
                tienePac = "SI";
            }
            root.appendChild(ConvierteXml.generaNodo(document, "tienePac", String.valueOf(tienePac)));
            root.appendChild(ConvierteXml.generaNodo(
                document,
                "nombreProducto",
                String.valueOf(configuracionSimuladorTO.getProducto(
                    parametrosSimulacion.getProducto()).getDescripcion())));
            String tieneIndividual = "NO";
            if (parametrosSimulacion.isSimulaIndividuales()) {
                tieneIndividual = "SI";
            }
            root.appendChild(ConvierteXml.generaNodo(document, "tieneIndividual",
                String.valueOf(tieneIndividual)));
            String tieneComparacion = "NO";
            if (parametrosSimulacion.isDeseaComparar()) {
                tieneComparacion = "SI";
            }
            root.appendChild(ConvierteXml.generaNodo(document, "tieneComparacion",
                String.valueOf(tieneComparacion)));
            root.appendChild(ConvierteXml.generaNodo(document, "nombreProductoComparacion",
                String.valueOf(resultadoSimulacionTO.getNombreProductoComparacion())));
            root.appendChild(ConvierteXml.generaNodo(document, "codigoProducto",
                String.valueOf(parametrosSimulacion.getProducto())));
            double tasaConveniencia = 0;
            root.appendChild(ConvierteXml.generaNodo(document, "tasaConveniencia",
                String.valueOf(tasaConveniencia)));
            root.appendChild(ConvierteXml.generaNodo(document, "seguro",
                String.valueOf(parametrosSimulacion.getSeguro())));
            root.appendChild(ConvierteXml.generaNodo(document, "porcentajeSegDesgravamenCliente",
                String.valueOf(parametrosSimulacion.getPorcentajeSegDesgravamenCliente())));
            root.appendChild(ConvierteXml.generaNodo(document, "porcentajeSegDesgravamenCodeudor",
                String.valueOf(parametrosSimulacion.getPorcentajeSegDesgravamenCodeudor())));
            for (int i = 0; i < resultadoSimulacionTO.getSimulacionTO().getSimulacionPlazos()
                .size(); i++) {
                SimulacionPlazoTO simulacionPlazoTO = resultadoSimulacionTO.getSimulacionTO()
                    .getSimulacionPlazos().get(i);
                if (simulacionPlazoTO.getPlazo() == parametrosSimulacion.getPlazo()
                    && !simulacionPlazoTO.isComparacionConveniencia()) {
                    root.appendChild(ConvierteXml.generaNodo(document, "dividendoNeto", String
                        .valueOf(resultadoSimulacionTO.getSimulacionPlazosTO().getDividendo())));
                    root.appendChild(ConvierteXml.generaNodo(document, "seguroIncendioColectivo",
                        String.valueOf(resultadoSimulacionTO.getSimulacionPlazosTO()
                            .getMontoSegIncSis())));
                    root.appendChild(ConvierteXml.generaNodo(document,
                        "seguroDesgravamenColectivo", String.valueOf(resultadoSimulacionTO
                            .getSimulacionPlazosTO().getMontoSegDesg())));
                    double dividendoTotal = resultadoSimulacionTO.getSimulacionPlazosTO()
                        .getDividendoTotal();
                    root.appendChild(ConvierteXml.generaNodo(document, "dividendoTotal",
                        String.valueOf(dividendoTotal)));
                    double dividendoTotalPesos = resultadoSimulacionTO.getSimulacionPlazosTO()
                        .getDividendoTotalPesos();
                    root.appendChild(ConvierteXml.generaNodo(document, "dividendoTotalPesos",
                        String.valueOf(dividendoTotalPesos)));
                    root.appendChild(ConvierteXml.generaNodo(document, "cae",
                        String.valueOf(resultadoSimulacionTO.getCalculoCAETO().getTasaCAE())));
                    root.appendChild(ConvierteXml.generaNodo(document, "costoTotalCredito",
                        String.valueOf(resultadoSimulacionTO.getCalculoCAETO().getCostoTotal())));
                }
            }
            if (parametrosSimulacion.getProducto() == PAGA_LA_MITAD
                || parametrosSimulacion.getProducto() == PAGA_LA_MITAD_PESOS) {
                root.appendChild(ConvierteXml.generaNodo(document, "dividendoNetoPagaMitad", String
                    .valueOf(resultadoSimulacionTO.getSimulacionPlazosPagaLaMitadTO()
                        .getDividendo())));
                root.appendChild(ConvierteXml.generaNodo(document,
                    "seguroIncendioColectivoPagaMitad", String.valueOf(resultadoSimulacionTO
                        .getSimulacionPlazosPagaLaMitadTO().getMontoSegIncSis())));
                root.appendChild(ConvierteXml.generaNodo(document,
                    "seguroDesgravamenColectivoPagaMitad", String.valueOf(resultadoSimulacionTO
                        .getSimulacionPlazosPagaLaMitadTO().getMontoSegDesg())));
                root.appendChild(ConvierteXml.generaNodo(document, "dividendoTotalPagaMitad",
                    String.valueOf(resultadoSimulacionTO.getSimulacionPlazosPagaLaMitadTO()
                        .getDividendoTotal())));
                root.appendChild(ConvierteXml.generaNodo(document, "dividendoTotalPesosPagaMitad",
                    String.valueOf(resultadoSimulacionTO.getSimulacionPlazosPagaLaMitadTO()
                        .getDividendoTotalPesos())));
            }
            for (int i = 0; i < resultadoSimulacionTO.getSimulacionTO().getSimulacionPlazos()
                .size(); i++) {
                SimulacionPlazoTO simulacionPlazoTO = resultadoSimulacionTO.getSimulacionTO()
                    .getSimulacionPlazos().get(i);
                if (simulacionPlazoTO.getPlazo() == parametrosSimulacion.getPlazo()
                    && simulacionPlazoTO.isComparacionConveniencia()) {
                    root.appendChild(ConvierteXml.generaNodo(document, "dividendoNetoComparacion",
                        String.valueOf(simulacionPlazoTO.getDividendo())));
                    root.appendChild(ConvierteXml.generaNodo(document,
                        "seguroIncendioColectivoComparacion",
                        String.valueOf(simulacionPlazoTO.getMontoSegIncSis())));
                    root.appendChild(ConvierteXml.generaNodo(document,
                        "seguroDesgravamenColectivoComparacion",
                        String.valueOf(simulacionPlazoTO.getMontoSegDesg())));
                    double dividendoTotalComparacion = simulacionPlazoTO.getDividendoTotal();
                    root.appendChild(ConvierteXml.generaNodo(document, "dividendoTotalComparacion",
                        String.valueOf(dividendoTotalComparacion)));
                    double dividendoTotalPesosComparacion = simulacionPlazoTO
                        .getDividendoTotalPesos();
                    root.appendChild(ConvierteXml.generaNodo(document,
                        "dividendoTotalPesosComparacion",
                        String.valueOf(dividendoTotalPesosComparacion)));
                    root.appendChild(ConvierteXml.generaNodo(document, "caeComparacion", String
                        .valueOf(resultadoSimulacionTO.getCalculoCAEComparacionTO().getTasaCAE())));
                    root.appendChild(ConvierteXml.generaNodo(document,
                        "costoTotalCreditoComparacion", String.valueOf(resultadoSimulacionTO
                            .getCalculoCAEComparacionTO().getCostoTotal())));
                }
            }
            String opcionIncendioIndividualDef = "";
            if (parametrosSimulacion.getSeguroIndividualIncendio() == 1) {
                opcionIncendioIndividualDef = "Seguro Incendio y Sismo Individual Básico";
            }
            if (parametrosSimulacion.getSeguroIndividualIncendio() == SIMULACION_CONVENIENCIA) {
                opcionIncendioIndividualDef = "Seguro Incendio y Sismo Individual Full";
            }
            root.appendChild(ConvierteXml.generaNodo(document, "opcionIncendioIndividual",
                opcionIncendioIndividualDef));
            String opcionDesgravamenIndividualDef = "";
            if (parametrosSimulacion.getSeguroIndividualDesgravamen() == 1) {
                opcionDesgravamenIndividualDef = "Seguro Desgravamen Individual Básico";
            }
            if (parametrosSimulacion.getSeguroIndividualDesgravamen() == SIMULACION_CONVENIENCIA) {
                opcionDesgravamenIndividualDef = "Seguro Desgravamen Individual Plus";
            }
            if (parametrosSimulacion.getSeguroIndividualDesgravamen() == TAMANO_GRUPO) {
                opcionDesgravamenIndividualDef = "Seguro Desgravamen Individual Full";
            }
            root.appendChild(ConvierteXml.generaNodo(document, "opcionDesgravamenIndividual",
                opcionDesgravamenIndividualDef));
            if (parametrosSimulacion.isSimulaIndividuales()) {
                for (int i = 0; i < resultadoSimulacionTO.getSimulacionTO().getSimulacionPlazos()
                    .size(); i++) {
                    SimulacionPlazoTO simulacionPlazoTO = resultadoSimulacionTO.getSimulacionTO()
                        .getSimulacionPlazos().get(i);
                    if (simulacionPlazoTO.getPlazo() == parametrosSimulacion.getPlazo()
                        && !simulacionPlazoTO.isComparacionConveniencia()) {
                        root.appendChild(ConvierteXml.generaNodo(document,
                            "dividendoNetoIndividual", String.valueOf(resultadoSimulacionTO
                                .getSimulacionPlazosIndividualesTO().getDividendo())));
                        if (parametrosSimulacion.getSeguroIndividualIncendio() == 1) {
                            root.appendChild(ConvierteXml.generaNodo(document,
                                "seguroIncendioIndividual",
                                String.valueOf(simulacionPlazoTO.getPrimaOfertaMinima())));
                            if (parametrosSimulacion.getProducto() == PAGA_LA_MITAD
                                || parametrosSimulacion.getProducto() == PAGA_LA_MITAD_PESOS) {
                                root.appendChild(ConvierteXml.generaNodo(document,
                                    "seguroIncendioPagaMitadIndividual", String
                                        .valueOf(resultadoSimulacionTO
                                            .getSimulacionPlazosPagaLaMitadTO()
                                            .getPrimaOfertaMinima())));
                            }
                        }
                        if (parametrosSimulacion.getSeguroIndividualIncendio() == SIMULACION_CONVENIENCIA) {
                            root.appendChild(ConvierteXml.generaNodo(document,
                                "seguroIncendioIndividual",
                                String.valueOf(simulacionPlazoTO.getPrimaOfertaMaxima())));
                            if (parametrosSimulacion.getProducto() == PAGA_LA_MITAD
                                || parametrosSimulacion.getProducto() == PAGA_LA_MITAD_PESOS) {
                                root.appendChild(ConvierteXml.generaNodo(document,
                                    "seguroIncendioPagaMitadIndividual", String
                                        .valueOf(resultadoSimulacionTO
                                            .getSimulacionPlazosPagaLaMitadTO()
                                            .getPrimaOfertaMaxima())));
                            }
                        }
                        if (parametrosSimulacion.getSeguroIndividualDesgravamen() == 1) {
                            root.appendChild(ConvierteXml.generaNodo(document,
                                "seguroDesgravamenIndividual",
                                String.valueOf(simulacionPlazoTO.getPrimaSeguroDesgravamenBasico())));
                            if (parametrosSimulacion.getProducto() == PAGA_LA_MITAD
                                || parametrosSimulacion.getProducto() == PAGA_LA_MITAD_PESOS) {
                                root.appendChild(ConvierteXml.generaNodo(document,
                                    "seguroDesgravamenPagaMitadIndividual", String
                                        .valueOf(resultadoSimulacionTO
                                            .getSimulacionPlazosPagaLaMitadTO()
                                            .getPrimaSeguroDesgravamenBasico())));
                            }
                        }
                        if (parametrosSimulacion.getSeguroIndividualDesgravamen() == SIMULACION_CONVENIENCIA) {
                            root.appendChild(ConvierteXml.generaNodo(document,
                                "seguroDesgravamenIndividual",
                                String.valueOf(simulacionPlazoTO.getPrimaSeguroDesgravamenPlus())));
                            if (parametrosSimulacion.getProducto() == PAGA_LA_MITAD
                                || parametrosSimulacion.getProducto() == PAGA_LA_MITAD_PESOS) {
                                root.appendChild(ConvierteXml.generaNodo(document,
                                    "seguroDesgravamenPagaMitadIndividual", String
                                        .valueOf(resultadoSimulacionTO
                                            .getSimulacionPlazosPagaLaMitadTO()
                                            .getPrimaSeguroDesgravamenPlus())));
                            }
                        }
                        if (parametrosSimulacion.getSeguroIndividualDesgravamen() == TAMANO_GRUPO) {
                            root.appendChild(ConvierteXml.generaNodo(document,
                                "seguroDesgravamenIndividual",
                                String.valueOf(simulacionPlazoTO.getPrimaSeguroDesgravamenFull())));
                            if (parametrosSimulacion.getProducto() == PAGA_LA_MITAD
                                || parametrosSimulacion.getProducto() == PAGA_LA_MITAD_PESOS) {
                                root.appendChild(ConvierteXml.generaNodo(document,
                                    "seguroDesgravamenPagaMitadIndividual", String
                                        .valueOf(resultadoSimulacionTO
                                            .getSimulacionPlazosPagaLaMitadTO()
                                            .getPrimaSeguroDesgravamenFull())));
                            }
                        }
                        root.appendChild(ConvierteXml.generaNodo(document,
                            "dividendoTotalIndividual", String.valueOf(resultadoSimulacionTO
                                .getSimulacionPlazosIndividualesTO()
                                .getDividendoTotalConIndividuales())));
                        root.appendChild(ConvierteXml.generaNodo(document,
                            "dividendoTotalPesosIndividual", String.valueOf(resultadoSimulacionTO
                                .getSimulacionPlazosIndividualesTO()
                                .getDividendoTotalPesosConIndividuales())));
                        root.appendChild(ConvierteXml.generaNodo(document, "caeIndividual", String
                            .valueOf(resultadoSimulacionTO.getCalculoCAEIndividualesTO()
                                .getTasaCAE())));
                        root.appendChild(ConvierteXml.generaNodo(document,
                            "costoTotalCreditoIndividual", String.valueOf(resultadoSimulacionTO
                                .getCalculoCAEIndividualesTO().getCostoTotal())));
                    }
                }
                if (parametrosSimulacion.getProducto() == PAGA_LA_MITAD
                    || parametrosSimulacion.getProducto() == PAGA_LA_MITAD_PESOS) {
                    root.appendChild(ConvierteXml.generaNodo(document,
                        "dividendoNetoPagaMitadIndividual", String.valueOf(resultadoSimulacionTO
                            .getSimulacionPlazosPagaLaMitadIndividualesTO().getDividendo())));
                    root.appendChild(ConvierteXml.generaNodo(document,
                        "dividendoTotalPagaMitadIndividual", String.valueOf(resultadoSimulacionTO
                            .getSimulacionPlazosPagaLaMitadIndividualesTO()
                            .getDividendoTotalConIndividuales())));
                    root.appendChild(ConvierteXml.generaNodo(document,
                        "dividendoTotalPesosPagaMitadIndividual", String
                            .valueOf(resultadoSimulacionTO
                                .getSimulacionPlazosPagaLaMitadIndividualesTO()
                                .getDividendoTotalPesosConIndividuales())));
                }
            }
            root.appendChild(ConvierteXml.generaNodo(
                document,
                "tasacion",
                String.valueOf(resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales()
                    .getTasacion())));
            root.appendChild(ConvierteXml.generaNodo(
                document,
                "estudioTitulos",
                String.valueOf(resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales()
                    .getEstudioTitulos())));
            root.appendChild(ConvierteXml.generaNodo(
                document,
                "borradorEscritura",
                String.valueOf(resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales()
                    .getBorradorEscritura())));
            root.appendChild(ConvierteXml.generaNodo(
                document,
                "notaria",
                String.valueOf(resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales()
                    .getNotariales())));
            root.appendChild(ConvierteXml.generaNodo(
                document,
                "impuesto",
                String.valueOf(resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales()
                    .getImptoAlMutuo())));
            root.appendChild(ConvierteXml.generaNodo(
                document,
                "conservadorBR",
                String.valueOf(resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales()
                    .getInscripcionConservador())));
            root.appendChild(ConvierteXml.generaNodo(
                document,
                "gestoria",
                String.valueOf(resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales()
                    .getGestoria())));
            double totalGastos = resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales()
                .getTasacion()
                + resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales()
                    .getEstudioTitulos()
                + resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales()
                    .getBorradorEscritura()
                + resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales().getNotariales()
                + resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales()
                    .getImptoAlMutuo()
                + resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales()
                    .getInscripcionConservador()
                + resultadoSimulacionTO.getSimulacionTO().getGastosOperacionales().getGestoria();
            root.appendChild(ConvierteXml.generaNodo(document, "totalGastos",
                String.valueOf(totalGastos)));
            root.appendChild(ConvierteXml.generaNodo(document, "tasacionPesos",
                String.valueOf(resultadoSimulacionTO.getGastosOperacionalesPesos().getTasacion())));
            root.appendChild(ConvierteXml.generaNodo(document, "estudioTitulosPesos", String
                .valueOf(resultadoSimulacionTO.getGastosOperacionalesPesos().getEstudioTitulos())));
            root.appendChild(ConvierteXml.generaNodo(document, "borradorEscrituraPesos",
                String.valueOf(resultadoSimulacionTO.getGastosOperacionalesPesos()
                    .getBorradorEscritura())));
            root.appendChild(ConvierteXml.generaNodo(document, "notariaPesos",
                String.valueOf(resultadoSimulacionTO.getGastosOperacionalesPesos().getNotariales())));
            root.appendChild(ConvierteXml.generaNodo(document, "impuestoPesos", String
                .valueOf(resultadoSimulacionTO.getGastosOperacionalesPesos().getImptoAlMutuo())));
            root.appendChild(ConvierteXml.generaNodo(document, "conservadorBRPesos", String
                .valueOf(resultadoSimulacionTO.getGastosOperacionalesPesos()
                    .getInscripcionConservador())));
            root.appendChild(ConvierteXml.generaNodo(document, "gestoriaPesos",
                String.valueOf(resultadoSimulacionTO.getGastosOperacionalesPesos().getGestoria())));
            double totalGastosPesos = resultadoSimulacionTO.getGastosOperacionalesPesos()
                .getTasacion()
                + resultadoSimulacionTO.getGastosOperacionalesPesos().getEstudioTitulos()
                + resultadoSimulacionTO.getGastosOperacionalesPesos().getBorradorEscritura()
                + resultadoSimulacionTO.getGastosOperacionalesPesos().getNotariales()
                + resultadoSimulacionTO.getGastosOperacionalesPesos().getImptoAlMutuo()
                + resultadoSimulacionTO.getGastosOperacionalesPesos().getInscripcionConservador()
                + resultadoSimulacionTO.getGastosOperacionalesPesos().getGestoria();
            root.appendChild(ConvierteXml.generaNodo(document, "totalGastosPesos",
                String.valueOf(totalGastosPesos)));
            for (int i = 0; i < resultadoSimulacionTO.getSimulacionTO().getSimulacionPlazos()
                .size(); i++) {
                SimulacionPlazoTO simulacionPlazoTO = resultadoSimulacionTO.getSimulacionTO()
                    .getSimulacionPlazos().get(i);
                if (simulacionPlazoTO.getPlazo() == parametrosSimulacion.getPlazo()
                    && !simulacionPlazoTO.isComparacionConveniencia()) {
                    root.appendChild(ConvierteXml.generaNodo(document, "primaColectivaIncendio",
                        String.valueOf(simulacionPlazoTO.getMontoSegIncSis())));
                    root.appendChild(ConvierteXml.generaNodo(document, "primaIncendioBasica",
                        String.valueOf(simulacionPlazoTO.getPrimaOfertaMinima())));
                    root.appendChild(ConvierteXml.generaNodo(document, "primaIncendioFull",
                        String.valueOf(simulacionPlazoTO.getPrimaOfertaMaxima())));
                    root.appendChild(ConvierteXml.generaNodo(document, "primaColectivaDesgravamen",
                        String.valueOf(simulacionPlazoTO.getMontoSegDesg())));
                    root.appendChild(ConvierteXml.generaNodo(document, "primaDesgravamenBasica",
                        String.valueOf(simulacionPlazoTO.getPrimaSeguroDesgravamenBasico())));
                    root.appendChild(ConvierteXml.generaNodo(document, "primaDesgravamenPlus",
                        String.valueOf(simulacionPlazoTO.getPrimaSeguroDesgravamenPlus())));
                    root.appendChild(ConvierteXml.generaNodo(document, "primaDesgravamenFull",
                        String.valueOf(simulacionPlazoTO.getPrimaSeguroDesgravamenFull())));
                }
            }
            root.appendChild(ConvierteXml.generaNodo(document, "dfl2",
                String.valueOf(parametrosSimulacion.getDfl2())));
            String codSeguroCodSeguroCesantiaServiuDS40 = TablaValores.getValor(
                "SimCHIP.parametros", LLAVE_SEGUROS, LLAVE_SEGURO_CESANTIA_DS40);
            String codSeguroCodSeguroCesantiaServiuDS01 = TablaValores.getValor(
                "SimCHIP.parametros", LLAVE_SEGUROS, LLAVE_SEGURO_CESANTIA_DS01);
            String codigoServiu = "NO";
            Element nodoAdicionales = (Element) document.createElement("segurosAdicionales");
            for (int i = 0; i < resultadoSimulacionTO.getSegurosAdicionalesTO().length; i++) {
                if (resultadoSimulacionTO.getSegurosAdicionalesTO()[i].estado.equals("1")
                    && (resultadoSimulacionTO.getSegurosAdicionalesTO()[i].idProducto
                        .equals(codSeguroCodSeguroCesantiaServiuDS40) || resultadoSimulacionTO
                        .getSegurosAdicionalesTO()[i].idProducto
                        .equals(codSeguroCodSeguroCesantiaServiuDS01))) {
                    codigoServiu = "SI";
                }

                Element nodoAdicional = (Element) document.createElement("seguroAdicional");
                nodoAdicional.appendChild(ConvierteXml.generaNodo(document, "producto",
                    String.valueOf(resultadoSimulacionTO.getSegurosAdicionalesTO()[i].producto)));
                nodoAdicional.appendChild(ConvierteXml.generaNodo(document, "estado",
                    String.valueOf(resultadoSimulacionTO.getSegurosAdicionalesTO()[i].estado)));
                nodoAdicional.appendChild(ConvierteXml.generaNodo(document, "primaUF",
                    String.valueOf(resultadoSimulacionTO.getSegurosAdicionalesTO()[i].prima)));
                nodoAdicional.appendChild(ConvierteXml.generaNodo(
                    document,
                    "primaPesos",
                    String.valueOf(resultadoSimulacionTO.getSegurosAdicionalesTO()[i].prima
                        * configuracionSimuladorTO.getValorUF())));
                nodoAdicionales.appendChild(nodoAdicional);
            }
            root.appendChild(nodoAdicionales);
            root.appendChild(ConvierteXml.generaNodo(document, "totalPrimaSegurosAdicionalesUF",
                String.valueOf(resultadoSimulacionTO.getTotalPrimaSegurosAdicionales())));
            root.appendChild(ConvierteXml.generaNodo(document, "totalPrimaSegurosAdicionalesPesos",
                String.valueOf(resultadoSimulacionTO.getTotalPrimaSegurosAdicionalesPesos())));
            root.appendChild(ConvierteXml.generaNodo(document, "tieneServiu",
                String.valueOf(codigoServiu)));
            if (coberturasSeguros != null) {
                root.appendChild(ConvierteXml.generaNodo(document,
                    "porcentajeIncendioColectivoLicitado",
                    String.valueOf(coberturasSeguros[0].getPorcentajeIncendioColectivoLicitado())));
                root.appendChild(ConvierteXml.generaNodo(document,
                    "porcentajeIncendioIndividualBasico",
                    String.valueOf(coberturasSeguros[0].getPorcentajeIncendioIndividualBasico())));
                root.appendChild(ConvierteXml.generaNodo(document,
                    "porcentajeIncendioIndividualHogar",
                    String.valueOf(coberturasSeguros[0].getPorcentajeIncendioIndividualHogar())));
                root.appendChild(ConvierteXml.generaNodo(document,
                    "porcentajeDesgravamenIndividualFull",
                    String.valueOf(coberturasSeguros[0].getPorcentajeDesgravamenIndividualFull())));
                root.appendChild(ConvierteXml.generaNodo(document,
                    "porcentajeDesgravamenIndividualPlus",
                    String.valueOf(coberturasSeguros[0].getPorcentajeDesgravamenIndividualPlus())));
                root.appendChild(ConvierteXml.generaNodo(document,
                    "porcentajeDesgravamenIndividualBasico",
                    String.valueOf(coberturasSeguros[0].getPorcentajeDesgravamenIndividualBasico())));
                root.appendChild(ConvierteXml.generaNodo(document,
                    "porcentajeDesgravamenColectivoLicitado", String.valueOf(coberturasSeguros[0]
                        .getPorcentajeDesgravamenColectivoLicitado())));
                int inicioFinIncendio = Integer.parseInt(TablaValores.getValor(SIM_CHIP,
                    "InicioFinIncendio", "VALOR"));
                int inicioFinIncendioContenido = Integer.parseInt(TablaValores.getValor(SIM_CHIP,
                    "InicioFinIncendioContenido", "VALOR"));
                int inicioFinExclusionIncendio = Integer.parseInt(TablaValores.getValor(SIM_CHIP,
                    "InicioFinExclusionIncendio", "VALOR"));
                int inicioFinExclusionContenido = Integer.parseInt(TablaValores.getValor(SIM_CHIP,
                    "InicioFinExclusionContenido", "VALOR"));
                int inicioFinDesgravamen = Integer.parseInt(TablaValores.getValor(SIM_CHIP,
                    "InicioFinDesgravamen", "VALOR"));
                int inicioFinExclusionDesgravamen = Integer.parseInt(TablaValores.getValor(
                    SIM_CHIP, "InicioFinExclusionDesgravamen", "VALOR"));
                for (int i = 0; i < inicioFinIncendio; i++) {
                    Element docRoot = (Element) document.createElement("coberturasIncendioSismo");
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "descripcion",
                        coberturasSeguros[i].getDescripcionCobertura()));
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "subDescripcion",
                        coberturasSeguros[i].getSubDescripcionCobertura()));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "incendioColectivoLicitado",
                        String.valueOf(coberturasSeguros[i].getIncendioColectivoLicitado())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "incendioIndividualBasico",
                        String.valueOf(coberturasSeguros[i].getIncendioIndividualBasico())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "incendioIndividualHogar",
                        String.valueOf(coberturasSeguros[i].getIncendioIndividualHogar())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "incendioAdicional",
                        String.valueOf(coberturasSeguros[i].getIncendioAdicional())));
                    root.appendChild(docRoot);
                }
                for (int i = inicioFinIncendio; i < inicioFinIncendioContenido; i++) {
                    Element docRoot = (Element) document.createElement("coberturasContenido");
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "descripcion",
                        coberturasSeguros[i].getDescripcionCobertura()));
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "subDescripcion",
                        coberturasSeguros[i].getSubDescripcionCobertura()));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "incendioColectivoLicitado",
                        String.valueOf(coberturasSeguros[i].getIncendioColectivoLicitado())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "incendioIndividualBasico",
                        String.valueOf(coberturasSeguros[i].getIncendioIndividualBasico())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "incendioIndividualHogar",
                        String.valueOf(coberturasSeguros[i].getIncendioIndividualHogar())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "incendioAdicional",
                        String.valueOf(coberturasSeguros[i].getIncendioAdicional())));
                    root.appendChild(docRoot);
                }
                for (int i = inicioFinIncendioContenido; i < inicioFinExclusionIncendio; i++) {
                    Element docRoot = (Element) document.createElement("exclusionesIncendioSismo");
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "descripcion",
                        coberturasSeguros[i].getDescripcionCobertura()));
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "subDescripcion",
                        coberturasSeguros[i].getSubDescripcionCobertura()));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "incendioColectivoLicitado",
                        String.valueOf(coberturasSeguros[i].getIncendioColectivoLicitado())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "incendioIndividualBasico",
                        String.valueOf(coberturasSeguros[i].getIncendioIndividualBasico())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "incendioIndividualHogar",
                        String.valueOf(coberturasSeguros[i].getIncendioIndividualHogar())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "incendioAdicional",
                        String.valueOf(coberturasSeguros[i].getIncendioAdicional())));
                    root.appendChild(docRoot);
                }
                for (int i = inicioFinExclusionIncendio; i < inicioFinExclusionContenido; i++) {
                    Element docRoot = (Element) document.createElement("exclusionesContenido");
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "descripcion",
                        coberturasSeguros[i].getDescripcionCobertura()));
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "subDescripcion",
                        coberturasSeguros[i].getSubDescripcionCobertura()));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "incendioColectivoLicitado",
                        String.valueOf(coberturasSeguros[i].getIncendioColectivoLicitado())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "incendioIndividualBasico",
                        String.valueOf(coberturasSeguros[i].getIncendioIndividualBasico())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "incendioIndividualHogar",
                        String.valueOf(coberturasSeguros[i].getIncendioIndividualHogar())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "incendioAdicional",
                        String.valueOf(coberturasSeguros[i].getIncendioAdicional())));
                    root.appendChild(docRoot);
                }
                for (int i = inicioFinExclusionContenido; i < inicioFinDesgravamen; i++) {
                    Element docRoot = (Element) document.createElement("coberturasDesgravamen");
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "descripcion",
                        coberturasSeguros[i].getDescripcionCobertura()));
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "subDescripcion",
                        coberturasSeguros[i].getSubDescripcionCobertura()));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "desgravamenIndividualFull",
                        String.valueOf(coberturasSeguros[i].getDesgravamenIndividualFull())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "desgravamenIndividualPlus",
                        String.valueOf(coberturasSeguros[i].getDesgravamenIndividualPlus())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "desgravamenIndividualBasico",
                        String.valueOf(coberturasSeguros[i].getDesgravamenIndividualBasico())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "desgravamenColectivoLicitado",
                        String.valueOf(coberturasSeguros[i].getDesgravamenColectivoLicitado())));
                    root.appendChild(docRoot);
                }
                for (int i = inicioFinDesgravamen; i < inicioFinExclusionDesgravamen; i++) {
                    Element docRoot = (Element) document.createElement("exclusionDesgravamen");
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "descripcion",
                        coberturasSeguros[i].getDescripcionCobertura()));
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "subDescripcion",
                        coberturasSeguros[i].getSubDescripcionCobertura()));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "desgravamenIndividualFull",
                        String.valueOf(coberturasSeguros[i].getDesgravamenIndividualFull())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "desgravamenIndividualPlus",
                        String.valueOf(coberturasSeguros[i].getDesgravamenIndividualPlus())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "desgravamenIndividualBasico",
                        String.valueOf(coberturasSeguros[i].getDesgravamenIndividualBasico())));
                    docRoot.appendChild(ConvierteXml.generaNodo(document,
                        "desgravamenColectivoLicitado",
                        String.valueOf(coberturasSeguros[i].getDesgravamenColectivoLicitado())));
                    root.appendChild(docRoot);
                }
                int finNotas = Integer.parseInt(TablaValores
                    .getValor(SIM_CHIP, "NOTAS", "CANTIDAD"));
                int finNotasFijas = finNotas - 1;
                for (int i = 0; i < finNotasFijas; i++) {
                    Element docRoot = (Element) document.createElement("notas");
                    docRoot.appendChild(ConvierteXml.generaNodo(document, "nota",
                        TablaValores.getValor(SIM_CHIP, "nota" + i, "GLOSA")));
                    root.appendChild(docRoot);
                }
                root.appendChild(ConvierteXml.generaNodo(document, "notaVariable",
                    TablaValores.getValor(SIM_CHIP, "nota" + finNotasFijas, "GLOSA")));
            }
            return document;
        }
        catch (Exception e) {
            getLogger().error("[generarDocumentoPDF] Ha ocurrido una excepcion: " + e.getMessage());
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[generarDocumentoPDF]: fin del metodo.");
        }
        return null;
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
	 * @param resultSimulaProcesoCHIP
	 *            TO con los datos obtenidos en una simulacion de credito
	 *            hipotecario.
	 * @throws GeneralException
	 * 			Error General.
	 * @since 1.3
	 */
	public void guardarSimulacionProcesoCHIP(
			ResultSimulaProcesoCHIPTO resultSimulaProcesoCHIP)
			throws GeneralException {

		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[guardarSimulacionProcesoCHIP] [rut: "
							+ resultSimulaProcesoCHIP.getRutCliente()
							+ "] [BCI_INI]: MODEL.");
		}
		try {
		    crearEjbServiciosCreditoHipotecarios();
		    this.srvCreditoHipotecario.guardarSimulacionProcesoCHIP(this
					.convertirSimulacionCHIP(resultSimulaProcesoCHIP));
		}
		catch (RemoteException e) {
			if (logger.isEnabledFor(Level.ERROR)) {
				logger.error("[guardarSimulacionProcesoCHIP] [RemoteException] [rut: "
						+ resultSimulaProcesoCHIP.getRutCliente()
						+ "] [BCI_FINEX] : Error al guardar una simulacion CHIP. "
						, e);
			}
            throw new GeneralException("SIMCHIP002", e.getMessage());
		}
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[guardarSimulacionProcesoCHIP] [rut: "
							+ resultSimulaProcesoCHIP.getRutCliente()
							+ "] [BCI_FINOK]: MODEL.");
		}
	}

	/**
	 * Metodo para generar una instancia de proceso CHIP en el webservice.
	 * <p>
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
	 * </ul>
	 * <p>
	 * 
	 * @param resultSimulaProcesoCHIP
	 *            TO con los datos obtenidos en una simulacion de credito
	 *            hipotecario.
	 * @throws GeneralException
	 * 				Error General.
	 * @since 1.3
	 */
	public void generarInstanciaProcesoCHIP(
			ResultSimulaProcesoCHIPTO resultSimulaProcesoCHIP)
			throws GeneralException {
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[generarInstanciaProcesoCHIP] [rut: "
							+ resultSimulaProcesoCHIP.getRutCliente()
							+ "] [BCI_INI]: MODEL.");
		}
		try {
		    crearEjbServiciosCreditoHipotecarios();
			this.srvCreditoHipotecario.generarInstanciaProcesoCHIP(this
				.convertirResultSimulacionADatosInstanciaCHIP(resultSimulaProcesoCHIP));
		} 
		catch (RemoteException e) {
			if (logger.isEnabledFor(Level.ERROR)) {
			   logger.error("[generarInstanciaProcesoCHIP] [RemoteException] [rut: "
				+ resultSimulaProcesoCHIP.getRutCliente() + "] [BCI_FINEX] : Error " 
				+ "al generar una instancia del proceso CHIP. ", e);
			}
			throw new GeneralException("SIMCHIP001", e.getMessage());
		}
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[generarInstanciaProcesoCHIP] [rut: "
							+ resultSimulaProcesoCHIP.getRutCliente()
							+ "] [BCI_FINOK]: MODEL.");
		}
	}

	/**
	 * Transforma un ResultSimulaProcesoCHIPTO a DatosInstanciaProcesoCHIPTO.
	 * <p>
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
	 * </ul>
	 * <p>
	 * 
	 * @param resultado
	 *            Simulacion del proceso CHIP procedente de la capa web
	 * @return datosInstanciaProcesoCHIP Datos de la instancia del proceso CHIP
	 *         requeridos por el web service
	 * @since 1.3
	 */
	private DatosInstanciaProcesoCHIPTO convertirResultSimulacionADatosInstanciaCHIP(
			ResultSimulaProcesoCHIPTO resultado) {
	    
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[convertirResultSimulacionADatosInstanciaCHIP] ["
							+ resultado.toString() + "] [BCI_INI]: MODEL.");
		}
		DatosInstanciaProcesoCHIPTO datosInstancia = new DatosInstanciaProcesoCHIPTO();

		datosInstancia.setAnnosTasaFija(resultado.getAnnosTasaFija());
		datosInstancia.setApellidoMaterno(resultado.getApellidoMaterno());
		datosInstancia.setApellidoPaterno(resultado.getApellidoPaterno());

		datosInstancia.setCodAntiguedadVivienda(resultado
				.getCodAntiguedadVivienda());
		datosInstancia.setCodCanalCredito(resultado.getCodCanalCredito());
		datosInstancia.setCodCanalVenta(resultado.getCodCanalVenta());
		datosInstancia.setCodCiudad(resultado.getCodCiudad());
		datosInstancia.setCodComuna(resultado.getCodComuna());
		datosInstancia.setCodEjecutivo(resultado.getCodEjecutivo());
		datosInstancia.setCodeudor(resultado.isCodeudor());
		datosInstancia.setCodInmobiliaria(resultado.getCodInmobiliaria());
		datosInstancia.setCodMesExclusion(resultado.getCodMesExclusion());
		datosInstancia.setCodOficinaEje(resultado.getCodOficinaEje());
		datosInstancia.setCodProducto(resultado.getCodProducto());
		datosInstancia.setCodRegion(resultado.getCodRegion());
		datosInstancia.setCodSeguroAdicional(resultado.getCodSeguroAdicional());
		datosInstancia.setCodSeguroDesgravamenIndividual(resultado
				.getCodSeguroDesgravamenIndividual());
		datosInstancia.setCodSeguroIncendioSismoIndividual(resultado
				.getCodSeguroIncendioSismoIndividual());
		datosInstancia.setCodSeguroIncendioSismoOpcional(resultado
				.getCodSeguroIncendioSismoOpcional());
		datosInstancia.setCodTipoFinanciamiento(resultado
				.getCodTipoFinanciamiento());
		datosInstancia.setCodTipoVivienda(resultado.getCodTipoVivienda());
		datosInstancia.setContactar(resultado.isContactar());
		datosInstancia.setCostoTotalCreditoColectivo(resultado
				.getCostoTotalCreditoColectivo());
		datosInstancia.setCreditoUF(resultado.getCreditoUF());
		datosInstancia.setCuotaContadoUF(resultado.getCuotaContadoUF());

		datosInstancia.setDfl2(resultado.isDfl2());
		datosInstancia.setDiaVencimiento(resultado.getDiaVencimiento());
		datosInstancia.setDvCliente(resultado.getDvCliente());

		datosInstancia.setEmail(resultado.getEmail());

		datosInstancia.setFechaSimulacion(resultado.getFechaSimulacion());
		datosInstancia.setFono(resultado.getFono());

		datosInstancia.setGastosOperacionalesPesos(this
				.convertirGastosOperacionales(resultado
						.getGastosOperacionalesPesos()));
		datosInstancia.setGastosOperacionalesUF(this
				.convertirGastosOperacionales(resultado
						.getGastosOperacionalesUF()));
		datosInstancia.setGlosaAntiguedadVivienda(resultado
				.getGlosaAntiguedadVivienda());
		datosInstancia.setGlosaCanalCredito(resultado.getGlosaCanalCredito());
		datosInstancia.setGlosaCanalVenta(resultado.getGlosaCanalVenta());
		datosInstancia.setGlosaCiudad(resultado.getGlosaCiudad());
		datosInstancia.setGlosaComuna(resultado.getGlosaComuna());
		datosInstancia.setGlosaInmobiliaria(resultado.getGlosaInmobiliaria());
		datosInstancia.setGlosaMesExclusion(resultado.getGlosaMesExclusion());
		datosInstancia.setGlosaOficinaEje(resultado.getGlosaOficinaEje());
		datosInstancia.setGlosaProducto(resultado.getGlosaProducto());
		datosInstancia.setGlosaRegion(resultado.getGlosaRegion());
		datosInstancia.setGlosaSeguroAdicional(resultado
				.getGlosaSeguroAdicional());
		datosInstancia.setGlosaSeguroDesgravamenIndividual(resultado
				.getGlosaSeguroDesgravamenIndividual());
		datosInstancia.setGlosaSeguroIncendioSismoIndividual(resultado
				.getGlosaSeguroIncendioSismoIndividual());
		datosInstancia.setGlosaSeguroIncendioSismoOpcional(resultado
				.getGlosaSeguroIncendioSismoOpcional());
		datosInstancia.setGlosaTipoFinanciamiento(resultado
				.getGlosaTipoFinanciamiento());
		datosInstancia.setGlosaTipoVivienda(resultado.getGlosaTipoVivienda());

		datosInstancia.setIndCliente(resultado.isIndCliente());

		datosInstancia.setMesesGracia(resultado.getMesesGracia());

		datosInstancia.setNombreCliente(resultado.getNombreCliente());

		datosInstancia.setOrigenSimulacion(resultado.getOrigenSimulacion());

		datosInstancia.setPlazo(resultado.getPlazo());
		datosInstancia.setPorcentajeFinanciamiento(resultado
				.getPorcentajeFinanciamiento());
		datosInstancia.setPrecioViviendaUF(resultado.getPrecioViviendaUF());

		datosInstancia.setRentaLiquida(resultado.getRentaLiquida());
		datosInstancia.setResultadoFiltros(resultado.getResultadoFiltros());
		datosInstancia.setRutCliente(resultado.getRutCliente());

		datosInstancia.setSimulacionPlazoColectivo(this
				.convertirSimulacionPlazo(resultado
						.getSimulacionPlazoColectivo()));
		datosInstancia.setSimulacionPlazoPagaLaMitadColectivo(this
				.convertirSimulacionPlazo(resultado
						.getSimulacionPlazoPagaLaMitadColectivo()));
		datosInstancia.setSubsidioUF(resultado.getSubsidioUF());
		datosInstancia.setSuscribePAC(resultado.isSuscribePAC());

		datosInstancia.setTasaCAEColectivo(resultado.getTasaCAEColectivo());
		datosInstancia.setTasaCostoFondo(resultado.getTasaCostoFondo());
		datosInstancia.setTasaCredito(resultado.getTasaCredito());
		datosInstancia.setTasaSpread(resultado.getTasaSpread());
		datosInstancia.setTotalSimulaciones(resultado.getTotalSimulaciones());
		
		datosInstancia.setCodigoProyecto(resultado.getCodigoProyecto());
        datosInstancia.setGlosaProyecto(resultado.getGlosaProyecto());
		datosInstancia.setCodigoConvenio(resultado.getCodigoConvenio());

		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[convertirResultSimulacionADatosInstanciaCHIP] "
							+ datosInstancia.toString()
							+ " [BCI_FINOK]: MODEL.");
		}
		return datosInstancia;
	}

	/**
	 * Metodo para determinar si ya existe un proceso CHIP para un cliente .
	 * <p>
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
	 * </ul>
	 * <p>
	 * 
	 * @param rut
	 *            Rut del cliente.
	 * @return true En caso que el cliente ya tenga generado un proceso CHIP
	 * @throws GeneralException
	 * 				Error General.
	 * @since 1.3
	 */
	public boolean poseeProcesoCHIPVigente(long rut) throws GeneralException {
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[poseeProcesoCHIPVigente] [rut: " + rut
							+ "] [BCI_INI]: MODEL.");
		}
		boolean procesoActivo = false;
		try {
		    crearEjbServiciosCreditoHipotecarios();
		    procesoActivo = this.srvCreditoHipotecario.poseeProcesoCHIPVigente(rut);
		} 
		catch (RemoteException e) {
			if (logger.isEnabledFor(Level.ERROR)) {
				logger.error("[poseeProcesoCHIPVigente] [RemoteException] [rut: "
						+ rut + "] [BCI_FINEX] : Error al verificar si existe" 
						+ " un proceso CHIP vigente. ", e);
			}
			throw new GeneralException("SIMCHIP005", e.getMessage());
		}
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[poseeProcesoCHIPVigente] [rut: " + rut
							+ "] [procesoActivo: " + procesoActivo
							+ "] [BCI_FINOK]: MODEL.");
		}
		return procesoActivo;
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
	 * 					Error General.
	 * @since 1.3
	 */
	public void registrarInicioProcesoCHIP(long rut) throws GeneralException {
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[registrarInicioProcesoCHIP][rut: " + rut
							+ "] [BCI_INI]: MODEL.");
		}
		try {
		    crearEjbServiciosCreditoHipotecarios();
		    this.srvCreditoHipotecario.registrarInicioProcesoCHIP(rut);
		} 
		catch (RemoteException e) {
			if (logger.isEnabledFor(Level.ERROR)) {
				logger.error("[registrarInicioProcesoCHIP] [RemoteException] [rut: "
						+ rut + "] [BCI_FINEX] : Error al registrar la fecha del " 
						+ "inicio del proceso CHIP: ", e);
			}
			throw new GeneralException("SIMCHIP006", e.getMessage());
		}
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[registrarInicioProcesoCHIP] [rut: " + rut
							+ "] [BCI_FINOK]: MODEL.");
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
	 * @param rut
	 *            Rut del cliente.
	 * @throws GeneralException
	 * 			  Error general.
	 * @since 1.3
	 */
	public void marcarSimulacionProcesoCHIP(long rut) throws GeneralException {
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[marcarSimulacionProcesoCHIP] [rut: " + rut
							+ "] [BCI_INI]: MODEL.");
		}
		try {
		    crearEjbServiciosCreditoHipotecarios();
		    this.srvCreditoHipotecario.marcarSimulacionProcesoCHIP(rut);
		} 
		catch (RemoteException e) {

			if (logger.isEnabledFor(Level.ERROR)) {
				logger.error("[marcarSimulacionProcesoCHIP] [RemoteException] [rut: "
						+ rut
						+ "] [BCI_FINEX] : Error marcar la simulacion: "
						+ e.toString());
			}
			throw new GeneralException("SIMCHIP003", e.getMessage());
		}
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[marcarSimulacionProcesoCHIP] [rut: " + rut
							+ "] [BCI_FINOK]: MODEL.");
		}
	}

	/**
	 * Transforma un ResultSimulaProcesoCHIPTO a SimulacionProcesoCHIPTO.
	 * <p>
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
	 * </ul>
	 * <p>
	 * 
	 * @param resultado
	 *            Simulacion del proceso CHIP procedente de la capa web.
	 * @return simulacionProcesoCHIPTO SimulacionProcesoCHIPTO datos requeridos.
	 *         para almacenar en la bd
	 * @since 1.3
	 */
	private SimulacionProcesoCHIPTO convertirSimulacionCHIP(
			ResultSimulaProcesoCHIPTO resultado) {
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[convertirSimulacionCHIP] [" + resultado.toString()
							+ "] [BCI_INI]: MODEL.");
		}
		SimulacionProcesoCHIPTO simulacionCHIP = new SimulacionProcesoCHIPTO();

		simulacionCHIP.setAnnosTasaFija(resultado.getAnnosTasaFija());
		simulacionCHIP.setApellidoMaterno(resultado.getApellidoMaterno());
		simulacionCHIP.setApellidoPaterno(resultado.getApellidoPaterno());

		simulacionCHIP.setCodAntiguedadVivienda(resultado
				.getCodAntiguedadVivienda());
		simulacionCHIP.setCodCanalCredito(resultado.getCodCanalCredito());
		simulacionCHIP.setCodCanalVenta(resultado.getCodCanalVenta());
		simulacionCHIP.setCodCiudad(resultado.getCodCiudad());
		simulacionCHIP.setCodComuna(resultado.getCodComuna());
		simulacionCHIP.setCodDestino(resultado.getCodDestino());
		simulacionCHIP.setCodEjecutivo(resultado.getCodEjecutivo());
		simulacionCHIP.setCodeudor(resultado.isCodeudor());
		simulacionCHIP.setCodInmobiliaria(resultado.getCodInmobiliaria());
		simulacionCHIP.setCodMesExclusion(resultado.getCodMesExclusion());
		simulacionCHIP.setCodOficinaEje(resultado.getCodOficinaEje());
		simulacionCHIP.setCodProducto(resultado.getCodProducto());
		simulacionCHIP.setCodRegion(resultado.getCodRegion());
		simulacionCHIP.setCodSeguroAdicional(resultado.getCodSeguroAdicional());
		simulacionCHIP.setCodSeguroDesgravamenIndividual(resultado
				.getCodSeguroDesgravamenIndividual());
		simulacionCHIP.setCodSeguroIncendioSismoIndividual(resultado
				.getCodSeguroIncendioSismoIndividual());
		simulacionCHIP.setCodSeguroIncendioSismoOpcional(resultado
				.getCodSeguroIncendioSismoOpcional());
		simulacionCHIP.setCodTipoFinanciamiento(resultado
				.getCodTipoFinanciamiento());
		simulacionCHIP.setCodTipoMoneda(resultado.getCodTipoMoneda());
		simulacionCHIP.setCodTipoVivienda(resultado.getCodTipoVivienda());
		simulacionCHIP.setContactar(resultado.isContactar());
		simulacionCHIP.setCostoTotalCreditoColectivo(resultado
				.getCostoTotalCreditoColectivo());
		simulacionCHIP.setCostoTotalCreditoIndividual(resultado
				.getCostoTotalCreditoIndividual());
		simulacionCHIP.setCreditoUF(resultado.getCreditoUF());
		simulacionCHIP.setCuotaContadoUF(resultado.getCuotaContadoUF());

		simulacionCHIP.setDfl2(resultado.isDfl2());
		simulacionCHIP.setDiaVencimiento(resultado.getDiaVencimiento());
		simulacionCHIP.setDvCliente(resultado.getDvCliente());

		simulacionCHIP.setEmail(resultado.getEmail());

		simulacionCHIP.setFechaInstanciaCHIP(resultado.getFechaInstanciaCHIP());
		simulacionCHIP.setFechaNacimientoCliente(resultado
				.getFechaNacimientoCliente());
		simulacionCHIP.setFechaNacimientoCodeudor(resultado
				.getFechaNacimientoCodeudor());
		simulacionCHIP.setFechaSimulacion(resultado.getFechaSimulacion());
		simulacionCHIP.setFono(resultado.getFono());

		simulacionCHIP.setGastosOperacionalesPesos(this
				.convertirGastosOperacionales(resultado
						.getGastosOperacionalesPesos()));
		simulacionCHIP.setGastosOperacionalesUF(this
				.convertirGastosOperacionales(resultado
						.getGastosOperacionalesUF()));
		simulacionCHIP.setGlosaAntiguedadVivienda(resultado
				.getGlosaAntiguedadVivienda());
		simulacionCHIP.setGlosaCanalCredito(resultado.getGlosaCanalCredito());
		simulacionCHIP.setGlosaCanalVenta(resultado.getGlosaCanalVenta());
		simulacionCHIP.setGlosaCiudad(resultado.getGlosaCiudad());
		simulacionCHIP.setGlosaComuna(resultado.getGlosaComuna());
		simulacionCHIP.setGlosaDestino(resultado.getGlosaDestino());
		simulacionCHIP.setGlosaInmobiliaria(resultado.getGlosaInmobiliaria());
		simulacionCHIP.setGlosaMesExclusion(resultado.getGlosaMesExclusion());
		simulacionCHIP.setGlosaOficinaEje(resultado.getGlosaOficinaEje());
		simulacionCHIP.setGlosaProducto(resultado.getGlosaProducto());
		simulacionCHIP.setGlosaRegion(resultado.getGlosaRegion());
		simulacionCHIP.setGlosaSeguroAdicional(resultado
				.getGlosaSeguroAdicional());
		simulacionCHIP.setGlosaSeguroDesgravamenIndividual(resultado
				.getGlosaSeguroDesgravamenIndividual());
		simulacionCHIP.setGlosaSeguroIncendioSismoIndividual(resultado
				.getGlosaSeguroIncendioSismoIndividual());
		simulacionCHIP.setGlosaSeguroIncendioSismoOpcional(resultado
				.getGlosaSeguroIncendioSismoOpcional());
		simulacionCHIP.setGlosaTipoFinanciamiento(resultado
				.getGlosaTipoFinanciamiento());
		simulacionCHIP.setGlosaTipoMoneda(resultado.getGlosaTipoMoneda());
		simulacionCHIP.setGlosaTipoVivienda(resultado.getGlosaTipoVivienda());

		simulacionCHIP.setIndCliente(resultado.isIndCliente());

		simulacionCHIP.setMaterialAdobe(resultado.isMaterialAdobe());
		simulacionCHIP.setMesesGracia(resultado.getMesesGracia());

		simulacionCHIP.setNombreCliente(resultado.getNombreCliente());

		simulacionCHIP.setOrigenSimulacion(resultado.getOrigenSimulacion());

		simulacionCHIP.setPlazo(resultado.getPlazo());
		simulacionCHIP.setPorcentajeFinanciamiento(resultado
				.getPorcentajeFinanciamiento());
		simulacionCHIP.setPorcentajeSegDesgravamenCliente(resultado
				.getPorcentajeSegDesgravamenCliente());
		simulacionCHIP.setPorcentajeSegDesgravamenCodeudor(resultado
				.getPorcentajeSegDesgravamenCodeudor());
		simulacionCHIP.setPrecioViviendaUF(resultado.getPrecioViviendaUF());

		simulacionCHIP.setRentaLiquida(resultado.getRentaLiquida());
		simulacionCHIP.setResultadoFiltros(resultado.getResultadoFiltros());
		simulacionCHIP.setRutCliente(resultado.getRutCliente());

		simulacionCHIP.setSeguroCesantiaDobleProteccion(resultado
				.isSeguroCesantiaDobleProteccion());
		simulacionCHIP.setSeguroCesantiaDS01(resultado.isSeguroCesantiaDS01());
		simulacionCHIP.setSeguroCesantiaDS40(resultado.isSeguroCesantiaDS40());
		simulacionCHIP.setSeguroCesantiaInv(resultado.isSeguroCesantiaInv());
		simulacionCHIP.setSimulacionPlazoColectivo(this
				.convertirSimulacionPlazo(resultado
						.getSimulacionPlazoColectivo()));
		simulacionCHIP.setSimulacionPlazoIndividual(this
				.convertirSimulacionPlazo(resultado
						.getSimulacionPlazoIndividual()));
		simulacionCHIP.setSimulacionPlazoPagaLaMitadColectivo(this
				.convertirSimulacionPlazo(resultado
						.getSimulacionPlazoPagaLaMitadColectivo()));
		simulacionCHIP.setSimulacionPlazoPagaLaMitadIndividual(this
				.convertirSimulacionPlazo(resultado
						.getSimulacionPlazoPagaLaMitadIndividual()));
		simulacionCHIP.setSubsidioUF(resultado.getSubsidioUF());
		simulacionCHIP.setSuscribePAC(resultado.isSuscribePAC());

		simulacionCHIP.setTasaCAEColectivo(resultado.getTasaCAEColectivo());
		simulacionCHIP.setTasaCAEIndividual(resultado.getTasaCAEIndividual());
		simulacionCHIP.setTasaCostoFondo(resultado.getTasaCostoFondo());
		simulacionCHIP.setTasaCredito(resultado.getTasaCredito());
		simulacionCHIP.setTasaSpread(resultado.getTasaSpread());
		simulacionCHIP.setTotalGastosOperacionalesPesos(resultado
				.getTotalGastosOperacionalesPesos());
		simulacionCHIP.setTotalPrimaSegurosEscogidosPesos(resultado
				.getTotalPrimaSegurosEscogidosPesos());
		simulacionCHIP.setTotalPrimaSegurosEscogidosUF(resultado
				.getTotalPrimaSegurosEscogidosUF());
		simulacionCHIP.setTotalSimulaciones(resultado.getTotalSimulaciones());

		simulacionCHIP.setValorUF(resultado.getValorUF());
		simulacionCHIP.setCodigoProyecto(resultado.getCodigoProyecto());
		simulacionCHIP.setGlosaProyecto(resultado.getGlosaProyecto());
		simulacionCHIP.setCodigoConvenio(resultado.getCodigoConvenio());

		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[convertirSimulacionCHIP] [" + simulacionCHIP.toString()
							+ "] [BCI_FINOK]: MODEL.");
		}
		return simulacionCHIP;

	}

	/**
	 * Transforma un GastosOperacionalesTO a GastosOperacionalesCHIPTO.
	 * <p>
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
	 * </ul>
	 * <p>
	 * 
	 * @param gastos
	 *            gasto operacional de la capa web.
	 * @return gastosCHIP
	 * @since 1.3
	 */
	private GastosOperacionalesCHIPTO convertirGastosOperacionales(
			GastosOperacionalesTO gastos) {
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[convertirGastosOperacionales] [" + gastos.toString()
							+ "] [BCI_INI]: MODEL.");
		}
		GastosOperacionalesCHIPTO gastosCHIP = new GastosOperacionalesCHIPTO();
		gastosCHIP.setBorradorEscritura(gastos.getBorradorEscritura());
		gastosCHIP.setEstudioTitulos(gastos.getEstudioTitulos());
		gastosCHIP.setGestoria(gastos.getGestoria());
		gastosCHIP.setImptoAlMutuo(gastos.getImptoAlMutuo());
		gastosCHIP
				.setInscripcionConservador(gastos.getInscripcionConservador());
		gastosCHIP.setNotariales(gastos.getNotariales());
		gastosCHIP.setTasacion(gastos.getTasacion());
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[convertirGastosOperacionales] [BCI_FINOK]: MODEL.");
		}
		return gastosCHIP;
	}

	/**
	 * Transforma una SimulacionPlazoTO a SimulacionPlazoCHIPTO.
	 * <p>
	 * Registro de versiones:
	 * <ul>
	 * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
	 * </ul>
	 * <p>
	 * 
	 * @param plazo
	 *            Simulacion del proceso CHIP procedente de la capa web.
	 * @return simulacionProcesoCHIPTO SimulacionProcesoCHIPTO datos requeridos.
	 *         para almacenar en la bd
	 * @since 1.3
	 */
	private SimulacionPlazoCHIPTO convertirSimulacionPlazo(SimulacionPlazoTO plazo) {
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info(
					"[convertirSimulacionPlazo] [" + plazo.toString()
							+ "] [BCI_INI]: MODEL.");
		}
		SimulacionPlazoCHIPTO plazoCHIP = new SimulacionPlazoCHIPTO();
		plazoCHIP.setComparacionConveniencia(plazo.isComparacionConveniencia());
		plazoCHIP.setDividendo(plazo.getDividendo());
		plazoCHIP.setDividendoTotal(plazo.getDividendoTotal());
		plazoCHIP.setDividendoTotalConIndividuales(plazo.getDividendoTotalConIndividuales());
		plazoCHIP.setDividendoTotalPesos(plazo.getDividendoTotalPesos());
		plazoCHIP.setDividendoTotalPesosConIndividuales(plazo
				.getDividendoTotalPesosConIndividuales());
		plazoCHIP.setMontoSegDesg(plazo.getMontoSegDesg());
		plazoCHIP.setMontoSegIncSis(plazo.getMontoSegIncSis());
		plazoCHIP.setPlazo(plazo.getPlazo());
		plazoCHIP.setPrimaOfertaMaxima(plazo.getPrimaOfertaMaxima());
		plazoCHIP.setPrimaOfertaMinima(plazo.getPrimaOfertaMinima());
		plazoCHIP.setPrimaSeguroDesgravamenBasico(plazo
				.getPrimaSeguroDesgravamenBasico());
		plazoCHIP.setPrimaSeguroDesgravamenFull(plazo
				.getPrimaSeguroDesgravamenFull());
		plazoCHIP.setPrimaSeguroDesgravamenPlus(plazo
				.getPrimaSeguroDesgravamenPlus());
		plazoCHIP.setTasa(plazo.getTasa());
		plazoCHIP.setTasaBase(plazo.getTasaBase());
		plazoCHIP.setTasaDesgravamen(plazo.getTasaDesgravamen());
		plazoCHIP.setTasaIncendio(plazo.getTasaIncendio());
		plazoCHIP.setTasaOfertaMaxima(plazo.getTasaOfertaMaxima());
		plazoCHIP.setTasaOfertaMinima(plazo.getTasaOfertaMinima());
		plazoCHIP.setTasaSeguroDesgravamenBasico(plazo
				.getTasaSeguroDesgravamenBasico());
		plazoCHIP.setTasaSeguroDesgravamenFull(plazo
				.getTasaSeguroDesgravamenFull());
		plazoCHIP.setTasaSeguroDesgravamenPlus(plazo
				.getTasaSeguroDesgravamenPlus());
		plazoCHIP.setTasaSpread(plazo.getTasaSpread());
		plazoCHIP.setTotalOfertaMaxima(plazo.getTotalOfertaMaxima());
		plazoCHIP.setTotalOfertaMinima(plazo.getTotalOfertaMinima());
		plazoCHIP.setTramo(plazo.getTramo());
		if (getLogger().isEnabledFor(Level.INFO)) {
			getLogger().info("[convertirSimulacionPlazo] [BCI_FINOK]: MODEL.");
		}
		return plazoCHIP;
	}
	
	
    /**
     * Método encargado de crear la instancia del Ejb de ServiciosCreditoHipotecario.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Eduardo Mascayano (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * <p>
     * 
     * @throws GeneralException En el caso de ocurrir algún problema al crear la instancia del Ejb.
     * @since 1.3
     */
    private void crearEjbServiciosCreditoHipotecarios() throws GeneralException {
        
        try {
            Class homeClass = ServiciosCreditoHipotecarioHome.class;
            ServiciosCreditoHipotecarioHome srvCreditoHipotecarioHome = null;
            EnhancedServiceLocator locator;
            
            locator = EnhancedServiceLocator.getInstance();
            srvCreditoHipotecarioHome = (ServiciosCreditoHipotecarioHome) locator
                .getGenericService(JNDI_SERV_HIPOTECARIO, homeClass);
            this.srvCreditoHipotecario = srvCreditoHipotecarioHome.create();
        }
        catch (EnhancedServiceLocatorException ex) {
            getLogger().error("[crearEjb] EnhancedServiceLocatorException.", ex);
            throw new GeneralException("SERV");
        }
        catch (RemoteException ex) {
            getLogger().error("[crearEjb] RemoteException.", ex);
            throw new GeneralException("SERV");
        }
        catch (CreateException ex) {
            getLogger().error("[crearEjb] CreateException.", ex);
            throw new GeneralException("SERV");
        }
    }	
}
