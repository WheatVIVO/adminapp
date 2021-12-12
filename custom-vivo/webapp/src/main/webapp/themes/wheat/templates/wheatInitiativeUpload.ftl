<h2> Upload Spreadsheet </h2>
<p>${message}</p>

<#if success>
  <a href="${urls.base}/listDataSources">Return to data sources list</a>
<#else>
  <form method="post" enctype="multipart/form-data" action="">
    <div style="padding-top:1em;padding-bottom:1em;">
      <p><input type="radio" id="wheatInitiativeSubdir" style="display:inline;margin-right:0.5em;" name="subdir" value="/wheatInitiative"/><label style="display:inline;" for="wheatInitiativeSubdir">Wheat Initiative researcher signups</label></p>
      <p><input type="radio" id="arcSubdir" style="display:inline;margin-right:0.5em;" name="subdir" value="/arc"/><label style="display:inline;" for="arcSubdir">ARC grants</label></p>
      <p><input type="radio" id="grdcSubdir" style="display:inline;margin-right:0.5em;" name="subdir" value="/grdc"/><label style="display:inline;" for="grdcSubdir">GRDC grants</label></p>
    </div>

    <label for="file">.xlsx file</label>
    <input id="file" type="file" name="file" value="" />
    <p>&nbsp;</p>
    <input name="submit" class="submit" type="submit" value="Upload Spreadsheet"/> or <a href="${urls.base}/listDataSources">Cancel</a>
  </form>
</#if>

