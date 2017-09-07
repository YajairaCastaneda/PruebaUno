package wcorp.serv.hipotecario;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJBObject;
import javax.naming.NamingException;

import wcorp.aplicaciones.creditos.hipotecarios.dividendos.to.CertificadoDividendoTO;
import wcorp.aplicaciones.creditos.hipotecarios.dividendos.to.DeudaHipotecariaTO;
import wcorp.aplicaciones.creditos.hipotecarios.dividendos.to.DividendoTO;
import wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to.DatosInstanciaProcesoCHIPTO;
import wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to.SeguimientoChipTO;
import wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to.SimulacionProcesoCHIPTO;
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
import wcorp.hipotecario.vo.TasasVO;
import wcorp.serv.direcciones.to.CiudadTO;
import wcorp.serv.direcciones.to.ComunaTO;
import wcorp.serv.direcciones.to.RegionTO;
import wcorp.serv.hipotecario.dto.ProductoCreditoHipotecarioDTO;
import wcorp.serv.seguros.SeguroCliente;
import wcorp.util.GeneralException;
import wcorp.util.com.TuxedoException;
import wcorp.util.workstation.ServicioNoDisponibleException;

/**
 *
 * <b>ServiciosCreditoHipotecario</b>
 * <p>
 * Clase utilizada de interfaz para 
 * ServiciosCreditoHipotecario.
 * <p>
 * Registro de versiones:
 * <ul>
 *     <li>1.0 desconocido: versión inicial</li>
 *     <li>1.1 17/11/2014 Victor Caroca V. (SEnTRA): Se agregaron los metodos:
    *   <ul>
    *     <li>Se agrega el método {@link #obtenerSolicitudCHIP(long, char) } </li>
    *     <li>Se agrega el método {@link #detalleSolicitud(long, String)} </li>
    *   </ul>
*      </li>
 * </ul>
 * <p>
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 * <P> 
 * 
 * @see ServiciosCreditoHipotecarioBean
 */

 
public interface ServiciosCreditoHipotecario extends EJBObject {

    /**
     * @see ServiciosCreditoHipotecarioBean#getListaCreditoHipotecario(long)
     */     	
    public ProductoCreditoHipotecario[] getListaCreditoHipotecario(long rut) throws
            TuxedoException, GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#getDetalleCreditoHipotecario(String)
     */     
    public DetalleCreditoHipotecario getDetalleCreditoHipotecario(String numope) throws
            TuxedoException, GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#getLiqPrepCredHipExt(String, Date)
     */        
    public LiqPrepCredHipExt getLiqPrepCredHipExt(String numope, Date fecha) throws
            TuxedoException, GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#getHipConDivCanCre(String)
     */    
    public HipConDivCanCre getHipConDivCanCre(String numOperacion) throws
            TuxedoException, GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#getHipDivImpago(String)
     */          
    public HipConDivImpago[] getHipDivImpago(String numOperacion) throws
            TuxedoException, GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#getHipConDivImpago(String)
     */      
    public HipConDivImpago getHipConDivImpago(String numOperacion) throws
            TuxedoException, GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#getDivImpago(String, String, long, char)
     */          
    public HipConDivImpago getDivImpago(String numOperacion,
                                        String nroDividendo, long rut, char dv) throws
            TuxedoException, GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#generaPagoDividendo(String, String, String, String, String, String)
     */      
    public String generaPagoDividendo(String numOperacion, String nroDividendo,
                                      String subTotalPesos, String interesPenal,
                                      String gastoCobranza,
                                      String totalDividendo, String operador) throws
            TuxedoException, GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#getHipConLetraAsig(String)
     */      
    public HipConLetraAsig getHipConLetraAsig(String numOperacion) throws
            TuxedoException, GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#getDetalleDfl2(String)
     */      
    public DetalleDfl2 getDetalleDfl2(String numOperacion) throws
            TuxedoException, GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#getDetalleInteres(String)
     */    
    public DetalleInteres getDetalleInteres(String numOperacion) throws
            TuxedoException, GeneralException, RemoteException;

