package com.example.annotationprojet.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString @Builder
public class Tache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer ID;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateLimite;

    @OneToMany(mappedBy = "tache", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<coupleTexte> coupleTexte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_id")
    private DataSet data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotateur_id")
    private Annotateur annotateur;

    /**
     * Calcule le pourcentage d'avancement de la tâche
     * @return Le pourcentage d'avancement (0-100)
     */
    public int getProgressPercentage() {
        if (coupleTexte == null || coupleTexte.isEmpty()) {
            return 0;
        }

        int totalCouples = coupleTexte.size();
        int annotatedCouples = 0;

        for (coupleTexte couple : coupleTexte) {
            if (couple.getAnnotation() != null) {
                annotatedCouples++;
            }
        }

        return (int) Math.round((double) annotatedCouples / totalCouples * 100);
    }

    /**
     * Retourne le statut d'avancement de la tâche
     * @return Le statut ("Non commencé", "En cours", "Terminé")
     */
    public String getProgressStatus() {
        int percentage = getProgressPercentage();

        if (percentage == 0) {
            return "Non commencé";
        } else if (percentage == 100) {
            return "Terminé";
        } else {
            return "En cours";
        }
    }
}
