package wcorp.serv.hipotecario.dao.jdbc;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to.SimulacionProcesoCHIPTO;
import wcorp.util.GeneralException;
import wcorp.util.NumerosUtil;
import wcorp.util.SistemaException;
import wcorp.util.StringUtil;
import wcorp.util.TablaValores;
import ws.bci.productos.colocaciones.solicitudes.AutomatizacionCHIPBeanService.wscliente.AutomatizacionCHIPBean;
import ws.bci.productos.colocaciones.solicitudes.AutomatizacionCHIPBeanService.wscliente.AutomatizacionCHIPBeanService;
import ws.bci.productos.colocaciones.solicitudes.AutomatizacionCHIPBeanService.wscliente.AutomatizacionCHIPBeanService_Impl;
import ws.bci.productos.colocaciones.solicitudes.AutomatizacionCHIPBeanService.wscliente.AutomatizacionCHIPBean_Stub;
import cl.bci.chip.integracion.svc.DatosSolicitudCreditoTO;
import cl.bci.middleware.aplicacion.persistencia.ConectorServicioDePersistenciaDeDatos;
import cl.bci.middleware.aplicacion.persistencia.ejb.ConfiguracionException;
import cl.bci.middleware.aplicacion.persistencia.ejb.EjecutarException;
import cl.bci.middleware.aplicacion.persistencia.ejb.ServicioDatosException;

/**
 * Clase que define los metodos de acceso para Sybase y WS del proceso CHIP.
 * <p>
 * Registro de versiones:
 * <ul>
 * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial.
 * </ul>
 * <p>
 * </p>
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 * <P>
 */
public class ProcesoCHIPDAO {

    /**
     * Atributo estático que define CHAINED_SUPPORT utilizado por el servicio persistencia.
     */
    protected static final String CHAINED_SUPPORT = "CHAINED_SUPPORT";

    /**
     * Base de datos en que almacenaran las simulaciones CHIP.
     */
    private static final String BASE_DATOS_SIMULACION_CHIP = "procesos_chip";

    /**
     * Archivo de parámetros para hipotecario.
     */
    private static final String ARCHIVO_PARAMETROS = "hipotecarios.parametros";
    
    /**
     * Pack de seguros individuales básico.
     */
    private static final String PACK_SEG_INDIV_BASICO = "Básico";

    /**
     * Pack de seguros individuales Plus.
     */
    private static final String PACK_SEG_INDIV_PLUS = "Plus";

    /**
     * Pack de seguros individuales Full.
     */
    private static final String PACK_SEG_INDIV_FULL = "Full";
    
    /**
     * Código para almacenar el valor SI en los atributos que lo requieren.
     */
    private static final String COD_RESP_SI = "S";

    /**
     * Código para almacenar el valor NO en los atributos que lo requieren.
     */
    private static final String COD_RESP_NO = "N";

    /**
     * Logging utilzado por la clase.
     */
    private transient Logger logger = (Logger) Logger.getLogger(ProcesoCHIPDAO.class);

