// campaign.js — handles campaign.html (campaign detail page)

const API_BASE = "/api";
let campaignId = null;
let campaignData = null;

$(document).ready(function () {

    // Read campaign ID from URL query string e.g. campaign.html?id=1
    const params = new URLSearchParams(window.location.search);
    campaignId = params.get("id");

    // No ID in URL — redirect to homepage
    if (!campaignId) {
        window.location.href = "index.html";
        return;
    }

    // Load campaign and donors
    loadCampaign();

    // Wire up donation form submit button
    $("#submitDonation").click(function () {
        submitDonation();
    });
});

/**
 * Fetches campaign details from the API and renders the page.
 */
function loadCampaign() {
    $.ajax({
        url: API_BASE + "/campaigns/" + campaignId,
        method: "GET",
        success: function (campaign) {
            campaignData = campaign;
            renderCampaign(campaign);
            loadDonors();
            $("#loadingState").hide();
            $("#pageContent").fadeIn();
        },
        error: function () {
            $("#loadingState").hide();
            $("#errorState").show();
        }
    });
}

/**
 * Renders campaign data into the page.
 * @param {Object} campaign - campaign object from the API
 */
function renderCampaign(campaign) {
    const today = new Date().toISOString().split("T")[0];
    const isExpired = campaign.deadline < today;
    const isFunded = campaign.raisedAmount >= campaign.goalAmount;
    const percent = Math.min(
        Math.round((campaign.raisedAmount / campaign.goalAmount) * 100), 100
    );
    const daysLeft = Math.max(
        Math.ceil((new Date(campaign.deadline) - new Date()) / (1000 * 60 * 60 * 24)), 0
    );

    // Update page title
    document.title = campaign.title + " — GiveBridge";

    // Header
    $("#campaignTitle").text(campaign.title);
    $("#campaignDeadline").text(formatDate(campaign.deadline));

    // Badge
    let badge = "";
    if (isFunded) {
        badge = '<span class="badge badge-funded">🎉 Goal Reached!</span>';
    } else if (isExpired) {
        badge = '<span class="badge badge-expired">Expired</span>';
    } else {
        badge = '<span class="badge badge-active">Active</span>';
    }
    $("#campaignBadge").html(badge);

    // Description
    $("#campaignDescription").text(campaign.description);

    // Progress
    $("#raisedAmount").text(formatCurrency(campaign.raisedAmount));
    $("#goalAmount").text(formatCurrency(campaign.goalAmount));
    $("#percentLabel").text(percent + "% funded");
    $("#progressBarFill").css("width", percent + "%");

    if (isFunded) {
        $("#progressBarFill").addClass("funded");
        $("#remainingLabel").text("🎉 Goal reached!");
    } else {
        const remaining = campaign.goalAmount - campaign.raisedAmount;
        $("#remainingLabel").text(formatCurrency(remaining) + " to go");
    }

    // Days left
    if (isExpired) {
        $("#daysLeft").text("Ended");
    } else {
        $("#daysLeft").text(daysLeft);
    }

    // Show/hide donation form based on campaign status
    if (isExpired) {
        $("#donationForm").hide();
        $("#expiredMessage").show();
    }
}

/**
 * Fetches and renders the donor list for this campaign.
 */
function loadDonors() {
    $("#donorsLoading").show();
    $("#donorList").empty();
    $("#noDonors").hide();

    $.ajax({
        url: API_BASE + "/donations/campaign/" + campaignId,
        method: "GET",
        success: function (donations) {
            $("#donorsLoading").hide();
            $("#donorCount").text(donations.length);

            if (donations.length === 0) {
                $("#noDonors").show();
                return;
            }

            donations.forEach(function (donation) {
                $("#donorList").append(buildDonorItem(donation));
            });
        },
        error: function () {
            $("#donorsLoading").hide();
            $("#noDonors").show();
        }
    });
}

/**
 * Builds a donor list item HTML string.
 * @param {Object} donation - donation object from the API
 * @returns {string} HTML string
 */
function buildDonorItem(donation) {
    // Get first letter of name for avatar
    const initial = donation.name.charAt(0).toUpperCase();
    const message = donation.optionalMessage
        ? escapeHtml(donation.optionalMessage)
        : "No message";

    return `
        <div class="donor-item">
            <div class="donor-avatar">${initial}</div>
            <div class="donor-info">
                <div class="donor-name">${escapeHtml(donation.name)}</div>
                <div class="donor-message">"${message}"</div>
            </div>
            <div style="text-align: right; flex-shrink: 0;">
                <div class="donor-amount">${formatCurrency(donation.amount)}</div>
                <div class="donor-time">${timeAgo(donation.donatedAt)}</div>
            </div>
        </div>
    `;
}

