<?php
  // $Id$
  // note: provider certification database functions
  // lic : GPL

LoadObjectDependency('FreeMED.MaintenanceModule');

class ProviderCertificationsMaintenance extends MaintenanceModule {

	var $MODULE_NAME    = "Provider Certifications Maintenance";
	var $MODULE_AUTHOR  = "jeff b (jeff@ourexchange.net)";
	var $MODULE_VERSION = "0.1";
	var $MODULE_FILE    = __FILE__;

	var $PACKAGE_MINIMUM_VERSION = '0.6.0';

	var $record_name = "Provider Certifications";
	var $table_name = "degrees";

	var $variables      = array (
		"degdegree",
		"degname",
		"degdate"
	);

	function ProviderCertificationsMaintenance () {
		global $deg_date;
		$degdate = date("Y-m-d");

		// Table definition
		$this->table_definition = array (
			'degdegree' => SQL_CHAR(10),
			'degname' => SQL_VARCHAR(50),
			'degdate' => SQL_DATE,
			'id' => SQL_SERIAL
		);

		// Run constructor
		$this->MaintenanceModule();
	} // end constructor ProviderCertificationsMaintenance

	function form () { $this->view(); }

	function view () {
		global $display_buffer;
		foreach ($GLOBALS AS $k => $v) { global ${$k}; }

		if ($action=="modform") {
			$r = freemed::get_link_rec($id, $this->table_name);
			extract ($r);
		} // modform fetching

		// display the table 
		$display_buffer .= freemed_display_itemlist(
			$sql->query(
				"SELECT * ".
				"FROM ".$this->table_name." ".
				freemed::itemlist_conditions()." ".
				"ORDER BY degdegree,degname"
			),
			$this->page_name,
			array (
				__("Degree") => "degdegree",
				__("Description") => "degname"
			),
			array ( "", __("NO DESCRIPTION") ), "", "d_page"
		);
  
		$display_buffer .= "
		<form ACTION=\"$this->page_name\" METHOD=\"POST\">
		<input TYPE=\"HIDDEN\" NAME=\"action\" VALUE=\"".(($action=="modform") ? 
				"mod" : "add")."\"/> 
		<input TYPE=\"HIDDEN\" NAME=\"id\" VALUE=\"".prepare($id)."\"/>
		<input TYPE=\"HIDDEN\" NAME=\"module\" VALUE=\"".prepare($module)."\"/>
		<div ALIGN=\"CENTER\">
		".html_form::form_table(array(

		__("Degree") =>
		html_form::text_widget('degdegree', 10),

		__("Degree Description") =>
		html_form::text_widget('degname', 30, 50)

		))."
		</div>
		<div align=\"CENTER\">
		<input TYPE=\"SUBMIT\" VALUE=\"".($action=="modform" ? 
		__("Update") : __("Add"))." \"/>
		<input TYPE=\"RESET\" VALUE=\"".__("Remove Changes")."\"/>
		</div>
		</form>
		";
	} // end function ProviderCertificationsMaintenance->view()

} // end class ProviderCertificationsMaintenance

register_module ("ProviderCertificationsMaintenance");

?>
