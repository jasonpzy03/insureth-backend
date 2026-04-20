<#import "base-layout.ftl" as layout>
<#import "components.ftl" as ui>

<@layout.mainLayout title="Your Insureth Policy Is Confirmed" preheader="Flight Policy Purchase Confirmation">
    <@ui.sectionTitle text="Your coverage is now active" />
    
    <@ui.paragraph text="Dear <strong>${username!'Valued Customer'}</strong>," />
    
    <@ui.paragraph text="We are pleased to confirm that your flight delay insurance policy has been successfully purchased and recorded on-chain. Please find the policy summary below for your records." />

    <@ui.summaryTable 
        title="Policy Summary" 
        fields=[
            {"label": "Policy Reference", "value": (policyId!'Pending confirmation')},
            {"label": "Flight", "value": (flightNumber!'-')},
            {"label": "Route", "value": (departureAirportName!'-') + " (" + (departureAirportIata!'-') + ") to " + (arrivalAirportName!'-') + " (" + (arrivalAirportIata!'-') + ")"},
            {"label": "Scheduled Departure", "value": (departureTime!'-')},
            {"label": "Total Paid", "value": (premiumPaidEth!'-') + " " + (currency!'ETH')},
            {"label": "Platform Fee", "value": (platformFeeEth!'-') + " " + (currency!'ETH')},
            {"label": "Coverage Premium", "value": (netPremiumEth!'-') + " " + (currency!'ETH')},
            {"label": "Transaction Hash", "value": (transactionHash!'-'), "breakAll": true}
        ]
    />

    <@ui.actionButton label="View My Policies" link="${managePoliciesLink!'http://localhost:4200/policies'}" />

    <p style="font-size: 14px; line-height: 1.6; margin: 0; color: #555555;">
        Please retain this email for your records. If your insured flight experiences a covered disruption,
        payout handling will follow the terms visible in your policy dashboard.
    </p>
</@layout.mainLayout>
