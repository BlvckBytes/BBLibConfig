package me.blvckbytes.bblibconfig;

import me.blvckbytes.bblibconfig.component.TextComponent;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibutil.Tuple;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/13/2022

  Generates gradient texts (or plain color points) based on a linear gradient
  definition made up of multiple colors and their percentage point from 0 to 1.

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published
  by the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
@AutoConstruct
public class GradientGenerator {

  /**
   * Create a new gradient text from a plain string
   * @param text Plain string to add a gradient to
   * @param colors Colors making up the gradient (have to be sorted by percentage ascending)
   * @return String with applied gradient as a component
   */
  public TextComponent gradientize(
    String text,
    List<Tuple<Color, Double>> colors
  ) {
    TextComponent res = new TextComponent("");

    // Iterate all characters of the string
    char[] chars = text.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      // How far the loop is into the string
      double percentage = (i + 1D) / chars.length;

      // Create a new component containing only the current character
      TextComponent curr = new TextComponent(String.valueOf(chars[i]));

      // Apply the color at the current point within the gradient
      Color color = getGradientPoint(colors, percentage);
      curr.setColor(String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));

      res.addSibling(curr);
    }

    return res;
  }

  /**
   * Get a color point on a linear gradient made up of multiple colors at certain points
   * @param colors Colors making up the gradient (have to be sorted by percentage ascending)
   * @param percentage Percentage to pick the color at
   * @return Picked color
   */
  public Color getGradientPoint(List<Tuple<Color, Double>> colors, double percentage) {
    // No colors present, print all white
    if (colors.size() == 0)
      return Color.WHITE;

    // Only one color present
    if (colors.size() == 1)
      return colors.get(0).getA();

    // Quick exit: If the first color has a higher value than 0,
    // the first n percent are that color statically.
    Tuple<Color, Double> first = colors.get(0);
    if (percentage <= first.getB())
      return first.getA();

    // Quick exit: If the last color has a higher value than 1,
    // the first n percent are that color statically.
    Tuple<Color, Double> last = colors.get(colors.size() - 1);
    if (percentage >= last.getB())
      return last.getA();

    // Find the two nearest colors around the current percentage point which
    // will make up the smaller in-between-gradient the caller is interested in

    // Start out assuming that A will be the first and B the last color
    Tuple<Color, Double> a = first, b = last;

    // Only iterate from 1 until n-1, as first and last are already active
    for (int i = 1; i < colors.size() - 1; i++) {
      Tuple<Color, Double> color = colors.get(i);

      // Set A if the color is below the percentage but higher
      // up than the previous A color
      if (color.getB() < percentage && color.getB() > a.getB())
        a = color;

      // Set B if the color is above the percentage but lower
      // down than the previous B color
      if (color.getB() > percentage && color.getB() < b.getB())
        b = color;
    }

    // Relativize the percentage to that smaller gradient section
    // How far into the sub-gradient is that point, from 0 to 1,
    // which is the ratio from the length travelled on the whole gradient
    // to get from A to percentage, divided by the span of A and B.
    percentage = (percentage - a.getB()) / (b.getB() - a.getB());

    // Linearly interpolate
    double resultRed   = a.getA().getRed()   + percentage * (b.getA().getRed()   - a.getA().getRed());
    double resultGreen = a.getA().getGreen() + percentage * (b.getA().getGreen() - a.getA().getGreen());
    double resultBlue  = a.getA().getBlue()  + percentage * (b.getA().getBlue()  - a.getA().getBlue());

    // Floor to the next nearest integer when converting back into a color
    return new Color(
      (int) Math.floor(resultRed),
      (int) Math.floor(resultGreen),
      (int) Math.floor(resultBlue)
    );
  }

  /**
   * Tries to parse a gradient notation into a usable gradient color point
   * list, sorted by the percentage value ascending
   * @param notation Color notation of format {@code <#RRGGBB:(0-1) * n>} (colors are
   *                 separated by spaces), example: {@code <#FF0000:0 #00FF00:.5 #0000FF:1>}
   * @return Parsed notation on success, empty if the notation was malformed
   */
  public Optional<List<Tuple<Color, Double>>> parseGradientNotation(String notation) {
    // Not enclosed by angle brackets
    if (!(notation.startsWith("<") && notation.endsWith(">")))
      return Optional.empty();

    List<Tuple<Color, Double>> res = new ArrayList<>();

    // Split on space to get individual color notations
    String[] colors = notation.substring(1, notation.length() - 1).split(" ");

    for (String color : colors) {
      // Split on colon to get the color data
      String[] data = color.split(":");

      // Malformed color notation
      if (data.length != 2)
        return Optional.empty();

      // Has to start with # to be a valid hex notation
      if (!data[0].startsWith("#"))
        return Optional.empty();

      char[] chars = data[0].toCharArray();

      // Only try to parse if there are enough characters available
      if (chars.length != 6 + 1)
        return Optional.empty();

      // Try to parse the color's R G and B separately
      Color c;
      double percentage;
      try {
        c = new Color(
          Integer.parseInt(chars[1] + "" + chars[2], 16),
          Integer.parseInt(chars[3] + "" + chars[4], 16),
          Integer.parseInt(chars[5] + "" + chars[6], 16)
        );

        percentage = Double.parseDouble(data[1]);
      }

      // Unparsable color or percentage
      catch (Exception ignored) {
        return Optional.empty();
      }

      // Percentage out of range
      if (percentage < 0 || percentage > 1)
        return Optional.empty();

      // Add color point to the list
      res.add(new Tuple<>(c, percentage));
    }

    // Don't accept empty lists
    if (res.size() == 0)
      return Optional.empty();

    // Return the list of colors sorted by their percentage
    res.sort(Comparator.comparingDouble(Tuple::getB));
    return Optional.of(res);
  }
}
