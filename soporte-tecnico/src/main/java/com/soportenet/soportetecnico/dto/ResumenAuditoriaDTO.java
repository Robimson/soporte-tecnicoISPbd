package com.soportenet.soportetecnico.dto;

public class ResumenAuditoriaDTO {

    private long sesionesActivas;
    private long cambiosHoy;
    private long inserts;
    private long updates;
    private long eliminaciones;
    private long accionesSistema;

    public long getSesionesActivas() { return sesionesActivas; }
    public void setSesionesActivas(long sesionesActivas) { this.sesionesActivas = sesionesActivas; }

    public long getCambiosHoy() { return cambiosHoy; }
    public void setCambiosHoy(long cambiosHoy) { this.cambiosHoy = cambiosHoy; }

    public long getInserts() { return inserts; }
    public void setInserts(long inserts) { this.inserts = inserts; }

    public long getUpdates() { return updates; }
    public void setUpdates(long updates) { this.updates = updates; }

    public long getEliminaciones() { return eliminaciones; }
    public void setEliminaciones(long eliminaciones) { this.eliminaciones = eliminaciones; }

    public long getAccionesSistema() { return accionesSistema; }
    public void setAccionesSistema(long accionesSistema) { this.accionesSistema = accionesSistema; }
}