
package wcorp.bprocess.multilinea;



import java.lang.*;

import java.io.*;

import java.util.*;

import wcorp.serv.creditosglobales.ResultLiquidacionDeOperacionDeCreditoOpc;

import wcorp.bprocess.simulacion.InputIngresoRocAmpliada;

/**
 * <b>ResultAvanceMultilinea</b>
 *
 * clase que contiene los datos de Result de Simulacion de Avance Multilinea
 *
 * <p>
 * Registro de versiones: <ul>
 *   <li>1.0 (??/??/2003, ?????? ??????????? (BEE S.A.)): versión inicial</li>
 *   <li>1.1 (24/04/2009, Hector Carranza    (BEE S.A.)): Se amplia datos de Result para mas contexto del avance multilinea, se incluye clase InputIngresoRocAmpliada con set y get, +import</li>
 *   <li>1.2 (20/07/2016,  Manuel Ecárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI)): Se agrega atributo valorGastoNotarial.</li>
 * </ul>
 * <p>
 *
 * <B>Todos los derechos reservados por Banco de Crédito e Inversiones.</B>
 * <P>
 */

public class ResultAvanceMultilinea implements Serializable {

    /** 
    * Número de versión utilizado durante la serialización. 
    */
   private static final long serialVersionUID = 1L;


    /** <b>tasaOriginal</b> */
    private double tasaOriginal;

    /** <b>descuento</b> */
    private double descuento;

     /** <b>tasa</b> */
    private double tasa;

     /** <b>condicionGarantia</b> */
    private String condicionGarantia;

     /** <b>fechaCurse</b> */
    private  Date fechaCurse;

    private ResultLiquidacionDeOperacionDeCreditoOpc resultLiqOpc;

    /**  Cuenta de Cargo */
    private String[]                         ctaCargo = null;
    /**  Cuenta de Abono */
    private String                           ctaAbono = null;
    /**  Nombre del Ejecutivo */
    private String                           ejecutivo = null;
    /**  Oficina del Ejecutivo */
    private String                           oficina = null;
    
    /**
     * Valor de gasto notarial.
     */
    private double valorGastoNotarial;
    
    /**  <b>Datos de imput ROC (relacion Operacion Concepto)</b> */
    private InputIngresoRocAmpliada[]        arregloROC = null;

    public ResultAvanceMultilinea() {}

    public double getTasa() {
        return this.tasa;
    }

    public void setTasa(double tasa) {
        this.tasa = tasa;
    }

    public double getTasaOriginal() {
        return this.tasaOriginal;
    }

    public void setTasaOriginal(double tasaOriginal) {
        this.tasaOriginal = tasaOriginal;
    }

    public double getDescuento() {
        return this.descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public String getCondicionGarantia() {
        return this.condicionGarantia;
    }

    public void setCondicionGarantia(String condicionGarantia) {
        this.condicionGarantia = condicionGarantia;
    }

    public Date getFechaCurse() {
        return this.fechaCurse;
    }

    public void setFechaCurse(Date fechaCurse) {
        this.fechaCurse = fechaCurse;
    }

	public ResultLiquidacionDeOperacionDeCreditoOpc getResultLiqOpc() {
		return this.resultLiqOpc;
	}

	public void setResultLiqOpc(ResultLiquidacionDeOperacionDeCreditoOpc resultLiqOpc) {
		this.resultLiqOpc = resultLiqOpc;
	}

    public String[] getCtaCargo() {
        return ctaCargo;
    }

    public String getCtaAbono() {
        return ctaAbono;
    }

    public String getEjecutivo() {
        return ejecutivo;
    }

    public String getOficina() {
        return oficina;
    }

    public InputIngresoRocAmpliada[] getArregloROC() {
        return arregloROC;
    }

    public void setCtaCargo(String[] ctaCargo) {
        this.ctaCargo = ctaCargo;
    }

    public void setCtaAbono(String ctaAbono) {
        this.ctaAbono = ctaAbono;
    }

    public void setEjecutivo(String ejecutivo) {
        this.ejecutivo = ejecutivo;
    }

    public void setOficina(String oficina) {
        this.oficina = oficina;
    }

    public void setArregloROC(InputIngresoRocAmpliada[] arregloROC) {
        this.arregloROC = arregloROC;
    }

	public double getValorGastoNotarial() {
		return valorGastoNotarial;
	}

	public void setValorGastoNotarial(double valorGastoNotarial) {
		this.valorGastoNotarial = valorGastoNotarial;
	}

}
