package info.papdt.express.helper.model;

public class BaseMessage<T> {

	private int code;
	private T object;

	public final static int CODE_OKAY = 200, CODE_ERROR = -1;

	public BaseMessage(int code, T object) {
		this.code = code;
		this.object = object;
	}

	public BaseMessage(int code) {
		this.code = code;
	}

	public BaseMessage() {

	}

	public int getCode() {
		return this.code;
	}

	public T getData() {
		return this.object;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setData(T object) {
		this.object = object;
	}

}
