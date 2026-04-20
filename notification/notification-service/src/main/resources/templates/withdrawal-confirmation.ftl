<#import "base-layout.ftl" as layout>
<#import "components.ftl" as ui>

<@layout.mainLayout title="Withdrawal Confirmed - Insureth" preheader="Capital Withdrawal Receipt">
    <@ui.sectionTitle text="Your funds have been successfully withdrawn" />
    
    <@ui.paragraph text="Dear <strong>${username!'Valued Investor'}</strong>," />
    
    <@ui.paragraph text="This is a confirmation that your request to withdraw capital from the Insureth Liquidity Pool has been processed successfully. The funds have been transferred to your wallet." />

    <@ui.summaryTable 
        title="Withdrawal Summary" 
        fields=[
            {"label": "Amount Withdrawn", "value": (assetsEth!'-') + " ETH"},
            {"label": "Pool Shares Burned", "value": (sharesBurned!'-')},
            {"label": "Transaction Hash", "value": (transactionHash!'-'), "breakAll": true},
            {"label": "Confirmation Time", "value": confirmationTime!'-'}
        ]
    />

    <@ui.actionButton label="Go to Investor Dashboard" link="${investorDashboardLink!'http://localhost:4200/investor'}" />

    <p style="font-size: 14px; line-height: 1.6; margin: 0; color: #555555;">
        You can continue to provide liquidity or reinvest your earnings at any time through the Insureth Portal. 
        Thank you for supporting our decentralized insurance infrastructure.
    </p>

    <p style="font-size: 14px; line-height: 1.6; margin: 20px 0 0 0; color: #555555; font-style: italic;">
        We hope to see you back soon!
    </p>
</@layout.mainLayout>
