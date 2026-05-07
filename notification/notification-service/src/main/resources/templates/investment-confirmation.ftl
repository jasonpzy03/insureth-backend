<#import "base-layout.ftl" as layout>
<#import "components.ftl" as ui>

<@layout.mainLayout title="Investment Confirmed - Insureth" preheader="Pool Liquidity Provision Receipt">
    <@ui.sectionTitle text="Your liquidity has been added to the pool" />
    
    <@ui.paragraph text="Dear <strong>${username!'Valued Investor'}</strong>," />
    
    <@ui.paragraph text="Thank you for providing capital to the Insureth Liquidity Pool. Your contribution helps decentralized travel protection grow while allowing you to earn a share of the premium revenue." />

    <@ui.summaryTable 
        title="Investment Summary" 
        fields=[
            {"label": "Amount Provided", "value": (assetsEth!'-') + " ETH"},
            {"label": "Pool Shares Minted", "value": (sharesMinted!'-')},
            {"label": "Transaction Hash", "value": (transactionHash!'-'), "breakAll": true},
            {"label": "Confirmation Time", "value": confirmationTime!'-'}
        ]
    />

    <@ui.actionButton label="View Investor Dashboard" link="${investorDashboardLink!'http://localhost:4200/investor'}" />

    <p style="font-size: 14px; line-height: 1.6; margin: 0; color: #555555;">
        You can monitor your investment performance, earned premiums, and current pool state at any time via the Investor Dashboard. 
        Withdrawals are subject to free liquidity availability as defined in our protocol terms.
    </p>

    <p style="font-size: 14px; line-height: 1.6; margin: 20px 0 0 0; color: #555555; font-style: italic;">
        Thank you for being part of the future of parametric insurance.
    </p>
</@layout.mainLayout>
