package wcorp.bprocess.multilinea.to;

import java.io.Serializable;

/**
 * Clase que contiene los datos para disclaimer.
 * <p>
 * Registro de versiones:
 * <ul>
 * <li> 1.0 05/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI)  : Versión Inicial.</li>
 * </ul>
 * <p>
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 * <p>
 */

public class DatosDisclaimerTO implements Serializable {

	/**
	 * Número de versión utilizado en la serialización.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Rut del cliente.
	 */	
	private String rutCliente;
	
	/**
	 * Rut de Empresa.
	 */		
	private String rutEmpresa;
	
	/**
	 * Nombre de Cliente.
	 */	
	private String nombreCliente;
	
	/**
	 * Nombre Empresa.
	 */	
	private String nombreempresa;
	
	/**
	 * Monto de credito.
	 */	
	private String monto;

	public String getRutCliente() {
		return rutCliente;
	}
	public void setRutCliente(String rutCliente) {
		this.rutCliente = rutCliente;
	}
	public String getRutEmpresa() {
		return rutEmpresa;
	}
	public void setRutEmpresa(String rutEmpresa) {
		this.rutEmpresa = rutEmpresa;
	}
	public String getNombreCliente() {
		return nombreCliente;
	}
	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}
	public String getNombreempresa() {
		return nombreempresa;
	}
	public void setNombreempresa(String nombreempresa) {
		this.nombreempresa = nombreempresa;
	}

    public String getMonto() {
		return monto;
	}
	public void setMonto(String monto) {
		this.monto = monto;
	}
	/**
     * Retorna buffer con informacion de la clase.
     *
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0  02/08/2016 Manuel Escarate. (BEE) - Felipe Ojeda. (ing.Soft.BCI) : Versión inicial. </li>
     * </ul>
     * </p>
     * 
     * @return string string de buffer.
     * @since 1.0
     */	
	public String toString() {
		StringBuffer builder = new StringBuffer();
		builder.append("DatosDisclaimerTO [rutCliente=");
		builder.append(rutCliente);
		builder.append(", rutEmpresa=");
		builder.append(rutEmpresa);
		builder.append(", nombreCliente=");
		builder.append(nombreCliente);
		builder.append(", nombreempresa=");
		builder.append(nombreempresa);
		builder.append(", monto=");
		builder.append(monto);
		builder.append("]");
		return builder.toString();
	}

}
