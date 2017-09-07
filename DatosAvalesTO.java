package wcorp.bprocess.multilinea.to;

import java.io.Serializable;

/**
 * Clase que contiene los datos de los avales.
 * <p>
 * Registro de versiones:
 * <ul>
 * <li> 1.0 19/01/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión Inicial.</li>
 * <li> 1.1 12/08/2016 Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI) : Se agregan los siguientes campos direccionAval,
 *                                                 comunaAval,mailAval,ciudadAval.</li>
 * </ul>
 * <p>
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 * <p>
 */
public class DatosAvalesTO implements Serializable{

	/**
	 * Número de versión utilizado en la serialización.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Rut de aval.
	 */
	private String rutAval;
	
	/**
	 * Digito verificador de aval.
	 */	
	private String dvAval;
	
	/**
	 * Nombre de aval.
	 */	
	private String nombreAval;
	
	 /**
     * Dirección Aval. 
     */
    private String direccionAval;
    
    /**
     * Comuna Aval. 
     */
    private String comunaAval;
    
    /**
     * Mail Aval.  
     */
	private String mailAval;
	
	/**
	 * Ciudad Aval. 
	 */
	private String ciudadAval;

	public String getRutAval() {
		return rutAval;
	}

	public void setRutAval(String rutAval) {
		this.rutAval = rutAval;
	}

	public String getDvAval() {
		return dvAval;
	}

	public void setDvAval(String dvAval) {
		this.dvAval = dvAval;
	}

	public String getNombreAval() {
		return nombreAval;
	}

	public void setNombreAval(String nombreAval) {
		this.nombreAval = nombreAval;
	}
	
    public String getDireccionAval() {
		return direccionAval;
	}

	public void setDireccionAval(String direccionAval) {
		this.direccionAval = direccionAval;
	}

	public String getComunaAval() {
		return comunaAval;
	}

	public void setComunaAval(String comunaAval) {
		this.comunaAval = comunaAval;
	}

	public String getMailAval() {
		return mailAval;
	}

	public void setMailAval(String mailAval) {
		this.mailAval = mailAval;
	}
	
	public String getCiudadAval() {
		return ciudadAval;
	}

	public void setCiudadAval(String ciudadAval) {
		this.ciudadAval = ciudadAval;
	}

	/**
     * Retorna buffer con informacion de la clase.
     *
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0  10/06/2015 Manuel Escarate. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión inicial. </li>
     * <li>1.1  12/08/2016 Manuel Escarate. (BEE) - Felipe Ojeda (ing.Soft.BCI) : Se agregan los siguientes campos direccionAval,
     *                                                 comunaAval,mailAval,ciudadAval.
     * </ul>
     * </p>
     * 
     * @return string string de buffer.
     * @since 1.0
     */	
	public String toString() {
	        StringBuffer sb = new StringBuffer();
	        sb.append("DatosAvalesTO: rutAval[").append(rutAval);
	        sb.append("], dvAval[").append(dvAval);	
	        sb.append("], nombreAval[").append(nombreAval);	
	        sb.append("], direccionAval[").append(direccionAval);	
	        sb.append("], comunaAval[").append(comunaAval);
	        sb.append("], mailAval[").append(mailAval);	
	        sb.append("], ciudadAval[").append(ciudadAval);
	        sb.append("]");
	        return sb.toString();
	    }		
}
