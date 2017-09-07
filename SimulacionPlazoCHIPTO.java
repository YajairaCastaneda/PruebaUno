package wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to;

import java.io.Serializable;

/**
 * Clase "Transfer Object" que modela el plazo de una simulacion.
 * <p>
 * Registro de versiones:
 * <ul>
 * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial</li>
 * </ul>
 * </p>
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 */
public class SimulacionPlazoCHIPTO implements Serializable {

	/**
	 * Numero de version para serializacion.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Atributo que representa el plazo de la simulacion.
	 */
	private int plazo;

	/**
	 * Atributo que representa el tramo del plazo de la simulacion.
	 */
	private String tramo;

	/**
	 * Atributo que representa la tasa del plazo de la simulacion.
	 */
	private double tasa;

	/**
	 * Atributo que representa la tasa base del plazo de la simulacion.
	 */
	private double tasaBase;

	/**
	 * Atributo que representa la tasa de desgravamen del plazo de la
	 * simulacion.
	 */
	private double tasaDesgravamen;

	/**
	 * Atributo que representa la tasa de incendio del plazo de la simulacion.
	 */
	private double tasaIncendio;

	/**
	 * Atributo que representa la tasa de oferta maxima del plazo de la
	 * simulacion.
	 */
	private double tasaOfertaMaxima;

	/**
	 * Atributo que representa la tasa de oferta minima del plazo de la
	 * simulacion.
	 */
	private double tasaOfertaMinima;

	/**
	 * Atributo que representa la tasa del seguro de desgravamen basico del
	 * plazo de la simulacion.
	 */
	private double tasaSeguroDesgravamenBasico;

	/**
	 * Atributo que representa la tasa del seguro de desgravamen full del plazo
	 * de la simulacion.
	 */
	private double tasaSeguroDesgravamenFull;

	/**
	 * Atributo que representa la tasa del seguro de desgravamen plus del plazo
	 * de la simulacion.
	 */
	private double tasaSeguroDesgravamenPlus;

	/**
	 * Atributo que representa la tasa de spread del plazo de la simulacion.
	 */
	private double tasaSpread;

	/**
	 * Atributo que representa el dividendo del plazo de la simulacion.
	 */
	private double dividendo;

	/**
	 * Atributo que representa el dividendo total del plazo de la simulacion.
	 */
	private double dividendoTotal;

	/**
	 * Atributo que representa el dividendo total con individuales del plazo de
	 * la simulación.
	 */
	private double dividendoTotalConIndividuales;

	/**
	 * Atributo que representa el dividendo total en pesos del plazo de la
	 * simulación.
	 */
	private double dividendoTotalPesos;

	/**
	 * Atributo que representa el dividendo total en pesos con individuales del
	 * plazo de la simulación.
	 */
	private double dividendoTotalPesosConIndividuales;

	/**
	 * Atributo que representa el monto sel seguro de desgravamen del plazo de
	 * la simulación.
	 */
	private double montoSegDesg;

	/**
	 * Atributo que representa el monto del seguro incendio y sismo del plazo de
	 * la simulación.
	 */
	private double montoSegIncSis;

	/**
	 * Atributo que representa el valor reducido del plazo de la simulación.
	 */
	private double valorReducido;

	/**
	 * Atributo que representa la prima en oferta maxima del plazo de la
	 * simulación.
	 */
	private double primaOfertaMaxima;

	/**
	 * Atributo que representa la prima en oferta minima del plazo de la
	 * simulación.
	 */
	private double primaOfertaMinima;

	/**
	 * Atributo que representa la prima del seguro de desgravamen básico del
	 * plazo de la simulación.
	 */
	private double primaSeguroDesgravamenBasico;

	/**
	 * Atributo que representa la prima del seguro de desgravamen full del plazo
	 * de la simulación.
	 */
	private double primaSeguroDesgravamenFull;

	/**
	 * Atributo que representa la prima del seguro de desgravamen plus del plazo
	 * de la simulación.
	 */
	private double primaSeguroDesgravamenPlus;

	/**
	 * Atributo que representa la oferta total maxima del plazo de la
	 * simulación.
	 */
	private double totalOfertaMaxima;

	/**
	 * Atributo que representa la oferta total minima del plazo de la
	 * simulación.
	 */
	private double totalOfertaMinima;

	/**
	 * comparación conveniencia.
	 */
	private boolean comparacionConveniencia;


