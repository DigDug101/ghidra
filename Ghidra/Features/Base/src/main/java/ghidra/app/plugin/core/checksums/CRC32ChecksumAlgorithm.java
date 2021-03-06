/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.app.plugin.core.checksums;

import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import ghidra.program.model.address.AddressSetView;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryAccessException;
import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitor;

/**
 * This class is used for the computation of the CRC-32 checksum algorithm.
 */
public class CRC32ChecksumAlgorithm extends ChecksumAlgorithm {

	/**
	 * Constructor for the CRC-32 checksum algorithm.
	 */
	public CRC32ChecksumAlgorithm() {
		super("CRC-32");
	}

	@Override
	public void updateChecksum(Memory memory, AddressSetView addrSet, TaskMonitor monitor,
			ComputeChecksumsProvider provider) throws MemoryAccessException, CancelledException {
		if (provider == null) {
			updateChecksum(memory, addrSet, monitor, false, false);
		}
		else {
			updateChecksum(memory, addrSet, monitor, provider.isOnes(), provider.isTwos());
		}
	}

	/**
	 * Computes the checksum with the given options.
	 * 
	 * @param memory The memory to generate the checksum from.
	 * @param addrSet The addresses over which to generate the checksum.
	 * @param monitor Cancelable task monitor to cancel the computation.
	 * @param onesComp True if the checksum should be complemented with a ones complement.
	 * @param twosComp True if the checksum should be complemented with a twos complement.
	 * @throws MemoryAccessException If there was a problem reading the memory.
	 * @throws CancelledException If the user cancels the task.
	 */
	public void updateChecksum(Memory memory, AddressSetView addrSet, TaskMonitor monitor,
			boolean onesComp, boolean twosComp) throws MemoryAccessException, CancelledException {

		byte[] bytes = new byte[1024];
		long crc;
		try (CheckedInputStream cis =
			new CheckedInputStream(new MemoryInputStream(memory, addrSet), new CRC32())) {
			while (cis.read(bytes) > 0) {
				if (monitor.isCancelled()) {
					throw new CancelledException();
				}
			}
			crc = cis.getChecksum().getValue();
		}
		catch (IOException e) {
			throw new MemoryAccessException(e.getMessage());
		}

		if (onesComp) {
			crc = ~crc;
		}
		else if (twosComp) {
			crc = -crc;
		}
		checksum = toArray(crc, 4);
	}

	@Override
	public boolean supportsDecimal() {
		return true;
	}
}