   /**
     * @see ServiciosCreditoHipotecarioBean#selectSolicitudes(long)
    */
    public Collection selectSolicitudes(long rut) throws com.schema.util.
            GeneralException, wcorp.env.BusinessException, TuxedoException,
            RemoteException;

   /**
     * @see ServiciosCreditoHipotecarioBean#calculaSimulacion(DatosOperacionVO)
    */
    public Collection calculaSimulacion(DatosOperacionVO data) throws com.
            schema.util.GeneralException, wcorp.util.GeneralException,
            TuxedoException, RemoteException;

   /**
     * @see ServiciosCreditoHipotecarioBean#calculaSimulacionNegociada(DatosOperacionVO)
    */
    public Collection calculaSimulacionNegociada(DatosOperacionVO data) throws
            com.schema.util.GeneralException, wcorp.util.GeneralException,
            TuxedoException, RemoteException;

   /**
     * @see ServiciosCreditoHipotecarioBean#calculaSimulacionPromocion(DatosOperacionVO)
    */
    public Collection calculaSimulacionPromocion(DatosOperacionVO data) throws
            com.schema.util.GeneralException, wcorp.util.GeneralException,
            TuxedoException, RemoteException;

   /**
     * @see ServiciosCreditoHipotecarioBean#calculaSimulacionProyeccion(DatosOperacionVO)
    */
    public Collection calculaSimulacionProyeccion(DatosOperacionVO data) throws
            com.schema.util.GeneralException, wcorp.util.GeneralException,
            TuxedoException, RemoteException;

   /**
     * @see ServiciosCreditoHipotecarioBean#ingresaSolicitud(SolicitudVO)
    */
    public void ingresaSolicitud(SolicitudVO solicitud) throws com.schema.util.
            GeneralException, wcorp.util.GeneralException, TuxedoException,
            RemoteException;

   /**
     * @see ServiciosCreditoHipotecarioBean#ingresaSolicitud(SolicitudVO, GastosOperacionVO)
    */
    public void ingresaSolicitud(SolicitudVO solicitud,
                                 GastosOperacionVO gastosOpUf) throws com.
            schema.util.GeneralException, wcorp.util.GeneralException,
            TuxedoException, RemoteException;


   /**
     * @see ServiciosCreditoHipotecarioBean#calculaGastosOperacion(DatosOperacionVO)
    */
    public GastosOperacionVO calculaGastosOperacion(DatosOperacionVO data) throws
            com.schema.util.GeneralException, wcorp.util.GeneralException,
            TuxedoException, RemoteException;

   /**
     * @see ServiciosCreditoHipotecarioBean#getTabla(String)
    */
    public Collection getTabla(String tabla) throws com.schema.util.
            GeneralException, wcorp.util.GeneralException, TuxedoException,
            RemoteException;

   /**
     * @see ServiciosCreditoHipotecarioBean#getTablaProducto()
    */
    public Collection getTablaProducto() throws com.schema.util.
            GeneralException, wcorp.util.GeneralException, TuxedoException,
            RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#getTablaProducto(String)
     */
     public Collection getTablaProducto(String canal) throws com.schema.util.
             GeneralException, wcorp.util.GeneralException, TuxedoException,
             RemoteException;

   /**
      * @see ServiciosCreditoHipotecarioBean#getEstadoSolicitud(long, int)
    */
    public Collection getEstadoSolicitud(long rut, int nroOperacion) throws com.
            schema.util.GeneralException, wcorp.util.GeneralException,
            TuxedoException, RemoteException;