	public int getPlazo() {
        return plazo;
    }

    public String getTramo() {
        return tramo;
    }

    public double getTasa() {
        return tasa;
    }

    public double getTasaBase() {
        return tasaBase;
    }

    public double getTasaDesgravamen() {
        return tasaDesgravamen;
    }

    public double getTasaIncendio() {
        return tasaIncendio;
    }

    public double getTasaOfertaMaxima() {
        return tasaOfertaMaxima;
    }

    public double getTasaOfertaMinima() {
        return tasaOfertaMinima;
    }

    public double getTasaSeguroDesgravamenBasico() {
        return tasaSeguroDesgravamenBasico;
    }

    public double getTasaSeguroDesgravamenFull() {
        return tasaSeguroDesgravamenFull;
    }

    public double getTasaSeguroDesgravamenPlus() {
        return tasaSeguroDesgravamenPlus;
    }

    public double getTasaSpread() {
        return tasaSpread;
    }

    public double getDividendo() {
        return dividendo;
    }

    public double getDividendoTotal() {
        return dividendoTotal;
    }

    public double getDividendoTotalConIndividuales() {
        return dividendoTotalConIndividuales;
    }

    public double getDividendoTotalPesos() {
        return dividendoTotalPesos;
    }

    public double getDividendoTotalPesosConIndividuales() {
        return dividendoTotalPesosConIndividuales;
    }

    public double getMontoSegDesg() {
        return montoSegDesg;
    }

    public double getMontoSegIncSis() {
        return montoSegIncSis;
    }

    public double getValorReducido() {
        return valorReducido;
    }

    public double getPrimaOfertaMaxima() {
        return primaOfertaMaxima;
    }

    public double getPrimaOfertaMinima() {
        return primaOfertaMinima;
    }

    public double getPrimaSeguroDesgravamenBasico() {
        return primaSeguroDesgravamenBasico;
    }

    public double getPrimaSeguroDesgravamenFull() {
        return primaSeguroDesgravamenFull;
    }

    public double getPrimaSeguroDesgravamenPlus() {
        return primaSeguroDesgravamenPlus;
    }

    public double getTotalOfertaMaxima() {
        return totalOfertaMaxima;
    }

    public double getTotalOfertaMinima() {
        return totalOfertaMinima;
    }

    public boolean isComparacionConveniencia() {
        return comparacionConveniencia;
    }

    public void setPlazo(int plazo) {
        this.plazo = plazo;
    }

    public void setTramo(String tramo) {
        this.tramo = tramo;
    }

    public void setTasa(double tasa) {
        this.tasa = tasa;
    }

    public void setTasaBase(double tasaBase) {
        this.tasaBase = tasaBase;
    }

    public void setTasaDesgravamen(double tasaDesgravamen) {
        this.tasaDesgravamen = tasaDesgravamen;
    }

    public void setTasaIncendio(double tasaIncendio) {
        this.tasaIncendio = tasaIncendio;
    }

    public void setTasaOfertaMaxima(double tasaOfertaMaxima) {
        this.tasaOfertaMaxima = tasaOfertaMaxima;
    }

    public void setTasaOfertaMinima(double tasaOfertaMinima) {
        this.tasaOfertaMinima = tasaOfertaMinima;
    }

    public void setTasaSeguroDesgravamenBasico(double tasaSeguroDesgravamenBasico) {
        this.tasaSeguroDesgravamenBasico = tasaSeguroDesgravamenBasico;
    }

    public void setTasaSeguroDesgravamenFull(double tasaSeguroDesgravamenFull) {
        this.tasaSeguroDesgravamenFull = tasaSeguroDesgravamenFull;
    }

    public void setTasaSeguroDesgravamenPlus(double tasaSeguroDesgravamenPlus) {
        this.tasaSeguroDesgravamenPlus = tasaSeguroDesgravamenPlus;
    }

    public void setTasaSpread(double tasaSpread) {
        this.tasaSpread = tasaSpread;
    }

    public void setDividendo(double dividendo) {
        this.dividendo = dividendo;
    }

    public void setDividendoTotal(double dividendoTotal) {
        this.dividendoTotal = dividendoTotal;
    }

    public void setDividendoTotalConIndividuales(double dividendoTotalConIndividuales) {
        this.dividendoTotalConIndividuales = dividendoTotalConIndividuales;
    }