/**
 * Validates and submits the donation form.
 */
function submitDonation() {

    // Clear previous errors
    $(".form-error").hide().text("");
    $("#donationSuccess").hide();
    $("#donationError").hide();

    // Read form values
    const name = $("#donorName").val().trim();
    const email = $("#donorEmail").val().trim();
    const amount = $("#donationAmount").val().trim();
    const message = $("#donorMessage").val().trim();

    // Client-side validation
    let hasError = false;

    if (!name) {
        $("#nameError").text("Name is required").show();
        hasError = true;
    }

    if (!email || !isValidEmail(email)) {
        $("#emailError").text("A valid email address is required").show();
        hasError = true;
    }

    if (!amount || parseFloat(amount) < 1) {
        $("#amountError").text("Donation amount must be at least $1").show();
        hasError = true;
    }

    if (hasError) return;

    // Disable button while submitting
    $("#submitDonation").prop("disabled", true).text("Processing...");

    // Build request body
    const donationRequest = {
        name: name,
        email: email,
        amount: parseFloat(amount),
        optionalMessage: message || null,
        campaignId: parseInt(campaignId)
    };

    // POST to API
    $.ajax({
        url: API_BASE + "/donations",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify(donationRequest),
        success: function (response) {
            // Show success message
            $("#donationSuccess").show().hide().fadeIn();

            // Clear form
            $("#donorName").val("");
            $("#donorEmail").val("");
            $("#donationAmount").val("");
            $("#donorMessage").val("");

            // Re-enable button
            $("#submitDonation").prop("disabled", false).text("💛 Donate Now");

            // Refresh campaign stats and donor list
            refreshCampaignStats();
            loadDonors();
        },
        error: function (xhr) {
            $("#submitDonation").prop("disabled", false).text("💛 Donate Now");

            // Show specific error message from API if available
            let errorMsg = "Something went wrong. Please try again.";
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMsg = xhr.responseJSON.message;
            }
            $("#donationError").text(errorMsg).show().hide().fadeIn();
        }
    });
}

/**
 * Refreshes just the campaign funding stats after a donation.
 * Avoids reloading the whole page.
 */
function refreshCampaignStats() {
    $.ajax({
        url: API_BASE + "/campaigns/" + campaignId,
        method: "GET",
        success: function (campaign) {
            campaignData = campaign;
            const percent = Math.min(
                Math.round((campaign.raisedAmount / campaign.goalAmount) * 100), 100
            );
            const isFunded = campaign.raisedAmount >= campaign.goalAmount;

            $("#raisedAmount").text(formatCurrency(campaign.raisedAmount));
            $("#progressBarFill").css("width", percent + "%");
            $("#percentLabel").text(percent + "% funded");

            if (isFunded) {
                $("#progressBarFill").addClass("funded");
                $("#remainingLabel").text("🎉 Goal reached!");
                $("#campaignBadge").html(
                    '<span class="badge badge-funded">🎉 Goal Reached!</span>'
                );
            } else {
                const remaining = campaign.goalAmount - campaign.raisedAmount;
                $("#remainingLabel").text(formatCurrency(remaining) + " to go");
            }
        }
    });
}

/**
 * Validates an email address format.
 * @param {string} email
 * @returns {boolean}
 */
function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// ── Shared utility functions ──────────────────────────────

function formatCurrency(amount) {
    return new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD"
    }).format(amount);
}

function formatDate(dateStr) {
    return new Date(dateStr + "T00:00:00").toLocaleDateString("en-US", {
        month: "short", day: "numeric", year: "numeric"
    });
}

function timeAgo(dateTimeStr) {
    const date = new Date(dateTimeStr);
    const now = new Date();
    const diffDays = Math.floor((now - date) / (1000 * 60 * 60 * 24));
    if (diffDays === 0) return "Today";
    if (diffDays === 1) return "Yesterday";
    if (diffDays < 30) return `${diffDays} days ago`;
    if (diffDays < 365) return `${Math.floor(diffDays / 30)} months ago`;
    return `${Math.floor(diffDays / 365)} years ago`;
}

function escapeHtml(str) {
    const div = document.createElement("div");
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
}