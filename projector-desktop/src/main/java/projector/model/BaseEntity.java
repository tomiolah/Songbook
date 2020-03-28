package projector.model;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

public abstract class BaseEntity extends AbstractModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @DatabaseField(generatedId = true, index = true)
    private Long id;

    public BaseEntity() {
        this((Long) null);
    }

    public BaseEntity(Long id) {
        super();
        this.id = id;
    }

    public BaseEntity(BaseEntity other) {
        super(other);
        this.id = other.id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
