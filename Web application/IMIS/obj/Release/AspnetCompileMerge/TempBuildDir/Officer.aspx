<%-- Copyright (c) 2016-2017 Swiss Agency for Development and Cooperation (SDC)

The program users must agree to the following terms:

Copyright notices
This program is free software: you can redistribute it and/or modify it under the terms of the GNU AGPL v3 License as published by the 
Free Software Foundation, version 3 of the License.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU AGPL v3 License for more details www.gnu.org.

Disclaimer of Warranty
There is no warranty for the program, to the extent permitted by applicable law; except when otherwise stated in writing the copyright 
holders and/or other parties provide the program "as is" without warranty of any kind, either expressed or implied, including, but not 
limited to, the implied warranties of merchantability and fitness for a particular purpose. The entire risk as to the quality and 
performance of the program is with you. Should the program prove defective, you assume the cost of all necessary servicing, repair or correction.

Limitation of Liability 
In no event unless required by applicable law or agreed to in writing will any copyright holder, or any other party who modifies and/or 
conveys the program as permitted above, be liable to you for damages, including any general, special, incidental or consequential damages 
arising out of the use or inability to use the program (including but not limited to loss of data or data being rendered inaccurate or losses 
sustained by you or third parties or a failure of the program to operate with any other programs), even if such holder or other party has been 
advised of the possibility of such damages.

In case of dispute arising out or in relation to the use of the program, it is subject to the public law of Switzerland. The place of jurisdiction is Berne.--%>


<%@ Page Language="vb" AutoEventWireup="false" MasterPageFile="~/IMIS.Master" CodeBehind="Officer.aspx.vb" Inherits="IMIS.Officer" 
    title='<%$ Resources:Resource,L_OFFICERS %>' %>

<%@ Register Assembly="AjaxControlToolkit" Namespace="AjaxControlToolkit" TagPrefix="asp" %>
<asp:Content ID="Head" runat="server" ContentPlaceHolderID="head">
    <style type="text/css">
        /*.mGrid tr:hover
        {
            background-color:#ffd800;
        }*/
        .highlight
        {
            font-weight:bold;
        }
    </style>

