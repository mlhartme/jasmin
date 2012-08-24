/**
 * Copyright 1&1 Internet AG, http://www.1and1.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.beezle.jasmin.main;

import net.sf.beezle.sushi.fs.World;
import org.junit.Test;

public class RunnerFullTest {
    @Test
    public void all() throws Exception {
        Runner.create("all", new World()).addAll().invoke();
    }

    @Test
    public void all2() throws Exception {
        Runner.create("all2", new World()).addAll().invoke(2, 10000, true, true, true);
    }

    @Test
    public void all12() throws Exception {
        Runner.create("all12", new World()).addAll().invoke(12, 10000, true, true, true);
    }

    @Test
    public void explicit() throws Exception {
        World world;

        world = new World();
        Runner.create("explicit", world).add("foo/css/head", "foo/js/v1\nfoo/js/v2").invoke();
    }
}
