package wcorp.bprocess.multilinea.to;

import java.io.Serializable;
import java.util.Date;

import wcorp.serv.creditosglobales.CalendarioPago;

/**
 * Clase que contiene los datos de respuesta para operaciones.
 * <p>
 * Registro de versiones:
 * <ul>
 * <li> 1.0 19/01/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI)  : Versión Inicial.</li>
 * <li> 1.1 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agregan atributo de excepciones. </li>
 * <li> 1.2 20/07/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega atributo valorGastoNotarial. </li>
 *                                                       
 * </ul>
 * <p>
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 * <p>
 */
public class DatosResultadoOperacionesTO implements Serializable{

	/**
	 * Número de versión utilizado en la serialización.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Numero de operacion.
	 */	
	private String numOperacion;

	/**
	 * Flag de respuesta.
	 */	
	private boolean respuesta;
	
	/**
	 * Factor cae.
	 */
	private double factorCae;
    
	 /**
     * valor de la cuota.
     */
    private double valorCuota;
    
    /**
     * Tasa de interés internet.
     */
    private double tasaInteresInternet;

	/**
     * Impuestos.
     */
    private double impuestos;
    
    /**
     * Intereses.
     */
    private double intereses;
    
    /**
     * Monto final del crédito.
     */
    private double montoFinalCredito;
    
    /**Costo total del crédito.
     * 
     */
    private double costoFinalCredito;

	/**
	 * Costo total de seguros.
	 */	
    private double costoTotalSeguros;

	/**
	 * Numero de seguros.
	 */	
	private int numSeguros;
    
	/**
	 * Numero segurosa obligatorios.
	 */	
    private int numSegurosObl;
    
	/**
	 * Numero seguros seleccionados.
	 */	
   	private int numSegurosSel;
   	
	/**
	 * Condiciones de garantia.
	 */	
   	private String condicionGarantia;
   	
	/**
	 * Cai operacion.
	 */		
   	private String caiOperacion;
   	
	/**
	 * Iic operacion.
	 */	
   	private int iicOperacion;

	/**
	 * Requiere DPS.
	 */	
	private int requiereDPS;
	
	/**
	 * Codigo de moneda.
	 */	
	private String codigoMoneda;
    
	/**
	 * Fecha de curse.
	 */	
	private Date fechaCurse;
	 
	/**
	 * Valor gasto notarial.
	 */
	private double valorGastoNotarial;
	/**
     * Datos de los seguros. 
     */
    private DatosSegurosTO[] datosSeguros;
    
	/**
	 * Calendario de pago.
	 */	
    private CalendarioPago[] calendario;
  
	/**
	 * Datos de avales.
	 */	
    private DatosAvalesTO[] avales;
    
    /**
	 * Datos de avales para correos.
	 */	
    private DatosAvalesTO[] avalesCorreo;
    
    /**
	 * Arreglo con errores.
	 */ 
	private String[] excepciones;

	public String[] getExcepciones() {
		return excepciones;
	}

	public void setExcepciones(String[] excepciones) {
		this.excepciones = excepciones;
	}

	public Date getFechaCurse() {
		return fechaCurse;
	}

	public void setFechaCurse(Date fechaCurse) {
		this.fechaCurse = fechaCurse;
	}

	public double getFactorCae() {
		return factorCae;
	}

