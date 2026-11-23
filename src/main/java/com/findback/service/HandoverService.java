package com.findback.service;

import com.findback.dao.ClaimDao;
import com.findback.dao.HandoverDao;
import com.findback.dao.NotificationDao;
import com.findback.dao.ReportDao;
import com.findback.enums.ClaimStatus;
import com.findback.enums.NotificationType;
import com.findback.enums.ReportStatus;
import com.findback.model.Claim;
import com.findback.model.Handover;
import com.findback.model.ItemReport;
import com.findback.model.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Manages the physical handover of items — executes the handover, generates receipt codes,
 * and updates all related statuses.
 */
public class HandoverService {

    private final HandoverDao handoverDao;
    private final ClaimDao claimDao;
    private final ReportDao reportDao;
    private final NotificationDao notificationDao;
    private final Random random;

    public HandoverService() {
        this.handoverDao = new HandoverDao();
        this.claimDao = new ClaimDao();
        this.reportDao = new ReportDao();
        this.notificationDao = new NotificationDao();
        this.random = new Random();
    }

    /**
     * Executes a handover for an approved claim.
     * Generates a unique receipt code, marks claim as COMPLETED,
     * lost report as RESOLVED, found report as RETURNED, and notifies both parties.
     *
     * @param claimId        the approved claim
     * @param staffWitnessId the staff member witnessing the handover
     * @param venue          the handover location
     * @param remarks        any additional remarks
     * @return the created Handover record
     */
    public Handover executeHandover(Long claimId, Long staffWitnessId, String venue, String remarks) {
        Claim claim = claimDao.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + claimId));

        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new IllegalStateException("Handover can only be executed for APPROVED claims. Current status: " + claim.getStatus());
        }

        // Generate unique receipt code: FB-RCV-XXXXXX-MU
        String receiptCode = generateReceiptCode();

        // Load related reports to get user IDs for notifications
        ItemReport lostReport = reportDao.findById(claim.getLostReportId())
                .orElseThrow(() -> new IllegalArgumentException("Lost report not found."));
        ItemReport foundReport = reportDao.findById(claim.getFoundReportId())
                .orElseThrow(() -> new IllegalArgumentException("Found report not found."));

        // Create handover record
        Handover handover = new Handover();
        handover.setClaimId(claimId);
        handover.setLostReportId(claim.getLostReportId());
        handover.setFoundReportId(claim.getFoundReportId());
        handover.setHandedById(foundReport.getUserId());
        handover.setReceivedById(claim.getClaimantId());
        handover.setStaffWitnessId(staffWitnessId);
        handover.setHandoverDate(LocalDateTime.now());
        handover.setVenue(venue);
        handover.setRemarks(remarks);
        handover.setReceiptCode(receiptCode);
        handover.setCompleted(true);

        Long id = handoverDao.save(handover);
        handover.setId(id);

        // Update claim status to COMPLETED
        claim.setStatus(ClaimStatus.COMPLETED);
        claimDao.update(claim);

        // Update report statuses
        reportDao.updateStatus(claim.getLostReportId(), ReportStatus.RESOLVED);
        reportDao.updateStatus(claim.getFoundReportId(), ReportStatus.RETURNED);

        // Notify the person who lost the item
        Notification lostNotif = new Notification();
        lostNotif.setUserId(lostReport.getUserId());
        lostNotif.setTitle("Item Recovered!");
        lostNotif.setMessage("Your lost item \"" + lostReport.getTitle() +
                "\" has been handed over successfully. Receipt code: " + receiptCode);
        lostNotif.setTargetUrl("/handovers/" + id);
        lostNotif.setType(NotificationType.ITEM_RECOVERED);
        lostNotif.setRead(false);
        lostNotif.setCreatedAt(LocalDateTime.now());
        notificationDao.save(lostNotif);

        // Notify the person who found the item
        Notification foundNotif = new Notification();
        foundNotif.setUserId(foundReport.getUserId());
        foundNotif.setTitle("Handover Complete");
        foundNotif.setMessage("The found item \"" + foundReport.getTitle() +
                "\" has been returned to its owner. Receipt code: " + receiptCode + ". Thank you!");
        foundNotif.setTargetUrl("/handovers/" + id);
        foundNotif.setType(NotificationType.ITEM_RECOVERED);
        foundNotif.setRead(false);
        foundNotif.setCreatedAt(LocalDateTime.now());
        notificationDao.save(foundNotif);

        return handover;
    }

    /**
     * Looks up a handover by receipt code.
     */
    public Handover getByReceiptCode(String code) {
        return handoverDao.findByReceiptCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Handover not found with receipt code: " + code));
    }

    /**
     * Gets all handover records.
     */
    public List<Handover> getAll() {
        return handoverDao.findAll();
    }

    /**
     * Gets the total count of recovered (completed handover) items.
     */
    public long countRecovered() {
        return handoverDao.countCompleted();
    }

    /**
     * Generates a unique receipt code in the format: FB-RCV-XXXXXX-MU
     * where XXXXXX is a 6-digit random number.
     */
    private String generateReceiptCode() {
        int code = 100000 + random.nextInt(900000); // 6-digit number
        return "FB-RCV-" + code + "-MU";
    }
}
