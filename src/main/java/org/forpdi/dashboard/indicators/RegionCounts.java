package org.forpdi.dashboard.indicators;

import java.io.Serializable;

public class RegionCounts implements Serializable {
    private Long regionId;
    private String regionName;
    private long universityCount;
    private long instituteCount;
	private long otherCount;

	public RegionCounts(Long regionId, String regionName, long universityCount, long instituteCount, long otherCount) {
        this.regionId = regionId;
        this.regionName = regionName;
        this.universityCount = universityCount;
        this.instituteCount = instituteCount;
        this.otherCount = otherCount;
    }
	public Long getRegionId() {
		return regionId;
	}
	public void setRegionId(Long regionId) {
		this.regionId = regionId;
	}
	public String getRegionName() {
		return regionName;
	}
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	public long getUniversityCount() {
		return universityCount;
	}
	public void setUniversityCount(long universityCount) {
		this.universityCount = universityCount;
	}
	public long getInstituteCount() {
		return instituteCount;
	}
	public void setInstituteCount(long instituteCount) {
		this.instituteCount = instituteCount;
	}
	public long getCompaniesCount() {
		return universityCount + instituteCount;
	}
	public long getOtherCount() {
        return otherCount;
    }
    public void setOtherCount(long otherCount) {
        this.otherCount = otherCount;
    }
}
