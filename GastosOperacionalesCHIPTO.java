package wcorp.aplicaciones.productos.colocaciones.creditohipotecario.to;

import java.io.Serializable;

/**
 * Clase "Transfer Object" que modela los Gastos Operacionales.
 * <p>
 * Registro de versiones:
 * <ul>
 * <li>1.0 07/04/2015 Jose Palma (TINet) - Patricio Candia (Ing. Soft. BCI): version inicial.</li>
 * </ul>
 * </p>
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 */
public class GastosOperacionalesCHIPTO implements Serializable {

	/**
	 * Numero de version para serializacion.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Atributo que representa el valor de la tasacion.
	 */
	private double tasacion;

	/**
	 * Atributo que representa el gasto del estudio de titulos.
	 */
	private double estudioTitulos;

	/**
	 * Atributo que representa el gasto del borrador de la escritura.
	 */
	private double borradorEscritura;

	/**
	 * Atributo que representa los gastos notariales.
	 */
	private double notariales;

	/**
	 * Atributo que representa el gasto de impuesto mutuo.
	 */
	private double imptoAlMutuo;

	/**
	 * Atributo que representa el gasto de la inscripcion en el conservador.
	 */
	private double inscripcionConservador;

	/**
	 * Atributo que representa la gestoria.
	 */
	private double gestoria;

    public double getTasacion() {
        return tasacion;
    }

    public double getEstudioTitulos() {
        return estudioTitulos;
    }

    public double getBorradorEscritura() {
        return borradorEscritura;
    }

    public double getNotariales() {
        return notariales;
    }

    public double getImptoAlMutuo() {
        return imptoAlMutuo;
    }

    public double getInscripcionConservador() {
        return inscripcionConservador;
    }

    public double getGestoria() {
        return gestoria;
    }

    public void setTasacion(double tasacion) {
        this.tasacion = tasacion;
    }

    public void setEstudioTitulos(double estudioTitulos) {
        this.estudioTitulos = estudioTitulos;
    }

    public void setBorradorEscritura(double borradorEscritura) {
        this.borradorEscritura = borradorEscritura;
    }

    public void setNotariales(double notariales) {
        this.notariales = notariales;
    }

    public void setImptoAlMutuo(double imptoAlMutuo) {
        this.imptoAlMutuo = imptoAlMutuo;
    }

    public void setInscripcionConservador(double inscripcionConservador) {
        this.inscripcionConservador = inscripcionConservador;
    }

    public void setGestoria(double gestoria) {
        this.gestoria = gestoria;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GastosOperacionalesCHIPTO: tasacion[").append(tasacion);
        sb.append("], estudioTitulos[").append(estudioTitulos);
        sb.append("], borradorEscritura[").append(borradorEscritura);
        sb.append("], notariales[").append(notariales);
        sb.append("], imptoAlMutuo[").append(imptoAlMutuo);
        sb.append("], inscripcionConservador[").append(inscripcionConservador);
        sb.append("], gestoria[").append(gestoria);
        sb.append("]");
        return sb.toString();
    }    
}
