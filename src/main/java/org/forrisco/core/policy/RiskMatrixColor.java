package org.forrisco.core.policy;

public enum RiskMatrixColor {
	RED(0, "Vermelho", "#f1705f"),
	GRAY(1, "Cinza", "#c8c8c8"),
	PINK(2, "Rosa", "#dd90be"),
	ORANGE(3, "Laranja", "#fcbc70"),
	GREEN(4, "Verde", "#9cdc9c"),
	BLUE(5, "Azul", "#8cbcdc"),
	YELLOW(6, "Amarelo", "#fff230"),
	DEFAULT(7, "Default", "#F3F2F3");
	
	private final int id;
	private final String name;
	private final String code;
	
	private RiskMatrixColor(int id, String name, String code) {
		this.id = id;
		this.name = name;
		this.code = code;
	}
	
	public static RiskMatrixColor getDefaultColor() {
		return DEFAULT;
	}
	
	public static String getCodeById(int id) {
		for (RiskMatrixColor value : values()) {
			if (value.id == id) {
				return value.code;
			}
		}
		return getDefaultColor().code;
	}

	public static String getNameById(int id) {
		for (RiskMatrixColor value : values()) {
			if (value.id == id) {
				return value.name;
			}
		}
		return getDefaultColor().name;
	}	
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}
}
