package org.forpdi.planning.structure.goals.history;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.SimpleIdentifiable;
import org.forpdi.core.user.User;
import org.forpdi.planning.structure.StructureLevelInstance;

@Entity(name = GoalJustificationHistory.TABLE)
@Table(name = GoalJustificationHistory.TABLE)
public class GoalJustificationHistory implements SimpleIdentifiable {
	private static final long serialVersionUID = 1L;
	public static final String TABLE = "fpdi_goal_justification_history";

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Long id;
	
	@Column(nullable = true, length = 4000)
	private String justification;

	@Column(nullable = true)
	private Double reachedValue;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Date updatedAt = new Date();
	
	@ManyToOne(targetEntity = StructureLevelInstance.class, optional = false)
	private StructureLevelInstance levelInstance;

	@ManyToOne(targetEntity = User.class, optional = false)
	private User user;
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public Double getReachedValue() {
		return reachedValue;
	}

	public void setReachedValue(Double reachedValue) {
		this.reachedValue = reachedValue;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public StructureLevelInstance getLevelInstance() {
		return levelInstance;
	}

	public void setLevelInstance(StructureLevelInstance levelInstance) {
		this.levelInstance = levelInstance;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
		
	public static class JustificationAndReachedValue {
		private String justification;
		private Double reachedValue;

		public String getJustification() {
			return justification;
		}
		public void setJustification(String justification) {
			this.justification = justification;
		}
		public Double getReachedValue() {
			return reachedValue;
		}
		public void setReachedValue(Double reachedValue) {
			this.reachedValue = reachedValue;
		}
		public void setValues(String justification, Double reachedValue) {
			this.justification = justification;
			this.reachedValue = reachedValue;
		}
		public boolean isEmpty() {
			return GeneralUtils.isEmpty(justification) && reachedValue == null;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((justification == null) ? 0 : justification.hashCode());
			result = prime * result + ((reachedValue == null) ? 0 : reachedValue.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JustificationAndReachedValue other = (JustificationAndReachedValue) obj;
			if (justification == null) {
				if (other.justification != null)
					return false;
			} else if (!justification.equals(other.justification))
				return false;
			if (reachedValue == null) {
				if (other.reachedValue != null)
					return false;
			} else if (!reachedValue.equals(other.reachedValue))
				return false;
			return true;
		}
	}
}
