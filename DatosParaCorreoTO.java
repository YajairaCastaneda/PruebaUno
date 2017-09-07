package wcorp.bprocess.multilinea.to;

import java.io.Serializable;



/**
 * Clase que contiene los datos para almacenar los datos asociados al correo.
 * <p>
 * Registro de versiones:
 * <ul>
 * <li> 1.0 19/01/2015 Manuel Escárate (BEE S.A.) - Jimmy Muñoz D. (ing.Soft.BCI)  : Versión Inicial.</li>
 * <li> 1.1 11/02/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se agregan los siguientes campos, telefonoCliente,
 *                                                        codTelefonoCliente, codTelefonoCliente, celularCliente, codCelularCliente
 *                                                        emailCliente, regionCliente,comunaCliente ,rutOperador,dvOperador</li>
 * <li> 1.2 12/08/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se eliminan nombreAval y mailAval y se agregan 
 *                                                 montoExpresadoEnPalabras,valorCuota,datosAvales,fechaUltimaCuota,direccionEmpresa,
 *                                                 comunaEmpresa,tipoOperacion,diaVencimiento,glosasAvales,mailEmpresa,impuestos,seguros
 *                                                 valorNotario,textosParaAvales </li>
 * </ul>
 * <p>
 * <b>Todos los derechos reservados por Banco de Crédito e Inversiones.</b>
 * <p>
 */
public class DatosParaCorreoTO implements Serializable{

	/**
	 * Número de versión utilizado en la serialización.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Cai operacion.
	 */
	private String caiOperacion;
	
	/**
	 * Iic operacion.
	 */	
	private int iicOperacion;
	
	/**
	 * Fecha de vencimiento seleccionada.
	 */	
	private String fechaVencimientoSeleccionada;
	
	/**
	 * Rut.
	 */	
	private long rut;
	
	/**
	 * Digito verificador.
	 */	
	private char dv;
	
	/**
	 * Razon social.
	 */	
	private String razonSocial;
	
	/**
	 * Monto final del credito.
	 */	
	private double montoFinalCredito;
	
	/**
	 * Cuota seleccionada.
	 */	
    private int cuotaSeleccionada;
    
	/**
	 * Destino del credito.
	 */	
    private String destinoDelCredito;
    
	/**
	 * Tasa de interes.
	 */	
    private String tasaInteres;
    
	/**
	 * Estado.
	 */	
    private String estado;
    
	/**
	 * Asunto.
	 */	
    private String asunto;
    
	/**
	 * Flag envia a backoffice.
	 */	
    private boolean enviaBackOffice;
   
    /**
     * Teléfono cliente.
     */
    private String telefonoCliente;

	/**
     * Código de teléfono.
     */
    private String codTelefonoCliente;
    
    /**
     * Celular cliente.
     */
    private String celularCliente;
    
    /**
     * Código celular.
     */
    private String codCelularCliente;
    
    /**
     * Email cliente.
     */
    private String emailCliente;
    
    /**
     * Region cliente.
     */
    private String regionCliente;

	/**
     * Comuna cliente.
     */
    private String comunaCliente;
    
    /**
     * Rut operador. 
     */
    private long rutOperador;
    
    /**
     * Dígito verificador. 
     */
    private char dvOperador;
    
    /**
     * Monto expresado en palabras.
     */
    private String montoExpresadoEnPalabras;
    
    /**
     * Valor cuota. 
     */
    private double valorCuota;
    
    /**
     * Datos de avales.
     */
    private DatosAvalesTO datosAvales;
    
    /**
     * Fecha última cuota a pagar. 
     */
    private String fechaUltimaCuota;
    
    /**
     * Dirección empresa. 
     */
    private String direccionEmpresa;
    
    /**
     * Comuna empresa. 
     */
    private String comunaEmpresa;
    
    /**
     * Ciudad empresa. 
     */
    private String ciudadEmpresa;
    
    /**
     * Tipo de operación. 
     */
    private String tipoOperacion;
    
    /**
     * Día de vencimiento. 
     */
    private int diaVencimiento;
    
    /**
     * Glosa con datos de avales. 
     */
    private String glosasAvales;
    
    /**
     * Mail de empresa.
     */
    private String mailEmpresa;
    
    /**
     * Impuestos.
     */
    private double impuestos;
    
    /**
     * Seguros.
     */
    private double seguros;
    
    /**
     * Valor Notario.
     */
    private double valorNotario;
    
    /**
     * Texto para avales.
     */
    private String textosParaAvales;
    
    
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

	public long getRut() {
		return rut;
	}

	public void setRut(long rut) {
		this.rut = rut;
	}

	public char getDv() {
		return dv;
	}

