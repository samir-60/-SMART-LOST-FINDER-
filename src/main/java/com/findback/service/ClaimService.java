package com.findback.service;

import com.findback.dao.ClaimDao;
import com.findback.dao.NotificationDao;
import com.findback.dao.ReportDao;
import com.findback.enums.ClaimStatus;
import com.findback.enums.NotificationType;
import com.findback.enums.ReportStatus;
import com.findback.model.Claim;
import com.findback.model.ItemReport;
import com.findback.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages the claim lifecycle — submission, review (approve/reject), and status queries.
 */
public class ClaimService {

    private final ClaimDao claimDao;
    private final ReportDao reportDao;
    private final NotificationDao notificationDao;

    public ClaimService() {
        this.claimDao = new ClaimDao();
        this.reportDao = new ReportDao();
        this.notificationDao = new NotificationDao();
    }

    /**
     * Submits a new claim for a matched lost-found pair.
     *
     * @param claimantId     the user submitting the claim
     * @param lostReportId   the lost report being claimed
     * @param foundReportId  the found report being claimed
     * @param matchId        the match that links the two reports (may be null)
     * @param proofDescription textual proof of ownership
     * @param proofImagePath optional image proof path
     * @return the created Claim
     */
    public Claim submitClaim(Long claimantId, Long lostReportId, Long foundReportId,
                             Long matchId, String proofDescription, String proofImagePath) {
        if (proofDescription == null || proofDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Proof description is required to submit a claim.");
        }

        // Validate reports exist
        ItemReport lostReport = reportDao.findById(lostReportId)
                .orElseThrow(() -> new IllegalArgumentException("Lost report not found with ID: " + lostReportId));
        ItemReport foundReport = reportDao.findById(foundReportId)
                .orElseThrow(() -> new IllegalArgumentException("Found report not found with ID: " + foundReportId));

        Claim claim = new Claim();
        claim.setClaimantId(claimantId);
        claim.setLostReportId(lostReportId);
        claim.setFoundReportId(foundReportId);
        claim.setMatchId(matchId);
        claim.setProofDescription(proofDescription.trim());
        claim.setProofImagePath(proofImagePath);
        claim.setStatus(ClaimStatus.PENDING);
        claim.setCreatedAt(LocalDateTime.now());

        Long id = claimDao.save(claim);
        claim.setId(id);

        // Update found report status to CLAIM_PENDING
        reportDao.updateStatus(foundReportId, ReportStatus.CLAIM_PENDING);

        // Notify the found report owner that a claim has been submitted
        Notification notification = new Notification();
        notification.setUserId(foundReport.getUserId());
        notification.setTitle("New Claim Submitted");
        notification.setMessage("A claim has been submitted for the found item: \"" +
                foundReport.getTitle() + "\". An administrator will review it shortly.");
        notification.setTargetUrl("/claims/" + id);
        notification.setType(NotificationType.CLAIM_SUBMITTED);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationDao.save(notification);

        return claim;
    }

    /**
     * Reviews a claim — approves or rejects it.
     *
     * @param claimId      the claim to review
     * @param newStatus    the new status (APPROVED or REJECTED)
     * @param adminRemarks administrator's remarks
     * @param reviewerId   the admin performing the review
     * @return the updated Claim
     */
    public Claim reviewClaim(Long claimId, ClaimStatus newStatus, String adminRemarks, Long reviewerId) {
        Claim claim = claimDao.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + claimId));

        claim.setStatus(newStatus);
        claim.setAdminRemarks(adminRemarks);
        claim.setReviewedById(reviewerId);
        claim.setReviewedAt(LocalDateTime.now());
        claimDao.update(claim);

        // If rejected, revert found report to OPEN
        if (newStatus == ClaimStatus.REJECTED) {
            reportDao.updateStatus(claim.getFoundReportId(), ReportStatus.OPEN);
        }

        // Notify the claimant about the review decision
        Notification notification = new Notification();
        notification.setUserId(claim.getClaimantId());

        if (newStatus == ClaimStatus.APPROVED) {
            notification.setTitle("Claim Approved!");
            notification.setMessage("Your claim has been approved. Please proceed to collect your item. " +
                    (adminRemarks != null ? "Remarks: " + adminRemarks : ""));
            notification.setType(NotificationType.CLAIM_APPROVED);
        } else if (newStatus == ClaimStatus.REJECTED) {
            notification.setTitle("Claim Rejected");
            notification.setMessage("Your claim has been rejected. " +
                    (adminRemarks != null ? "Reason: " + adminRemarks : ""));
            notification.setType(NotificationType.CLAIM_REJECTED);
        } else {
            notification.setTitle("Claim Updated");
            notification.setMessage("Your claim status has been updated to: " + newStatus.name());
            notification.setType(NotificationType.GENERAL);
        }

        notification.setTargetUrl("/claims/" + claimId);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationDao.save(notification);

        return claim;
    }

    /**
     * Gets all claims submitted by a specific user.
     */
    public List<Claim> getByClaimant(Long claimantId) {
        return claimDao.findByClaimantId(claimantId);
    }

    /**
     * Gets all claims with PENDING status.
     */
    public List<Claim> getPendingClaims() {
        return claimDao.findByStatus(ClaimStatus.PENDING);
    }

    /**
     * Gets all claims in the system.
     */
    public List<Claim> getAllClaims() {
        return claimDao.findAll();
    }
}
