// campaigns.js — handles index.html (campaign list page)

// Base URL for all API calls
const API_BASE = "/api";

// Run when the page is fully loaded
$(document).ready(function () {
    loadCampaigns();
});

/**
 * Fetches all campaigns from the API and renders them on the page.
 * Also calculates and displays stats (total campaigns, total raised, active).
 */
function loadCampaigns() {
    // Show loading spinner, hide other states
    $("#loadingState").show();
    $("#campaignGrid").hide();
    $("#errorState").hide();
    $("#emptyState").hide();
    $("#statsBar").hide();

    // AJAX call to GET /api/campaigns
    $.ajax({
        url: API_BASE + "/campaigns",
        method: "GET",
        success: function (campaigns) {
            $("#loadingState").hide();

            // No campaigns in database yet
            if (campaigns.length === 0) {
                $("#emptyState").show();
                return;
            }

            // Render stats bar
            renderStats(campaigns);
            $("#statsBar").show();

            // Render each campaign as a card
            campaigns.forEach(function (campaign) {
                $("#campaignGrid").append(buildCampaignCard(campaign));
            });

            $("#campaignGrid").show();
        },
        error: function () {
            $("#loadingState").hide();
            $("#errorState").show().fadeIn();
        }
    });
}

/**
 * Calculates and renders the stats bar at the top of the page.
 * @param {Array} campaigns - list of campaign objects from the API
 */
function renderStats(campaigns) {
    const today = new Date().toISOString().split("T")[0];

    // Count active campaigns (deadline not passed)
    const active = campaigns.filter(c => c.deadline >= today).length;

    // Sum all raisedAmount values
    const totalRaised = campaigns.reduce((sum, c) => sum + c.raisedAmount, 0);

    $("#totalCampaigns").text(campaigns.length);
    $("#activeCampaigns").text(active);
    $("#totalRaised").text(formatCurrency(totalRaised));
}

/**
 * Builds a campaign card HTML string from a campaign object.
 * @param {Object} campaign - campaign object from the API
 * @returns {string} HTML string for the card
 */
function buildCampaignCard(campaign) {
    const today = new Date().toISOString().split("T")[0];
    const isExpired = campaign.deadline < today;
    const isFunded = campaign.raisedAmount >= campaign.goalAmount;
    const percent = Math.min(
        Math.round((campaign.raisedAmount / campaign.goalAmount) * 100), 100
    );

    // Determine badge
    let badge = "";
    if (isFunded) {
        badge = '<span class="badge badge-funded">🎉 Funded</span>';
    } else if (isExpired) {
        badge = '<span class="badge badge-expired">Expired</span>';
    } else {
        badge = '<span class="badge badge-active">Active</span>';
    }

    // Progress bar color
    const fillClass = isFunded ? "funded" : "";

    return `
        <div class="campaign-card">
            <div class="campaign-card-body">
                <div class="campaign-card-header">
                    <h3 class="campaign-card-title">${escapeHtml(campaign.title)}</h3>
                    ${badge}
                </div>
                <p class="campaign-card-description">
                    ${escapeHtml(campaign.description)}
                </p>
                <div class="progress-container">
                    <div class="progress-label">
                        <span>${formatCurrency(campaign.raisedAmount)} raised</span>
                        <span>${percent}%</span>
                    </div>
                    <div class="progress-bar-bg">
                        <div class="progress-bar-fill ${fillClass}"
                             style="width: ${percent}%">
                        </div>
                    </div>
                    <div class="progress-label mt-4">
                        <span>Goal: ${formatCurrency(campaign.goalAmount)}</span>
                    </div>
                </div>
                <div class="campaign-card-meta">
                    <span>📅 Deadline: ${formatDate(campaign.deadline)}</span>
                    <span>🕐 ${timeAgo(campaign.createdAt)}</span>
                </div>
            </div>
            <div class="campaign-card-footer">
                <a href="campaign.html?id=${campaign.id}">View Campaign →</a>
            </div>
        </div>
    `;
}

/**
 * Formats a number as USD currency string.
 * @param {number} amount
 * @returns {string} e.g. "$1,500.00"
 */
function formatCurrency(amount) {
    return new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD"
    }).format(amount);
}

/**
 * Formats an ISO date string to a readable format.
 * @param {string} dateStr - e.g. "2026-10-10"
 * @returns {string} e.g. "Oct 10, 2026"
 */
function formatDate(dateStr) {
    return new Date(dateStr + "T00:00:00").toLocaleDateString("en-US", {
        month: "short", day: "numeric", year: "numeric"
    });
}

/**
 * Returns a human-readable time ago string.
 * @param {string} dateTimeStr - ISO datetime string
 * @returns {string} e.g. "3 days ago"
 */
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

/**
 * Escapes HTML special characters to prevent XSS attacks.
 * Always use this when inserting user-provided text into the DOM.
 * @param {string} str
 * @returns {string} safe string
 */
function escapeHtml(str) {
    const div = document.createElement("div");
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
}