package com.soportenet.soportetecnico.dto;

public class ResumenTecnicoDTO {

    private long enProceso;
    private long pendientes;
    private long resueltasHoy;
    private long totalCerradas;

    public long getEnProceso() { return enProceso; }
    public void setEnProceso(long enProceso) { this.enProceso = enProceso; }

    public long getPendientes() { return pendientes; }
    public void setPendientes(long pendientes) { this.pendientes = pendientes; }

    public long getResueltasHoy() { return resueltasHoy; }
    public void setResueltasHoy(long resueltasHoy) { this.resueltasHoy = resueltasHoy; }

    public long getTotalCerradas() { return totalCerradas; }
    public void setTotalCerradas(long totalCerradas) { this.totalCerradas = totalCerradas; }
}