/*
 * MIT License
 *
 * Copyright (c) 2022 ⭕️⃤ kiinse
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package kiinse.programs.telegram.pkghbot.core.schedule.data;

import kiinse.programs.telegram.pkghbot.api.schedule.Group;
import kiinse.programs.telegram.pkghbot.api.schedule.Lessons;
import kiinse.programs.telegram.pkghbot.api.schedule.enums.Weekday;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class BotGroup implements Group {

    private final JSONObject data;
    private final String name;

    protected BotGroup(@NotNull JSONObject data, @NotNull String name) {
        this.data = data;
        this.name = name;
    }

    @Override
    public @NotNull Lessons getLessons(@NotNull Weekday weekday) {
        return new BotLessons(data.getJSONArray(weekday.getRussian()), weekday, name) {};
    }

    @Override
    public @NotNull String getName() {
        return name;
    }
}
