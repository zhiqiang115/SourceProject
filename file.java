// detech link url in string and insert to textview
private SpannableString detectLinkURL(String data) {
//	data = data.replaceAll("<a", "123@#");
//	data = data.replaceAll("</a>", "124@#");
//	data = data.replaceAll("<", "&lt;");
//	data = data.replaceAll("123@#","<a");
//	data = data.replaceAll("124@#","</a>");
	Spanned text = Html.fromHtml(data);
	//		Spanned text = Spannable.Factory.getInstance().newSpannable(data);
	URLSpan[] currentSpans = text.getSpans(0, text.length(), URLSpan.class);
	SpannableString buffer = new SpannableString(text);
	Linkify.addLinks(buffer, Linkify.ALL);
	for (URLSpan span : currentSpans) {
		int end = text.getSpanEnd(span);
		int start = text.getSpanStart(span);
		buffer.setSpan(span, start, end, 0);
	}
	return buffer;
}
