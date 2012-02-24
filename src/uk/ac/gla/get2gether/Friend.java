/*
 * Friend.java
 * 
 * Author: C. Enrique Ortiz | http://cenriqueortiz.com
 * Copyright 2010 C. Enrique Ortiz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.gla.get2gether;

import android.graphics.Bitmap;

/**
 * Represents a Friend
 */
public class Friend {
	public String id;
	public String name;
	public byte[] picture;
	public Bitmap pictureBitmap;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Friend other = (Friend) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Friend [id=" + id + ", name=" + name + "]";
	};
}
