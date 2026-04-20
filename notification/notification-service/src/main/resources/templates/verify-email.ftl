<#import "base-layout.ftl" as layout>
<#import "components.ftl" as ui>

<@layout.mainLayout title="Verify Your Insureth Account" preheader="Next-Generation Parametric Insurance">
    <@ui.sectionTitle text="Account Verification Required" />
    
    <@ui.paragraph text="Dear <strong>${username}</strong>," />
    
    <@ui.paragraph text="Thank you for registering a Client Portal account with Insureth. To ensure the security of your account and complete your profile registration, please verify your email address by clicking the button below." />
    
    <@ui.actionButton label="Verify Email Address" link="${verificationLink}" />

    <p style="font-size: 14px; line-height: 1.6; margin: 0 0 10px 0; color: #555555;">
        If the button above does not work, please copy and paste the following URL securely into your web browser:
    </p>
    <p style="font-size: 13px; line-height: 1.6; margin: 0 0 30px 0; word-break: break-all;">
        <a href="${verificationLink}" style="color: #8732fb;">${verificationLink}</a>
    </p>
    
    <p style="font-size: 14px; line-height: 1.6; margin: 0; color: #555555;">
        This link will safely expire in exactly 24 hours. If you did not initiate this request, you may safely ignore this email.
    </p>
</@layout.mainLayout>
