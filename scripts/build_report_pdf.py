#!/usr/bin/env python3
"""Turn docs/REPORT.md into docs/REPORT.pdf. Uses fpdf2's HTML subset."""

import html
import re
from pathlib import Path

from fpdf import FPDF, TextStyle

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs" / "REPORT.md"
TARGET = ROOT / "docs" / "REPORT.pdf"
SANS = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
SANS_BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
MONO = "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf"


def inline(text: str, code: bool = True) -> str:
    escaped = html.escape(text)
    if code:
        escaped = re.sub(r"`([^`]+)`", r"<code>\1</code>", escaped)
    else:
        escaped = escaped.replace("`", "")
    escaped = re.sub(r"\*\*([^*]+)\*\*", r"<b>\1</b>", escaped)
    return escaped


def markdown_to_html(markdown: str) -> str:
    lines = markdown.splitlines()
    blocks = []
    i = 0
    while i < len(lines):
        line = lines[i]
        if not line.strip():
            i += 1
            continue
        if line.startswith("```"):
            i += 1
            body = []
            while i < len(lines) and not lines[i].startswith("```"):
                body.append(html.escape(lines[i]))
                i += 1
            i += 1
            blocks.append("<pre>" + "\n".join(body) + "</pre>")
            continue
        if line.startswith("|"):
            rows = []
            while i < len(lines) and lines[i].startswith("|"):
                rows.append(lines[i])
                i += 1
            blocks.append(table(rows))
            continue
        if line.startswith("#"):
            level = len(line) - len(line.lstrip("#"))
            level = min(level, 3)
            blocks.append(f"<h{level}>{inline(line[level:].strip())}</h{level}>")
            i += 1
            continue
        if line.startswith("- "):
            items = []
            while i < len(lines) and lines[i].startswith("- "):
                items.append(f"<li>{inline(lines[i][2:].strip())}</li>")
                i += 1
            blocks.append("<ul>" + "".join(items) + "</ul>")
            continue
        paragraph = [line.strip()]
        i += 1
        while i < len(lines) and lines[i].strip() and not lines[i].startswith(("#", "-", "|", "```")):
            paragraph.append(lines[i].strip())
            i += 1
        blocks.append("<p>" + inline(" ".join(paragraph)) + "</p>")
    return "\n".join(blocks)


def table(rows):
    parsed = []
    for row in rows:
        cells = [cell.strip() for cell in row.strip().strip("|").split("|")]
        if cells and all(re.fullmatch(r":?-{3,}:?", cell) for cell in cells):
            continue
        parsed.append(cells)
    if not parsed:
        return ""
    html_rows = []
    for index, cells in enumerate(parsed):
        tag = "th" if index == 0 else "td"
        html_rows.append(
            "<tr>" + "".join(f"<{tag}>{inline(cell, code=False)}</{tag}>" for cell in cells) + "</tr>"
        )
    return '<table width="100%">' + "".join(html_rows) + "</table>"


def main():
    document = markdown_to_html(SOURCE.read_text(encoding="utf-8"))
    pdf = FPDF(format="A4")
    pdf.set_auto_page_break(auto=True, margin=16)
    pdf.add_font("Sans", "", SANS)
    pdf.add_font("Sans", "B", SANS_BOLD)
    pdf.add_font("Mono", "", MONO)
    pdf.set_font("Sans", size=11)
    pdf.add_page()
    pdf.write_html(
        document,
        font_family="Sans",
        tag_styles={
            "h1": TextStyle(font_family="Sans", font_style="B", font_size_pt=16, t_margin=2, b_margin=2),
            "h2": TextStyle(font_family="Sans", font_style="B", font_size_pt=13, t_margin=3, b_margin=1),
            "h3": TextStyle(font_family="Sans", font_style="B", font_size_pt=12, t_margin=2, b_margin=1),
            "p": TextStyle(font_family="Sans", font_size_pt=11, b_margin=2),
            "li": TextStyle(font_family="Sans", font_size_pt=11),
            "pre": TextStyle(font_family="Mono", font_size_pt=8, t_margin=1, b_margin=2),
            "code": TextStyle(font_family="Mono", font_size_pt=9),
        },
    )
    pdf.output(str(TARGET))
    print(f"Wrote {TARGET} ({TARGET.stat().st_size} bytes, {pdf.pages_count} pages)")


if __name__ == "__main__":
    main()