</asp:Content>
<asp:Content ID="Content1" ContentPlaceHolderID="Body" runat="Server">

    
    <script type="text/javascript">

        //$(document).ready(function () {
        function pageLoadExtend() {
            //On load toggle villages
            OnLoadToggleVillages();


            //Check/Uncheck all the villages for the selected ward
            $('#<%= gvWards.ClientID %> input').click(function () {
                var WardId = $(this).closest("tr").find("input[type=hidden]").val();
                var checked = $(this).is(":checked");
                $('#<%= gvVillage.ClientID %> input[type=hidden]').each(function () {
                    if ($(this).val() == WardId) {
                        var chkBox = $(this).closest("tr").find("input[type=checkbox]");
                        $(chkBox).attr("checked", checked);
                    }

                    ToggleVillages(WardId, checked);

                    $('#<%= gvVillage.ClientID%> input').click(function () {
                        var WardId = $(this).closest("tr").find("input[type=hidden]").val();
                        var status = false;

                        $('#<%= gvVillage.ClientID %> input').each(function () {

                            if ($(this).closest("tr").find("input[type=hidden]").val() == WardId) {
                                if ($(this).is(":checked")) {
                                    status = true;
                                    return false;
                                }
                            }
                        });

                        $('#<%= gvWards.ClientID%> input[type=hidden]').each(function () {
                            if ($(this).val() == WardId) {
                                $(this).closest("tr").find("input[type=checkbox]").attr("checked", status);
                            }
                        });

                    });
                });
            });


            $('.mGrid tr').hover(function () {
                $(this).addClass('alt');
                var WardId = $(this).find('input[type=hidden]').val();
                var $RowsNo = $('#<%= gvVillage.ClientID %> tr').filter(function () {
                    return $(this).find("input[type=hidden]").val() == WardId
                });
                $RowsNo.addClass("highlight");

            }, function () {
                $(this).removeClass('alt');
                var WardId = $(this).find('input[type=hidden]').val();
                var $RowsNo = $('#<%= gvVillage.ClientID %> tr').filter(function () {
                    return $(this).find("input[type=hidden]").val() == WardId
                });

                $RowsNo.removeClass('highlight');
            });

        }

        //});

        function OnLoadToggleVillages() {
            $('#<%=gvWards.ClientID %> input[type=checkbox]').each(function () {
                //if ($(this).is(":checked") == false) {
                var WardId = $(this).closest("tr").find("input[type=hidden]").val();
                var checked = $(this).is(":checked");
                ToggleVillages(WardId, checked)
                //}
            });
        }
        function toggleCheckWards(status) {
            $('#<%= gvWards.ClientID %> input[type=checkbox]').each(function () {
                $(this).attr("checked", status);
            });
            toggleCheckVillages(status);
            OnLoadToggleVillages();
        }
        function toggleCheckVillages(status) {
            $('#<%= gvVillage.ClientID%> input[type=checkbox]').each(function () {
                $(this).attr("checked", status);
            });
            toggleCheckWards(status);
        }
        function ToggleVillages(WardId, checked) {
           var $RowsNo = $('#<%= gvVillage.ClientID %> tr').filter(function () {
                return $(this).find("input[type=hidden]").val() == WardId
            });

            if (checked == true) {
                $RowsNo.show("slow");
            } else {
                $RowsNo.hide("slow");
            }
        }

        

    </script>
    

    <div class="divBody">
        <asp:Panel ID="Panel2" runat="server" ScrollBars="Auto"
            CssClass="panel" GroupingText='<%$ Resources:Resource,G_OFFICER %>'>
             <asp:UpdatePanel ID="upWardVillages" runat="server">
                        <ContentTemplate>

            <table style="width:100%;">
                <tr>
                    <td>
                        <table class="style15">
                            <tr>
                                <td class="FormLabel">
                                    <asp:Label ID="L_Language" runat="server" Text='<%$ Resources:Resource,L_CODE %>'></asp:Label>
                                </td>
                                <td class="DataEntry">
                                    <asp:TextBox ID="txtCode" runat="server" Width="150px" MaxLength="8"></asp:TextBox>
                                </td>
                                <td>
                                    <asp:RequiredFieldValidator
                                        ID="RequiredFieldLanguage" runat="server"
                                        ControlToValidate="txtCode"
                                        SetFocusOnError="True"
                                        ValidationGroup="check"
                                        Text='*'></asp:RequiredFieldValidator>
                                </td>
                            </tr>
                            <tr>
                                <td class="FormLabel">
                                    <asp:Label
                                        ID="L_OtherName"
                                        runat="server"
                                        Text='<%$ Resources:Resource,L_OTHERNAMES %>'></asp:Label>
                                </td>
                                <td class="DataEntry">
                                    <asp:TextBox ID="txtOtherNames" runat="server" Width="150px" MaxLength="100"></asp:TextBox>
                                </td>
                                <td>
                                    <asp:RequiredFieldValidator
                                        ID="RequiredFieldOtherNames" runat="server"
                                        ControlToValidate="txtOtherNames"
                                        SetFocusOnError="True"
                                        ValidationGroup="check"
                                        Text='*'></asp:RequiredFieldValidator>
                                </td>
                            </tr>
                            <tr>
                                <td class="FormLabel">
                                    <asp:Label ID="L_LastName" runat="server" Text='<%$ Resources:Resource,L_LASTNAME %>'></asp:Label>
                                </td>
                                <td class="DataEntry">
                                    <asp:TextBox ID="txtLastName" runat="server" Width="150px" MaxLength="100"></asp:TextBox>
                                </td>
                                <td>
                                    <asp:RequiredFieldValidator ID="RequiredFieldLastName" runat="server"
                                        ControlToValidate="txtLastName" Text="*"
                                        ValidationGroup="check"></asp:RequiredFieldValidator>
                                </td>
                            </tr>

                            <tr>
                                <td class="FormLabel">
                                    <asp:Label ID="Label1"
                                        runat="server"
                                        Text='<%$ Resources:Resource,L_BIRTHDATE %>'></asp:Label>
                                </td>
                                <td class="DataEntry">
                                    <asp:TextBox ID="txtDob" runat="server" Width="140px"></asp:TextBox>
                                    <asp:MaskedEditExtender ID="txtDob_MaskedEditExtender" runat="server"
                                        CultureDateFormat="dd/MM/YYYY"
                                        TargetControlID="txtDob" Mask="99/99/9999" MaskType="Date"
                                        UserDateFormat="DayMonthYear">
                                    </asp:MaskedEditExtender>
                                    <asp:Button ID="Button1" runat="server" Class="dateButton" />


                                    <asp:CalendarExtender ID="CalendarExtender1" runat="server" TargetControlID="txtDob" PopupButtonID="Button1" Format="dd/MM/yyyy"></asp:CalendarExtender>

                                </td>
                                <td>
                                    <asp:RegularExpressionValidator ID="RegularExpressionValidatortxtDob" runat="server"
                                        ControlToValidate="txtDob" Text="*" SetFocusOnError="True"
                                        ValidationExpression="^(0[1-9]|[12][0-9]|3[01])[/](0[1-9]|1[012])[/](19|20)\d\d$"
                                        ValidationGroup="check"></asp:RegularExpressionValidator>

                                </td>
                            </tr>
                            <tr>
                                <td class="FormLabel">
                                    <asp:Label ID="L_PHONE" runat="server" Text='<%$ Resources:Resource,L_PHONE %>'></asp:Label>
                                </td>
                                <td class="DataEntry">
                                    <asp:TextBox ID="txtPhone" runat="server" Width="150px" MaxLength="50"></asp:TextBox>
                                </td>
                                <td>
                                    <%--<asp:RequiredFieldValidator ID="RequiredFieldLoginName" runat="server" 
                                ControlToValidate="txtPhone" Text="*" 
                                ValidationGroup="check"></asp:RequiredFieldValidator>--%>
                                    <asp:CheckBox ID="chkCommunicate" runat="server" Text='<%$ Resources:Resource, L_COMMUNICATE %>' Font-Size="9pt" ForeColor="Blue" Style="direction: ltr" />
                                </td>
                            </tr>

                            <tr>
                                <td class="FormLabel">
                                    <asp:Label ID="L_Email" runat="server" Style="direction: ltr" Text="<%$ Resources:Resource,L_EMAIL %>"></asp:Label>
                                </td>
                                <td class="DataEntry">
                                    <asp:TextBox ID="txtEmail" runat="server" MaxLength="200" Width="150px"></asp:TextBox>
                                </td>
                                <td style="direction: ltr">
                                    <asp:RegularExpressionValidator ID="RegularExpressionValidator2" runat="server" ControlToValidate="txtEmail" ErrorMessage="*" SetFocusOnError="True" ValidationExpression="\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*" ValidationGroup="check">*</asp:RegularExpressionValidator>
                                </td>
                            </tr>

                            <tr>
                                <td class="FormLabel">
                                    <asp:Label ID="L_PERMADDERSS" runat="server" Style="direction: ltr" Text="<%$ Resources:Resource,L_PARMANENTADDRESS %>"></asp:Label>
                                </td>
                                <td class="DataEntry">
                                    <asp:TextBox ID="txtpermaddress" runat="server" MaxLength="100" Height="40px"  TextMode="MultiLine" Width="150px" Style="resize: none;"></asp:TextBox>
                                </td>
                                
                            </tr>

                            <tr>
                                <td class="FormLabel">
                                    <asp:Label ID="L_REGION" runat="server" Text='<%$ Resources:Resource,L_REGION %>'></asp:Label>
                                </td>
                                <td class="DataEntry">
                                    
                                    <asp:DropDownList ID="ddlRegion" runat="server" AutoPostBack="true">
                                    </asp:DropDownList>
                                </td>
                                <td>
                                    
                                    <asp:RequiredFieldValidator ID="RequiredFieldRegion" runat="server" ControlToValidate="ddlRegion" InitialValue="0" Text="*" ValidationGroup="check"></asp:RequiredFieldValidator>
                                          
                                </td>
                            </tr>
                            <tr>
                                <td class="FormLabel">
                                    <asp:Label ID="L_District" runat="server" Text="<%$ Resources:Resource,L_DISTRICT %>"></asp:Label>
                                </td>
                                <td class="DataEntry">
                                    <asp:UpdatePanel ID="upDistrict" runat="server">
                                        <ContentTemplate>
                                            <asp:DropDownList ID="ddlDistrict" runat="server" AutoPostBack="true">
                                            </asp:DropDownList>
                                        </ContentTemplate>
                                    </asp:UpdatePanel>
                                </td>
                                <td>
                                    <asp:RequiredFieldValidator ID="RequiredFieldDistrict" runat="server" ControlToValidate="ddlDistrict" InitialValue="0" Text="*" ValidationGroup="check"></asp:RequiredFieldValidator>
                                </td>
                            </tr>
                            <tr>
                                <td class="FormLabel">
                                    <asp:Label ID="L_SUBSTITUTION" runat="server" Text='<%$ Resources:Resource,L_SUBSTITUTION %>'></asp:Label>
                                </td>
                                <td class="DataEntry">
                                    <asp:DropDownList ID="ddlSubstitution" runat="server">
                                    </asp:DropDownList>
                                </td>
                                <td>&nbsp;</td>
                            </tr>
                            <tr>
                                <td class="FormLabel" valign="top">
                                    <asp:Label ID="L_WorksTo" runat="server" Text='<%$ Resources:Resource,L_WORKSTO %>'></asp:Label>
                                </td>
                                <td class="DataEntry" colspan="1">
                                    <asp:TextBox ID="txtWorksTo" runat="server" Width="140px"></asp:TextBox>
                                    <asp:MaskedEditExtender ID="txtWorksTo_MaskedEditExtender" runat="server"
                                        CultureDateFormat="dd/MM/YYYY"
                                        TargetControlID="txtWorksTo" Mask="99/99/9999" MaskType="Date"
                                        UserDateFormat="DayMonthYear">
                                    </asp:MaskedEditExtender>
                                    <asp:Button ID="Button2" runat="server" Class="dateButton" />


                                    <asp:CalendarExtender ID="CalendarExtender2" runat="server" TargetControlID="txtWorksTo" PopupButtonID="Button2" Format="dd/MM/yyyy"></asp:CalendarExtender>

                                </td>
                                <td>

                                    <asp:RegularExpressionValidator ID="RegularExpressionValidatortxtWorksTo" runat="server"
                                        ControlToValidate="txtWorksTo" Text="*" SetFocusOnError="True"
                                        ValidationExpression="^(0[1-9]|[12][0-9]|3[01])[/](0[1-9]|1[012])[/](19|20)\d\d$"
                                        ValidationGroup="check"></asp:RegularExpressionValidator>

                                </td>
                            </tr>

                        </table>

                    </td>

                   

                            <td style="width: 200px">
                                <asp:CheckBox ID="chkCheckAllWards" runat="server" Text='<%$ Resources:Resource,L_CHECKALL%>' margin-left="10px" onClick="toggleCheckWards(this.checked);" />
                                <asp:Panel ID="pnlWards" runat="server" ScrollBars="Auto" Height="320px" Width="175px"
                                    CssClass="panel" GroupingText='<%$ Resources:Resource, L_WARD %>'>
                                    <asp:GridView ID="gvWards" runat="server"
                                        AutoGenerateColumns="False"
                                        ShowSelectButton="True"
                                        GridLines="None"
                                        CssClass="mGrid"
                                        PagerStyle-CssClass="pgr"
                                        DataKeyNames="checked,WardId"
                                        EmptyDataText='No wards found'>

                                        <Columns>
                                            <asp:TemplateField HeaderText="Select">
                                                <ItemTemplate>
                                                    <asp:CheckBox ID="chkWardSelect" runat="server" HeaderStyle-Width="30px" />

                                                </ItemTemplate>
                                                <HeaderTemplate>
                                                </HeaderTemplate>
                                            </asp:TemplateField>
                                            <asp:TemplateField HeaderText="">
                                                <ItemTemplate>
                                                    <asp:HiddenField ID="hfWard" runat="server" Value='<%#Eval("WardId")%>' />
                                                </ItemTemplate>
                                                <HeaderTemplate>
                                                </HeaderTemplate>
                                            </asp:TemplateField>
                                            <asp:BoundField DataField="WardName" HeaderText='<%$ Resources:Resource,L_WARD %>' SortExpression="WardName"
                                                HeaderStyle-Width="110px">
                                                <HeaderStyle Width="200px" />
                                            </asp:BoundField>


                                        </Columns>

                                        <PagerStyle CssClass="pgr" />
                                        <%--<AlternatingRowStyle CssClass="alt" />--%>
                                        <SelectedRowStyle CssClass="srs" />

                                    </asp:GridView>
                                    <span style="color: Red" id="errWardMsg" />

                                </asp:Panel>
                            </td>
                            <td style="width: 200px">
                                <asp:CheckBox ID="chkCheckAllVillages" runat="server" Text='<%$ Resources:Resource,L_CHECKALL%>' margin-left="10px" onClick="toggleCheckVillages(this.checked);" />
                                <asp:Panel ID="Panel1" runat="server" ScrollBars="Auto" Height="320px" Width="175px"
                                    CssClass="panel" GroupingText='<%$ Resources:Resource, L_VILLAGE %>'>
                                    <asp:GridView ID="gvVillage" runat="server"
                                        AutoGenerateColumns="False"
                                        ShowSelectButton="True"
                                        GridLines="None"
                                        CssClass="mGrid"
                                        PagerStyle-CssClass="pgr"
                                        DataKeyNames="checked,WardId, VillageID"
                                        EmptyDataText='No villages found'>

                                        <Columns>
                                            <asp:TemplateField HeaderText="Select">
                                                <ItemTemplate>
                                                    <asp:CheckBox ID="chkVillageSelect" runat="server" HeaderStyle-Width="30px" />

                                                </ItemTemplate>
                                                <HeaderTemplate>
                                                </HeaderTemplate>
                                            </asp:TemplateField>
                                            <asp:TemplateField HeaderText="">
                                                <ItemTemplate>
                                                    <asp:HiddenField ID="hfWard" runat="server" Value='<%#Eval("WardId")%>' />
                                                </ItemTemplate>
                                                <HeaderTemplate>
                                                </HeaderTemplate>
                                            </asp:TemplateField>
                                            <asp:BoundField DataField="VillageName" HeaderText='<%$ Resources:Resource,L_VILLAGE%>'
                                                HeaderStyle-Width="110px">
                                                <HeaderStyle Width="200px" />
                                            </asp:BoundField>


                                        </Columns>

                                        <PagerStyle CssClass="pgr" />
                                        <%--<AlternatingRowStyle CssClass="alt" />--%>
                                        <SelectedRowStyle CssClass="srs" />

                                    </asp:GridView>
                                    <span style="color: Red" id="Span1" />

                                </asp:Panel>
                            </td>

                </tr>
            </table>

                        </ContentTemplate>
                    </asp:UpdatePanel>

        </asp:Panel>
        <br />
        <br />
        <asp:Panel ID="pnlVeoOfficer" runat="server" CssClass="panel" GroupingText='<%$ Resources:Resource,L_VILLAGEOFFICER %>'>
            <table>
                <tr>
                    <td class="FormLabel">
                        <asp:Label ID="L_VEOCODE" runat="server" Text='<%$ Resources:Resource,L_CODE %>'> </asp:Label>
                    </td>
                    <td class="DataEntry">
                        <asp:TextBox ID="txtVeoCode" runat="server" Width="150px" MaxLength="25"></asp:TextBox>
                    </td>
                    <td>
                        <%-- <asp:RequiredFieldValidator 
                        ID="RequiredFieldValidator1" runat="server" 
                        ControlToValidate="txtVeoCode" 
                        SetFocusOnError="True" 
                        ValidationGroup="check"
                        Text='*'></asp:RequiredFieldValidator>--%>
                    </td>
                </tr>
                <tr>
                    <td class="FormLabel">
                        <asp:Label ID="L_VeoLastName" runat="server" Text='<%$ Resources:Resource,L_LastName %>'></asp:Label>
                    </td>
                    <td class="DataEntry">
                        <asp:TextBox ID="txtVeoLastName" runat="server" Width="150px" MaxLength="100"></asp:TextBox>
                    </td>
                    <td>
                        <%-- <asp:RequiredFieldValidator ID="RequiredFieldValidator2" runat="server" 
                                    ControlToValidate="txtVeoLastName" Text="*" 
                                    ValidationGroup="check"></asp:RequiredFieldValidator>--%>
                    </td>
                </tr>
                <tr>
                    <td class="FormLabel">
                        <asp:Label
                            ID="Label4"
                            runat="server"
                            Text='<%$ Resources:Resource,L_OTHERNAMES %>'></asp:Label>
                    </td>
                    <td class="DataEntry">
                        <asp:TextBox ID="txtVeoOtherName" runat="server" Width="150px" MaxLength="100"></asp:TextBox>
                    </td>
                    <td>
                        <%-- <asp:RequiredFieldValidator 
                        ID="RequiredFieldValidator3" runat="server" 
                        ControlToValidate="txtVeoOtherName" 
                        SetFocusOnError="True" 
                        ValidationGroup="check"
                        Text='*'></asp:RequiredFieldValidator>--%>
                    </td>
                </tr>
                <tr>
                    <td class="FormLabel">
                        <asp:Label ID="Label5" runat="server" Text='<%$ Resources:Resource,L_PHONE %>'></asp:Label>
                    </td>
                    <td class="DataEntry">
                        <asp:TextBox ID="txtVeoPhone" runat="server" Width="150px" MaxLength="50"></asp:TextBox>
                    </td>
                    <td>
                        <%--<asp:RequiredFieldValidator ID="RequiredFieldLoginName" runat="server" 
                                ControlToValidate="txtPhone" Text="*" 
                                ValidationGroup="check"></asp:RequiredFieldValidator>--%>
                    </td>
                </tr>
                <tr>
                    <td class="FormLabel">
                        <asp:Label ID="L_VEODOB"
                            runat="server"
                            Text='<%$ Resources:Resource,L_DOB %>'></asp:Label>
                    </td>
                    <td class="DataEntry">
                        <asp:TextBox ID="txtVeoDOB" runat="server" Width="130px"></asp:TextBox>
                        <asp:MaskedEditExtender ID="MaskedEditExtender1" runat="server"
                            CultureDateFormat="dd/MM/YYYY"
                            TargetControlID="txtVeoDOB" Mask="99/99/9999" MaskType="Date"
                            UserDateFormat="DayMonthYear">
                        </asp:MaskedEditExtender>
                        <asp:Button ID="btnVeoDOB" runat="server" class="dateButton" />


                        <asp:CalendarExtender ID="CalendarExtender3" runat="server" TargetControlID="txtVeoDOB" PopupButtonID="btnVeoDOB" Format="dd/MM/yyyy"></asp:CalendarExtender>

                    </td>
                    <td>
                        <%--<asp:RegularExpressionValidator ID="RegularExpressionValidator1" runat="server" 
                    ControlToValidate="txtVeoDOB" Text="*" SetFocusOnError="True" 
                    ValidationExpression="^(0[1-9]|[12][0-9]|3[01])[/](0[1-9]|1[012])[/](19|20)\d\d$" 
                    ValidationGroup="check"></asp:RegularExpressionValidator>--%>
                    
                    </td>
                </tr>
            </table>

        </asp:Panel>
    </div>
    <asp:Panel ID="pnlButtons" runat="server" CssClass="panelbuttons">
        <table width="100%" cellpadding="10 10 10 10">
            <tr>

                <td align="left">

                    <asp:Button
                        ID="B_SAVE"
                        runat="server"
                        Text='<%$ Resources:Resource,B_SAVE%>'
                        ValidationGroup="check" />
                </td>


                <td align="right">
                    <asp:Button
                        ID="B_CANCEL"
                        runat="server"
                        Text='<%$ Resources:Resource,B_CANCEL%>' />
                </td>

            </tr>
        </table>
    </asp:Panel>
</asp:Content>
<asp:Content ID="Content2" ContentPlaceHolderID="Footer" Runat="Server">
    <asp:label id="lblmsg" runat="server"></asp:label>
    <asp:ValidationSummary ID="validationSummary1" runat="server" HeaderText='<%$ Resources:Resource,V_SUMMARY%>' ValidationGroup="check" />
</asp:Content>