    /**
     * Metodo para almacenar el resultado de una simulacion.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * <p>
     * 
     * @param simulacion TO con los datos obtenidos en una simulacion de credito hipotecario.
     * @throws GeneralException En caso de errores aplicativos
     *             
     * @since 1.0
     */
    public void guardarSimulacionProcesoCHIP(SimulacionProcesoCHIPTO simulacion) throws GeneralException {

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[guardarSimulacionProcesoCHIP] [" + simulacion.toString() + "] [BCI_INI]: DAO.");
        }
        HashMap parametros = new HashMap();
        if (simulacion != null) {
            parametros.put("sch_rut_cli", new Long(simulacion.getRutCliente()));
            parametros.put("sch_dv_cli", String.valueOf(simulacion.getDvCliente()));
            parametros.put("sch_nom_cli", simulacion.getNombreCliente());
            parametros.put("sch_ape_paterno", simulacion.getApellidoPaterno());
            parametros.put("sch_ape_materno", simulacion.getApellidoMaterno());
            parametros.put("sch_fec_nac", simulacion.getFechaNacimientoCliente());
            parametros.put("sch_pct_seg_desg", new Double(simulacion.getPorcentajeSegDesgravamenCliente()));
            parametros.put("sch_ind_codeudor", (simulacion.isCodeudor() ? COD_RESP_SI : COD_RESP_NO));
            parametros.put("sch_fec_nac_codeudor", simulacion.getFechaNacimientoCodeudor());
            parametros.put("sch_pct_seg_desg_codeudor", 
                new Double(simulacion.getPorcentajeSegDesgravamenCodeudor()));
            parametros.put("sch_ind_cliente_bci", (simulacion.isIndCliente() ? COD_RESP_SI : COD_RESP_NO));
            parametros.put("sch_des_email", simulacion.getEmail());
            parametros.put("sch_num_telefono", new Integer(simulacion.getFono()));
            parametros.put("sch_mto_renta", new Double(simulacion.getRentaLiquida()));

            parametros.put("sch_gls_canal_venta", simulacion.getGlosaCanalCredito());
            parametros.put("sch_cod_canal_venta", simulacion.getCodCanalCredito());
            parametros.put("sch_fec_simulacion", simulacion.getFechaSimulacion());
            parametros.put("sch_mto_valor_uf", new Double(simulacion.getValorUF()));
            parametros.put("sch_num_result_filtros", new Integer(simulacion.getResultadoFiltros()));
            parametros.put("sch_ind_contactar_cli", (simulacion.isContactar() ? COD_RESP_SI : COD_RESP_NO));
            parametros.put("sch_gls_inmobiliaria", simulacion.getGlosaInmobiliaria());
            parametros.put("sch_cod_inmobiliaria", simulacion.getCodInmobiliaria());

            parametros.put("sch_ind_dfl2", (simulacion.isDfl2() ? COD_RESP_SI : COD_RESP_NO));
            parametros.put("sch_gls_tipo_vivienda", simulacion.getGlosaTipoVivienda());
            parametros.put("sch_cod_tipo_vivienda", simulacion.getCodTipoVivienda());
            parametros.put("sch_ind_material_adobe", (simulacion.isMaterialAdobe() ? COD_RESP_SI : COD_RESP_NO));
            parametros.put("sch_gls_antiguedad_vivienda", simulacion.getGlosaAntiguedadVivienda());
            parametros.put("sch_cod_antiguedad_vivienda", simulacion.getCodAntiguedadVivienda());
            parametros.put("sch_gls_seg_inc_sis_opc", simulacion.getGlosaSeguroIncendioSismoOpcional());
            parametros.put("sch_cod_seg_inc_sis_opc", new Integer(
                simulacion.getCodSeguroIncendioSismoOpcional()));
            parametros.put("sch_gls_region", simulacion.getGlosaRegion());
            parametros.put("sch_cod_region", new Integer(simulacion.getCodRegion()));
            parametros.put("sch_gls_ciudad", simulacion.getGlosaCiudad());
            parametros.put("sch_cod_ciudad", new Integer(simulacion.getCodCiudad()));
            parametros.put("sch_gls_comuna", simulacion.getGlosaComuna());
            parametros.put("sch_cod_comuna", new Integer(simulacion.getCodComuna()));

            parametros.put("sch_mto_precio_vivienda_uf", new Double(simulacion.getPrecioViviendaUF()));
            parametros.put("sch_mto_subsidio_uf", new Double(simulacion.getSubsidioUF()));
            parametros.put("sch_mto_cuota_contado_uf", new Double(simulacion.getCuotaContadoUF()));
            parametros.put("sch_mto_credito_uf", new Double(simulacion.getCreditoUF()));
            parametros.put("sch_pct_financiamiento", new Double(simulacion.getPorcentajeFinanciamiento()));
            parametros.put("sch_num_plazo", new Integer(simulacion.getPlazo()));
            parametros.put("sch_gls_producto", simulacion.getGlosaProducto());
            parametros.put("sch_cod_producto", new Integer(simulacion.getCodProducto()));
            parametros.put("sch_gls_tipo_moneda", simulacion.getGlosaTipoMoneda());
            parametros.put("sch_cod_tipo_moneda", new Integer(simulacion.getCodTipoMoneda()));
            parametros.put("sch_num_annos_tasa_fija", new Integer(simulacion.getAnnosTasaFija()));
            parametros.put("sch_gls_tipo_financiamiento", simulacion.getGlosaTipoFinanciamiento());
            parametros.put("sch_cod_tipo_financiamiento", new Integer(simulacion.getCodTipoFinanciamiento()));
            parametros.put("sch_ind_pac", (simulacion.isSuscribePAC() ? COD_RESP_SI : COD_RESP_NO));
            parametros.put("sch_gls_destino", simulacion.getGlosaDestino());
            parametros.put("sch_cod_destino", new Integer(simulacion.getCodDestino()));
            parametros.put("sch_num_mes_gracia", new Integer(simulacion.getMesesGracia()));
            parametros.put("sch_num_dia_venc", new Integer(simulacion.getDiaVencimiento()));
            parametros.put("sch_gls_mes_exclusion", simulacion.getGlosaMesExclusion());
            parametros.put("sch_cod_mes_exclusion", new Integer(simulacion.getCodMesExclusion()));
            parametros.put("sch_pct_tasa", new Double(simulacion.getTasaCredito()));
            parametros.put("sch_mto_col_tra1_div_neto_uf", new Double(NumerosUtil.redondearADosDecimales(
                simulacion.getSimulacionPlazoColectivo().getDividendo())));
            parametros.put("sch_mto_col_tra1_div_total_uf",new Double(NumerosUtil.redondearADosDecimales(
                simulacion.getSimulacionPlazoColectivo().getDividendoTotal())));
            parametros.put("sch_mto_col_tra1_div_total", new Double(simulacion.getSimulacionPlazoColectivo()
                .getDividendoTotalPesos()));

            parametros.put("sch_mto_col_tra2_div_neto_uf", new Double(NumerosUtil.redondearADosDecimales(
                simulacion.getSimulacionPlazoPagaLaMitadColectivo().getDividendo())));
            parametros.put("sch_mto_col_tra2_div_total_uf",new Double(NumerosUtil.redondearADosDecimales(
                simulacion.getSimulacionPlazoPagaLaMitadColectivo().getDividendoTotal())));
            parametros.put("sch_mto_col_tra2_div_total", new Double(
                simulacion.getSimulacionPlazoPagaLaMitadColectivo().getDividendoTotalPesos()));

            parametros.put("sch_mto_ind_tra1_div_neto_uf", new Double(NumerosUtil.redondearADosDecimales(
                simulacion.getSimulacionPlazoIndividual().getDividendo())));
            parametros.put("sch_mto_ind_tra1_div_total_uf", new Double(NumerosUtil.redondearADosDecimales(
                simulacion.getSimulacionPlazoIndividual().getDividendoTotalConIndividuales())));
            parametros.put("sch_mto_ind_tra1_div_total", new Double(NumerosUtil.redondearADosDecimales(
                simulacion.getSimulacionPlazoIndividual().getDividendoTotalPesosConIndividuales())));

            parametros.put("sch_mto_ind_tra2_div_neto_uf", new Double(NumerosUtil.redondearADosDecimales(
                simulacion.getSimulacionPlazoPagaLaMitadIndividual().getDividendo())));
            parametros.put("sch_mto_ind_tra2_div_total_uf", new Double(NumerosUtil.redondearADosDecimales(
                simulacion.getSimulacionPlazoPagaLaMitadIndividual().getDividendoTotalConIndividuales())));
            parametros.put("sch_mto_ind_tra2_div_total", new Double(simulacion
                .getSimulacionPlazoPagaLaMitadIndividual().getDividendoTotalPesosConIndividuales()));

            parametros.put("sch_pct_cae_col", new Double(simulacion.getTasaCAEColectivo()));
            parametros.put("sch_mto_ctc_col", new Double(simulacion.getCostoTotalCreditoColectivo()));
            parametros.put("sch_pct_cae_ind", new Double(simulacion.getTasaCAEIndividual()));
            parametros.put("sch_mto_ctc_ind", new Double(simulacion.getCostoTotalCreditoIndividual()));

            double segIncendioSismo = 0;
            double segDesgravamen = 0;

            if (simulacion.getGlosaSeguroDesgravamenIndividual().equals(PACK_SEG_INDIV_BASICO)) {
                segDesgravamen = simulacion.getSimulacionPlazoIndividual().getPrimaSeguroDesgravamenBasico();
            }
            if (simulacion.getGlosaSeguroDesgravamenIndividual().equals(PACK_SEG_INDIV_PLUS)) {
                segDesgravamen = simulacion.getSimulacionPlazoIndividual().getPrimaSeguroDesgravamenPlus();
            }
            if (simulacion.getGlosaSeguroDesgravamenIndividual().equals(PACK_SEG_INDIV_FULL)) {
                segDesgravamen = simulacion.getSimulacionPlazoIndividual().getPrimaSeguroDesgravamenFull();
            }
            if (simulacion.getGlosaSeguroIncendioSismoIndividual().equals(PACK_SEG_INDIV_BASICO)) {
                segIncendioSismo = simulacion.getSimulacionPlazoIndividual().getPrimaOfertaMinima();
            }
            if (simulacion.getGlosaSeguroIncendioSismoIndividual().equals(PACK_SEG_INDIV_FULL)) {
                segIncendioSismo = simulacion.getSimulacionPlazoIndividual().getPrimaOfertaMaxima();
            }
            parametros.put("sch_mto_col_seg_inc_sis", new Double(NumerosUtil.redondearADosDecimales(
                simulacion.getSimulacionPlazoColectivo().getMontoSegIncSis())));
            parametros.put("sch_mto_col_seg_desg", new Double(NumerosUtil.redondearADosDecimales(
                simulacion.getSimulacionPlazoColectivo().getMontoSegDesg())));
            parametros.put("sch_mto_ind_seg_inc_sis", new Double(NumerosUtil.redondearADosDecimales(
                segIncendioSismo)));
            parametros.put("sch_mto_ind_seg_desg", new Double(
                NumerosUtil.redondearADosDecimales(segDesgravamen)));
            parametros.put("sch_pct_costo_fondo", new Double(simulacion.getTasaCostoFondo()));
            parametros.put("sch_pct_spread", new Double(simulacion.getTasaSpread()));

            parametros.put("sch_ind_seg_ces_invol", (
                simulacion.isSeguroCesantiaInv() ? COD_RESP_SI : COD_RESP_NO));
            parametros.put("sch_ind_seg_ces_ds01", (
                simulacion.isSeguroCesantiaDS01() ? COD_RESP_SI : COD_RESP_NO));
            parametros.put("sch_ind_seg_ces_ds40", (
                simulacion.isSeguroCesantiaDS40() ? COD_RESP_SI : COD_RESP_NO));
            parametros.put("sch_ind_seg_ces_doble_prot",
                (simulacion.isSeguroCesantiaDobleProteccion() ? COD_RESP_SI : COD_RESP_NO));
            parametros.put("sch_mto_tot_prima_seg_sel_uf",
                new Double(simulacion.getTotalPrimaSegurosEscogidosUF()));
            parametros.put("sch_mto_tot_prima_seg_sel",
                new Double(simulacion.getTotalPrimaSegurosEscogidosPesos()));
            parametros.put("sch_gls_seg_desg_indiv", simulacion.getGlosaSeguroDesgravamenIndividual());
            parametros.put("sch_cod_seg_desg_indiv", new Integer(simulacion.getCodSeguroDesgravamenIndividual()));
            parametros.put("sch_gls_seg_adicional", simulacion.getGlosaSeguroAdicional());
            parametros.put("sch_cod_seg_adicional", new Integer(simulacion.getCodSeguroAdicional()));
            parametros.put("sch_gls_seg_inc_sis_indiv", simulacion.getGlosaSeguroIncendioSismoIndividual());
            parametros.put("sch_cod_seg_inc_sis_indiv", new Integer(
                simulacion.getCodSeguroIncendioSismoIndividual()));

            parametros.put("sch_mto_tasacion", new Double(
                simulacion.getGastosOperacionalesPesos().getTasacion()));
            parametros.put("sch_mto_estudio_titulos", new Double(
                simulacion.getGastosOperacionalesPesos().getEstudioTitulos()));
            parametros.put("sch_mto_borrador_escritura", new Double(
                simulacion.getGastosOperacionalesPesos().getBorradorEscritura()));
            parametros.put("sch_mto_notaria", new Double(simulacion.getGastosOperacionalesPesos()
                .getNotariales()));
            parametros.put("sch_mto_impuesto_uf", new Double(NumerosUtil.redondearADosDecimales(
                simulacion.getGastosOperacionalesUF().getImptoAlMutuo())));
            parametros.put("sch_mto_cbr_uf", new Double(NumerosUtil.redondearADosDecimales(
                simulacion.getGastosOperacionalesUF().getInscripcionConservador())));
            parametros.put("sch_mto_gestoria", new Double(
                simulacion.getGastosOperacionalesPesos().getGestoria()));
            parametros.put("sch_mto_total_gastos_ope", new Double(simulacion.getTotalGastosOperacionalesPesos()));
            parametros.put("sch_cod_ejecutivo", simulacion.getCodEjecutivo());
            parametros.put("sch_gls_canal_cred", simulacion.getGlosaCanalVenta());
            parametros.put("sch_cod_canal_cred", simulacion.getCodCanalVenta());
            parametros.put("sch_gls_ofic_eje", simulacion.getGlosaOficinaEje());
            parametros.put("sch_cod_ofic_eje", simulacion.getCodOficinaEje());
            parametros.put("sch_origen_simulacion", simulacion.getOrigenSimulacion());
            
            parametros.put("sch_cod_proyecto", simulacion.getCodigoProyecto());
            parametros.put("sch_gls_proyecto", simulacion.getGlosaProyecto());
            parametros.put("sch_cod_convenio", simulacion.getCodigoConvenio());

            if (getLogger().isEnabledFor(Level.INFO)) {
                getLogger().info(
                    "[guardarSimulacionProcesoCHIP] [parametros]: " + StringUtil.contenidoDe(parametros));
            }
            try {
                this.ejecutar(this.obtenerConector(), parametros, BASE_DATOS_SIMULACION_CHIP,
                    "guardarSimulacionProcesoCHIP");
            }
            catch (SistemaException ge) {
                if (getLogger().isEnabledFor(Level.ERROR)) {
                    getLogger().error(
                        "[guardarSimulacionProcesoCHIP] [BCI_FINEX] " + "DAO: Error al almacenar la simulacion: ",
                        ge);
                }
                throw new GeneralException("SIMCHIP002", ge.getMessage());
            }
        }

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[guardarSimulacionProcesoCHIP] [BCI_FINOK]: DAO.");
        }

    }

    /**
     * Obtiene el conector para acceder a la base de datos. 
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): Version inicial.
     * </ul>
     * <p>
     * 
     * @return Objeto conector del servicio de persistencia de datos
     * @since 1.0
     */
    private ConectorServicioDePersistenciaDeDatos obtenerConector() {
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtenerConector] [BCI_INI]: DAO.");
        }
        ConectorServicioDePersistenciaDeDatos conector = null;
        String jndi = TablaValores.getValor("JNDIConfig.parametros", "cluster", "param");

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtenerConector] contexto servicio de persistencia: " + jndi);
        }
        try {
            conector = new ConectorServicioDePersistenciaDeDatos(jndi);
            if (getLogger().isEnabledFor(Level.INFO)) {
                getLogger().info("Despues de obtener conector persistencia de datos");
            }
        }
        catch (Exception ex) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error("[obtenerConector] [BCI_FINEX] DAO: Error al obtener el conector.", ex);
            }
        }
        if (conector == null) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error("[obtenerConector] [BCI_FINEX]: DAO. Conector creado es null");
            }
        }
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[obtenerConector] [BCI_FINOK]: DAO.");
        }
        return conector;
    }

    /**
     * Ejecuta el servicio especificado utilizando el conector y los parametros dados. Utilizado para invocar
     * servicios que realizan operaciones insert o update.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): Version inicial.
     * </ul>
     * <p>
     * 
     * @param conector del servicio de persistencia de datos.
     * @param parametros necesarios para invocar al servicio.
     * @param motor en el que se encuentra el servicio a invocar.
     * @param servicio que se desea invocar.
     * @return el objeto retornado por el servicio de persistencia de datos.
     * @since 1.0
     */
    private Object ejecutar(ConectorServicioDePersistenciaDeDatos conector, HashMap parametros, String motor,
        String servicio) {
        
        try {
            return conector.ejecutar(motor, servicio, parametros);
        }
        catch (EjecutarException ee) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error(
                    "[ejecutar] [BCI_FINEX] " + "DAO: Error al ejecutar el servicio: ",
                    ee);
            }
            throw new SistemaException("Problemas ejecutando el servicio.", ee);
        }
        catch (ConfiguracionException ce) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error(
                    "[ejecutar] [BCI_FINEX] " + "DAO: Error al configurar el servicio: ",
                    ce);
            }
            throw new SistemaException("Problemas en la configuración del servicio de datos.", ce);
        }
        catch (ServicioDatosException sde) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error(
                    "[ejecutar] [BCI_FINEX] " + "DAO: Error al invocar el servicio: ",
                    sde);
            }
            throw new SistemaException("Problemas invocando al servicio de datos.", sde);
        }
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
     * @param rut Rut del cliente.
     * @return verdadero En caso que el cliente ya tenga generado un proceso CHIP
     * @throws GeneralException Error General.
     * @since 1.0
     */
    public boolean poseeProcesoCHIPVigente(long rut) throws GeneralException {
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[poseeProcesoCHIPVigente] [BCI_INI]: DAO.");
        }
        HashMap parametros = new HashMap();
        boolean procesoVigente = false;
        parametros.put("rut_cli", new Long(rut));
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[poseeProcesoCHIPVigente] [parametros]: " + StringUtil.contenidoDe(parametros));
        }
        
        try {
            List resultados = (List) this.consultar(this.obtenerConector(), parametros,
                BASE_DATOS_SIMULACION_CHIP, "poseeProcesoCHIPVigente");
            if (getLogger().isEnabledFor(Level.INFO)) {
                getLogger().info("[poseeProcesoCHIPVigente] [resultados]: " + StringUtil.contenidoDe(resultados));
            }

            if (resultados != null && resultados.size() > 0) {
                HashMap datos = (HashMap) resultados.get(0);
                if (datos != null && !datos.isEmpty()) {
                    if (datos.get("procesoVigente").equals(COD_RESP_SI)) {
                        procesoVigente = true;
                    }
                }
            }
        }
        catch (SistemaException ge) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger()
                    .error(
                        "[poseeProcesoCHIPVigente] [BCI_FINEX] DAO: Error al verificar si existe un proceso CHIP "
                        + "vigente del cliente: ", ge);
            }
            throw new GeneralException("SIMCHIP005", ge.getMessage());
        }        
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[poseeProcesoCHIPVigente] [BCI_FINOK] [procesoVigente]: " + procesoVigente);
        }
        return procesoVigente;
    }

    /**
     * Ejecuta el servicio especificado, utilizando el conector y los parámetros especificados, y retorna sus
     * resultados como un {@link List}.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): Version inicial.
     * </ul>
     * <p>
     * 
     * @param conector del servicio de persistencia de datos.
     * @param parametros necesarios para invocar al servicio.
     * @param motor en el que se encuentra el servicio a invocar.
     * @param servicio que se desea invocar.
     * @return {@link List} con los resultados obtenidos por la invocación del servicio.
     * @since 1.0
     */
    private List consultar(ConectorServicioDePersistenciaDeDatos conector, HashMap parametros, String motor,
        String servicio) {
        
        try {
            return conector.consultar(motor, servicio, parametros);
        }
        catch (EjecutarException ee) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error(
                    "[ejecutar] [BCI_FINEX] " + "DAO: Error al ejecutar el servicio: ",
                    ee);
            }
            throw new SistemaException("Problemas ejecutando el servicio.", ee);
        }
        catch (ConfiguracionException ce) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error(
                    "[ejecutar] [BCI_FINEX] " + "DAO: Error al configurar el servicio: ",
                    ce);
            }
            throw new SistemaException("Problemas en la configuración del servicio de datos.", ce);
        }
        catch (ServicioDatosException sde) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error(
                    "[ejecutar] [BCI_FINEX] " + "DAO: Error al invocar el servicio: ",
                    sde);
            }
            throw new SistemaException("Problemas invocando al servicio de datos.", sde);
        }

    }

    /**
     * Metodo para registrar la fecha cuando se genero una instancia de proceso CHIP para un cliente.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * <p>
     * 
     * @param rut Rut del cliente.
     * @throws GeneralException Error General.
     * @since 1.0
     */
    public void registrarInicioProcesoCHIP(long rut) throws GeneralException {
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[registrarInicioProcesoCHIP] [BCI_INI]: DAO.");
        }
        HashMap parametros = new HashMap();
        parametros.put("rut_cli", new Long(rut));
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[registrarInicioProcesoCHIP] [parametros]: " + StringUtil.contenidoDe(parametros));
        }
        
        try {
            this.ejecutar(this.obtenerConector(), parametros, BASE_DATOS_SIMULACION_CHIP,
                "registrarInicioProcesoCHIP");
        }
        catch (SistemaException ge) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger()
                    .error(
                        "[registrarInicioProcesoCHIP] [BCI_FINEX] DAO: Error al registrar en BD la fecha de "
                            + "creación de la instancia del proceso CHIP vigente del cliente: ", ge);
            }
            throw new GeneralException("SIMCHIP006", ge.getMessage());
        }       
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[registrarInicioProcesoCHIP] [BCI_FINOK]: DAO.");
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
     * @param rut Rut del cliente.
     * @throws GeneralException Error General.
     * @since 1.0
     */
    public void marcarSimulacionProcesoCHIP(long rut) throws GeneralException {
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[marcarSimulacionProcesoCHIP] [BCI_INI]: DAO.");
        }
        HashMap parametros = new HashMap();
        parametros.put("rut_cli", new Long(rut));
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[marcarSimulacionProcesoCHIP] [parametros]: " + StringUtil.contenidoDe(parametros));
        }
        
        try {
            this.ejecutar(this.obtenerConector(), parametros, BASE_DATOS_SIMULACION_CHIP,
                "marcarSimulacionProcesoCHIP");
        }
        catch (SistemaException ge) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger()
                    .error(
                        "[marcarSimulacionProcesoCHIP] [BCI_FINEX] DAO: Error al marcar la simulación para que "
                            + "el cliente sea contactado por un ejecutivo", ge);
            }
            throw new GeneralException("SIMCHIP003", ge.getMessage());
        }        
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[marcarSimulacionProcesoCHIP] [BCI_FINOK]: DAO.");
        }
    }

    /**
     * Metodo para obtener la cantidad de simulaciones que ha realizado el cliente.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * <p>
     * 
     * @param rut Rut del cliente.
     * @return cantidad de simulaciones.
     * @throws GeneralException Error General.
     * @since 1.0
     */
    public int consultarTotalSimulacionesCHIP(long rut) throws GeneralException {
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[consultarTotalSimulacionesCHIP] [BCI_INI]: DAO.");
        }
        HashMap parametros = new HashMap();
        int cantidadSimulaciones = 0;
        parametros.put("rut_cli", new Long(rut));
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[consultarTotalSimulacionesCHIP] [parametros]: " + StringUtil.contenidoDe(parametros));
        }
        
        try {
            List resultados = (List) this.consultar(this.obtenerConector(), parametros,
                BASE_DATOS_SIMULACION_CHIP, "consultaTotalSimulacionesCHIP");

            if (getLogger().isEnabledFor(Level.INFO)) {
                getLogger().info(
                    "[consultarTotalSimulacionesCHIP] [resultados]: " + StringUtil.contenidoDe(resultados));
            }
            if (resultados != null && resultados.size() > 0) {
                HashMap datos = (HashMap) resultados.get(0);
                if (datos != null && !datos.isEmpty()) {
                    cantidadSimulaciones = Integer.parseInt(String.valueOf(datos.get("totalSimulaciones")));
                }
            }
        }
        catch (SistemaException ge) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger()
                    .error(
                        "[consultarTotalSimulacionesCHIP] [BCI_FINEX] DAO: Error al consultar el total de "
                            + "simulaciones realizadas por el cliente", ge);
            }
            throw new GeneralException("SIMCHIP007", ge.getMessage());
        }
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info(
                "[consultarTotalSimulacionesCHIP] [BCI_FINOK]: [cantidadSimulaciones]: " + cantidadSimulaciones);
        }
        return cantidadSimulaciones;
    }

    /**
     * Metodo que envia los datos de una simulacion de credito al web service.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * <p>
     * 
     * @param datosSolicitudCredito Datos requeridos por el web service
     * @throws GeneralException Error de Sistema.
     * @since 1.0
     */
    public void generarInstanciaProcesoCHIP(DatosSolicitudCreditoTO datosSolicitudCredito) 
        throws GeneralException {
        
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[generarInstanciaProcesoCHIP] [BCI_INI] [" 
                + datosSolicitudCredito.toString() + "]");
        }
        String wsdl = TablaValores.getValor(ARCHIVO_PARAMETROS, "URL.INSTANCIA_CHIP", "url");
        long respuesta = 0;
        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[generarInstanciaProcesoCHIP] [wsdl]: " + wsdl);
        }
        try {
            AutomatizacionCHIPBeanService wsCHIP = new AutomatizacionCHIPBeanService_Impl(wsdl);
            AutomatizacionCHIPBean puerto = wsCHIP.getAutomatizacionCHIPBean();
            AutomatizacionCHIPBean_Stub stubCHIP = (AutomatizacionCHIPBean_Stub) puerto;
            respuesta = stubCHIP.iniciarProcesoCHIP(datosSolicitudCredito);
        }
        catch (Exception e) {
            if (getLogger().isEnabledFor(Level.ERROR)) {
                getLogger().error(
                    "[generarInstanciaProcesoCHIP] [BCI_FINEX] "
                        + "DAO: Error de comunicacion con el web service AutomatizacionCHIPBeanPS: ", e);
            }
            throw new GeneralException("SIMCHIP001", e.getMessage());
        }

        if (getLogger().isEnabledFor(Level.INFO)) {
            getLogger().info("[generarInstanciaProcesoCHIP][BCI_FINOK][respuesta]: " + respuesta);
        }
    }

    /**
     * Obtiene el logger de la clase.
     * <p>
     * Registro de versiones:
     * <ul>
     * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
     * </ul>
     * <p>
     * 
     * @return logger logeo de la aplicacion.
     * @since 1.0
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass());
        }
        return logger;
    }

}