   /**
     * @see ServiciosCreditoHipotecarioBean#getRangosCreditoProducto(ProductoVO, String, int, String)
    */
    public ProductoVO getRangosCreditoProducto(ProductoVO pvo,
                                               String codAntiguedad,
                                               int codObjetivo,
                                               String codBienRaiz) throws com.
            schema.util.GeneralException, wcorp.util.GeneralException,
            RemoteException;

/**
     * @see ServiciosCreditoHipotecarioBean#getProducto(int, String, int, String)
    */
   public ProductoVO getProducto( int codProducto, String codAntiguedad,
                                  int codObjetivo, String codBienRaiz) throws
            com.schema.util.GeneralException, wcorp.util.GeneralException,
            TuxedoException, RemoteException;

   /**
    * @see ServiciosCreditoHipotecarioBean#getProducto(int, String, int, String, String)
    */
   public ProductoVO getProducto( int codProducto, String codAntiguedad,
                                  int codObjetivo, String codBienRaiz, String canal) throws
            com.schema.util.GeneralException, wcorp.util.GeneralException,
            TuxedoException, RemoteException;

   /**
    * @see ServiciosCreditoHipotecarioBean#getTasas(DatosOperacionVO)
    * @deprecated
    */
    public TasasVO getTasas(DatosOperacionVO data) throws com.schema.util.
            GeneralException, wcorp.util.GeneralException, RemoteException;

   /**
     * @see ServiciosCreditoHipotecarioBean#getSettings()
    */
    public SettingsVO getSettings() throws com.schema.util.GeneralException,
            wcorp.util.GeneralException, TuxedoException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#getSettings(String)
     */
     public SettingsVO getSettings(String canal) throws com.schema.util.GeneralException,
             wcorp.util.GeneralException, TuxedoException, RemoteException;

   /**
      * @see ServiciosCreditoHipotecarioBean#reloadSettings()
    */
    public SettingsVO reloadSettings() throws com.schema.util.GeneralException,
            wcorp.util.GeneralException, TuxedoException, RemoteException;

   /**
     * @see ServiciosCreditoHipotecarioBean#getAvisoVencimientoDividendo(String, int)
    */
    public AvisoVencimientoVO getAvisoVencimientoDividendo(String codOperacion,
            int nroDividendo) throws com.schema.util.GeneralException,
            wcorp.util.GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#generaPagoDividendo(String, String, String, String, String, String, String,String,String)
      */
    public String generaPagoDividendo(String numOperacion, String nroDividendo,
                                      String subTotalPesos, String interesPenal,
                                      String gastoCobranza,
                                      String totalDividendo, String oficina,
                                      String formaPago, String operador) throws
            TuxedoException, GeneralException, RemoteException;


       /**
     * @see ServiciosCreditoHipotecarioBean#consultaCertificadosDisponibles(String)
        */
       public CertificadosDisponiblesVO[] consultaCertificadosDisponibles( String codOperacion )
            throws com.schema.util.GeneralException, wcorp.util.GeneralException, RemoteException;

       /**
        * @see ServiciosCreditoHipotecarioBean#consultaCertificadoIntereses(String, int)
        */
       public CertificadoInteresesVO consultaCertificadoIntereses( String codOperacion, int periodo )
            throws com.schema.util.GeneralException, wcorp.util.GeneralException, RemoteException;

       /**
        * @see ServiciosCreditoHipotecarioBean#consultaCertificadoDfl2(String, int)
        */
       public CertificadoDFL2VO consultaCertificadoDfl2( String codOperacion, int periodo )
            throws com.schema.util.GeneralException, wcorp.util.GeneralException, RemoteException;

    /**
        * @see ServiciosCreditoHipotecarioBean#consultaDividendosPagados(String, String, String)
     */
    public HipConDivCanCre[] consultaDividendosPagados(String numOperacion, String mes, String ano)
            throws com.schema.util.GeneralException, wcorp.util.GeneralException, RemoteException;


	 /**
     * @see ServiciosCreditoHipotecarioBean#calculaSimulacionMonto(DatosOperacionVO)
	*/
	public SimulacionPlazosTasasVO[] calculaSimulacionMonto(DatosOperacionVO data) throws com.
	        schema.util.GeneralException, wcorp.util.GeneralException,
	        TuxedoException, RemoteException;

