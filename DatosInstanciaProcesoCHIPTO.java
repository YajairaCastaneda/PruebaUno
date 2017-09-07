package wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to;

import java.io.Serializable;
import java.util.Date;

/**
 * Objeto que contiene la informacion de los parametros de la simulacion que
 * seran enviados al proceso CHIP.
 * <p>
 * Registro de versiones:
 * <ul>
 * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial.</li>
 * </ul>
 * <B>Todos los derechos reservados por Banco de Crédito e Inversiones.</B>
 * </p>
 */
public class DatosInstanciaProcesoCHIPTO implements Serializable {

    /**
     * Numero de version para serializacion.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Atributo que representa el rut del cliente.
     */
    private long rutCliente;

    /**
     * Atributo que representa digito verificador del cliente.
     */
    private char dvCliente;

    /**
     * Atributo que representa los nombres del cliente.
     */
    private String nombreCliente;

    /**
     * Atributo que representa el apellido paterno del cliente.
     */
    private String apellidoPaterno;

    /**
     * Atributo que representa el apellido materno del cliente.
     */
    private String apellidoMaterno;

    /**
     * Atributo que representa si existe un codeudor. True existe codeudor.
     */
    private boolean codeudor;

    /**
     * Atributo que representa el indicador cliente. True es cliente
     */
    private boolean indCliente;

    /**
     * Variable que representa el rut mail del cliente.
     */
    private String email;

    /**
     * Variable que representa el teléfono del cliente.
     */
    private int fono;

    /**
     * Variable que representa la renta del cliente.
     */
    private double rentaLiquida;

    /**
     * Código del Canal donde se ejecuta la simulación.
     */
    private String codCanalVenta;

    /**
     * Glosa del Canal donde se ejecuta la simulación.
     */
    private String glosaCanalVenta;

    /**
     * Atributo que representa fecha en que se realiza la simulacion.
     */
    private Date fechaSimulacion;

    /**
     * Atributo que representa el total de las simulaciones realizadas por el cliente.
     */
    private int totalSimulaciones;

    /**
     * Atributo que representa el resultado de los filtros.
     */
    private int resultadoFiltros;

    /**
     * Atributo que representa si el cliente desea ser contactado.
     */
    private boolean contactar;

    /**
     * Atributo que representa el nombre de la inmobiliaria.
     */
    private String glosaInmobiliaria;

    /**
     * Atributo que representa el codigo de la inmobiliaria.
     */
    private String codInmobiliaria;

    /**
     * Atributo que representa si la propiedad es dfl2.
     */
    private boolean dfl2;

    /**
     * Atributo que representa la glosa del tipo de vivienda.
     */
    private String glosaTipoVivienda;

    /**
     * Atributo que representa el codigo del tipo de vivienda.
     */
    private String codTipoVivienda;

    /**
     * Atributo que representa la glosa de la antiguedad de la vivienda.
     */
    private String glosaAntiguedadVivienda;

    /**
     * Atributo que representa la glosa de la antiguedad de la vivienda.
     */
    private String codAntiguedadVivienda;

    /**
     * Atributo que representa el tipo de seguro asociado a la vivienda.
     */
    private String glosaSeguroIncendioSismoOpcional;

    /**
     * Atributo que representa el tipo de seguro asociado a la vivienda.
     */
    private int codSeguroIncendioSismoOpcional;

    /**
     * Atributo que representa la glosa de la region.
     */
    private String glosaRegion;

    /**
     * Atributo que representa el codigo de la Region.
     */
    private int codRegion;

    /**
     * Atributo que representa la glosa de la Ciudad.
     */
    private String glosaCiudad;

    /**
     * Atributo que representa el codigo de la Ciudad.
     */
    private int codCiudad;

    /**
     * Atributo que representa la glosa de la Comuna.
     */
    private String glosaComuna;

    /**
     * Atributo que representa el codigo de la Comuna.
     */
    private int codComuna;

    /**
     * Atributo que representa el precio de la vivienda en UF.
     */
    private double precioViviendaUF;

    /**
     * Atributo que representa subsidio en UF.
     */
    private double subsidioUF;

