package com.arsw.ids_ia.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO que representa las características de un paquete de red para análisis ML.
 * Mapea exactamente la estructura esperada por el modelo AWS Lambda.
 */
public class NetworkTrafficFeatures {

    private Integer duration;
    
    @JsonProperty("protocol_type")
    private String protocolType;
    
    private String service;
    private String flag;
    
    @JsonProperty("src_bytes")
    private Integer srcBytes;
    
    @JsonProperty("dst_bytes")
    private Integer dstBytes;
    
    private Integer land;
    
    @JsonProperty("wrong_fragment")
    private Integer wrongFragment;
    
    private Integer urgent;
    private Integer hot;
    
    @JsonProperty("num_failed_logins")
    private Integer numFailedLogins;
    
    @JsonProperty("logged_in")
    private Integer loggedIn;
    
    @JsonProperty("num_compromised")
    private Integer numCompromised;
    
    @JsonProperty("root_shell")
    private Integer rootShell;
    
    @JsonProperty("su_attempted")
    private Integer suAttempted;
    
    @JsonProperty("num_root")
    private Integer numRoot;
    
    @JsonProperty("num_file_creations")
    private Integer numFileCreations;
    
    @JsonProperty("num_shells")
    private Integer numShells;
    
    @JsonProperty("num_access_files")
    private Integer numAccessFiles;
    
    @JsonProperty("num_outbound_cmds")
    private Integer numOutboundCmds;
    
    @JsonProperty("is_host_login")
    private Integer isHostLogin;
    
    @JsonProperty("is_guest_login")
    private Integer isGuestLogin;
    
    private Integer count;
    
    @JsonProperty("srv_count")
    private Integer srvCount;
    
    @JsonProperty("serror_rate")
    private Double serrorRate;
    
    @JsonProperty("srv_serror_rate")
    private Double srvSerrorRate;
    
    @JsonProperty("rerror_rate")
    private Double rerrorRate;
    
    @JsonProperty("srv_rerror_rate")
    private Double srvRerrorRate;
    
    @JsonProperty("same_srv_rate")
    private Double sameSrvRate;
    
    @JsonProperty("diff_srv_rate")
    private Double diffSrvRate;
    
    @JsonProperty("srv_diff_host_rate")
    private Double srvDiffHostRate;
    
    @JsonProperty("dst_host_count")
    private Integer dstHostCount;
    
    @JsonProperty("dst_host_srv_count")
    private Integer dstHostSrvCount;
    
    @JsonProperty("dst_host_same_srv_rate")
    private Double dstHostSameSrvRate;
    
    @JsonProperty("dst_host_diff_srv_rate")
    private Double dstHostDiffSrvRate;
    
    @JsonProperty("dst_host_same_src_port_rate")
    private Double dstHostSameSrcPortRate;
    
    @JsonProperty("dst_host_srv_diff_host_rate")
    private Double dstHostSrvDiffHostRate;
    
    @JsonProperty("dst_host_serror_rate")
    private Double dstHostSerrorRate;
    
    @JsonProperty("dst_host_srv_serror_rate")
    private Double dstHostSrvSerrorRate;
    
    @JsonProperty("dst_host_rerror_rate")
    private Double dstHostRerrorRate;
    
    @JsonProperty("dst_host_srv_rerror_rate")
    private Double dstHostSrvRerrorRate;