	/**
     * @see ServiciosCreditoHipotecarioBean#guardarSimulacion(FichaSimulacionVO)
     */
	public boolean guardarSimulacion(FichaSimulacionVO data) throws com.schema.util.GeneralException, wcorp.util.GeneralException,  TuxedoException, RemoteException;


    /**
     * @see ServiciosCreditoHipotecarioBean#pagaDividendoVerificaPACCliente(String, String, String, String,
     *      String, String, String, String, String, String)
     * 
     */
	public String pagaDividendoVerificaPACCliente(String numOperacion, String nroDividendo,
			String subTotalPesos, String interesPenal,
			String gastoCobranza,
			String totalDividendo, String oficina,
                        String formaPago, String operador, String tipoServicio) throws com.schema.util.GeneralException,
                        wcorp.util.GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#consultaDividendosPagadosMonto(String, String, String)
     */	
	public HipConDivCanCre[] consultaDividendosPagadosMonto(String numOperacion, String mes, String ano)
	throws com.schema.util.GeneralException, wcorp.util.GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#obtenerDividendos(int)
     */
    public DividendoTO[] obtenerDividendos(String numeroOperacion) throws ServicioNoDisponibleException, 
        RemoteException;

         /**
     * @see ServiciosCreditoHipotecarioBean#traerListaCreditoHipotecario(long)
         */
        public ProductoCreditoHipotecarioDTO[] traerListaCreditoHipotecario(long rut)  throws TuxedoException, GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#efectuarPagoDividendo(String, String, String,
     *       String, String, String, HipConDivImpago, String) 
     */
    public String efectuarPagoDividendo(String cuentaCteCargo, String totalDividendo,
            String trxCargo, String codMnemonico, String numeroOperacion, String numeroDividendo,
            HipConDivImpago datosDividendoAPagar, String trxReverso) throws com.schema.util.GeneralException,
            wcorp.util.GeneralException, RemoteException;


    /**
     * @see ServiciosCreditoHipotecarioBean#simulacionConComplementario(DatosOperacionVO)
     */
    public ProductoCreditoTO[] simulacionConComplementario(DatosOperacionVO data) throws GeneralException,
        RemoteException;


    /**
     * @see ServiciosCreditoHipotecarioBean#detalleCreditoConComplementario(DatosOperacionVO)
     */
    public ProductoCreditoTO[] detalleCreditoConComplementario(DatosOperacionVO data
        , ProductoCreditoTO[] creditoTO) throws GeneralException, RemoteException;


    /**
     * @see ServiciosCreditoHipotecarioBean#simulacionMixto(DatosOperacionVO)
     */
    public ProductoCreditoTO[] simulacionMixto(DatosOperacionVO data) throws GeneralException, RemoteException;


    /**
     * @see ServiciosCreditoHipotecarioBean#detalleMixto(DatosOperacionVO)
     */
    public ProductoCreditoTO[] detalleMixto(DatosOperacionVO data, ProductoCreditoTO[] creditoTO)
        throws GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#segurosVinculados(DatosOperacionVO)
     */
    public SeguroCliente[] segurosVinculados(DatosOperacionVO data) throws GeneralException, RemoteException;
    
   /**
     * @see ServiciosCreditoHipotecarioBean#listaSegurosVinculados()
     */
    public SeguroCliente[] listaSegurosVinculados() throws GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#obtenerCertificadoDividendosPagados(String)
     */
    public CertificadoDividendoTO[] obtenerCertificadoDividendosPagados(String operacion) throws GeneralException, RemoteException;
 
    /**
     * @see ServiciosCreditoHipotecarioBean#obtenerCertificadoInteresesPagados(String, int)
     */
    public CertificadoInteresesVO obtenerCertificadoInteresesPagados(String operacion, int periodo) throws GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#obtenerDeudaHipotecaria(String)
     */
    public DeudaHipotecariaTO obtenerDeudaHipotecaria(String operacion) throws GeneralException, RemoteException;
    
    /**
     * @see ServiciosCreditoHipotecarioBean#calculaCAE(DatosCalculoCAEVO)
     */
   public ResultCalculoCAEVO calculaCAE(DatosCalculoCAEVO data ) throws com.schema.util.GeneralException, wcorp.util.GeneralException,
   TuxedoException, RemoteException;

   /**
    * @see ServiciosCreditoHipotecarioBean#obtenerRegiones()
    */
   public RegionTO[] obtenerRegiones() throws Exception, GeneralException;

   /**
    * @see ServiciosCreditoHipotecarioBean#obtenerCiudadesPorRegion(String)
    */
   public CiudadTO[] obtenerCiudadesPorRegion(String codigoRegion) throws Exception, GeneralException;

   /**
    * @see ServiciosCreditoHipotecarioBean#obtenerComunasPorCiudad(String)
    */
   public ComunaTO[] obtenerComunasPorCiudad(String codigoCiudad) throws Exception, GeneralException;

   /**
    * @see ServiciosCreditoHipotecarioBean#getTablaProducto(String, int)
    */
    public Collection getTablaProducto(String canal, int codigoProducto) throws com.schema.util.
            GeneralException, wcorp.util.GeneralException, TuxedoException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#calculaSimulacionTotal(DatosOperacionVO)
     */
    public Collection calculaSimulacionTotal(DatosOperacionVO data) throws com.schema.util.GeneralException,
        wcorp.util.GeneralException, TuxedoException, RemoteException, wcorp.util.GeneralException,
        NamingException;

    /**
     * @see ServiciosCreditoHipotecarioBean#calculaSimulacionRenegociadaTotal(DatosOperacionVO)
     */
    public Collection calculaSimulacionRenegociadaTotal(DatosOperacionVO data)
        throws com.schema.util.GeneralException, wcorp.util.GeneralException, TuxedoException, RemoteException,
        wcorp.util.GeneralException, NamingException;
    
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
     * @throws GeneralException en caso de error GeneralException
     * @throws RemoteException en caso de error RemoteException
     * @return List.
     * @since 1.1
     */
    public List obtenerSolicitudCHIP(long rut, char dv) 
          throws GeneralException, RemoteException;
    
     /**
     * Metodo que Obtiene el detalle 
     * de la solicitud.
     * <p>
     * 
     * Registro de versiones:
     * <UL>
     * <li>1.0 17/11/2014 Víctor Caroca.   (Sentra): versión inicial.</LI>
     * 
     * </UL>
     * <P>
     * @param rut long.
     * @param numeroOperacion String.
     * @throws GeneralException en caso de error GeneralException
     * @throws RemoteException en caso de error RemoteException
     * @return SeguimientoChipTO.
     * @since 1.1
     */
    public SeguimientoChipTO detalleSolicitud(long rut, String numeroOperacion)
	 throws GeneralException, RemoteException;


    /**
     * @see ServiciosCreditoHipotecarioBean#guardarSimulacionProcesoCHIP(SimulacionProcesoCHIPTO)
     */
    public void guardarSimulacionProcesoCHIP(SimulacionProcesoCHIPTO simulacionProcesoCHIP)
        throws wcorp.util.GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#generarInstanciaProcesoCHIP(DatosInstanciaProcesoCHIPTO)
     */
    public void generarInstanciaProcesoCHIP(DatosInstanciaProcesoCHIPTO datosInstanciaProcesoCHIP)
        throws GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#poseeProcesoCHIPVigente(long)
     */
    public boolean poseeProcesoCHIPVigente(long rut) throws GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#registrarInicioProcesoCHIP(long)
     */
    public void registrarInicioProcesoCHIP(long rut) throws GeneralException, RemoteException;

    /**
     * @see ServiciosCreditoHipotecarioBean#marcarSimulacionProcesoCHIP(long)
     */
    public void marcarSimulacionProcesoCHIP(long rut) throws GeneralException, RemoteException;

}

