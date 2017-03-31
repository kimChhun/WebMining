package ch.heigvd.wem.data;

public class Document {
	Metadata metadata;
	String content;
	
	public Metadata getMetadata() {
		return metadata;
	}
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + ((metadata == null) ? 0 : metadata.getDocID()));
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
		Document other = (Document) obj;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		} else if (metadata.getDocID() != other.metadata.getDocID())
			return false;
		return true;
	}
}
