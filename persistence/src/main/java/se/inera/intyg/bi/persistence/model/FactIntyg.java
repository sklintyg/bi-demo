/**
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of bi (https://github.com/sklintyg/bi).
 *
 * bi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.bi.persistence.model;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * Created by eriklupander on 2016-10-26.
 */
// @Entity
//@Table(name = "fact_intyg")
public class FactIntyg {

    /**
     * Id of the certificate.
     */
    @Id
    @Column(name = "ID")
    private String id;

    /**
     * Type of the certificate.
     */
    @Column(name = "CERTIFICATE_TYPE", nullable = false)
    private String type;

    /**
     * Name of the doctor that signed the certificate.
     */
    @Column(name = "SIGNING_DOCTOR_ID", nullable = false)
    private String signingDoctorId;

    /**
     * Name of the doctor that signed the certificate.
     */
    @Column(name = "SIGNING_DOCTOR_NAME", nullable = false)
    private String signingDoctorName;

    /**
     * Date and time when the certificate was signed.
     */
    @Column(name = "SIGNING_DATETIME", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime signingDateTime;

    /**
     * Id of care unit.
     */
    @Column(name = "CARE_UNIT_ID", nullable = false)
    private String careUnitId;

    /**
     * Name of care unit.
     */
    @Column(name = "CARE_UNIT_NAME", nullable = false)
    private String careUnitName;

    /**
     * Id of care giver.
     */
    @Column(name = "CARE_GIVER_ID", nullable = false)
    private String careGiverId;

    /**
     * Civic registration number for patient.
     */
    @Column(name = "PATIENT_BIRTHDATE", nullable = false)
    private String civicRegistrationNumber;


    /**
     * Main diagnose code.
     */
    @Column(name = "DIAGNOSE_CODE", nullable = false)
    private String diagnoseCode;

    /**
     * Total sjukskrivningslängd
     */
    @Column(name = "TOTAL_DURATION", nullable = false)
    private Integer totalDuration;


    private FactIntyg() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSigningDoctorId() {
        return signingDoctorId;
    }

    public void setSigningDoctorId(String signingDoctorId) {
        this.signingDoctorId = signingDoctorId;
    }

    public String getSigningDoctorName() {
        return signingDoctorName;
    }

    public void setSigningDoctorName(String signingDoctorName) {
        this.signingDoctorName = signingDoctorName;
    }

    public LocalDateTime getSigningDateTime() {
        return signingDateTime;
    }

    public void setSigningDateTime(LocalDateTime signingDateTime) {
        this.signingDateTime = signingDateTime;
    }

    public String getCareUnitId() {
        return careUnitId;
    }

    public void setCareUnitId(String careUnitId) {
        this.careUnitId = careUnitId;
    }

    public String getCareUnitName() {
        return careUnitName;
    }

    public void setCareUnitName(String careUnitName) {
        this.careUnitName = careUnitName;
    }

    public String getCareGiverId() {
        return careGiverId;
    }

    public void setCareGiverId(String careGiverId) {
        this.careGiverId = careGiverId;
    }

    public String getCivicRegistrationNumber() {
        return civicRegistrationNumber;
    }

    public void setCivicRegistrationNumber(String civicRegistrationNumber) {
        this.civicRegistrationNumber = civicRegistrationNumber;
    }

    public String getDiagnoseCode() {
        return diagnoseCode;
    }

    public void setDiagnoseCode(String diagnoseCode) {
        this.diagnoseCode = diagnoseCode;
    }

    public Integer getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Integer totalDuration) {
        this.totalDuration = totalDuration;
    }
}
