<h2> Upload Wheat Initiative Spreadsheet </h2>
<p>${message}</p>

<#if success>
  <a href="${urls.base}/listDataSources">Return to data sources list</a>
<#else>
  <form method="post" enctype="multipart/form-data" action="">
    <label for="file">.xlsx file</label>
    <input id="file" type="file" name="file" value="" />
    <p>&nbsp;</p>
    <input name="submit" class="submit" type="submit" value="Upload Spreadsheet"/> or <a href="${urls.base}/listDataSources">Cancel</a>
  </form>
</#if>