    /**
     * Atributo que representa la cuota al contado en UF.
     */
    private double cuotaContadoUF;

    /**
     * Atributo que representa el credito en UF.
     */
    private double creditoUF;

    /**
     * Atributo que representa el porcentaje de financiamiento.
     */
    private double porcentajeFinanciamiento;

    /**
     * Atributo que representa el plazo.
     */
    private int plazo;

    /**
     * Atributo que representa la glosa del Producto.
     */
    private String glosaProducto;

    /**
     * Atributo que representa el codigo del Producto.
     */
    private int codProducto;

    /**
     * Atributo que representa la cantidad de annos de tasa fija.
     */
    private int annosTasaFija;

    /**
     * Tipo de financiamiento.
     */
    private String glosaTipoFinanciamiento;

    /**
     * Codigo del tipo de financiamiento.
     */
    private int codTipoFinanciamiento;

    /**
     * Atributo que representa si el cliente suscribe PAC.
     */
    private boolean suscribePAC;

    /**
     * Atributo que representa los meses de gracia.
     */
    private int mesesGracia;

    /**
     * Atributo que representa dia de vencimiento.
     */
    private int diaVencimiento;

    /**
     * Atributo que representa el mes de exclusion.
     */
    private String glosaMesExclusion;

    /**
     * Atributo que representa el mes de exclusion.
     */
    private int codMesExclusion;

    /**
     * Atributo que representa tasa del credito.
     */
    private double tasaCredito;

    /**
     * Atributo que representa el detalle del dividendo colectivo .
     */
    private SimulacionPlazoCHIPTO simulacionPlazoColectivo;

    /**
     * Atributo que representa el detalle del dividendo colectivo para el tramo 2.
     */
    private SimulacionPlazoCHIPTO simulacionPlazoPagaLaMitadColectivo;

    /**
     * Atributo que representa la tasa del CAE colectivo.
     */
    private double tasaCAEColectivo;

    /**
     * Atributo que representa el costo total del credito colectivo.
     */
    private double costoTotalCreditoColectivo;

    /**
     * Atributo que representa tasa de costo fondo.
     */
    private double tasaCostoFondo;

    /**
     * Atributo que representa la tasa de spread del plazo de la simulacion.
     */
    private double tasaSpread;

    /**
     * Atributo que representa el tipo de seguro de desgravamen individual elegido.
     */
    private String glosaSeguroDesgravamenIndividual;

    /**
     * Atributo que representa codigo del tipo de seguro de desgravamen individual elegido.
     */
    private String codSeguroDesgravamenIndividual;

    /**
     * Atributo que representa el tipo de seguro de incendio y sismo individual elegido.
     */
    private String glosaSeguroIncendioSismoIndividual;

    /**
     * Atributo que representa codigo del tipo de seguro de incendio y sismo individual elegido.
     */
    private String codSeguroIncendioSismoIndividual;

    /**
     * Atributo que representa la glosa del seguro adicional seleccionado.
     */
    private String glosaSeguroAdicional;

    /**
     * Atributo que representa el codigo del seguro adicional seleccionado.
     */
    private int codSeguroAdicional;

    /**
     * Atributo que representa los gastos operacionales de la simulacion en Pesos.
     */
    private GastosOperacionalesCHIPTO gastosOperacionalesPesos;

    /**
     * Atributo que representa los gastos operacionales de la simulacion en UF.
     */
    private GastosOperacionalesCHIPTO gastosOperacionalesUF;

    /**
     * Atributo que representa el codigo del ejecutivo que realiza la simulacion.
     */
    private String codEjecutivo;

    /**
     * Atributo que representa el código del canal de venta del crédito.
     */
    private String codCanalCredito;

    /**
     * Atributo que representa la glosa del canal de venta del crédito.
     */
    private String glosaCanalCredito;

    /**
     * Atributo que representa el codigo de la oficina.
     */
    private String codOficinaEje;

    /**
     * Atributo que representa el nombre de la oficina.
     */
    private String glosaOficinaEje;

    /**
     * Atributo que representa el codigo del origen de la simulacion.
     */
    private String origenSimulacion;

