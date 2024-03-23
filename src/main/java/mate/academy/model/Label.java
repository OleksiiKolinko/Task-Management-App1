package mate.academy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Data
@SQLDelete(sql = "UPDATE labels SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Table(name = "labels")
public class Label {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    @Column(nullable = false)
    private String color;
    @OneToMany
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Fetch(value = FetchMode.JOIN)
    @JoinTable(name = "labels_tasks",
            joinColumns = @JoinColumn(name = "label_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "task_id", nullable = false))
    private Set<Task> tasks = new HashSet<>();
    @Column(name = "is_deleted")
    private boolean isDeleted;
}
