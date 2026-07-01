// create-campaign.js — handles create-campaign.html

const API_BASE = "/api";

$(document).ready(function () {

    // Set minimum date for deadline input to tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    $("#deadline").attr("min", tomorrow.toISOString().split("T")[0]);

    // Wire up submit button
    $("#submitCampaign").click(function () {
        submitCampaign();
    });

    // Live preview — update as user types
    $("#campaignTitle").on("input", function () {
        const val = $(this).val().trim();
        $("#previewTitle").text(val || "Your campaign title");
    });

    $("#campaignDescription").on("input", function () {
        const val = $(this).val().trim();
        $("#previewDescription").text(val || "Your campaign description will appear here...");
    });

    $("#goalAmount").on("input", function () {
        const val = parseFloat($(this).val());
        $("#previewGoal").text(val ? formatCurrency(val) : "$0");
    });

    $("#deadline").on("change", function () {
        const val = $(this).val();
        $("#previewDeadline").text(val ? formatDate(val) : "—");
    });
});

/**
 * Validates and submits the create campaign form.
 */
function submitCampaign() {

    // Clear previous errors
    $(".form-error").hide().text("");
    $("#createSuccess").hide();
    $("#createError").hide();

    // Read form values
    const title = $("#campaignTitle").val().trim();
    const description = $("#campaignDescription").val().trim();
    const goalAmount = $("#goalAmount").val().trim();
    const deadline = $("#deadline").val();

    // Client-side validation
    let hasError = false;

    if (!title) {
        $("#titleError").text("Campaign title is required").show();
        hasError = true;
    } else if (title.length > 100) {
        $("#titleError").text("Title must be under 100 characters").show();
        hasError = true;
    }

    if (!description) {
        $("#descriptionError").text("Description is required").show();
        hasError = true;
    }

    if (!goalAmount || parseFloat(goalAmount) < 1) {
        $("#goalError").text("Goal amount must be at least $1").show();
        hasError = true;
    }

    if (!deadline) {
        $("#deadlineError").text("Deadline is required").show();
        hasError = true;
    } else {
        const today = new Date().toISOString().split("T")[0];
        if (deadline <= today) {
            $("#deadlineError").text("Deadline must be a future date").show();
            hasError = true;
        }
    }

    if (hasError) return;

    // Disable button while submitting
    $("#submitCampaign").prop("disabled", true).text("Launching...");

    // Build request body
    const campaignRequest = {
        title: title,
        description: description,
        goalAmount: parseFloat(goalAmount),
        deadline: deadline
    };

    // POST to API
    $.ajax({
        url: API_BASE + "/campaigns",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify(campaignRequest),
        success: function (campaign) {
            $("#createSuccess").show().hide().fadeIn();
            $("#submitCampaign").prop("disabled", true);

            // Redirect to the new campaign's detail page after 1.5 seconds
            setTimeout(function () {
                window.location.href = "campaign.html?id=" + campaign.id;
            }, 1500);
        },
        error: function (xhr) {
            $("#submitCampaign").prop("disabled", false).text("🚀 Launch Campaign");

            let errorMsg = "Something went wrong. Please try again.";
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMsg = xhr.responseJSON.message;
            }
            $("#createError").text(errorMsg).show().hide().fadeIn();
        }
    });
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