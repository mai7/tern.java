package tern.server.protocol.completions;

import java.util.ArrayList;
import java.util.List;

import tern.utils.StringUtils;

public class TernCompletionItem {

	private final String name;
	private final String type;
	private final String origin;

	private final String signature;
	private final boolean function;
	private boolean array;
	private String jsType;
	private List<Parameter> parameters;

	public TernCompletionItem(String name, String type, String origin) {
		this.name = name;
		this.type = type;
		this.origin = origin;
		this.parameters = null;
		StringBuilder currentParam = null;
		boolean currentParamRequired = true;
		StringBuilder signature = new StringBuilder(name);
		boolean typeParsing = false;
		this.jsType = type;
		if (!StringUtils.isEmpty(type)) {
			this.function = type.startsWith("fn(");
			if (function) {
				signature.append("(");
				int bracket = 0;
				String afterStartFn = type.substring(2, type.length());
				int i = 0;
				for (i = 0; i < afterStartFn.length(); i++) {
					char c = afterStartFn.charAt(i);
					switch (c) {
					case '(':
						bracket++;
						break;
					case ')':
						bracket--;
						break;
					default:
						if (bracket == 1) {
							if (typeParsing) {
								if (c == ',')
									typeParsing = false;
							} else {
								if (currentParam == null) {
									if (c != ' ' && c != '?') {
										currentParam = new StringBuilder();
										currentParam.append(c);
									}
								} else {
									if (c == ':') {
										typeParsing = true;
										if (parameters == null) {
											parameters = new ArrayList<Parameter>();
										} else {
											signature.append(", ");
										}
										signature.append(currentParam
												.toString());
										parameters.add(new Parameter(
												currentParam.toString(),
												currentParamRequired));
										currentParam = null;
									} else {
										if (c != ' ' && c != '?') {
											currentParam.append(c);
										}
									}
								}
							}
						}
					}
					if (bracket == 0)
						break;
				}
				signature.append(")");
				StringBuilder s = null;
				for (int j = i + 1; j < afterStartFn.length(); j++) {
					char c = afterStartFn.charAt(j);
					if (s != null) {
						s.append(c);
					} else {
						if (c == '>') {
							s = new StringBuilder();
						}
					}
				}
				jsType = s != null ? s.toString().trim() : null;
			} else {
				this.array = type.indexOf("[") != -1;
			}
		} else {
			this.function = false;
			this.array = false;
		}
		this.signature = signature.toString();
	}

	public String getText() {
		if (StringUtils.isEmpty(origin) && StringUtils.isEmpty(jsType)) {
			return signature;
		}
		StringBuilder text = new StringBuilder(signature);

		if (!StringUtils.isEmpty(jsType)) {
			text.append(" : ");
			text.append(jsType);
		}
		if (!StringUtils.isEmpty(origin)) {
			text.append(" - ");
			text.append(origin);
		}
		return text.toString();
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public boolean isFunction() {
		return function;
	}

	public boolean isArray() {
		return array;
	}

	public String getName() {
		return name;
	}

	public String getOrigin() {
		return origin;
	}

	public String getSignature() {
		return signature;
	}

	public String getType() {
		return type;
	}

	public String getJsType() {
		return jsType;
	}
}