	public void setFactorCae(double factorCae) {
		this.factorCae = factorCae;
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

	public double getCostoTotalSeguros() {
		return costoTotalSeguros;
	}

	public void setCostoTotalSeguros(double costoTotalSeguros) {
		this.costoTotalSeguros = costoTotalSeguros;
	}

	public DatosSegurosTO[] getDatosSeguros() {
		return datosSeguros;
	}

	public void setDatosSeguros(DatosSegurosTO[] datosSeguros) {
		this.datosSeguros = datosSeguros;
	}

	public int getNumSeguros() {
		return numSeguros;
	}

	public void setNumSeguros(int numSeguros) {
		this.numSeguros = numSeguros;
	}

	public int getNumSegurosObl() {
		return numSegurosObl;
	}

	public void setNumSegurosObl(int numSegurosObl) {
		this.numSegurosObl = numSegurosObl;
	}

	public int getNumSegurosSel() {
		return numSegurosSel;
	}

	public void setNumSegurosSel(int numSegurosSel) {
		this.numSegurosSel = numSegurosSel;
	}

	public String getCondicionGarantia() {
		return condicionGarantia;
	}

	public void setCondicionGarantia(String condicionGarantia) {
		this.condicionGarantia = condicionGarantia;
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

	public int getRequiereDPS() {
		return requiereDPS;
	}

	public void setRequiereDPS(int requiereDPS) {
		this.requiereDPS = requiereDPS;
	}

	public String getCodigoMoneda() {
		return codigoMoneda;
	}

	public void setCodigoMoneda(String codigoMoneda) {
		this.codigoMoneda = codigoMoneda;
	}

	public String getNumOperacion() {
		return numOperacion;
	}

	public void setNumOperacion(String numOperacion) {
		this.numOperacion = numOperacion;
	}

	public boolean isRespuesta() {
		return respuesta;
	}

	public void setRespuesta(boolean respuesta) {
		this.respuesta = respuesta;
	}
	
	public CalendarioPago[] getCalendario() {
		return calendario;
	}

	public void setCalendario(CalendarioPago[] calendario) {
		this.calendario = calendario;
	}

	public double getIntereses() {
		return intereses;
	}

	public double getValorGastoNotarial() {
		return valorGastoNotarial;
	}

	public void setValorGastoNotarial(double valorGastoNotarial) {
		this.valorGastoNotarial = valorGastoNotarial;
	}

	public void setIntereses(double intereses) {
		this.intereses = intereses;
	}

	public DatosAvalesTO[] getAvales() {
		return avales;
	}

	public void setAvales(DatosAvalesTO[] avales) {
		this.avales = avales;
	}
	
	
	
    public DatosAvalesTO[] getAvalesCorreo() {
		return avalesCorreo;
	}

	public void setAvalesCorreo(DatosAvalesTO[] avalesCorreo) {
		this.avalesCorreo = avalesCorreo;
	}

	/**
     * Retorna buffer con informacion de la clase.
     *
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0  10/06/2015 Manuel Escarate. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión inicial. </li>
     * <li> 1.1 20/07/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agrega atributo valorGastoNotarial a la salida. </li>
     * </ul>
     * </p>
     * 
     * @return string string de buffer.
     * @since 1.0
     */	
	public String toString() {
	        StringBuffer sb = new StringBuffer();
	        sb.append("DatosResultadoOperacionesTO: numOperacion[").append(numOperacion);
	        sb.append("], respuesta[").append(respuesta);
	        sb.append("], factorCae[").append(factorCae);
	        sb.append("], valorCuota[").append(valorCuota);
	        sb.append("], tasaInteresInternet[").append(tasaInteresInternet);
	        sb.append("], impuestos[").append(impuestos);
	        sb.append("], intereses[").append(intereses);
	        sb.append("], montoFinalCredito[").append(montoFinalCredito);
	        sb.append("], costoFinalCredito[").append(costoFinalCredito);
	        sb.append("], costoTotalSeguros[").append(costoTotalSeguros);
	        sb.append("], numSeguros[").append(numSeguros);
	        sb.append("], numSegurosObl[").append(numSegurosObl);
	        sb.append("], numSegurosSel[").append(numSegurosSel);
	        sb.append("], condicionGarantia[").append(condicionGarantia);
	        sb.append("], caiOperacion[").append(caiOperacion);
	        sb.append("], iicOperacion[").append(iicOperacion);
	        sb.append("], requiereDPS[").append(requiereDPS);
	        sb.append("], codigoMoneda[").append(codigoMoneda);
	        sb.append("], fechaCurse[").append(fechaCurse);
	        sb.append("], valorGastoNotarial[").append(valorGastoNotarial);
	        sb.append("], datosSeguros[").append(datosSeguros);
	        sb.append("], calendario[").append(calendario);
	        sb.append("], avales[").append(avales);
	        sb.append("], avalesCorreo[").append(avalesCorreo);
	        sb.append("]");
	        return sb.toString();
	    }	
	
}