	public void setDv(char dv) {
		this.dv = dv;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public double getMontoFinalCredito() {
		return montoFinalCredito;
	}

	public void setMontoFinalCredito(double montoFinalCredito) {
		this.montoFinalCredito = montoFinalCredito;
	}

	public int getCuotaSeleccionada() {
		return cuotaSeleccionada;
	}

	public void setCuotaSeleccionada(int cuotaSeleccionada) {
		this.cuotaSeleccionada = cuotaSeleccionada;
	}

	public String getDestinoDelCredito() {
		return destinoDelCredito;
	}

	public void setDestinoDelCredito(String destinoDelCredito) {
		this.destinoDelCredito = destinoDelCredito;
	}
	
	public String getTasaInteres() {
		return tasaInteres;
	}

	public void setTasaInteres(String tasaInteres) {
		this.tasaInteres = tasaInteres;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getAsunto() {
		return asunto;
	}

	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}

	public String getFechaVencimientoSeleccionada() {
		return fechaVencimientoSeleccionada;
	}

	public void setFechaVencimientoSeleccionada(String fechaVencimientoSeleccionada) {
		this.fechaVencimientoSeleccionada = fechaVencimientoSeleccionada;
	}

	public boolean isEnviaBackOffice() {
		return enviaBackOffice;
	}

	public void setEnviaBackOffice(boolean enviaBackOffice) {
		this.enviaBackOffice = enviaBackOffice;
	}
	
	public long getRutOperador() {
		return rutOperador;
	}

	public void setRutOperador(long rutOperador) {
		this.rutOperador = rutOperador;
	}

	public char getDvOperador() {
		return dvOperador;
	}

	public void setDvOperador(char dvOperador) {
		this.dvOperador = dvOperador;
	}	
	
	public String getTelefonoCliente() {
		return telefonoCliente;
	}

	public void setTelefonoCliente(String telefonoCliente) {
		this.telefonoCliente = telefonoCliente;
	}

	public String getCelularCliente() {
		return celularCliente;
	}

	public void setCelularCliente(String celularCliente) {
		this.celularCliente = celularCliente;
	}

	public String getEmailCliente() {
		return emailCliente;
	}

	public void setEmailCliente(String emailCliente) {
		this.emailCliente = emailCliente;
	}

	public String getRegionCliente() {
		return regionCliente;
	}

	public void setRegionCliente(String regionCliente) {
		this.regionCliente = regionCliente;
	}

	public String getComunaCliente() {
		return comunaCliente;
	}

	public void setComunaCliente(String comunaCliente) {
		this.comunaCliente = comunaCliente;
	}
	
    public String getCodTelefonoCliente() {
		return codTelefonoCliente;
	}

	public void setCodTelefonoCliente(String codTelefonoCliente) {
		this.codTelefonoCliente = codTelefonoCliente;
	}

	public String getCodCelularCliente() {
		return codCelularCliente;
	}

	public void setCodCelularCliente(String codCelularCliente) {
		this.codCelularCliente = codCelularCliente;
	}
	
	public String getMontoExpresadoEnPalabras() {
		return montoExpresadoEnPalabras;
	}

	public void setMontoExpresadoEnPalabras(String montoExpresadoEnPalabras) {
		this.montoExpresadoEnPalabras = montoExpresadoEnPalabras;
	}
	
	public double getValorCuota() {
		return valorCuota;
	}

	public void setValorCuota(double valorCuota) {
		this.valorCuota = valorCuota;
	}
	
	public String getFechaUltimaCuota() {
		return fechaUltimaCuota;
	}

	public void setFechaUltimaCuota(String fechaUltimaCuota) {
		this.fechaUltimaCuota = fechaUltimaCuota;
	}

	public DatosAvalesTO getDatosAvales() {
		return datosAvales;
	}

	public void setDatosAvales(DatosAvalesTO datosAvales) {
		this.datosAvales = datosAvales;
	}
	
	public String getDireccionEmpresa() {
		return direccionEmpresa;
	}

	public void setDireccionEmpresa(String direccionEmpresa) {
		this.direccionEmpresa = direccionEmpresa;
	}

	public String getComunaEmpresa() {
		return comunaEmpresa;
	}

	public void setComunaEmpresa(String comunaEmpresa) {
		this.comunaEmpresa = comunaEmpresa;
	}
	
	public String getCiudadEmpresa() {
		return ciudadEmpresa;
	}

	public void setCiudadEmpresa(String ciudadEmpresa) {
		this.ciudadEmpresa = ciudadEmpresa;
	}

	public String getTipoOperacion() {
		return tipoOperacion;
	}

	public void setTipoOperacion(String tipoOperacion) {
		this.tipoOperacion = tipoOperacion;
	}
	
	public int getDiaVencimiento() {
		return diaVencimiento;
	}

	public void setDiaVencimiento(int diaVencimiento) {
		this.diaVencimiento = diaVencimiento;
	}
	
	public String getGlosasAvales() {
		return glosasAvales;
	}

	public void setGlosasAvales(String glosasAvales) {
		this.glosasAvales = glosasAvales;
	}

	public String getMailEmpresa() {
		return mailEmpresa;
	}

	public void setMailEmpresa(String mailEmpresa) {
		this.mailEmpresa = mailEmpresa;
	}

	public double getImpuestos() {
		return impuestos;
	}

	public void setImpuestos(double impuestos) {
		this.impuestos = impuestos;
	}

	public double getSeguros() {
		return seguros;
	}

	public void setSeguros(double seguros) {
		this.seguros = seguros;
	}

	public double getValorNotario() {
		return valorNotario;
	}

	public void setValorNotario(double valorNotario) {
		this.valorNotario = valorNotario;
	}

	public String getTextosParaAvales() {
		return textosParaAvales;
	}

	public void setTextosParaAvales(String textosParaAvales) {
		this.textosParaAvales = textosParaAvales;
	}

	/**
     * Retorna buffer con informacion de la clase.
     *
     * <p>
     * Registro de versiones:<ul>
     * <li>1.0  10/06/2015 Manuel Escarate. (BEE) - Jimmy Muñoz D. (ing.Soft.BCI) : Versión inicial. </li>
     * <li>1.1  11/02/2016 Manuel Escarate. (BEE) - Felipe Ojeda D. (ing.Soft.BCI) : Se agregan nuevos parametros al método.</li>
     * <li>1.2  12/08/2016, Manuel Escárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI): Se eliminan nombreAval y mailAval y se agregan 
     *                                                 montoExpresadoEnPalabras,valorCuota,datosAvales,fechaUltimaCuota,direccionEmpresa,
     *                                                 comunaEmpresa,tipoOperacion,diaVencimiento,glosasAvales,mailEmpresa,impuestos,seguros
 	 *                                                 valorNotario,textosParaAvales </li>
     * </ul>
     * </p>
     * 
     * @return string string de buffer.
     * @since 1.0
     */	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("DatosParaCorreoTO: iicOperacion[").append(iicOperacion);
		sb.append("], fechaVencimientoSeleccionada[").append(fechaVencimientoSeleccionada);	
		sb.append("], rut[").append(rut);	
		sb.append("], dv[").append(dv);	
		sb.append("], razonSocial[").append(razonSocial);	
		sb.append("], montoFinalCredito[").append(montoFinalCredito);	
		sb.append("], cuotaSeleccionada[").append(cuotaSeleccionada);	
		sb.append("], destinoDelCredito[").append(destinoDelCredito);	
		sb.append("], tasaInteres[").append(tasaInteres);	
		sb.append("], estado[").append(estado);
		sb.append("], asunto[").append(asunto);
		sb.append("], enviaBackOffice[").append(enviaBackOffice);
		sb.append("], telefonoCliente[").append(telefonoCliente);
		sb.append("], codTelefonoCliente[").append(codTelefonoCliente);
		sb.append("], celularCliente[").append(celularCliente);
		sb.append("], codCelularCliente[").append(codCelularCliente);
		sb.append("], emailCliente[").append(emailCliente);
		sb.append("], regionCliente[").append(regionCliente);
		sb.append("], comunaCliente[").append(comunaCliente);
		sb.append("], rutOperador[").append(rutOperador);
		sb.append("], dvOperador[").append(dvOperador);
		sb.append("], montoExpresadoEnPalabras[").append(montoExpresadoEnPalabras);
		sb.append("], valorCuota[").append(valorCuota);
		sb.append("], datosAvales[").append(datosAvales);
		sb.append("], fechaUltimaCuota[").append(fechaUltimaCuota);
		sb.append("], direccionEmpresa[").append(direccionEmpresa);
		sb.append("], comunaEmpresa[").append(comunaEmpresa);
		sb.append("], ciudadEmpresa[").append(ciudadEmpresa);
		sb.append("], tipoOperacion[").append(tipoOperacion);
		sb.append("], diaVencimiento[").append(diaVencimiento);
		sb.append("], glosasAvales[").append(glosasAvales);
		sb.append("], mailEmpresa[").append(mailEmpresa);
		sb.append("], impuestos[").append(impuestos);
		sb.append("], seguros[").append(seguros);
		sb.append("], valorNotario[").append(valorNotario);
		sb.append("], textosParaAvales[").append(textosParaAvales);
		sb.append("]");
		return sb.toString();
	}
}
