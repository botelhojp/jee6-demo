package ch.demo.dom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import ch.demo.dom.jpa.JPAPhoneNumberConverter;
import ch.demo.dom.moxy.PhoneNumberAdapter;

/**
 * Models a student.
 * 
 * @author hostettler
 */
@Entity
@NamedQuery(name = "findAllStudentsByFirstName", query = "SELECT s FROM Student s WHERE s.mFirstName = :firstname")
@Table(name = "STUDENTS")
@SecondaryTable(name = "PICTURES", pkJoinColumns = 
@PrimaryKeyJoinColumn(name = "STUDENT_ID", referencedColumnName = "ID"))
@XmlRootElement(name = "student")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://ch.demo.app")
public class Student implements Serializable {

    /** The serial-id. */
    private static final long serialVersionUID = -6146935825517747043L;

    /** The unique id. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mId;

    /** The student last name. */
    @Column(name = "LAST_NAME", length = 35)
    @XmlElement(name = "last_name")
    private String mLastName;

    /** The student first name. */
    @Column(name = "FIRST_NAME", nullable = false, length = 35)
    private String mFirstName;

    /** The student birth date. */
    @Column(name = "BIRTH_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date mBirthDate;

    /** The student phone number. */
    @Column(name = "PHONE_NUMBER")
    @Converter(name = "phoneConverter", converterClass = JPAPhoneNumberConverter.class)
    @Convert("phoneConverter")
    @XmlJavaTypeAdapter(PhoneNumberAdapter.class)
    private PhoneNumber mPhoneNumber;

    /** The student's gender. */
    private transient Gender mGender;

    /** The address of the student. */
    @Embedded
    private Address mAddress;

    /** The set of grades of the student. */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "mStudent")
    @OrderBy("mDiscipline DSC")
    private List<Grade> mGrades;

    /** Alternative representation of the set of grades of the student. */
    @ElementCollection
    @CollectionTable(name = "GRADES", joinColumns = @JoinColumn(name = "STUDENT_ID"))
    @MapKeyColumn(name = "Discipline")
    @Column(name = "GRADE")
    private Map<Discipline, Integer> mAlternativeGrades;

    /** A picture of the student. */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(table = "PICTURES", name = "PICTURE", nullable = false)
    private byte[] mPicture;

    /** The Student's badge. */
    @OneToOne(mappedBy = "mStudent")
    private Badge mBadge;

    /**
     * Empty (default) constructor.
     */
    public Student() {
        this.mGrades = new ArrayList<Grade>();
        for (Discipline d : Discipline.values()) {
            Grade g = new Grade(d);
            this.mGrades.add(g);
        }

    }

    /**
     * @return the phoneNumber
     */
    public final PhoneNumber getPhoneNumber() {
        return mPhoneNumber;
    }

    /**
     * @param phoneNumber
     *            the phoneNumber to set
     */
    public final void setPhoneNumber(final PhoneNumber phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    /**
     * 
     * @param lastName
     *            The student last name.
     * @param firstName
     *            The student first name.
     * @param birthDate
     *            The student birth date.
     */
    public Student(final String lastName, final String firstName,
            final Date birthDate) {
        this();
        this.mFirstName = firstName;
        this.mLastName = lastName;
        this.mBirthDate = birthDate;
        validate();
    }

    /**
     * @return an unique identifier
     */
    public String getKey() {
        if (this.mId != null) {
            return String.valueOf(this.mId);
        } else {
            return String.valueOf(this.hashCode());
        }
    }

    /**
     * @return the lastName
     */
    public final String getLastName() {
        return mLastName;
    }

    /**
     * @param lastName
     *            the lastName to set
     */
    public final void setLastName(final String lastName) {
        mLastName = lastName;
    }

    /**
     * @return the firstName
     */
    public final String getFirstName() {
        return mFirstName;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public final void setFirstName(final String firstName) {
        mFirstName = firstName;
    }

    /**
     * @return the birthDate
     */
    public final Date getBirthDate() {
        return mBirthDate;
    }

    /**
     * @param birthDate
     *            the birthDate to set
     */
    public final void setBirthDate(final Date birthDate) {
        mBirthDate = birthDate;
    }

    /**
     * @return the grade
     */
    public final Float getAvgGrade() {
        Float avg = 0.0f;
        for (Grade grade : this.mGrades) {
            if (grade.getGrade() != null) {
                Float f = grade.getGrade().floatValue();
                avg += f;
            }
        }
        return avg / this.mGrades.size();
    }

    /**
     * @return the grades
     */
    public final List<Grade> getGrades() {
        return mGrades;
    }

    /**
     * @return the actual list of discipline for this student.
     */
    public final Discipline[] getDisciplines() {
        return Discipline.values();
    }

    /**
     * Validates the current state of the student information.
     */
    public final void validate() {
        if (this.getFirstName() == null) {
            throw new IllegalArgumentException("Firstname is mandatory");
        }
        if (this.getLastName() == null) {
            throw new IllegalArgumentException("Lastname is mandatory");
        }
        if (this.getBirthDate() == null) {
            throw new IllegalArgumentException("Birthdate is mandatory");
        }
    }

    /**
     * @return the gender
     */
    public final Gender getGender() {
        return mGender;
    }

    /**
     * @param gender
     *            the gender to set
     */
    public final void setGender(final Gender gender) {
        mGender = gender;
    }

    @Override
    public int hashCode() {
        if (this.mLastName == null) {
            return -1;
        } else {
            return this.mLastName.hashCode() ^ this.mFirstName.hashCode()
                    ^ this.mBirthDate.hashCode();
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Student) {
            if (this.mLastName.equals(((Student) obj).mLastName)
                    && this.mFirstName.equals(((Student) obj).mFirstName)
                    && this.mBirthDate.equals(((Student) obj).mBirthDate)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Student [mId=" + mId + ", mLastName=" + mLastName
                + ", mFirstName=" + mFirstName + ", mBirthDate=" + mBirthDate
                + ", mPhoneNumber=" + mPhoneNumber + ", mGrades=" + mGrades
                + "]";
    }

    /**
     * @return the address
     */
    public final Address getAddress() {
        return mAddress;
    }

    /**
     * @param address
     *            the address to set
     */
    public final void setAddress(final Address address) {
        mAddress = address;
    }

    /**
     * @return the picture
     */
    public final byte[] getPicture() {
        return mPicture;
    }

    /**
     * @param picture
     *            the picture to set
     */
    public void setPicture(final byte[] picture) {
        mPicture = picture;
    }

    /**
     * @return the badge
     */
    public Badge getBadge() {
        return mBadge;
    }

    /**
     * @param badge
     *            the badge to set
     */
    public void setBadge(final Badge badge) {
        mBadge = badge;
    }

    /**
     * @return the alternativeGrades
     */
    public Map<Discipline, Integer> getAlternativeGrades() {
        return mAlternativeGrades;
    }

}
