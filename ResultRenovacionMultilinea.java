
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
 *   <li>1.2 (29/07/2016, Manuel Ecárate (BEE S.A.) - Felipe Ojeda (ing.Soft.BCI)): Se agrega atributo valorGastoNotarial.</li>
 * </ul>
 * <p>
 *
 * <B>Todos los derechos reservados por Banco de Crédito e Inversiones.</B>
 * <P>
 */

public class ResultRenovacionMultilinea implements Serializable {

    /** 
    * Número de versión utilizado durante la serialización. 
    */
   private static final long serialVersionUID = 1L;


    /** <b>tasa</b> */
    private double tasa;

     /** <b>tasaOriginal</b> */
    private double tasaOriginal;

     /** <b>descuento</b> */
    private double descuento;

     /** <b>fechaCurse</b> */
    private  Date fechaCurse;

	private ResultLiquidacionDeOperacionDeCreditoOpc resultLiqOpc;

    /**  <b>Datos de imput ROC (relacion Operacion Concepto)</b> */
    private InputIngresoRocAmpliada[]        arregloROC = null;
    
    /**
     * Valor de gasto notarial.
     */
    private double valorGastoNotarial;
    
    /**
     *
     */
    public ResultRenovacionMultilinea() {}

    public double getTasa() {
        return this.tasa;
    }

    public void setTasa(double tasa) {
        this.tasa = tasa;
    }

    public double getDescuento() {
        return this.descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public Date getFechaCurse() {
        return this.fechaCurse;
    }

    public InputIngresoRocAmpliada[] getArregloROC() {
        return arregloROC;
    }

    public void setFechaCurse(Date fechaCurse) {
        this.fechaCurse = fechaCurse;
    }

    public double getTasaOriginal() {
        return this.tasaOriginal;
    }

    public void setTasaOriginal(double tasaOriginal) {
        this.tasaOriginal = tasaOriginal;
    }
    
	public ResultLiquidacionDeOperacionDeCreditoOpc getResultLiqOpc() {
		return this.resultLiqOpc;
	}

	public void setResultLiqOpc(ResultLiquidacionDeOperacionDeCreditoOpc resultLiqOpc) {
		this.resultLiqOpc = resultLiqOpc;
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
