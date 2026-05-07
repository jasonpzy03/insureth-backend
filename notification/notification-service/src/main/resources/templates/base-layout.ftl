<#macro mainLayout title preheader="">
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>
</head>
<body style="margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f9f9fb; color: #333333;">
    <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #f9f9fb; padding: 40px 0;">
        <tr>
            <td align="center">
                <table width="600" border="0" cellspacing="0" cellpadding="0" style="background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05);">
                    <!-- Header Sector -->
                    <tr>
                        <td align="center" style="background-color: #f4eeff; padding: 40px 20px; border-bottom: 3px solid #8732fb;">
                            <h1 style="margin: 0; color: #8732fb; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">Insureth</h1>
                            <p style="margin: 10px 0 0; color: #6b5a8e; font-size: 16px;">${preheader}</p>
                        </td>
                    </tr>

                    <!-- Body Sector -->
                    <tr>
                        <td style="padding: 40px 40px 20px 40px;">
                            <#nested>
                        </td>
                    </tr>

                    <!-- Footer Sector -->
                    <tr>
                        <td align="center" style="padding: 30px 40px; background-color: #f7f7f9; border-top: 1px solid #eeeeee;">
                            <p style="font-size: 12px; color: #999999; margin: 0 0 10px 0; line-height: 1.5;">
                                This is an automated email sent from a secured server. Please do not reply directly to this message.
                            </p>
                            <p style="font-size: 12px; color: #999999; margin: 0;">
                                &copy; 2026 Insureth Parametric Web3 Insurance. All rights reserved.
                            </p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
</#macro>
