package se.skltp.cooperation.web.rest.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * A problem details object.
 *
 * @see <a href="http://tools.ietf.org/html/draft-nottingham-http-problem-00">draft-nottingham-http-problem-00</a>
 *
 * @author Peter Merikan
 */
@XmlRootElement(name="problem")
@JsonInclude(Include.NON_EMPTY)
public class ProblemDetail {

	/**
	 * An absolute URI [RFC3986] that identifies the problem type. When
	 * dereferenced, it SHOULD provide human-readable documentation for
	 * the problem type (e.g., using HTML [W3C.REC-html401-19991224]).
	 * When this member is not present, its value is assumed to be "about:blank".
	 */
	private URI type;
	/**
	 * A short, human-readable summary of the problem type.  It SHOULD NOT
	 * change from occurrence to occurrence of the problem, except for purposes
	 * of localisation.
	 */
	private String title;
	/**
	 * The HTTP status code ([RFC7231], Section 6) generated by the origin server
	 * for this occurrence of the problem.
	 */
	private Integer status;
	/**
	 * An human readable explanation specific to this occurrence of the problem.
	 */
	private String detail;
	/**
	 * An absolute URI that identifies the specific occurrence of the problem.  It
	 * may or may not yield further information if dereferenced.
	 */
	private String instance;

	public URI getType() {
		return type;
	}

	public void setType(URI type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}
}
