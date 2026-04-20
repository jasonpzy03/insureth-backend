<#macro summaryTable title fields>
    <table width="100%" border="0" cellspacing="0" cellpadding="0" style="border: 1px solid #ece7fa; border-radius: 10px; overflow: hidden; margin-bottom: 28px;">
        <tr>
            <td colspan="2" style="background-color: #faf7ff; padding: 16px 20px; font-size: 14px; font-weight: 700; color: #5b2bb0;">
                ${title}
            </td>
        </tr>
        <#list fields as field>
        <tr>
            <td style="padding: 14px 20px; font-size: 14px; color: #6b7280; width: 42%; border-top: 1px solid #f2f0f9;">${field.label}</td>
            <td style="padding: 14px 20px; font-size: 14px; color: #111827; font-weight: 600; border-top: 1px solid #f2f0f9; <#if field.breakAll!false>word-break: break-all;</#if>">
                ${field.value!'-'}
            </td>
        </tr>
        </#list>
    </table>
</#macro>

<#macro actionButton label link>
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
            <td align="center" style="padding: 10px 0 36px 0;">
                <a href="${link}" style="background-color: #8732fb; color: #ffffff; text-decoration: none; padding: 14px 32px; font-size: 16px; font-weight: bold; border-radius: 6px; display: inline-block;">${label}</a>
            </td>
        </tr>
    </table>
</#macro>

<#macro sectionTitle text>
    <h2 style="margin: 0 0 20px 0; font-size: 22px; color: #111111;">${text}</h2>
</#macro>

<#macro paragraph text>
    <p style="font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">${text}</p>
</#macro>