    public NetworkTrafficFeatures() {
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public Integer getSrcBytes() {
        return srcBytes;
    }

    public void setSrcBytes(Integer srcBytes) {
        this.srcBytes = srcBytes;
    }

    public Integer getDstBytes() {
        return dstBytes;
    }

    public void setDstBytes(Integer dstBytes) {
        this.dstBytes = dstBytes;
    }

    public Integer getLand() {
        return land;
    }

    public void setLand(Integer land) {
        this.land = land;
    }

    public Integer getWrongFragment() {
        return wrongFragment;
    }

    public void setWrongFragment(Integer wrongFragment) {
        this.wrongFragment = wrongFragment;
    }

    public Integer getUrgent() {
        return urgent;
    }

    public void setUrgent(Integer urgent) {
        this.urgent = urgent;
    }

    public Integer getHot() {
        return hot;
    }

    public void setHot(Integer hot) {
        this.hot = hot;
    }

    public Integer getNumFailedLogins() {
        return numFailedLogins;
    }

    public void setNumFailedLogins(Integer numFailedLogins) {
        this.numFailedLogins = numFailedLogins;
    }

    public Integer getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(Integer loggedIn) {
        this.loggedIn = loggedIn;
    }

    public Integer getNumCompromised() {
        return numCompromised;
    }

    public void setNumCompromised(Integer numCompromised) {
        this.numCompromised = numCompromised;
    }

    public Integer getRootShell() {
        return rootShell;
    }

    public void setRootShell(Integer rootShell) {
        this.rootShell = rootShell;
    }

    public Integer getSuAttempted() {
        return suAttempted;
    }

    public void setSuAttempted(Integer suAttempted) {
        this.suAttempted = suAttempted;
    }

    public Integer getNumRoot() {
        return numRoot;
    }

    public void setNumRoot(Integer numRoot) {
        this.numRoot = numRoot;
    }

    public Integer getNumFileCreations() {
        return numFileCreations;
    }

    public void setNumFileCreations(Integer numFileCreations) {
        this.numFileCreations = numFileCreations;
    }

    public Integer getNumShells() {
        return numShells;
    }

    public void setNumShells(Integer numShells) {
        this.numShells = numShells;
    }

    public Integer getNumAccessFiles() {
        return numAccessFiles;
    }

    public void setNumAccessFiles(Integer numAccessFiles) {
        this.numAccessFiles = numAccessFiles;
    }

    public Integer getNumOutboundCmds() {
        return numOutboundCmds;
    }

    public void setNumOutboundCmds(Integer numOutboundCmds) {
        this.numOutboundCmds = numOutboundCmds;
    }

    public Integer getIsHostLogin() {
        return isHostLogin;
    }

    public void setIsHostLogin(Integer isHostLogin) {
        this.isHostLogin = isHostLogin;
    }

    public Integer getIsGuestLogin() {
        return isGuestLogin;
    }

    public void setIsGuestLogin(Integer isGuestLogin) {
        this.isGuestLogin = isGuestLogin;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getSrvCount() {
        return srvCount;
    }

    public void setSrvCount(Integer srvCount) {
        this.srvCount = srvCount;
    }

    public Double getSerrorRate() {
        return serrorRate;
    }

    public void setSerrorRate(Double serrorRate) {
        this.serrorRate = serrorRate;
    }

    public Double getSrvSerrorRate() {
        return srvSerrorRate;
    }

    public void setSrvSerrorRate(Double srvSerrorRate) {
        this.srvSerrorRate = srvSerrorRate;
    }

    public Double getRerrorRate() {
        return rerrorRate;
    }

    public void setRerrorRate(Double rerrorRate) {
        this.rerrorRate = rerrorRate;
    }

    public Double getSrvRerrorRate() {
        return srvRerrorRate;
    }

    public void setSrvRerrorRate(Double srvRerrorRate) {
        this.srvRerrorRate = srvRerrorRate;
    }

    public Double getSameSrvRate() {
        return sameSrvRate;
    }

    public void setSameSrvRate(Double sameSrvRate) {
        this.sameSrvRate = sameSrvRate;
    }

    public Double getDiffSrvRate() {
        return diffSrvRate;
    }

    public void setDiffSrvRate(Double diffSrvRate) {
        this.diffSrvRate = diffSrvRate;
    }

    public Double getSrvDiffHostRate() {
        return srvDiffHostRate;
    }

    public void setSrvDiffHostRate(Double srvDiffHostRate) {
        this.srvDiffHostRate = srvDiffHostRate;
    }

    public Integer getDstHostCount() {
        return dstHostCount;
    }

    public void setDstHostCount(Integer dstHostCount) {
        this.dstHostCount = dstHostCount;
    }

    public Integer getDstHostSrvCount() {
        return dstHostSrvCount;
    }

    public void setDstHostSrvCount(Integer dstHostSrvCount) {
        this.dstHostSrvCount = dstHostSrvCount;
    }

    public Double getDstHostSameSrvRate() {
        return dstHostSameSrvRate;
    }

    public void setDstHostSameSrvRate(Double dstHostSameSrvRate) {
        this.dstHostSameSrvRate = dstHostSameSrvRate;
    }

    public Double getDstHostDiffSrvRate() {
        return dstHostDiffSrvRate;
    }

    public void setDstHostDiffSrvRate(Double dstHostDiffSrvRate) {
        this.dstHostDiffSrvRate = dstHostDiffSrvRate;
    }

    public Double getDstHostSameSrcPortRate() {
        return dstHostSameSrcPortRate;
    }

    public void setDstHostSameSrcPortRate(Double dstHostSameSrcPortRate) {
        this.dstHostSameSrcPortRate = dstHostSameSrcPortRate;
    }

    public Double getDstHostSrvDiffHostRate() {
        return dstHostSrvDiffHostRate;
    }

    public void setDstHostSrvDiffHostRate(Double dstHostSrvDiffHostRate) {
        this.dstHostSrvDiffHostRate = dstHostSrvDiffHostRate;
    }

    public Double getDstHostSerrorRate() {
        return dstHostSerrorRate;
    }

    public void setDstHostSerrorRate(Double dstHostSerrorRate) {
        this.dstHostSerrorRate = dstHostSerrorRate;
    }

    public Double getDstHostSrvSerrorRate() {
        return dstHostSrvSerrorRate;
    }

    public void setDstHostSrvSerrorRate(Double dstHostSrvSerrorRate) {
        this.dstHostSrvSerrorRate = dstHostSrvSerrorRate;
    }

    public Double getDstHostRerrorRate() {
        return dstHostRerrorRate;
    }

    public void setDstHostRerrorRate(Double dstHostRerrorRate) {
        this.dstHostRerrorRate = dstHostRerrorRate;
    }

    public Double getDstHostSrvRerrorRate() {
        return dstHostSrvRerrorRate;
    }

    public void setDstHostSrvRerrorRate(Double dstHostSrvRerrorRate) {
        this.dstHostSrvRerrorRate = dstHostSrvRerrorRate;
    }
}