    public void setDividendoTotalPesos(double dividendoTotalPesos) {
        this.dividendoTotalPesos = dividendoTotalPesos;
    }

    public void setDividendoTotalPesosConIndividuales(double dividendoTotalPesosConIndividuales) {
        this.dividendoTotalPesosConIndividuales = dividendoTotalPesosConIndividuales;
    }
    
    public void setMontoSegDesg(double montoSegDesg) {
        this.montoSegDesg = montoSegDesg;
    }

    public void setMontoSegIncSis(double montoSegIncSis) {
        this.montoSegIncSis = montoSegIncSis;
    }

    public void setValorReducido(double valorReducido) {
        this.valorReducido = valorReducido;
    }

    public void setPrimaOfertaMaxima(double primaOfertaMaxima) {
        this.primaOfertaMaxima = primaOfertaMaxima;
    }

    public void setPrimaOfertaMinima(double primaOfertaMinima) {
        this.primaOfertaMinima = primaOfertaMinima;
    }

    public void setPrimaSeguroDesgravamenBasico(double primaSeguroDesgravamenBasico) {
        this.primaSeguroDesgravamenBasico = primaSeguroDesgravamenBasico;
    }

    public void setPrimaSeguroDesgravamenFull(double primaSeguroDesgravamenFull) {
        this.primaSeguroDesgravamenFull = primaSeguroDesgravamenFull;
    }

    public void setPrimaSeguroDesgravamenPlus(double primaSeguroDesgravamenPlus) {
        this.primaSeguroDesgravamenPlus = primaSeguroDesgravamenPlus;
    }

    public void setTotalOfertaMaxima(double totalOfertaMaxima) {
        this.totalOfertaMaxima = totalOfertaMaxima;
    }

    public void setTotalOfertaMinima(double totalOfertaMinima) {
        this.totalOfertaMinima = totalOfertaMinima;
    }

    public void setComparacionConveniencia(boolean comparacionConveniencia) {
        this.comparacionConveniencia = comparacionConveniencia;
    }


    public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("SimulacionPlazoCHIPTO: plazo[").append(plazo);
		sb.append("], tramo[").append(tramo);
		sb.append("], tasa[").append(tasa);
		sb.append("], tasaBase[").append(tasaBase);
		sb.append("], tasaDesgravamen[").append(tasaDesgravamen);
		sb.append("], tasaIncendio[").append(tasaIncendio);
		sb.append("], tasaOfertaMaxima[").append(tasaOfertaMaxima);
		sb.append("], tasaOfertaMinima[").append(tasaOfertaMinima);
		sb.append("], tasaSpread[").append(tasaSpread);
		sb.append("], dividendo[").append(dividendo);
		sb.append("], dividendoTotal[").append(dividendoTotal);
		sb.append("], dividendoTotalPesos[").append(dividendoTotalPesos);
		sb.append("], montoSegDesg[").append(montoSegDesg);
		sb.append("], montoSegIncSis[").append(montoSegIncSis);
		sb.append("], valorReducido[").append(valorReducido);
		sb.append("], primaOfertaMaxima[").append(primaOfertaMaxima);
		sb.append("], primaOfertaMinima[").append(primaOfertaMinima);
		sb.append("], totalOfertaMaxima[").append(totalOfertaMaxima);
		sb.append("], totalOfertaMinima[").append(totalOfertaMinima);
		sb.append("], comparacionConveniencia[")
				.append(comparacionConveniencia);
		sb.append("], tasaSeguroDesgravamenBasico[").append(
				tasaSeguroDesgravamenBasico);
		sb.append("], tasaSeguroDesgravamenFull[").append(
				tasaSeguroDesgravamenFull);
		sb.append("], tasaSeguroDesgravamenPlus[").append(
				tasaSeguroDesgravamenPlus);
		sb.append("], dividendoTotalConIndividuales[").append(
				dividendoTotalConIndividuales);
		sb.append("], primaSeguroDesgravamenBasico[").append(
				primaSeguroDesgravamenBasico);
		sb.append("], primaSeguroDesgravamenFull[").append(
				primaSeguroDesgravamenFull);
		sb.append("], primaSeguroDesgravamenPlus[").append(
				primaSeguroDesgravamenPlus);
		sb.append("], dividendoTotalPesosConIndividuales[").append(
				dividendoTotalPesosConIndividuales);
		sb.append("]");
		return sb.toString();
	}
}