    /**
     * Atributo que representa el codigo del proyecto inmobiliario.
     */
    private String codigoProyecto;

    /**
     * Atributo que representa la glosa del proyecto inmobiliario.
     */
    private String glosaProyecto;

    /**
     * Atributo que representa el código de convenio exitente entre el banco y la inmobiliaria para un proyecto
     * determinado.
     */
    private String codigoConvenio;

    public long getRutCliente() {
        return rutCliente;
    }

    public char getDvCliente() {
        return dvCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public boolean isCodeudor() {
        return codeudor;
    }

    public boolean isIndCliente() {
        return indCliente;
    }

    public String getEmail() {
        return email;
    }

    public int getFono() {
        return fono;
    }

    public double getRentaLiquida() {
        return rentaLiquida;
    }

    public String getCodCanalVenta() {
        return codCanalVenta;
    }

    public String getGlosaCanalVenta() {
        return glosaCanalVenta;
    }

    public Date getFechaSimulacion() {
        return fechaSimulacion;
    }

    public int getTotalSimulaciones() {
        return totalSimulaciones;
    }

    public int getResultadoFiltros() {
        return resultadoFiltros;
    }

    public boolean isContactar() {
        return contactar;
    }

    public String getGlosaInmobiliaria() {
        return glosaInmobiliaria;
    }

    public String getCodInmobiliaria() {
        return codInmobiliaria;
    }

    public boolean isDfl2() {
        return dfl2;
    }

    public String getGlosaTipoVivienda() {
        return glosaTipoVivienda;
    }

    public String getCodTipoVivienda() {
        return codTipoVivienda;
    }

    public String getGlosaAntiguedadVivienda() {
        return glosaAntiguedadVivienda;
    }

    public String getCodAntiguedadVivienda() {
        return codAntiguedadVivienda;
    }

    public String getGlosaSeguroIncendioSismoOpcional() {
        return glosaSeguroIncendioSismoOpcional;
    }

    public int getCodSeguroIncendioSismoOpcional() {
        return codSeguroIncendioSismoOpcional;
    }

    public String getGlosaRegion() {
        return glosaRegion;
    }

    public int getCodRegion() {
        return codRegion;
    }

    public String getGlosaCiudad() {
        return glosaCiudad;
    }

    public int getCodCiudad() {
        return codCiudad;
    }

    public String getGlosaComuna() {
        return glosaComuna;
    }

    public int getCodComuna() {
        return codComuna;
    }

    public double getPrecioViviendaUF() {
        return precioViviendaUF;
    }

    public double getSubsidioUF() {
        return subsidioUF;
    }

    public double getCuotaContadoUF() {
        return cuotaContadoUF;
    }

    public double getCreditoUF() {
        return creditoUF;
    }

    public double getPorcentajeFinanciamiento() {
        return porcentajeFinanciamiento;
    }

    public int getPlazo() {
        return plazo;
    }

    public String getGlosaProducto() {
        return glosaProducto;
    }

    public int getCodProducto() {
        return codProducto;
    }

    public int getAnnosTasaFija() {
        return annosTasaFija;
    }

    public String getGlosaTipoFinanciamiento() {
        return glosaTipoFinanciamiento;
    }

    public int getCodTipoFinanciamiento() {
        return codTipoFinanciamiento;
    }

    public boolean isSuscribePAC() {
        return suscribePAC;
    }

    public int getMesesGracia() {
        return mesesGracia;
    }

    public int getDiaVencimiento() {
        return diaVencimiento;
    }

    public String getGlosaMesExclusion() {
        return glosaMesExclusion;
    }

    public int getCodMesExclusion() {
        return codMesExclusion;
    }

    public double getTasaCredito() {
        return tasaCredito;
    }

    public SimulacionPlazoCHIPTO getSimulacionPlazoColectivo() {
        return simulacionPlazoColectivo;
    }

    public SimulacionPlazoCHIPTO getSimulacionPlazoPagaLaMitadColectivo() {
        return simulacionPlazoPagaLaMitadColectivo;
    }

    public double getTasaCAEColectivo() {
        return tasaCAEColectivo;
    }

    public double getCostoTotalCreditoColectivo() {
        return costoTotalCreditoColectivo;
    }

    public double getTasaCostoFondo() {
        return tasaCostoFondo;
    }

    public double getTasaSpread() {
        return tasaSpread;
    }

    public String getGlosaSeguroDesgravamenIndividual() {
        return glosaSeguroDesgravamenIndividual;
    }

    public String getCodSeguroDesgravamenIndividual() {
        return codSeguroDesgravamenIndividual;
    }

    public String getGlosaSeguroIncendioSismoIndividual() {
        return glosaSeguroIncendioSismoIndividual;
    }

    public String getCodSeguroIncendioSismoIndividual() {
        return codSeguroIncendioSismoIndividual;
    }

    public String getGlosaSeguroAdicional() {
        return glosaSeguroAdicional;
    }

    public int getCodSeguroAdicional() {
        return codSeguroAdicional;
    }

    public GastosOperacionalesCHIPTO getGastosOperacionalesPesos() {
        return gastosOperacionalesPesos;
    }

    public GastosOperacionalesCHIPTO getGastosOperacionalesUF() {
        return gastosOperacionalesUF;
    }

    public String getCodEjecutivo() {
        return codEjecutivo;
    }

    public String getCodCanalCredito() {
        return codCanalCredito;
    }

    public String getGlosaCanalCredito() {
        return glosaCanalCredito;
    }

    public String getCodOficinaEje() {
        return codOficinaEje;
    }

    public String getGlosaOficinaEje() {
        return glosaOficinaEje;
    }

    public String getOrigenSimulacion() {
        return origenSimulacion;
    }

    public String getCodigoProyecto() {
        return codigoProyecto;
    }

    public String getGlosaProyecto() {
        return glosaProyecto;
    }

    public String getCodigoConvenio() {
        return codigoConvenio;
    }

    public void setRutCliente(long rutCliente) {
        this.rutCliente = rutCliente;
    }

    public void setDvCliente(char dvCliente) {
        this.dvCliente = dvCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public void setCodeudor(boolean codeudor) {
        this.codeudor = codeudor;
    }

    public void setIndCliente(boolean indCliente) {
        this.indCliente = indCliente;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFono(int fono) {
        this.fono = fono;
    }

    public void setRentaLiquida(double rentaLiquida) {
        this.rentaLiquida = rentaLiquida;
    }

    public void setCodCanalVenta(String codCanalVenta) {
        this.codCanalVenta = codCanalVenta;
    }

    public void setGlosaCanalVenta(String glosaCanalVenta) {
        this.glosaCanalVenta = glosaCanalVenta;
    }

    public void setFechaSimulacion(Date fechaSimulacion) {
        this.fechaSimulacion = fechaSimulacion;
    }

    public void setTotalSimulaciones(int totalSimulaciones) {
        this.totalSimulaciones = totalSimulaciones;
    }

    public void setResultadoFiltros(int resultadoFiltros) {
        this.resultadoFiltros = resultadoFiltros;
    }

    public void setContactar(boolean contactar) {
        this.contactar = contactar;
    }

    public void setGlosaInmobiliaria(String glosaInmobiliaria) {
        this.glosaInmobiliaria = glosaInmobiliaria;
    }

    public void setCodInmobiliaria(String codInmobiliaria) {
        this.codInmobiliaria = codInmobiliaria;
    }

    public void setDfl2(boolean dfl2) {
        this.dfl2 = dfl2;
    }

    public void setGlosaTipoVivienda(String glosaTipoVivienda) {
        this.glosaTipoVivienda = glosaTipoVivienda;
    }

    public void setCodTipoVivienda(String codTipoVivienda) {
        this.codTipoVivienda = codTipoVivienda;
    }

    public void setGlosaAntiguedadVivienda(String glosaAntiguedadVivienda) {
        this.glosaAntiguedadVivienda = glosaAntiguedadVivienda;
    }

    public void setCodAntiguedadVivienda(String codAntiguedadVivienda) {
        this.codAntiguedadVivienda = codAntiguedadVivienda;
    }

    public void setGlosaSeguroIncendioSismoOpcional(String glosaSeguroIncendioSismoOpcional) {
        this.glosaSeguroIncendioSismoOpcional = glosaSeguroIncendioSismoOpcional;
    }

    public void setCodSeguroIncendioSismoOpcional(int codSeguroIncendioSismoOpcional) {
        this.codSeguroIncendioSismoOpcional = codSeguroIncendioSismoOpcional;
    }

    public void setGlosaRegion(String glosaRegion) {
        this.glosaRegion = glosaRegion;
    }

    public void setCodRegion(int codRegion) {
        this.codRegion = codRegion;
    }

    public void setGlosaCiudad(String glosaCiudad) {
        this.glosaCiudad = glosaCiudad;
    }

    public void setCodCiudad(int codCiudad) {
        this.codCiudad = codCiudad;
    }

    public void setGlosaComuna(String glosaComuna) {
        this.glosaComuna = glosaComuna;
    }

    public void setCodComuna(int codComuna) {
        this.codComuna = codComuna;
    }

    public void setPrecioViviendaUF(double precioViviendaUF) {
        this.precioViviendaUF = precioViviendaUF;
    }

    public void setSubsidioUF(double subsidioUF) {
        this.subsidioUF = subsidioUF;
    }

    public void setCuotaContadoUF(double cuotaContadoUF) {
        this.cuotaContadoUF = cuotaContadoUF;
    }

    public void setCreditoUF(double creditoUF) {
        this.creditoUF = creditoUF;
    }

    public void setPorcentajeFinanciamiento(double porcentajeFinanciamiento) {
        this.porcentajeFinanciamiento = porcentajeFinanciamiento;
    }

    public void setPlazo(int plazo) {
        this.plazo = plazo;
    }

    public void setGlosaProducto(String glosaProducto) {
        this.glosaProducto = glosaProducto;
    }

    public void setCodProducto(int codProducto) {
        this.codProducto = codProducto;
    }

    public void setAnnosTasaFija(int annosTasaFija) {
        this.annosTasaFija = annosTasaFija;
    }

    public void setGlosaTipoFinanciamiento(String glosaTipoFinanciamiento) {
        this.glosaTipoFinanciamiento = glosaTipoFinanciamiento;
    }

    public void setCodTipoFinanciamiento(int codTipoFinanciamiento) {
        this.codTipoFinanciamiento = codTipoFinanciamiento;
    }

    public void setSuscribePAC(boolean suscribePAC) {
        this.suscribePAC = suscribePAC;
    }

    public void setMesesGracia(int mesesGracia) {
        this.mesesGracia = mesesGracia;
    }

    public void setDiaVencimiento(int diaVencimiento) {
        this.diaVencimiento = diaVencimiento;
    }

    public void setGlosaMesExclusion(String glosaMesExclusion) {
        this.glosaMesExclusion = glosaMesExclusion;
    }

    public void setCodMesExclusion(int codMesExclusion) {
        this.codMesExclusion = codMesExclusion;
    }

    public void setTasaCredito(double tasaCredito) {
        this.tasaCredito = tasaCredito;
    }

    public void setSimulacionPlazoColectivo(SimulacionPlazoCHIPTO simulacionPlazoColectivo) {
        this.simulacionPlazoColectivo = simulacionPlazoColectivo;
    }

    public void setSimulacionPlazoPagaLaMitadColectivo(SimulacionPlazoCHIPTO simulacionPlazoPagaLaMitadColectivo) {
        this.simulacionPlazoPagaLaMitadColectivo = simulacionPlazoPagaLaMitadColectivo;
    }

    public void setTasaCAEColectivo(double tasaCAEColectivo) {
        this.tasaCAEColectivo = tasaCAEColectivo;
    }

    public void setCostoTotalCreditoColectivo(double costoTotalCreditoColectivo) {
        this.costoTotalCreditoColectivo = costoTotalCreditoColectivo;
    }

    public void setTasaCostoFondo(double tasaCostoFondo) {
        this.tasaCostoFondo = tasaCostoFondo;
    }

    public void setTasaSpread(double tasaSpread) {
        this.tasaSpread = tasaSpread;
    }

    public void setGlosaSeguroDesgravamenIndividual(String glosaSeguroDesgravamenIndividual) {
        this.glosaSeguroDesgravamenIndividual = glosaSeguroDesgravamenIndividual;
    }

    public void setCodSeguroDesgravamenIndividual(String codSeguroDesgravamenIndividual) {
        this.codSeguroDesgravamenIndividual = codSeguroDesgravamenIndividual;
    }

    public void setGlosaSeguroIncendioSismoIndividual(String glosaSeguroIncendioSismoIndividual) {
        this.glosaSeguroIncendioSismoIndividual = glosaSeguroIncendioSismoIndividual;
    }

    public void setCodSeguroIncendioSismoIndividual(String codSeguroIncendioSismoIndividual) {
        this.codSeguroIncendioSismoIndividual = codSeguroIncendioSismoIndividual;
    }

    public void setGlosaSeguroAdicional(String glosaSeguroAdicional) {
        this.glosaSeguroAdicional = glosaSeguroAdicional;
    }

    public void setCodSeguroAdicional(int codSeguroAdicional) {
        this.codSeguroAdicional = codSeguroAdicional;
    }

    public void setGastosOperacionalesPesos(GastosOperacionalesCHIPTO gastosOperacionalesPesos) {
        this.gastosOperacionalesPesos = gastosOperacionalesPesos;
    }

    public void setGastosOperacionalesUF(GastosOperacionalesCHIPTO gastosOperacionalesUF) {
        this.gastosOperacionalesUF = gastosOperacionalesUF;
    }

    public void setCodEjecutivo(String codEjecutivo) {
        this.codEjecutivo = codEjecutivo;
    }

    public void setCodCanalCredito(String codCanalCredito) {
        this.codCanalCredito = codCanalCredito;
    }

    public void setGlosaCanalCredito(String glosaCanalCredito) {
        this.glosaCanalCredito = glosaCanalCredito;
    }

    public void setCodOficinaEje(String codOficinaEje) {
        this.codOficinaEje = codOficinaEje;
    }

    public void setGlosaOficinaEje(String glosaOficinaEje) {
        this.glosaOficinaEje = glosaOficinaEje;
    }

    public void setOrigenSimulacion(String origenSimulacion) {
        this.origenSimulacion = origenSimulacion;
    }

    public void setCodigoProyecto(String codigoProyecto) {
        this.codigoProyecto = codigoProyecto;
    }

    public void setGlosaProyecto(String glosaProyecto) {
        this.glosaProyecto = glosaProyecto;
    }

    public void setCodigoConvenio(String codigoConvenio) {
        this.codigoConvenio = codigoConvenio;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("DatosInstanciaProcesoCHIPTO: rutCliente[").append(rutCliente);
        sb.append("], dvCliente[").append(dvCliente);
        sb.append("], nombreCliente[").append(nombreCliente);
        sb.append("], apellidoPaterno[").append(apellidoPaterno);
        sb.append("], apellidoMaterno[").append(apellidoMaterno);
        sb.append("], codeudor[").append(codeudor);
        sb.append("], indCliente[").append(indCliente);
        sb.append("], email[").append(email);
        sb.append("], fono[").append(fono);
        sb.append("], rentaLiquida[").append(rentaLiquida);
        sb.append("], codCanalVenta[").append(codCanalVenta);
        sb.append("], glosaCanalVenta[").append(glosaCanalVenta);
        sb.append("], fechaSimulacion[").append(fechaSimulacion);
        sb.append("], totalSimulaciones[").append(totalSimulaciones);
        sb.append("], resultadoFiltros[").append(resultadoFiltros);
        sb.append("], contactar[").append(contactar);
        sb.append("], codInmobiliaria[").append(codInmobiliaria);
        sb.append("], glosaInmobiliaria[").append(glosaInmobiliaria);
        sb.append("], dfl2[").append(dfl2);
        sb.append("], codTipoVivienda[").append(codTipoVivienda);
        sb.append("], glosaTipoVivienda[").append(glosaTipoVivienda);
        sb.append("], codAntiguedadVivienda[").append(codAntiguedadVivienda);
        sb.append("], glosaAntiguedadVivienda[").append(glosaAntiguedadVivienda);
        sb.append("], codSeguroIncendioSismoOpcional[").append(codSeguroIncendioSismoOpcional);
        sb.append("], glosaSeguroIncendioSismoOpcional[").append(glosaSeguroIncendioSismoOpcional);
        sb.append("], glosaRegion[").append(glosaRegion);
        sb.append("], codRegion[").append(codRegion);
        sb.append("], glosaCiudad[").append(glosaCiudad);
        sb.append("], codCiudad[").append(codCiudad);
        sb.append("], glosaComuna[").append(glosaComuna);
        sb.append("], codComuna[").append(codComuna);
        sb.append("], precioViviendaUF[").append(precioViviendaUF);
        sb.append("], subsidioUF[").append(subsidioUF);
        sb.append("], cuotaContadoUF[").append(cuotaContadoUF);
        sb.append("], creditoUF[").append(creditoUF);
        sb.append("], porcentajeFinanciamiento[").append(porcentajeFinanciamiento);
        sb.append("], plazo[").append(plazo);
        sb.append("], codProducto[").append(codProducto);
        sb.append("], glosaProducto[").append(glosaProducto);
        sb.append("], annosTasaFija[").append(annosTasaFija);
        sb.append("], codTipoFinanciamiento[").append(codTipoFinanciamiento);
        sb.append("], glosaTipoFinanciamiento[").append(glosaTipoFinanciamiento);
        sb.append("], suscribePAC[").append(suscribePAC);
        sb.append("], mesesGracia[").append(mesesGracia);
        sb.append("], diaVencimiento[").append(diaVencimiento);
        sb.append("], codMesExclusion[").append(codMesExclusion);
        sb.append("], glosaMesExclusion[").append(glosaMesExclusion);
        sb.append("], tasaCredito[").append(tasaCredito);
        sb.append("], simulacionPlazoColectivo[").append(simulacionPlazoColectivo);
        sb.append("], simulacionPlazoPagaLaMitadColectivo[").append(simulacionPlazoPagaLaMitadColectivo);
        sb.append("], tasaCAEColectivo[").append(tasaCAEColectivo);
        sb.append("], costoTotalCreditoColectivo[").append(costoTotalCreditoColectivo);
        sb.append("], tasaCostoFondo[").append(tasaCostoFondo);
        sb.append("], tasaSpread[").append(tasaSpread);
        sb.append("], codSeguroDesgravamenIndividual[").append(codSeguroDesgravamenIndividual);
        sb.append("], glosaSeguroDesgravamenIndividual[").append(glosaSeguroDesgravamenIndividual);
        sb.append("], codSeguroIncendioSismoIndividual[").append(codSeguroIncendioSismoIndividual);
        sb.append("], glosaSeguroIncendioSismoIndividual[").append(glosaSeguroIncendioSismoIndividual);
        sb.append("], codSeguroAdicional[").append(codSeguroAdicional);
        sb.append("], glosaSeguroAdicional[").append(glosaSeguroAdicional);
        sb.append("], gastosOperacionalesPesos[").append(gastosOperacionalesPesos);
        sb.append("], gastosOperacionalesUF[").append(gastosOperacionalesUF);
        sb.append("], codEjecutivo[").append(codEjecutivo);
        sb.append("], codCanalCredito[").append(codCanalCredito);
        sb.append("], glosaCanalCredito[").append(glosaCanalCredito);
        sb.append("], codOficinaEje[").append(codOficinaEje);
        sb.append("], glosaOficinaEje[").append(glosaOficinaEje);
        sb.append("], origenSimulacion[").append(origenSimulacion);
        sb.append("], codProyecto[").append(codigoProyecto);
        sb.append("], glosaProyecto[").append(glosaProyecto);
        sb.append("], codConvenio[").append(codigoConvenio);
        sb.append("]");
        return sb.toString();
    }    
}
